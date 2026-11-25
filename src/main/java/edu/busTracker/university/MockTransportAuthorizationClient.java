package edu.busTracker.university;

import edu.busTracker.dao.*;
import edu.busTracker.exception.StudentNotEligibleException;
import edu.busTracker.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Local mock client that looks up synthetic demo students directly from the database.
 * Every mock class and log line clearly states MOCK.
 */
@Component
public class MockTransportAuthorizationClient implements TransportAuthorizationClient {

    private static final Logger log = LoggerFactory.getLogger(MockTransportAuthorizationClient.class);

    private final StudentDao studentDao;
    private final AllocationDao allocationDao;

    @Autowired
    public MockTransportAuthorizationClient(StudentDao studentDao, AllocationDao allocationDao) {
        this.studentDao = studentDao;
        this.allocationDao = allocationDao;
        log.info("[MOCK] MOCK UNIVERSITY API Client initialized");
    }

    @Override
    public TransportAuthorization lookup(String email) {
        log.info("[MOCK] University transport authorization lookup for email: {}", email);

        Optional<Student> studentOpt = studentDao.findByEmail(email);
        if (studentOpt.isEmpty()) {
            throw new StudentNotEligibleException("NOT_FOUND", "No student record found for email: " + email);
        }

        Student student = studentOpt.get();
        TransportAuthorization auth = new TransportAuthorization();
        auth.setMock(true);
        auth.setRegNo(student.getRegNo());
        auth.setName(student.getName());
        auth.setEmail(student.getEmail());
        auth.setFeeStatus(student.getFeeStatus());
        auth.setEnrolledInTransport(student.isTransportActive());
        auth.setAcademicYear("2026-27");
        auth.setDestination("SRM University-AP (Campus)");

        // Fetch allocation
        Optional<StudentAllocation> allocOpt = allocationDao.findActiveByRegNo(student.getRegNo());
        if (allocOpt.isPresent()) {
            StudentAllocation alloc = allocOpt.get();
            auth.setBusNumber(alloc.getBus().getBusNumber());
            auth.setBoardingStop(alloc.getBoardingStop().getName());
        }

        return auth;
    }
}
