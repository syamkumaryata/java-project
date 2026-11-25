package edu.busTracker.model;

/**
 * Customizable arrival alert settings per student.
 */
public class AlertSettings {

    private String regNo;
    private boolean enabled;
    private int minutesBefore;
    private boolean soundEnabled;
    private String lastFiredDate;
    private String updatedAt;

    public AlertSettings() {}

    public AlertSettings(String regNo, boolean enabled, int minutesBefore,
                         boolean soundEnabled, String lastFiredDate, String updatedAt) {
        this.regNo = regNo;
        this.enabled = enabled;
        this.minutesBefore = minutesBefore;
        this.soundEnabled = soundEnabled;
        this.lastFiredDate = lastFiredDate;
        this.updatedAt = updatedAt;
    }

    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) { this.regNo = regNo; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public int getMinutesBefore() { return minutesBefore; }
    public void setMinutesBefore(int minutesBefore) { this.minutesBefore = minutesBefore; }

    public boolean isSoundEnabled() { return soundEnabled; }
    public void setSoundEnabled(boolean soundEnabled) { this.soundEnabled = soundEnabled; }

    public String getLastFiredDate() { return lastFiredDate; }
    public void setLastFiredDate(String lastFiredDate) { this.lastFiredDate = lastFiredDate; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
