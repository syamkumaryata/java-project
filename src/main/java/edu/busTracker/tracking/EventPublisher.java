package edu.busTracker.tracking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Event publisher implementing the Observer Pattern.
 * Demonstrates: Observer pattern / event handling (Unit 3).
 */
@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final List<LocationEventListener> listeners = new CopyOnWriteArrayList<>();

    public void register(LocationEventListener listener) {
        listeners.add(listener);
    }

    public void publish(LocationEvent event) {
        for (LocationEventListener listener : listeners) {
            try {
                listener.onLocationUpdate(event);
            } catch (Exception e) {
                log.error("Error dispatching location event: {}", e.getMessage(), e);
            }
        }
    }
}
