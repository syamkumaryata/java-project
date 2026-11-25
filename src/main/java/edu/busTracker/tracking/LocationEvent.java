package edu.busTracker.tracking;

import edu.busTracker.model.BusLocation;

/**
 * Event published on every location tick or status change.
 * Demonstrates: observer pattern (Unit 3).
 */
public class LocationEvent {

    private final BusLocation location;
    private final String busNumber;
    private final String routeId;
    private final String state;       // "NOT_STARTED" | "RUNNING" | "ARRIVED"
    private final Long nextStopId;
    private final String nextStopName;

    public LocationEvent(BusLocation location, String busNumber, String routeId,
                         String state, Long nextStopId, String nextStopName) {
        this.location = location;
        this.busNumber = busNumber;
        this.routeId = routeId;
        this.state = state;
        this.nextStopId = nextStopId;
        this.nextStopName = nextStopName;
    }

    public BusLocation getLocation() { return location; }
    public String getBusNumber() { return busNumber; }
    public String getRouteId() { return routeId; }
    public String getState() { return state; }
    public Long getNextStopId() { return nextStopId; }
    public String getNextStopName() { return nextStopName; }
}
