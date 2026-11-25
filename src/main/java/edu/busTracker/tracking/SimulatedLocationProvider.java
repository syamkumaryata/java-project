package edu.busTracker.tracking;

import edu.busTracker.model.BusLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SimulatedLocationProvider implements LocationProvider {

    private final LocationStore locationStore;

    @Autowired
    public SimulatedLocationProvider(LocationStore locationStore) {
        this.locationStore = locationStore;
    }

    @Override
    public Optional<BusLocation> getCurrentLocation(Long busId) {
        return locationStore.getLocation(busId);
    }

    @Override
    public String getSourceType() {
        return "SIMULATED";
    }
}
