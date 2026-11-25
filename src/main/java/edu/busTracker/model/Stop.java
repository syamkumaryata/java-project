package edu.busTracker.model;

/**
 * Represents a canonical bus stop.
 */
public class Stop {

    private Long stopId;
    private String name;
    private Double latitude;
    private Double longitude;
    private String geocodeStatus;
    private String geocodeSource;
    private boolean campus;
    private String notes;

    public Stop() {}

    public Stop(Long stopId, String name, Double latitude, Double longitude,
                String geocodeStatus, String geocodeSource, boolean campus, String notes) {
        this.stopId = stopId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.geocodeStatus = geocodeStatus;
        this.geocodeSource = geocodeSource;
        this.campus = campus;
        this.notes = notes;
    }

    public boolean isGeocoded() {
        return latitude != null && longitude != null
                && ("AUTO".equals(geocodeStatus) || "MANUAL".equals(geocodeStatus));
    }

    public Long getStopId() { return stopId; }
    public void setStopId(Long stopId) { this.stopId = stopId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getGeocodeStatus() { return geocodeStatus; }
    public void setGeocodeStatus(String geocodeStatus) { this.geocodeStatus = geocodeStatus; }

    public String getGeocodeSource() { return geocodeSource; }
    public void setGeocodeSource(String geocodeSource) { this.geocodeSource = geocodeSource; }

    public boolean isCampus() { return campus; }
    public void setCampus(boolean campus) { this.campus = campus; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return "Stop{stopId=" + stopId + ", name='" + name + "', geocodeStatus='" + geocodeStatus + "'}";
    }
}
