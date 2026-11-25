package edu.busTracker.exception;

public class SessionExpiredException extends TransportException {

    public SessionExpiredException() {
        super("SESSION_EXPIRED", "Your session has expired. Please log in again.", 401);
    }

    public SessionExpiredException(String message) {
        super("SESSION_EXPIRED", message, 401);
    }
}
