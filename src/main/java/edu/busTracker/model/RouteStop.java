package edu.busTracker.model;

/**
 * Represents a stop on a specific route, with sequence order and scheduled time.
 */
public class RouteStop {

    private String routeId;
    private Stop stop;
    private int sequence;
    private String scheduledTime;

    public RouteStop() {}

    public RouteStop(String routeId, Stop stop, int sequence, String scheduledTime) {
        this.routeId = routeId;
        this.stop = stop;
        this.sequence = sequence;
        this.scheduledTime = scheduledTime;
    }

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public Stop getStop() { return stop; }
    public void setStop(Stop stop) { this.stop = stop; }

    public int getSequence() { return sequence; }
    public void setSequence(int sequence) { this.sequence = sequence; }

    public String getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(String scheduledTime) { this.scheduledTime = scheduledTime; }

    @Override
    public String toString() {
        return "RouteStop{route='" + routeId + "', seq=" + sequence
                + ", stop='" + (stop != null ? stop.getName() : "null") + "', time='" + scheduledTime + "'}";
    }
}
