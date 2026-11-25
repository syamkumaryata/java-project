package edu.busTracker.alternatives;

import java.io.Serializable;
import java.util.List;

/**
 * DTO representing an eligible alternative university bus suggestion.
 */
public class AlternativeBusOption implements Serializable {

    private Long busId;
    private String busNumber;
    private String displayBusNumber;
    private String registrationNo;
    private String driverName;
    private String driverPhone;
    private String routeId;
    private String routeName;
    private String destination;
    private double currentLatitude;
    private double currentLongitude;
    private String source;            // "SIMULATED"
    private long etaToStopSeconds;
    private long etaToStopMinutes;
    private String matchType;         // "SAME_STOP"
    private List<String> intermediateStops;

    public AlternativeBusOption() {}

    public Long getBusId() { return busId; }
    public void setBusId(Long busId) { this.busId = busId; }

    public String getBusNumber() { return busNumber; }
    public void setBusNumber(String busNumber) { this.busNumber = busNumber; }

    public String getDisplayBusNumber() { return displayBusNumber; }
    public void setDisplayBusNumber(String displayBusNumber) { this.displayBusNumber = displayBusNumber; }

    public String getRegistrationNo() { return registrationNo; }
    public void setRegistrationNo(String registrationNo) { this.registrationNo = registrationNo; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getDriverPhone() { return driverPhone; }
    public void setDriverPhone(String driverPhone) { this.driverPhone = driverPhone; }

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public double getCurrentLatitude() { return currentLatitude; }
    public void setCurrentLatitude(double currentLatitude) { this.currentLatitude = currentLatitude; }

    public double getCurrentLongitude() { return currentLongitude; }
    public void setCurrentLongitude(double currentLongitude) { this.currentLongitude = currentLongitude; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public long getEtaToStopSeconds() { return etaToStopSeconds; }
    public void setEtaToStopSeconds(long etaToStopSeconds) { this.etaToStopSeconds = etaToStopSeconds; }

    public long getEtaToStopMinutes() { return etaToStopMinutes; }
    public void setEtaToStopMinutes(long etaToStopMinutes) { this.etaToStopMinutes = etaToStopMinutes; }

    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }

    public List<String> getIntermediateStops() { return intermediateStops; }
    public void setIntermediateStops(List<String> intermediateStops) { this.intermediateStops = intermediateStops; }
}
