package edu.busTracker.model;

public class StopAlias {

    private String aliasNormalized;
    private Long stopId;
    private String stopName;
    private String confidence;

    public StopAlias() {}

    public StopAlias(String aliasNormalized, Long stopId, String stopName, String confidence) {
        this.aliasNormalized = aliasNormalized;
        this.stopId = stopId;
        this.stopName = stopName;
        this.confidence = confidence;
    }

    public String getAliasNormalized() { return aliasNormalized; }
    public void setAliasNormalized(String aliasNormalized) { this.aliasNormalized = aliasNormalized; }

    public Long getStopId() { return stopId; }
    public void setStopId(Long stopId) { this.stopId = stopId; }

    public String getStopName() { return stopName; }
    public void setStopName(String stopName) { this.stopName = stopName; }

    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }
}
