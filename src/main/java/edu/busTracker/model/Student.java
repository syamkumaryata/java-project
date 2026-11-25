package edu.busTracker.model;

/**
 * Represents a university student who uses the bus transport service.
 * Extends Person (Unit 1 — inheritance from abstract class).
 * Demonstrates: method overriding of describe() and toString() (Unit 2).
 */
public class Student extends Person {

    private String regNo;           // e.g. AP26DEMO0001 — primary key
    private String gender;
    private Integer batch;
    private String school;
    private String program;
    private String feeStatus;       // PAID | PENDING | OVERDUE
    private boolean transportActive;
    private boolean demo;           // true for synthetic demo accounts

    public Student() {
        super();
    }

    public Student(String regNo, String name, String email, String feeStatus, boolean transportActive) {
        super(name, email);
        this.regNo = regNo;
        this.feeStatus = feeStatus;
        this.transportActive = transportActive;
    }

    public Student(String regNo, String name, String email, String gender,
                   Integer batch, String school, String program,
                   String feeStatus, boolean transportActive, boolean demo) {
        super(name, email);
        this.regNo = regNo;
        this.gender = gender;
        this.batch = batch;
        this.school = school;
        this.program = program;
        this.feeStatus = feeStatus;
        this.transportActive = transportActive;
        this.demo = demo;
    }

    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) { this.regNo = regNo; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Integer getBatch() { return batch; }
    public void setBatch(Integer batch) { this.batch = batch; }

    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public String getFeeStatus() { return feeStatus; }
    public void setFeeStatus(String feeStatus) { this.feeStatus = feeStatus; }

    public boolean isTransportActive() { return transportActive; }
    public void setTransportActive(boolean transportActive) { this.transportActive = transportActive; }

    public boolean isDemo() { return demo; }
    public void setDemo(boolean demo) { this.demo = demo; }

    @Override
    public String describe() {
        return "Student [" + regNo + "] " + name + " | Fee: " + feeStatus
                + " | Transport: " + (transportActive ? "Active" : "Inactive");
    }

    @Override
    public String toString() {
        return "Student{regNo='" + regNo + "', name='" + name + "', email='" + email
                + "', feeStatus='" + feeStatus + "', transportActive=" + transportActive + "}";
    }
}
