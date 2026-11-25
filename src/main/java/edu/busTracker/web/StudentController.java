package edu.busTracker.web;

import edu.busTracker.dao.*;
import edu.busTracker.exception.SessionExpiredException;
import edu.busTracker.exception.TransportException;
import edu.busTracker.model.*;
import edu.busTracker.session.SessionData;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentDao studentDao;
    private final AllocationDao allocationDao;
    private final RouteDao routeDao;
    private final AlertSettingsDao alertSettingsDao;

    @Autowired
    public StudentController(StudentDao studentDao, AllocationDao allocationDao,
                             RouteDao routeDao, AlertSettingsDao alertSettingsDao) {
        this.studentDao = studentDao;
        this.allocationDao = allocationDao;
        this.routeDao = routeDao;
        this.alertSettingsDao = alertSettingsDao;
    }

    private String getLoggedInRegNo(HttpServletRequest request) {
        SessionData session = (SessionData) request.getAttribute("session");
        if (session == null || !"STUDENT".equalsIgnoreCase(session.getRole())) {
            throw new SessionExpiredException("Student authentication required");
        }
        return session.getRegNoOrEmail();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(HttpServletRequest request) {
        String regNo = getLoggedInRegNo(request);

        Student student = studentDao.findById(regNo)
                .orElseThrow(() -> new TransportException("STUDENT_NOT_FOUND", "Student not found", 404));

        Optional<StudentAllocation> allocOpt = allocationDao.findActiveByRegNo(regNo);

        Map<String, Object> data = new HashMap<>();
        data.put("regNo", student.getRegNo());
        data.put("name", student.getName());
        data.put("email", student.getEmail());
        data.put("feeStatus", student.getFeeStatus());
        data.put("transportActive", student.isTransportActive());

        if (allocOpt.isPresent()) {
            StudentAllocation alloc = allocOpt.get();
            Bus bus = alloc.getBus();
            data.put("allocated", true);
            data.put("busNumber", bus.getBusNumber());
            data.put("displayBusNumber", bus.getDisplayLabel());
            data.put("registrationNo", bus.getRegistrationNo());
            data.put("busId", bus.getBusId());
            data.put("routeId", bus.getRouteId());

            Driver driver = bus.getDriver();
            if (driver != null) {
                data.put("driverName", driver.getName());
                data.put("driverPhone", driver.getPhone());
            }

            data.put("boardingStop", alloc.getBoardingStop().getName());
            data.put("boardingStopId", alloc.getBoardingStop().getStopId());
            data.put("destination", "SRM University-AP (Campus)");
            data.put("academicYear", alloc.getAcademicYear());

            // Summary of route
            List<RouteStop> stops = routeDao.loadStopsForRoute(bus.getRouteId());
            data.put("totalStops", stops.size());
            if (!stops.isEmpty()) {
                data.put("departureTime", stops.get(0).getScheduledTime());
                data.put("arrivalTime", stops.get(stops.size() - 1).getScheduledTime());
            }
        } else {
            data.put("allocated", false);
        }

        return ResponseEntity.ok(data);
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(HttpServletRequest request) {
        String regNo = getLoggedInRegNo(request);

        Student student = studentDao.findById(regNo)
                .orElseThrow(() -> new TransportException("STUDENT_NOT_FOUND", "Student not found", 404));

        Optional<StudentAllocation> allocOpt = allocationDao.findActiveByRegNo(regNo);

        Map<String, Object> data = new HashMap<>();
        data.put("student", student);
        allocOpt.ifPresent(alloc -> data.put("allocation", alloc));

        return ResponseEntity.ok(data);
    }

    @GetMapping("/route")
    public ResponseEntity<Map<String, Object>> getRoute(HttpServletRequest request) {
        String regNo = getLoggedInRegNo(request);

        StudentAllocation alloc = allocationDao.findActiveByRegNo(regNo)
                .orElseThrow(() -> new TransportException("NO_ALLOCATION", "No active bus allocation", 404));

        String routeId = alloc.getBus().getRouteId();
        Route route = routeDao.findByIdWithStops(routeId)
                .orElseThrow(() -> new TransportException("ROUTE_NOT_FOUND", "Route not found: " + routeId, 404));

        List<Map<String, Object>> stopsList = new ArrayList<>();
        Long boardingStopId = alloc.getBoardingStop().getStopId();

        for (RouteStop rs : route.getStops()) {
            Map<String, Object> sMap = new HashMap<>();
            sMap.put("sequence", rs.getSequence());
            sMap.put("stopId", rs.getStop().getStopId());
            sMap.put("name", rs.getStop().getName());
            sMap.put("latitude", rs.getStop().getLatitude());
            sMap.put("longitude", rs.getStop().getLongitude());
            sMap.put("isGeocoded", rs.getStop().isGeocoded());
            sMap.put("isCampus", rs.getStop().isCampus());
            sMap.put("scheduledTime", rs.getScheduledTime());
            sMap.put("isMyBoardingStop", rs.getStop().getStopId().equals(boardingStopId));
            sMap.put("status", rs.getStop().isCampus() ? "FINAL" : "UPCOMING");
            stopsList.add(sMap);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("routeId", route.getRouteId());
        res.put("routeName", route.getName());
        res.put("region", route.getRegion());
        res.put("geometryJson", route.getGeometryJson());
        res.put("geometrySource", route.getGeometrySource());
        res.put("stops", stopsList);

        return ResponseEntity.ok(res);
    }

    @GetMapping("/alerts")
    public ResponseEntity<AlertSettings> getAlertSettings(HttpServletRequest request) {
        String regNo = getLoggedInRegNo(request);
        AlertSettings settings = alertSettingsDao.findByRegNo(regNo)
                .orElse(new AlertSettings(regNo, false, 10, true, null, java.time.Instant.now().toString()));
        return ResponseEntity.ok(settings);
    }

    @PutMapping("/alerts")
    public ResponseEntity<?> updateAlertSettings(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String regNo = getLoggedInRegNo(request);

        boolean enabled = Boolean.TRUE.equals(body.get("enabled"));
        boolean soundEnabled = Boolean.TRUE.equals(body.get("soundEnabled"));

        int minutesBefore = 10;
        if (body.get("minutesBefore") instanceof Number n) {
            minutesBefore = n.intValue();
        }

        if (minutesBefore != 5 && minutesBefore != 10 && minutesBefore != 15) {
            throw new TransportException("INVALID_ALERT_SETTING", "minutesBefore must be 5, 10, or 15", 400);
        }

        AlertSettings settings = new AlertSettings(regNo, enabled, minutesBefore, soundEnabled, null, java.time.Instant.now().toString());
        alertSettingsDao.upsert(settings);

        return ResponseEntity.ok(settings);
    }
}
