package edu.busTracker.model;

/**
 * Latest GPS location of a bus.
 * source MUST be "SIMULATED" for simulated GPS.
 */
public class BusLocation {

    private Long busId;
    private double latitude;
    private double longitude;
    private Double speedKmph;
    private Double headingDeg;
    private String recordedAt;
    private String source;

    public BusLocation() {}

    public BusLocation(Long busId, double latitude, double longitude,
                       Double speedKmph, Double headingDeg,
                       String recordedAt, String source) {
        this.busId = busId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speedKmph = speedKmph;
        this.headingDeg = headingDeg;
        this.recordedAt = recordedAt;
        this.source = source;
    }

    public Long getBusId() { return busId; }
    public void setBusId(Long busId) { this.busId = busId; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public Double getSpeedKmph() { return speedKmph; }
    public void setSpeedKmph(Double speedKmph) { this.speedKmph = speedKmph; }

    public Double getHeadingDeg() { return headingDeg; }
    public void setHeadingDeg(Double headingDeg) { this.headingDeg = headingDeg; }

    public String getRecordedAt() { return recordedAt; }
    public void setRecordedAt(String recordedAt) { this.recordedAt = recordedAt; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    @Override
    public String toString() {
        return "BusLocation{busId=" + busId + ", lat=" + latitude + ", lon=" + longitude
                + ", source='" + source + "', at='" + recordedAt + "'}";
    }
}
