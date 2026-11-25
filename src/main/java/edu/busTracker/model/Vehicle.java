package edu.busTracker.model;

/**
 * Abstract base class for university vehicles.
 * Demonstrates: abstract class hierarchy (Unit 1 OOP).
 * Bus extends this class.
 */
public abstract class Vehicle {

    protected Long vehicleId;
    protected String registrationNo;
    protected boolean active;

    public Vehicle() {}

    public Vehicle(Long vehicleId, String registrationNo, boolean active) {
        this.vehicleId = vehicleId;
        this.registrationNo = registrationNo;
        this.active = active;
    }

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public String getRegistrationNo() { return registrationNo; }
    public void setRegistrationNo(String registrationNo) { this.registrationNo = registrationNo; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public abstract String describe();
}
