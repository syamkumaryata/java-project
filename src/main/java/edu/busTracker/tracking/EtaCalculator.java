package edu.busTracker.tracking;

import edu.busTracker.dao.RouteDao;
import edu.busTracker.model.BusLocation;
import edu.busTracker.model.RouteStop;
import edu.busTracker.model.Stop;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Calculates remaining time (in seconds) to a target stop.
 * Demonstrates: method overloading (Unit 2 Polymorphism).
 */
@Component
public class EtaCalculator {

    private final RouteDao routeDao;
    private final LocationStore locationStore;

    @Autowired
    public EtaCalculator(RouteDao routeDao, LocationStore locationStore) {
        this.routeDao = routeDao;
        this.locationStore = locationStore;
    }

    /**
     * Overload 1: Calculate ETA using stop ID.
     */
    public EtaResult eta(Long busId, String routeId, Long targetStopId) {
        if (locationStore.hasPassedStop(busId, targetStopId)) {
            return new EtaResult(0, true, "PASSED");
        }

        List<RouteStop> stops = routeDao.loadStopsForRoute(routeId);
        RouteStop target = null;
        for (RouteStop rs : stops) {
            if (rs.getStop().getStopId().equals(targetStopId)) {
                target = rs;
                break;
            }
        }

        if (target == null) {
            return new EtaResult(0, false, "NOT_FOUND");
        }

        return calculate(busId, stops, target);
    }

    /**
     * Overload 2: Calculate ETA using Stop entity.
     */
    public EtaResult eta(Long busId, String routeId, Stop targetStop) {
        if (targetStop == null || targetStop.getStopId() == null) {
            return new EtaResult(0, false, "UNKNOWN");
        }
        return eta(busId, routeId, targetStop.getStopId());
    }

    private EtaResult calculate(Long busId, List<RouteStop> stops, RouteStop target) {
        BusLocation loc = locationStore.getLocation(busId).orElse(null);
        int delayMin = locationStore.getBusDelay(busId);

        if (loc == null || "NOT_STARTED".equals(locationStore.getState(busId))) {
            return new EtaResult(900 + (delayMin * 60), false, "NOT_STARTED");
        }

        if ("ARRIVED".equals(locationStore.getState(busId))) {
            return new EtaResult(0, true, "ARRIVED");
        }

        // Distance from current position to target stop
        double distKm = 0.0;
        if (target.getStop().getLatitude() != null && target.getStop().getLongitude() != null) {
            distKm = DistanceCalculator.distance(loc.getLatitude(), loc.getLongitude(),
                    target.getStop().getLatitude(), target.getStop().getLongitude());
        }

        double speedKmph = (loc.getSpeedKmph() != null && loc.getSpeedKmph() > 5.0) ? loc.getSpeedKmph() : 35.0;
        long etaSec = Math.round((distKm / speedKmph) * 3600.0) + (delayMin * 60L);
        if (etaSec < 0) etaSec = 0;

        return new EtaResult(etaSec, false, target.getStop().getName());
    }

    public static class EtaResult {
        private final long etaSeconds;
        private final boolean passed;
        private final String stopName;

        public EtaResult(long etaSeconds, boolean passed, String stopName) {
            this.etaSeconds = etaSeconds;
            this.passed = passed;
            this.stopName = stopName;
        }

        public long getEtaSeconds() { return etaSeconds; }
        public boolean isPassed() { return passed; }
        public String getStopName() { return stopName; }
        public long getEtaMinutes() { return Math.max(0, Math.round(etaSeconds / 60.0)); }
    }
}
