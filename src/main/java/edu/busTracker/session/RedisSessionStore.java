package edu.busTracker.session;

import edu.busTracker.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Production session store using Redis through StringRedisTemplate (Lettuce client).
 * Session key: session:{sid} (hash)
 * User sessions index: user-sessions:{regNoOrEmail} (set for bulk revocation)
 * Sliding TTL: 30 minutes; Absolute TTL: 8 hours.
 */
public class RedisSessionStore implements SessionStore {

    private static final Logger log = LoggerFactory.getLogger(RedisSessionStore.class);

    private final StringRedisTemplate redis;
    private final long ttlMinutes;
    private final long maxHours;

    public RedisSessionStore(StringRedisTemplate redis, long ttlMinutes, long maxHours) {
        this.redis = redis;
        this.ttlMinutes = ttlMinutes;
        this.maxHours = maxHours;
    }

    private String sessionKey(String sid) { return "session:" + sid; }
    private String userKey(String user) { return "user-sessions:" + user; }

    @Override
    public void save(SessionData session) {
        try {
            String key = sessionKey(session.getSessionId());
            Map<String, String> hash = new HashMap<>();
            hash.put("sessionId", session.getSessionId());
            hash.put("regNoOrEmail", session.getRegNoOrEmail());
            hash.put("role", session.getRole());
            hash.put("csrfToken", session.getCsrfToken());
            hash.put("userAgentHash", session.getUserAgentHash() != null ? session.getUserAgentHash() : "");
            hash.put("createdAt", session.getCreatedAt());
            hash.put("lastSeen", Instant.now().toString());

            redis.opsForHash().putAll(key, hash);
            redis.expire(key, Duration.ofMinutes(ttlMinutes));

            // Track in user-sessions set for bulk revocation
            redis.opsForSet().add(userKey(session.getRegNoOrEmail()), session.getSessionId());
            redis.expire(userKey(session.getRegNoOrEmail()), Duration.ofHours(maxHours));
        } catch (Exception e) {
            log.error("Redis error saving session: {}", e.getMessage());
            throw new ExternalServiceException("Redis Session Service", "Failed to persist session", e);
        }
    }

    @Override
    public Optional<SessionData> get(String sessionId) {
        try {
            String key = sessionKey(sessionId);
            Map<Object, Object> hash = redis.opsForHash().entries(key);
            if (hash == null || hash.isEmpty()) {
                return Optional.empty();
            }

            // Check absolute expiry (8 hours from createdAt)
            String createdAtStr = (String) hash.get("createdAt");
            if (createdAtStr != null) {
                Instant created = Instant.parse(createdAtStr);
                if (Instant.now().isAfter(created.plus(Duration.ofHours(maxHours)))) {
                    delete(sessionId);
                    return Optional.empty();
                }
            }

            SessionData data = new SessionData();
            data.setSessionId((String) hash.get("sessionId"));
            data.setRegNoOrEmail((String) hash.get("regNoOrEmail"));
            data.setRole((String) hash.get("role"));
            data.setCsrfToken((String) hash.get("csrfToken"));
            data.setUserAgentHash((String) hash.get("userAgentHash"));
            data.setCreatedAt(createdAtStr);
            data.setLastSeen(Instant.now().toString());

            // Refresh sliding expiration
            redis.opsForHash().put(key, "lastSeen", data.getLastSeen());
            redis.expire(key, Duration.ofMinutes(ttlMinutes));

            return Optional.of(data);
        } catch (ExternalServiceException ese) {
            throw ese;
        } catch (Exception e) {
            log.error("Redis error fetching session {}: {}", sessionId, e.getMessage());
            throw new ExternalServiceException("Redis Session Service", "Failed to retrieve session", e);
        }
    }

    @Override
    public void delete(String sessionId) {
        try {
            String key = sessionKey(sessionId);
            String user = (String) redis.opsForHash().get(key, "regNoOrEmail");
            redis.delete(key);
            if (user != null) {
                redis.opsForSet().remove(userKey(user), sessionId);
            }
        } catch (Exception e) {
            log.error("Redis error deleting session {}: {}", sessionId, e.getMessage());
            throw new ExternalServiceException("Redis Session Service", "Failed to delete session", e);
        }
    }

    @Override
    public void revokeAllForUser(String regNoOrEmail) {
        try {
            String uKey = userKey(regNoOrEmail);
            Set<String> sids = redis.opsForSet().members(uKey);
            if (sids != null) {
                for (String sid : sids) {
                    redis.delete(sessionKey(sid));
                }
            }
            redis.delete(uKey);
            log.info("Revoked all sessions for user: {}", regNoOrEmail);
        } catch (Exception e) {
            log.error("Redis error revoking user {}: {}", regNoOrEmail, e.getMessage());
            throw new ExternalServiceException("Redis Session Service", "Failed to revoke user sessions", e);
        }
    }
}
