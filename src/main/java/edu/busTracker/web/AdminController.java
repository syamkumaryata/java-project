package edu.busTracker.web;

import edu.busTracker.dao.*;
import edu.busTracker.exception.TransportException;
import edu.busTracker.model.*;
import edu.busTracker.session.SessionStore;
import edu.busTracker.tracking.LocationStore;
import edu.busTracker.tracking.SimulationClock;
import edu.busTracker.tracking.SimulationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final StudentDao studentDao;
    private final BusDao busDao;
    private final DriverDao driverDao;
    private final StopDao stopDao;
    private final RouteDao routeDao;
    private final AllocationDao allocationDao;
    private final SessionStore sessionStore;
    private final SimulationClock simulationClock;
    private final SimulationManager simulationManager;
    private final LocationStore locationStore;

    @Autowired
    public AdminController(StudentDao studentDao, BusDao busDao, DriverDao driverDao,
                           StopDao stopDao, RouteDao routeDao, AllocationDao allocationDao,
                           SessionStore sessionStore, SimulationClock simulationClock,
                           SimulationManager simulationManager, LocationStore locationStore) {
        this.studentDao = studentDao;
        this.busDao = busDao;
        this.driverDao = driverDao;
        this.stopDao = stopDao;
        this.routeDao = routeDao;
        this.allocationDao = allocationDao;
        this.sessionStore = sessionStore;
        this.simulationClock = simulationClock;
        this.simulationManager = simulationManager;
        this.locationStore = locationStore;
    }

    // ── Students CRUD ──
    @GetMapping("/students")
    public List<Student> listStudents() { return studentDao.findAll(); }

    @PostMapping("/students")
    public Student createStudent(@RequestBody Student s) { return studentDao.save(s); }

    @PutMapping("/students/{regNo}")
    public ResponseEntity<?> updateStudent(@PathVariable String regNo, @RequestBody Student s) {
        s.setRegNo(regNo);
        studentDao.update(s);
        return ResponseEntity.ok(s);
    }

    @DeleteMapping("/students/{regNo}")
    public ResponseEntity<?> deleteStudent(@PathVariable String regNo) {
        studentDao.deleteById(regNo);
        return ResponseEntity.ok(Map.of("message", "Deleted student " + regNo));
    }

    // ── Buses CRUD ──
    @GetMapping("/buses")
    public List<Bus> listBuses() { return busDao.findAll(); }

    @PostMapping("/buses")
    public Bus createBus(@RequestBody Bus b) { return busDao.save(b); }

    @PutMapping("/buses/{busId}")
    public ResponseEntity<?> updateBus(@PathVariable Long busId, @RequestBody Bus b) {
        b.setBusId(busId);
        busDao.update(b);
        return ResponseEntity.ok(b);
    }

    @DeleteMapping("/buses/{busId}")
    public ResponseEntity<?> deleteBus(@PathVariable Long busId) {
        if (!allocationDao.findByBusId(busId).isEmpty()) {
            throw new TransportException("CONFLICT", "Cannot delete bus with active student allocations", 409);
        }
        busDao.deleteById(busId);
        return ResponseEntity.ok(Map.of("message", "Deleted bus " + busId));
    }

    // ── Drivers CRUD ──
    @GetMapping("/drivers")
    public List<Driver> listDrivers() { return driverDao.findAll(); }

    @PostMapping("/drivers")
    public Driver createDriver(@RequestBody Driver d) { return driverDao.save(d); }

    @PutMapping("/drivers/{driverId}")
    public ResponseEntity<?> updateDriver(@PathVariable Long driverId, @RequestBody Driver d) {
        d.setDriverId(driverId);
        driverDao.update(d);
        return ResponseEntity.ok(d);
    }

    @DeleteMapping("/drivers/{driverId}")
    public ResponseEntity<?> deleteDriver(@PathVariable Long driverId) {
        driverDao.deleteById(driverId);
        return ResponseEntity.ok(Map.of("message", "Deleted driver " + driverId));
    }

    // ── Routes & Stops ──
    @GetMapping("/routes")
    public List<Route> listRoutes() { return routeDao.findAll(); }

    @GetMapping("/stops")
    public List<Stop> listStops() { return stopDao.findAll(); }

    @PutMapping("/stops/{stopId}")
    public ResponseEntity<?> updateStopCoordinates(@PathVariable Long stopId, @RequestBody Map<String, Object> body) {
        double lat = ((Number) body.get("latitude")).doubleValue();
        double lon = ((Number) body.get("longitude")).doubleValue();
        stopDao.updateCoordinates(stopId, lat, lon, "MANUAL", "admin-edit");
        return ResponseEntity.ok(Map.of("message", "Updated stop coordinates"));
    }

    // ── Allocations ──
    @GetMapping("/allocations")
    public List<StudentAllocation> listAllocations() { return allocationDao.findAll(); }

    // ── Data Quality ──
    @GetMapping("/data-quality")
    public Map<String, Object> getDataQuality() {
        Map<String, Object> res = new HashMap<>();
        res.put("pendingStops", stopDao.findPendingOrAutoGeocoded());
        res.put("probableAliases", stopDao.findProbableAliases());
        return res;
    }

    // ── Simulation Controls ──
    @PostMapping("/simulation/start")
    public Map<String, Object> startSim() {
        simulationManager.restartSimulation();
        return Map.of("status", "RUNNING", "speed", simulationClock.getSpeedMultiplier());
    }

    @PostMapping("/simulation/stop")
    public Map<String, Object> stopSim() {
        simulationManager.pauseSimulation();
        return Map.of("status", "STOPPED");
    }

    @PostMapping("/simulation/speed")
    public Map<String, Object> setSpeed(@RequestBody Map<String, Object> body) {
        double speed = ((Number) body.get("multiplier")).doubleValue();
        simulationClock.setSpeedMultiplier(speed);
        return Map.of("speed", simulationClock.getSpeedMultiplier());
    }

    @PostMapping("/simulation/bus/{busId}/delay")
    public Map<String, Object> setBusDelay(@PathVariable Long busId, @RequestBody Map<String, Object> body) {
        int delay = ((Number) body.get("minutes")).intValue();
        locationStore.setBusDelay(busId, delay);
        return Map.of("busId", busId, "delayMinutes", delay);
    }

    // ── Session Revocation ──
    @PostMapping("/sessions/revoke")
    public Map<String, Object> revokeSessions(@RequestBody Map<String, String> body) {
        String user = body.get("regNoOrEmail");
        sessionStore.revokeAllForUser(user);
        return Map.of("message", "Revoked all sessions for " + user);
    }
}
