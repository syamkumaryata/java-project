package edu.busTracker.exception;

/**
 * Root of the custom exception hierarchy.
 * Demonstrates: custom exception hierarchy (Unit 2 — exception handling).
 */
public class TransportException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;

    public TransportException(String errorCode, String message, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public TransportException(String errorCode, String message, int httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() { return errorCode; }
    public int getHttpStatus() { return httpStatus; }
}
