package edu.busTracker.tracking;

import edu.busTracker.dao.AllocationDao;
import edu.busTracker.model.StudentAllocation;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages Server-Sent Events subscribers and broadcasts live updates.
 * Demonstrates:
 *   - ConcurrentHashMap (Unit 4)
 *   - Iterator with removal for dead emitters (Unit 4 Collections)
 *   - Synchronized emitter delivery (Unit 3)
 */
@Component
public class SseBroadcaster implements LocationEventListener {

    private static final Logger log = LoggerFactory.getLogger(SseBroadcaster.class);

    private final EventPublisher eventPublisher;
    private final LocationStore locationStore;
    private final EtaCalculator etaCalculator;
    private final AllocationDao allocationDao;

    // session or user identifier -> list of emitters
    private final Map<String, List<SseClient>> clients = new ConcurrentHashMap<>();
    private final AtomicLong eventSequence = new AtomicLong(0);

    @Autowired
    public SseBroadcaster(EventPublisher eventPublisher, LocationStore locationStore,
                          EtaCalculator etaCalculator, AllocationDao allocationDao) {
        this.eventPublisher = eventPublisher;
        this.locationStore = locationStore;
        this.etaCalculator = etaCalculator;
        this.allocationDao = allocationDao;
    }

    @PostConstruct
    public void init() {
        eventPublisher.register(this);
    }

    public static class SseClient {
        private final String userId;
        private final String role;
        private final SseEmitter emitter;
        private Long allocatedBusId;
        private Long boardingStopId;
        private String routeId;

        public SseClient(String userId, String role, SseEmitter emitter) {
            this.userId = userId;
            this.role = role;
            this.emitter = emitter;
        }

        public void setAllocation(Long busId, Long stopId, String routeId) {
            this.allocatedBusId = busId;
            this.boardingStopId = stopId;
            this.routeId = routeId;
        }

        public String getUserId() { return userId; }
        public String getRole() { return role; }
        public SseEmitter getEmitter() { return emitter; }
        public Long getAllocatedBusId() { return allocatedBusId; }
        public Long getBoardingStopId() { return boardingStopId; }
        public String getRouteId() { return routeId; }
    }

    public SseEmitter registerClient(String userId, String role) {
        SseEmitter emitter = new SseEmitter(1800000L); // 30 min timeout
        SseClient client = new SseClient(userId, role, emitter);

        if ("STUDENT".equalsIgnoreCase(role)) {
            allocationDao.findActiveByRegNo(userId).ifPresent(alloc -> {
                client.setAllocation(alloc.getBus().getBusId(),
                                     alloc.getBoardingStop().getStopId(),
                                     alloc.getBus().getRouteId());
            });
        }

        clients.computeIfAbsent(userId, k -> new ArrayList<>()).add(client);

        Runnable cleanup = () -> removeClient(userId, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        // Send initial connection event
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .id(String.valueOf(eventSequence.incrementAndGet()))
                    .data(Map.of("status", "connected", "simulatedGps", true)));
        } catch (IOException e) {
            cleanup.run();
        }

        return emitter;
    }

    public synchronized void removeClient(String userId, SseEmitter emitter) {
        List<SseClient> list = clients.get(userId);
        if (list != null) {
            // Demonstrates: Iterator removal (Unit 4)
            Iterator<SseClient> it = list.iterator();
            while (it.hasNext()) {
                if (it.next().getEmitter() == emitter) {
                    it.remove();
                    break;
                }
            }
            if (list.isEmpty()) clients.remove(userId);
        }
    }

    @Override
    public void onLocationUpdate(LocationEvent event) {
        long seq = eventSequence.incrementAndGet();

        // Broadcast to clients
        for (Map.Entry<String, List<SseClient>> entry : clients.entrySet()) {
            Iterator<SseClient> it = entry.getValue().iterator();
            while (it.hasNext()) {
                SseClient client = it.next();

                // Check visibility: Admin sees all; Student sees own allocated bus + alternatives
                boolean visible = "ADMIN".equalsIgnoreCase(client.getRole()) ||
                        (client.getAllocatedBusId() != null && client.getAllocatedBusId().equals(event.getLocation().getBusId()));

                if (!visible) continue;

                Map<String, Object> payload = new HashMap<>();
                payload.put("busId", event.getLocation().getBusId());
                payload.put("busNumber", event.getBusNumber());
                payload.put("latitude", event.getLocation().getLatitude());
                payload.put("longitude", event.getLocation().getLongitude());
                payload.put("speedKmph", event.getLocation().getSpeedKmph());
                payload.put("headingDeg", event.getLocation().getHeadingDeg());
                payload.put("source", "SIMULATED"); // Invariant: ALWAYS SIMULATED
                payload.put("state", event.getState());
                payload.put("recordedAt", event.getLocation().getRecordedAt());

                if (client.getBoardingStopId() != null && client.getRouteId() != null) {
                    var etaRes = etaCalculator.eta(event.getLocation().getBusId(), client.getRouteId(), client.getBoardingStopId());
                    payload.put("etaToMyStopSec", etaRes.getEtaSeconds());
                    payload.put("passedMyStop", etaRes.isPassed());
                }

                try {
                    synchronized (client.getEmitter()) {
                        client.getEmitter().send(SseEmitter.event()
                                .name("bus-location")
                                .id(String.valueOf(seq))
                                .data(payload));
                    }
                } catch (Exception ex) {
                    it.remove(); // Drop dead emitter
                }
            }
        }
    }

    public void sendArrivalAlert(String regNo, String busNumber, String stopName, int minutesBefore, long etaSec) {
        List<SseClient> userClients = clients.get(regNo);
        if (userClients == null) return;

        Map<String, Object> data = Map.of(
            "busNumber", busNumber,
            "stopName", stopName,
            "minutesBefore", minutesBefore,
            "etaSec", etaSec
        );

        long seq = eventSequence.incrementAndGet();
        for (SseClient c : userClients) {
            try {
                synchronized (c.getEmitter()) {
                    c.getEmitter().send(SseEmitter.event()
                            .name("arrival-alert")
                            .id(String.valueOf(seq))
                            .data(data));
                }
            } catch (Exception e) {
                // dropped
            }
        }
    }
}
