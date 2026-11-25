package edu.busTracker.tracking;

import org.springframework.stereotype.Component;

/**
 * Maps simulation progress.
 * Modes:
 *   - DEMO: trips advance based on speed multiplier (1x - 60x).
 *   - LIVE_CLOCK: synchronizes with system wall-clock time.
 */
@Component
public class SimulationClock {

    private double speedMultiplier = 10.0;
    private boolean running = false; // Starts when admin presses Start (Section 9.2)
    private long simulationStartTimeMs = System.currentTimeMillis();

    public double getSpeedMultiplier() { return speedMultiplier; }
    public void setSpeedMultiplier(double speedMultiplier) {
        this.speedMultiplier = Math.max(1.0, Math.min(60.0, speedMultiplier));
    }

    public boolean isRunning() { return running; }
    public void setRunning(boolean running) {
        this.running = running;
        if (running) {
            this.simulationStartTimeMs = System.currentTimeMillis();
        }
    }

    public long getElapsedTimeSec() {
        if (!running) return 0;
        long elapsedRealMs = System.currentTimeMillis() - simulationStartTimeMs;
        return Math.round((elapsedRealMs / 1000.0) * speedMultiplier);
    }

    public void reset() {
        this.simulationStartTimeMs = System.currentTimeMillis();
    }
}
