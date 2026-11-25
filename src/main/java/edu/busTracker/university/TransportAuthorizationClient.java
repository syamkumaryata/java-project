package edu.busTracker.university;

/**
 * Interface for verifying transport authorization with university backend.
 * Demonstrates: interface abstraction (Unit 1 OOP).
 */
public interface TransportAuthorizationClient {
    TransportAuthorization lookup(String email);
}
