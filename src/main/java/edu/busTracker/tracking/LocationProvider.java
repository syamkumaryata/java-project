package edu.busTracker.tracking;

import edu.busTracker.model.BusLocation;
import java.util.Optional;

/**
 * Interface for location producers.
 * Demonstrates: interface polymorphism (Unit 1 & 2).
 * Implementations: SimulatedLocationProvider (demo) and DeviceLocationProvider (real GPS devices).
 */
public interface LocationProvider {
    Optional<BusLocation> getCurrentLocation(Long busId);
    String getSourceType(); // "SIMULATED" or "DEVICE"
}
