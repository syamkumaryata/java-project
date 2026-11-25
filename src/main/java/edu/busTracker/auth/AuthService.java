package edu.busTracker.auth;

import edu.busTracker.dao.*;
import edu.busTracker.exception.InvalidBoardingStopException;
import edu.busTracker.exception.StudentNotEligibleException;
import edu.busTracker.exception.TransportException;
import edu.busTracker.model.*;
import edu.busTracker.session.SessionData;
import edu.busTracker.session.SessionStore;
import edu.busTracker.university.MockTransportAuthorizationClient;
import edu.busTracker.university.TransportAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final SecureRandom secureRandom = new SecureRandom();

    private final SessionStore sessionStore;
    private final StudentDao studentDao;
    private final BusDao busDao;
    private final StopDao stopDao;
    private final RouteDao routeDao;
    private final AllocationDao allocationDao;
    private final AdminDao adminDao;
    private final MockTransportAuthorizationClient mockUniversityClient;

    @Value("${app.auth.mode:demo}")
    private String authMode;

    @Autowired
    public AuthService(SessionStore sessionStore, StudentDao studentDao, BusDao busDao,
                       StopDao stopDao, RouteDao routeDao, AllocationDao allocationDao,
                       AdminDao adminDao, MockTransportAuthorizationClient mockUniversityClient) {
        this.sessionStore = sessionStore;
        this.studentDao = studentDao;
        this.busDao = busDao;
        this.stopDao = stopDao;
        this.routeDao = routeDao;
        this.allocationDao = allocationDao;
        this.adminDao = adminDao;
        this.mockUniversityClient = mockUniversityClient;
    }

    public String generateSecureId(int byteCount) {
        byte[] bytes = new byte[byteCount];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public SessionData loginUser(String email, String userAgent) {
        log.info("Processing login for email: {}", email);

        // Check if admin
        if (adminDao.isAdmin(email)) {
            log.info("Authenticated as ADMIN: {}", email);
            String sid = generateSecureId(32);
            String csrf = generateSecureId(16);
            SessionData session = new SessionData(sid, email, "ADMIN", csrf, userAgent);
            sessionStore.save(session);
            return session;
        }

        // Student Authorization Lookup
        TransportAuthorization auth = mockUniversityClient.lookup(email);

        // Eligibility Rules Enforced:
        if (!auth.isEnrolledInTransport()) {
            throw new StudentNotEligibleException("TRANSPORT_NOT_ENROLLED",
                    "You are not currently enrolled in the university bus transport service.");
        }

        if (!"PAID".equalsIgnoreCase(auth.getFeeStatus())) {
            throw new StudentNotEligibleException("FEE_NOT_PAID",
                    "Your transport fee status is " + auth.getFeeStatus() + ". Access requires PAID status.");
        }

        if (auth.getBusNumber() == null || auth.getBoardingStop() == null) {
            throw new StudentNotEligibleException("NO_ALLOCATION",
                    "No bus or boarding stop has been allocated to your account.");
        }

        // Resolve canonical stop via alias table
        Optional<Stop> stopOpt = stopDao.resolveAlias(auth.getBoardingStop());
        if (stopOpt.isEmpty()) {
            stopOpt = stopDao.findByName(auth.getBoardingStop());
        }

        if (stopOpt.isEmpty()) {
            throw new StudentNotEligibleException("NO_ALLOCATION",
                    "Your boarding point '" + auth.getBoardingStop() + "' could not be matched. Contact the transport office.");
        }

        Stop boardingStop = stopOpt.get();

        // Validate Boarding Stop Invariants:
        if (boardingStop.isCampus()) {
            throw new InvalidBoardingStopException("Boarding stop cannot be the campus stop.");
        }

        Optional<Bus> busOpt = busDao.findByBusNumber(auth.getBusNumber());
        if (busOpt.isEmpty()) {
            throw new StudentNotEligibleException("NO_ALLOCATION", "Allocated bus " + auth.getBusNumber() + " not found.");
        }

        Bus bus = busOpt.get();
        if (!routeDao.stopIsOnRoute(bus.getRouteId(), boardingStop.getStopId())) {
            throw new InvalidBoardingStopException("Boarding stop " + boardingStop.getName() + " does not belong to route " + bus.getRouteId());
        }

        // Upsert student & allocation into SQLite
        Student student = new Student(auth.getRegNo(), auth.getName(), auth.getEmail(),
                                      auth.getFeeStatus(), auth.isEnrolledInTransport());
        studentDao.upsert(student);

        StudentAllocation allocation = new StudentAllocation(null, student.getRegNo(), bus,
                                                             boardingStop, auth.getAcademicYear(), true);
        allocationDao.upsert(allocation);

        // Create Session
        String sid = generateSecureId(32);
        String csrf = generateSecureId(16);
        SessionData session = new SessionData(sid, student.getRegNo(), "STUDENT", csrf, userAgent);
        sessionStore.save(session);
        log.info("Login successful for student: {}", student.getRegNo());

        return session;
    }

    public void logout(String sessionId) {
        sessionStore.delete(sessionId);
    }
}
