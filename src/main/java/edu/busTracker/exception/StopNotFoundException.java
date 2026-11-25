package edu.busTracker.exception;

public class StopNotFoundException extends TransportException {

    public StopNotFoundException(String stopIdentifier) {
        super("STOP_NOT_FOUND", "Stop not found: " + stopIdentifier, 404);
    }
}
