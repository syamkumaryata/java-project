package edu.busTracker.session;

import java.util.Optional;

/**
 * Interface for session storage.
 * Demonstrates: interface abstraction (Unit 1 OOP) & runtime polymorphism (Unit 2).
 * Implementations: RedisSessionStore (production/demo) and InMemorySessionStore (tests).
 */
public interface SessionStore {

    /** Save or update a session with sliding TTL. */
    void save(SessionData session);

    /** Retrieve session by ID and refresh its sliding TTL. */
    Optional<SessionData> get(String sessionId);

    /** Delete a session by ID. */
    void delete(String sessionId);

    /** Revoke all active sessions for a given user. */
    void revokeAllForUser(String regNoOrEmail);
}
