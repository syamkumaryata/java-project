package edu.busTracker.tracking;

import edu.busTracker.model.BusLocation;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe in-memory store for the latest location and state of each bus.
 * Demonstrates:
 *   - ConcurrentHashMap (Unit 4 Collections)
 *   - HashSet (Unit 4 Collections)
 *   - ReentrantReadWriteLock & synchronized blocks (Unit 3 Multithreading)
 */
@Component
public class LocationStore {

    private final Map<Long, BusLocation> latestLocations = new ConcurrentHashMap<>();
    private final Map<Long, String> busStates = new ConcurrentHashMap<>();          // NOT_STARTED, RUNNING, ARRIVED
    private final Map<Long, Set<Long>> passedStops = new ConcurrentHashMap<>();    // busId -> set of passed stopIds
    private final Map<Long, Integer> busDelays = new ConcurrentHashMap<>();         // busId -> delay in minutes

    // ReentrantReadWriteLock to coordinate multi-field atomic reads and writes
    // (Prevents readers from observing half an update)
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    public void updateLocation(Long busId, BusLocation location, String state, Set<Long> passed) {
        rwLock.writeLock().lock();
        try {
            latestLocations.put(busId, location);
            busStates.put(busId, state);
            if (passed != null) {
                passedStops.put(busId, new HashSet<>(passed));
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public Optional<BusLocation> getLocation(Long busId) {
        rwLock.readLock().lock();
        try {
            return Optional.ofNullable(latestLocations.get(busId));
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public String getState(Long busId) {
        return busStates.getOrDefault(busId, "NOT_STARTED");
    }

    public boolean hasPassedStop(Long busId, Long stopId) {
        Set<Long> passed = passedStops.get(busId);
        return passed != null && passed.contains(stopId);
    }

    public Set<Long> getPassedStops(Long busId) {
        Set<Long> set = passedStops.get(busId);
        return set != null ? Collections.unmodifiableSet(set) : Collections.emptySet();
    }

    public void setBusDelay(Long busId, int delayMinutes) {
        busDelays.put(busId, delayMinutes);
    }

    public int getBusDelay(Long busId) {
        return busDelays.getOrDefault(busId, 0);
    }

    public void clear() {
        rwLock.writeLock().lock();
        try {
            latestLocations.clear();
            busStates.clear();
            passedStops.clear();
            busDelays.clear();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public Map<Long, BusLocation> getAllLocations() {
        rwLock.readLock().lock();
        try {
            return new HashMap<>(latestLocations);
        } finally {
            rwLock.readLock().unlock();
        }
    }
}
