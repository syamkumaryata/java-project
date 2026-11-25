package edu.busTracker.exception;

public class ExternalServiceException extends TransportException {

    public ExternalServiceException(String service, String message) {
        super("SERVICE_UNAVAILABLE", service + " is currently unavailable. " + message, 503);
    }

    public ExternalServiceException(String service, String message, Throwable cause) {
        super("SERVICE_UNAVAILABLE", service + " is currently unavailable. " + message, 503, cause);
    }
}
