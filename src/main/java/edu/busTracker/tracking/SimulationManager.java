package edu.busTracker.tracking;

import edu.busTracker.dao.BusDao;
import edu.busTracker.dao.RouteDao;
import edu.busTracker.model.Bus;
import edu.busTracker.model.RouteStop;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages simulation thread pool and worker schedules.
 * Demonstrates: ScheduledExecutorService thread pool (Unit 3 Multithreading).
 */
@Component
public class SimulationManager {

    private static final Logger log = LoggerFactory.getLogger(SimulationManager.class);

    private final BusDao busDao;
    private final RouteDao routeDao;
    private final LocationStore locationStore;
    private final EventPublisher eventPublisher;
    private final SimulationClock clock;

    @Value("${app.simulation.tick-interval-ms:2000}")
    private long tickIntervalMs;

    private ScheduledExecutorService executorService;

    @Autowired
    public SimulationManager(BusDao busDao, RouteDao routeDao, LocationStore locationStore,
                             EventPublisher eventPublisher, SimulationClock clock) {
        this.busDao = busDao;
        this.routeDao = routeDao;
        this.locationStore = locationStore;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @PostConstruct
    public void startSimulation() {
        log.info("Starting GPS simulation engine (tick: {} ms)...", tickIntervalMs);
        List<Bus> buses = busDao.findActive();
        if (buses.isEmpty()) {
            log.warn("No active buses found to simulate");
            return;
        }

        executorService = Executors.newScheduledThreadPool(Math.max(2, buses.size()));

        for (Bus bus : buses) {
            List<RouteStop> stops = routeDao.loadStopsForRoute(bus.getRouteId());
            SimulatedGpsWorker worker = new SimulatedGpsWorker(bus, stops, locationStore, eventPublisher, clock);
            executorService.scheduleAtFixedRate(worker, 1000, tickIntervalMs, TimeUnit.MILLISECONDS);
            log.info("Simulated GPS worker scheduled for Bus {}", bus.getBusNumber());
        }
    }

    public void restartSimulation() {
        clock.reset();
        clock.setRunning(true);
        log.info("Simulation restarted");
    }

    public void pauseSimulation() {
        clock.setRunning(false);
        log.info("Simulation paused");
    }

    @PreDestroy
    public void stopSimulation() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
