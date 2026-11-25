package edu.busTracker.tracking;

import edu.busTracker.model.Bus;
import edu.busTracker.model.BusLocation;
import edu.busTracker.model.RouteStop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Worker thread that updates bus position along its route road geometry / timetable.
 * Demonstrates: Runnable interface & thread execution (Unit 3 Multithreading).
 */
public class SimulatedGpsWorker implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(SimulatedGpsWorker.class);

    private final Bus bus;
    private final List<RouteStop> routeStops;
    private final LocationStore locationStore;
    private final EventPublisher eventPublisher;
    private final SimulationClock clock;

    private int currentSegmentIndex = 0;
    private double segmentProgress = 0.0; // 0.0 to 1.0 between stops
    private final Set<Long> passedStopIds = new HashSet<>();
    private String currentState = "NOT_STARTED";

    public SimulatedGpsWorker(Bus bus, List<RouteStop> routeStops,
                              LocationStore locationStore, EventPublisher eventPublisher,
                              SimulationClock clock) {
        this.bus = bus;
        this.routeStops = routeStops;
        this.locationStore = locationStore;
        this.eventPublisher = eventPublisher;
        this.clock = clock;

        // Initialize at first stop if coordinates exist
        if (!routeStops.isEmpty()) {
            RouteStop first = routeStops.get(0);
            double lat = first.getStop().getLatitude() != null ? first.getStop().getLatitude() : 16.2437;
            double lon = first.getStop().getLongitude() != null ? first.getStop().getLongitude() : 80.6480;
            BusLocation loc = new BusLocation(bus.getBusId(), lat, lon, 0.0, 0.0, Instant.now().toString(), "SIMULATED");
            locationStore.updateLocation(bus.getBusId(), loc, "NOT_STARTED", passedStopIds);
        }
    }

    @Override
    public void run() {
        try {
            if (!clock.isRunning()) {
                return;
            }

            if (routeStops.size() < 2) {
                return;
            }

            currentState = "RUNNING";

            // Advance interpolation between current stop and next stop
            double step = 0.05 * (clock.getSpeedMultiplier() / 10.0);
            segmentProgress += step;

            if (segmentProgress >= 1.0) {
                // Arrived at current target stop
                RouteStop reached = routeStops.get(currentSegmentIndex);
                passedStopIds.add(reached.getStop().getStopId());
                currentSegmentIndex++;
                segmentProgress = 0.0;

                if (currentSegmentIndex >= routeStops.size() - 1) {
                    // Reached final stop (Campus)
                    RouteStop campusStop = routeStops.get(routeStops.size() - 1);
                    passedStopIds.add(campusStop.getStop().getStopId());
                    currentState = "ARRIVED";

                    double lat = campusStop.getStop().getLatitude() != null ? campusStop.getStop().getLatitude() : 16.4638;
                    double lon = campusStop.getStop().getLongitude() != null ? campusStop.getStop().getLongitude() : 80.5078;
                    BusLocation loc = new BusLocation(bus.getBusId(), lat, lon, 0.0, 0.0, Instant.now().toString(), "SIMULATED");
                    locationStore.updateLocation(bus.getBusId(), loc, currentState, passedStopIds);

                    eventPublisher.publish(new LocationEvent(loc, bus.getBusNumber(), bus.getRouteId(), currentState,
                            campusStop.getStop().getStopId(), campusStop.getStop().getName()));
                    return;
                }
            }

            RouteStop from = routeStops.get(currentSegmentIndex);
            RouteStop to = routeStops.get(currentSegmentIndex + 1);

            double fromLat = from.getStop().getLatitude() != null ? from.getStop().getLatitude() : 16.2437;
            double fromLon = from.getStop().getLongitude() != null ? from.getStop().getLongitude() : 80.6480;
            double toLat = to.getStop().getLatitude() != null ? to.getStop().getLatitude() : 16.4638;
            double toLon = to.getStop().getLongitude() != null ? to.getStop().getLongitude() : 80.5078;

            // Linear interpolation
            double curLat = fromLat + (toLat - fromLat) * segmentProgress;
            double curLon = fromLon + (toLon - fromLon) * segmentProgress;
            double speed = 35.0; // km/h

            BusLocation loc = new BusLocation(bus.getBusId(), curLat, curLon, speed, 45.0, Instant.now().toString(), "SIMULATED");
            locationStore.updateLocation(bus.getBusId(), loc, currentState, passedStopIds);

            eventPublisher.publish(new LocationEvent(loc, bus.getBusNumber(), bus.getRouteId(), currentState,
                    to.getStop().getStopId(), to.getStop().getName()));

        } catch (Exception e) {
            log.error("Error in SimulatedGpsWorker for bus {}: {}", bus.getBusNumber(), e.getMessage());
        }
    }
}
