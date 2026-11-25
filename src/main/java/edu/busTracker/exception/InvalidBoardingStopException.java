package edu.busTracker.exception;

public class InvalidBoardingStopException extends TransportException {

    public InvalidBoardingStopException(String reason) {
        super("INVALID_BOARDING_STOP", reason, 400);
    }
}
