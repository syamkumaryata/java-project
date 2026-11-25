package edu.busTracker.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory session store used ONLY for unit tests or when app.session.store=memory.
 * Demonstrates: Collections — ConcurrentHashMap, Iterator with removal (Unit 4).
 */
public class InMemorySessionStore implements SessionStore {

    private static final Logger log = LoggerFactory.getLogger(InMemorySessionStore.class);

    private final Map<String, SessionData> sessions = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> userSessionMap = new ConcurrentHashMap<>();
    private final long ttlMinutes;

    public InMemorySessionStore(long ttlMinutes) {
        this.ttlMinutes = ttlMinutes;
    }

    @Override
    public void save(SessionData session) {
        session.setLastSeen(Instant.now().toString());
        sessions.put(session.getSessionId(), session);
        userSessionMap.computeIfAbsent(session.getRegNoOrEmail(), k -> ConcurrentHashMap.newKeySet())
                      .add(session.getSessionId());
    }

    @Override
    public Optional<SessionData> get(String sessionId) {
        cleanExpired();
        SessionData data = sessions.get(sessionId);
        if (data != null) {
            data.setLastSeen(Instant.now().toString());
            return Optional.of(data);
        }
        return Optional.empty();
    }

    @Override
    public void delete(String sessionId) {
        SessionData data = sessions.remove(sessionId);
        if (data != null) {
            Set<String> sids = userSessionMap.get(data.getRegNoOrEmail());
            if (sids != null) sids.remove(sessionId);
        }
    }

    @Override
    public void revokeAllForUser(String regNoOrEmail) {
        Set<String> sids = userSessionMap.remove(regNoOrEmail);
        if (sids != null) {
            for (String sid : sids) {
                sessions.remove(sid);
            }
        }
    }

    /**
     * Periodically sweeps expired sessions using an explicit Iterator.
     * Demonstrates: Iterator with remove() during traversal (Unit 4 Collections).
     */
    private void cleanExpired() {
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(ttlMinutes));
        Iterator<Map.Entry<String, SessionData>> it = sessions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, SessionData> entry = it.next();
            Instant lastSeen = Instant.parse(entry.getValue().getLastSeen());
            if (lastSeen.isBefore(cutoff)) {
                it.remove(); // safe removal during iteration
            }
        }
    }
}
