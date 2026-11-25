package edu.busTracker.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class SessionConfig {

    private static final Logger log = LoggerFactory.getLogger(SessionConfig.class);

    @Value("${app.session.store:redis}")
    private String storeType;

    @Value("${app.session.ttl-minutes:30}")
    private long ttlMinutes;

    @Value("${app.session.max-hours:8}")
    private long maxHours;

    @Bean
    public SessionStore sessionStore(StringRedisTemplate redisTemplate) {
        if ("memory".equalsIgnoreCase(storeType)) {
            log.info("Using InMemorySessionStore (configured via app.session.store=memory)");
            return new InMemorySessionStore(ttlMinutes);
        }
        try {
            if (redisTemplate.getConnectionFactory() != null) {
                redisTemplate.getConnectionFactory().getConnection().ping();
                log.info("Redis connection verified. Configuring RedisSessionStore with TTL: {} min, Max: {} hr", ttlMinutes, maxHours);
                return new RedisSessionStore(redisTemplate, ttlMinutes, maxHours);
            }
        } catch (Exception e) {
            log.warn("Redis is unreachable ({}: {}). Falling back to InMemorySessionStore for standalone demonstration.",
                    e.getClass().getSimpleName(), e.getMessage());
        }
        return new InMemorySessionStore(ttlMinutes);
    }
}
