package edu.busTracker.web;

import edu.busTracker.dao.AllocationDao;
import edu.busTracker.dao.BusDao;
import edu.busTracker.exception.TransportException;
import edu.busTracker.model.Bus;
import edu.busTracker.model.BusLocation;
import edu.busTracker.model.StudentAllocation;
import edu.busTracker.session.SessionData;
import edu.busTracker.tracking.EtaCalculator;
import edu.busTracker.tracking.LocationStore;
import edu.busTracker.tracking.SseBroadcaster;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class TrackingController {

    private final LocationStore locationStore;
    private final SseBroadcaster sseBroadcaster;
    private final EtaCalculator etaCalculator;
    private final AllocationDao allocationDao;
    private final BusDao busDao;

    @Autowired
    public TrackingController(LocationStore locationStore, SseBroadcaster sseBroadcaster,
                              EtaCalculator etaCalculator, AllocationDao allocationDao,
                              BusDao busDao) {
        this.locationStore = locationStore;
        this.sseBroadcaster = sseBroadcaster;
        this.etaCalculator = etaCalculator;
        this.allocationDao = allocationDao;
        this.busDao = busDao;
    }

    @GetMapping(value = "/stream/tracking", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTracking(HttpServletRequest request) {
        SessionData session = (SessionData) request.getAttribute("session");
        if (session == null) {
            throw new TransportException("UNAUTHORIZED", "Active session required for tracking stream", 401);
        }
        return sseBroadcaster.registerClient(session.getRegNoOrEmail(), session.getRole());
    }

    @GetMapping("/tracking/bus/{busId}")
    public ResponseEntity<Map<String, Object>> getBusTracking(@PathVariable Long busId, HttpServletRequest request) {
        SessionData session = (SessionData) request.getAttribute("session");
        if (session == null) {
            throw new TransportException("UNAUTHORIZED", "Active session required", 401);
        }

        Bus bus = busDao.findById(busId)
                .orElseThrow(() -> new TransportException("NOT_FOUND", "Bus not found: " + busId, 404));

        BusLocation loc = locationStore.getLocation(busId)
                .orElse(new BusLocation(busId, 16.2437, 80.6480, 0.0, 0.0, java.time.Instant.now().toString(), "SIMULATED"));

        Map<String, Object> res = new HashMap<>();
        res.put("busId", bus.getBusId());
        res.put("busNumber", bus.getBusNumber());
        res.put("displayBusNumber", bus.getDisplayLabel());
        res.put("registrationNo", bus.getRegistrationNo());
        res.put("latitude", loc.getLatitude());
        res.put("longitude", loc.getLongitude());
        res.put("speedKmph", loc.getSpeedKmph());
        res.put("headingDeg", loc.getHeadingDeg());
        res.put("source", "SIMULATED"); // Invariant: ALWAYS SIMULATED
        res.put("state", locationStore.getState(busId));

        if ("STUDENT".equalsIgnoreCase(session.getRole())) {
            Optional<StudentAllocation> allocOpt = allocationDao.findActiveByRegNo(session.getRegNoOrEmail());
            allocOpt.ifPresent(alloc -> {
                var etaRes = etaCalculator.eta(busId, bus.getRouteId(), alloc.getBoardingStop().getStopId());
                res.put("etaToMyStopSec", etaRes.getEtaSeconds());
                res.put("etaToMyStopMin", etaRes.getEtaMinutes());
                res.put("passedMyStop", etaRes.isPassed());
                res.put("myStopName", alloc.getBoardingStop().getName());
            });
        }

        return ResponseEntity.ok(res);
    }
}
