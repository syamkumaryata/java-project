package edu.busTracker.model;

/**
 * Represents a bus driver employed by the university.
 * Extends Person (Unit 1 — inheritance).
 */
public class Driver extends Person {

    private Long driverId;
    private String phone;

    public Driver() {
        super();
    }

    public Driver(Long driverId, String name, String phone) {
        super(name, null);
        this.driverId = driverId;
        this.phone = phone;
    }

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    @Override
    public String describe() {
        return "Driver [" + driverId + "] " + name + " | Phone: " + maskPhone();
    }

    public String maskPhone() {
        if (phone == null || phone.length() < 4) return "****";
        return "****" + phone.substring(phone.length() - 4);
    }

    @Override
    public String toString() {
        return "Driver{driverId=" + driverId + ", name='" + name + "'}";
    }
}
