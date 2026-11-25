package edu.busTracker.model;

/**
 * Represents a university bus.
 * Extends Vehicle (Unit 1 — inheritance from abstract class).
 * "Bus number" is the route code (e.g. TN-2).
 */
public class Bus extends Vehicle {

    private Long busId;
    private String busNumber;
    private String routeId;
    private Driver driver;

    public Bus() {
        super();
    }

    public Bus(Long busId, String busNumber, String registrationNo,
               String routeId, Driver driver, boolean active) {
        super(busId, registrationNo, active);
        this.busId = busId;
        this.busNumber = busNumber;
        this.routeId = routeId;
        this.driver = driver;
    }

    public Long getBusId() { return busId; }
    public void setBusId(Long busId) { this.busId = busId; }

    public String getBusNumber() { return busNumber; }
    public void setBusNumber(String busNumber) { this.busNumber = busNumber; }

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }

    public String getDisplayLabel() {
        return "Bus " + busNumber;
    }

    @Override
    public String describe() {
        return "Bus " + busNumber + " [" + registrationNo + "] Route: " + routeId
                + " Driver: " + (driver != null ? driver.getName() : "N/A")
                + " Active: " + active;
    }

    @Override
    public String toString() {
        return "Bus{busId=" + busId + ", busNumber='" + busNumber
                + "', registrationNo='" + registrationNo + "', routeId='" + routeId + "'}";
    }
}
