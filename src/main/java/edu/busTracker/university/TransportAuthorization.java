package edu.busTracker.university;

import java.io.Serializable;

/**
 * DTO returned by TransportAuthorizationClient.lookup(email).
 */
public class TransportAuthorization implements Serializable {

    private boolean mock;
    private String regNo;
    private String name;
    private String email;
    private boolean enrolledInTransport;
    private String feeStatus;           // "PAID" | "PENDING" | "OVERDUE"
    private String busNumber;           // e.g. "TN-2"
    private String boardingStop;        // raw name from student list
    private String destination;         // e.g. "SRM University-AP (Campus)"
    private String academicYear;

    public TransportAuthorization() {}

    public boolean isMock() { return mock; }
    public void setMock(boolean mock) { this.mock = mock; }

    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) { this.regNo = regNo; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isEnrolledInTransport() { return enrolledInTransport; }
    public void setEnrolledInTransport(boolean enrolledInTransport) { this.enrolledInTransport = enrolledInTransport; }

    public String getFeeStatus() { return feeStatus; }
    public void setFeeStatus(String feeStatus) { this.feeStatus = feeStatus; }

    public String getBusNumber() { return busNumber; }
    public void setBusNumber(String busNumber) { this.busNumber = busNumber; }

    public String getBoardingStop() { return boardingStop; }
    public void setBoardingStop(String boardingStop) { this.boardingStop = boardingStop; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
}
