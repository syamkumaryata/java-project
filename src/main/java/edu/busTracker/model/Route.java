package edu.busTracker.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bus route (e.g. TN-2, TN-3, TN-4).
 * Demonstrates: ArrayList/List usage (Unit 4 Collections).
 */
public class Route {

    private String routeId;
    private String name;
    private String region;
    private String geometryJson;
    private String geometrySource;
    private List<RouteStop> stops = new ArrayList<>();

    public Route() {}

    public Route(String routeId, String name, String region) {
        this.routeId = routeId;
        this.name = name;
        this.region = region;
    }

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getGeometryJson() { return geometryJson; }
    public void setGeometryJson(String geometryJson) { this.geometryJson = geometryJson; }

    public String getGeometrySource() { return geometrySource; }
    public void setGeometrySource(String geometrySource) { this.geometrySource = geometrySource; }

    public List<RouteStop> getStops() { return stops; }
    public void setStops(List<RouteStop> stops) { this.stops = stops; }

    @Override
    public String toString() {
        return "Route{routeId='" + routeId + "', name='" + name + "', stops=" + stops.size() + "}";
    }
}
