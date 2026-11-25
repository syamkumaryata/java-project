package edu.busTracker.alternatives;

import edu.busTracker.dao.AllocationDao;
import edu.busTracker.dao.BusDao;
import edu.busTracker.dao.RouteDao;
import edu.busTracker.dao.StudentDao;
import edu.busTracker.exception.StudentNotEligibleException;
import edu.busTracker.model.*;
import edu.busTracker.tracking.EtaCalculator;
import edu.busTracker.tracking.LocationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Finds eligible alternative university buses serving a student's boarding stop.
 * Section 11 Rules:
 *   1. Student must be authorized (active, fee PAID, allocated).
 *   2. Boarding stop S must not be the campus stop.
 *   3. Alternative bus B must serve stop S on its route.
 *   4. B must be different from allocated bus.
 *   5. Exclude B if already passed stop S or ARRIVED/NOT_STARTED without future schedule.
 *   6. Sort results by ETA ascending.
 */
@Service
public class AlternativeBusService {

    private static final Logger log = LoggerFactory.getLogger(AlternativeBusService.class);

    private final StudentDao studentDao;
    private final AllocationDao allocationDao;
    private final BusDao busDao;
    private final RouteDao routeDao;
    private final LocationStore locationStore;
    private final EtaCalculator etaCalculator;

    @Value("${app.alternatives.allow-cross-route-boarding:true}")
    private boolean allowCrossRouteBoarding;

    @Autowired
    public AlternativeBusService(StudentDao studentDao, AllocationDao allocationDao,
                                 BusDao busDao, RouteDao routeDao,
                                 LocationStore locationStore, EtaCalculator etaCalculator) {
        this.studentDao = studentDao;
        this.allocationDao = allocationDao;
        this.busDao = busDao;
        this.routeDao = routeDao;
        this.locationStore = locationStore;
        this.etaCalculator = etaCalculator;
    }

    public List<AlternativeBusOption> findAlternatives(String regNo) {
        if (!allowCrossRouteBoarding) {
            log.info("Cross route boarding disabled by policy flag");
            return Collections.emptyList();
        }

        // Rule 1: Student is currently authorized
        Student student = studentDao.findById(regNo)
                .orElseThrow(() -> new StudentNotEligibleException("NOT_FOUND", "Student not found"));

        if (!student.isTransportActive() || !"PAID".equalsIgnoreCase(student.getFeeStatus())) {
            throw new StudentNotEligibleException("NOT_AUTHORIZED", "Student is not currently authorized for transport");
        }

        StudentAllocation alloc = allocationDao.findActiveByRegNo(regNo)
                .orElseThrow(() -> new StudentNotEligibleException("NO_ALLOCATION", "No active bus allocation found"));

        Stop boardingStop = alloc.getBoardingStop();
        Long boardingStopId = boardingStop.getStopId();

        // Rule 2: S is not the campus stop
        if (boardingStop.isCampus()) {
            return Collections.emptyList();
        }

        Long allocatedBusId = alloc.getBus().getBusId();
        List<Bus> activeBuses = busDao.findActive();
        List<AlternativeBusOption> suggestions = new ArrayList<>();

        for (Bus b : activeBuses) {
            // Rule 4: Exclude the allocated bus
            if (b.getBusId().equals(allocatedBusId)) {
                continue;
            }

            // Rule 3: Alternative bus's route must serve boarding stop S
            if (!routeDao.stopIsOnRoute(b.getRouteId(), boardingStopId)) {
                continue;
            }

            // Rule 5: Exclude if simulator reports bus has already passed S or is ARRIVED
            String state = locationStore.getState(b.getBusId());
            if ("ARRIVED".equals(state) || locationStore.hasPassedStop(b.getBusId(), boardingStopId)) {
                continue;
            }

            var etaRes = etaCalculator.eta(b.getBusId(), b.getRouteId(), boardingStopId);
            if (etaRes.isPassed()) {
                continue;
            }

            // Build Option
            AlternativeBusOption opt = new AlternativeBusOption();
            opt.setBusId(b.getBusId());
            opt.setBusNumber(b.getBusNumber());
            opt.setDisplayBusNumber(b.getDisplayLabel());
            opt.setRegistrationNo(b.getRegistrationNo());
            if (b.getDriver() != null) {
                opt.setDriverName(b.getDriver().getName());
                opt.setDriverPhone(b.getDriver().getPhone());
            }
            opt.setRouteId(b.getRouteId());
            opt.setDestination("SRM University-AP (Campus)");
            opt.setSource("SIMULATED"); // Invariant: ALWAYS SIMULATED
            opt.setMatchType("SAME_STOP");
            opt.setEtaToStopSeconds(etaRes.getEtaSeconds());
            opt.setEtaToStopMinutes(etaRes.getEtaMinutes());

            locationStore.getLocation(b.getBusId()).ifPresent(loc -> {
                opt.setCurrentLatitude(loc.getLatitude());
                opt.setCurrentLongitude(loc.getLongitude());
            });

            suggestions.add(opt);
        }

        // Rule 6: Sort by ETA ascending
        suggestions.sort(Comparator.comparingLong(AlternativeBusOption::getEtaToStopSeconds));
        return suggestions;
    }
}
