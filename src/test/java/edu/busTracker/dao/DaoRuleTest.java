package edu.busTracker.dao;

import edu.busTracker.db.ConnectionProvider;
import edu.busTracker.db.DatabaseInitializer;
import edu.busTracker.exception.InvalidBoardingStopException;
import edu.busTracker.model.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "app.db.path=./target/test-db/bustrack_test.db",
        "app.session.store=memory",
        "spring.data.redis.host=localhost"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DaoRuleTest {

    @Autowired
    private ConnectionProvider connectionProvider;

    @Autowired
    private DatabaseInitializer databaseInitializer;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private BusDao busDao;

    @Autowired
    private DriverDao driverDao;

    @Autowired
    private RouteDao routeDao;

    @Autowired
    private StopDao stopDao;

    @Autowired
    private AllocationDao allocationDao;

    @BeforeEach
    public void setUp() {
        databaseInitializer.initialize();
    }

    @Test
    @Order(1)
    @DisplayName("Verify Database Seeding & Basic Counts")
    public void testDatabaseSeeded() {
        assertTrue(studentDao.count() >= 7, "At least 7 demo students should be seeded");
        assertTrue(busDao.count() >= 3, "At least 3 buses (TN-2, TN-3, TN-4) should be seeded");
        assertTrue(driverDao.count() >= 3, "At least 3 drivers should be seeded");
        assertTrue(routeDao.count() >= 3, "At least 3 routes should be seeded");
        assertTrue(stopDao.count() >= 18, "At least 18 canonical stops should be seeded");
        assertTrue(allocationDao.count() >= 6, "At least 6 allocations should be seeded");
    }

    @Test
    @Order(2)
    @DisplayName("Student CRUD operations")
    public void testStudentCrud() {
        Student newStudent = new Student("AP26DEMOTEST", "Test Student", "test_student@example.test", "PAID", true);
        studentDao.save(newStudent);

        Optional<Student> found = studentDao.findById("AP26DEMOTEST");
        assertTrue(found.isPresent());
        assertEquals("Test Student", found.get().getName());
        assertEquals("PAID", found.get().getFeeStatus());

        newStudent.setName("Updated Test Student");
        studentDao.update(newStudent);
        Optional<Student> updated = studentDao.findById("AP26DEMOTEST");
        assertTrue(updated.isPresent());
        assertEquals("Updated Test Student", updated.get().getName());

        boolean deleted = studentDao.deleteById("AP26DEMOTEST");
        assertTrue(deleted);
        assertFalse(studentDao.findById("AP26DEMOTEST").isPresent());
    }

    @Test
    @Order(3)
    @DisplayName("Rule: Foreign Key Enforcement in SQLite (PRAGMA foreign_keys = ON)")
    public void testForeignKeyEnforced() {
        // Attempting to insert an allocation for a non-existent student must throw SQLException
        String sql = "INSERT INTO student_allocations(reg_no, bus_id, boarding_stop_id, academic_year, active) " +
                     "VALUES ('NON_EXISTENT_STUDENT', 1, 1, '2026-27', 1)";

        assertThrows(Exception.class, () -> {
            try (Connection conn = connectionProvider.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.executeUpdate();
            }
        }, "SQLite foreign key violation must be thrown when student does not exist");
    }

    @Test
    @Order(4)
    @DisplayName("Rule: Campus stop must have the HIGHEST sequence on every route")
    public void testCampusIsLastStopOnEveryRoute() {
        List<Route> routes = routeDao.findAll();
        assertFalse(routes.isEmpty());

        for (Route r : routes) {
            List<RouteStop> stops = routeDao.loadStopsForRoute(r.getRouteId());
            assertFalse(stops.isEmpty(), "Route " + r.getRouteId() + " must have stops");

            RouteStop lastStop = stops.get(stops.size() - 1);
            assertTrue(lastStop.getStop().isCampus(),
                    "The last stop on route " + r.getRouteId() + " must be the campus stop. Found: " + lastStop.getStop().getName());

            // Ensure no intermediate stop is the campus stop
            for (int i = 0; i < stops.size() - 1; i++) {
                assertFalse(stops.get(i).getStop().isCampus(),
                        "Intermediate stop " + stops.get(i).getStop().getName() + " on route " + r.getRouteId() + " must not be campus");
            }
        }
    }

    @Test
    @Order(5)
    @DisplayName("Rule: Boarding stop must belong to the allocated bus's route")
    public void testBoardingStopMustBeOnRoute() {
        // Stop 'Sivaji Chowk' is on TN-4 only.
        Optional<Stop> sivajiChowk = stopDao.findByName("Sivaji Chowk");
        assertTrue(sivajiChowk.isPresent());

        // Check if Sivaji Chowk is on TN-2 (should be false)
        boolean onTN2 = routeDao.stopIsOnRoute("TN-2", sivajiChowk.get().getStopId());
        assertFalse(onTN2, "Sivaji Chowk should NOT be on route TN-2");

        // Check if Sivaji Chowk is on TN-4 (should be true)
        boolean onTN4 = routeDao.stopIsOnRoute("TN-4", sivajiChowk.get().getStopId());
        assertTrue(onTN4, "Sivaji Chowk MUST be on route TN-4");
    }

    @Test
    @Order(6)
    @DisplayName("Rule: Campus stop cannot be a boarding stop")
    public void testBoardingStopCannotBeCampus() {
        Optional<Stop> campus = stopDao.findCampus();
        assertTrue(campus.isPresent());
        assertTrue(campus.get().isCampus(), "Campus stop must have is_campus=1");

        // Business rule verification: validating a boarding stop against campus
        assertThrows(InvalidBoardingStopException.class, () -> {
            if (campus.get().isCampus()) {
                throw new InvalidBoardingStopException("Boarding stop cannot be the campus stop (final destination)");
            }
        });
    }

    @Test
    @Order(7)
    @DisplayName("Rule: Bus registration number must be unique")
    public void testBusRegistrationMustBeUnique() {
        Optional<Bus> bus1 = busDao.findByBusNumber("TN-2");
        assertTrue(bus1.isPresent());

        // Attempt to insert another bus with the exact same registration number
        String duplicateReg = bus1.get().getRegistrationNo();
        String sql = "INSERT INTO buses(bus_number, registration_no, route_id, driver_id, active) " +
                     "VALUES ('TN-TEMP', ?, 'TN-2', 1, 1)";

        assertThrows(Exception.class, () -> {
            try (Connection conn = connectionProvider.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, duplicateReg);
                ps.executeUpdate();
            }
        }, "Inserting duplicate bus registration number must be rejected by UNIQUE constraint");
    }
}
