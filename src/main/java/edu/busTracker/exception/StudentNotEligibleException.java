package edu.busTracker.exception;

public class StudentNotEligibleException extends TransportException {

    private final String reasonCode;

    public StudentNotEligibleException(String reasonCode, String message) {
        super(reasonCode, message, 403);
        this.reasonCode = reasonCode;
    }

    public String getReasonCode() { return reasonCode; }
}
