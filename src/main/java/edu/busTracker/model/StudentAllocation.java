package edu.busTracker.model;

/**
 * Links a student to their allocated bus and boarding stop for an academic year.
 */
public class StudentAllocation {

    private Long allocationId;
    private String regNo;
    private Bus bus;
    private Stop boardingStop;
    private String academicYear;
    private boolean active;

    public StudentAllocation() {}

    public StudentAllocation(Long allocationId, String regNo, Bus bus,
                              Stop boardingStop, String academicYear, boolean active) {
        this.allocationId = allocationId;
        this.regNo = regNo;
        this.bus = bus;
        this.boardingStop = boardingStop;
        this.academicYear = academicYear;
        this.active = active;
    }

    public Long getAllocationId() { return allocationId; }
    public void setAllocationId(Long allocationId) { this.allocationId = allocationId; }

    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) { this.regNo = regNo; }

    public Bus getBus() { return bus; }
    public void setBus(Bus bus) { this.bus = bus; }

    public Stop getBoardingStop() { return boardingStop; }
    public void setBoardingStop(Stop boardingStop) { this.boardingStop = boardingStop; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return "StudentAllocation{regNo='" + regNo + "', bus="
                + (bus != null ? bus.getBusNumber() : "null")
                + ", boardingStop=" + (boardingStop != null ? boardingStop.getName() : "null")
                + ", year='" + academicYear + "'}";
    }
}
