package edu.busTracker.tracking;

/**
 * Observer interface for location events.
 * Listeners: SseBroadcaster, DbLocationWriter, AlertEvaluator.
 */
public interface LocationEventListener {
    void onLocationUpdate(LocationEvent event);
}
