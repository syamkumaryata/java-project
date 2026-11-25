package edu.busTracker.auth;

import edu.busTracker.dao.StudentDao;
import edu.busTracker.db.DatabaseInitializer;
import edu.busTracker.exception.InvalidBoardingStopException;
import edu.busTracker.exception.StudentNotEligibleException;
import edu.busTracker.session.SessionData;
import edu.busTracker.session.SessionStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "app.db.path=./target/test-db/bustrack_auth_test.db",
        "app.session.store=memory",
        "app.auth.mode=demo"
})
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private SessionStore sessionStore;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private DatabaseInitializer databaseInitializer;

    @BeforeEach
    public void setup() {
        databaseInitializer.initialize();
    }

    @Test
    @DisplayName("AP26DEMO0001 (Demo Student A) - Login OK with Duggirala on TN-2")
    public void testDemoStudentA_Allowed() {
        SessionData session = authService.loginUser("demo1@example.test", "TestBrowser/1.0");
        assertNotNull(session);
        assertEquals("AP26DEMO0001", session.getRegNoOrEmail());
        assertEquals("STUDENT", session.getRole());
        assertNotNull(session.getCsrfToken());

        // Verify session is active in store
        Optional<SessionData> inStore = sessionStore.get(session.getSessionId());
        assertTrue(inStore.isPresent());
    }

    @Test
    @DisplayName("AP26DEMO0002 (Demo Student B) - Login OK with Ravindrapadu on TN-2")
    public void testDemoStudentB_Allowed() {
        SessionData session = authService.loginUser("demo2@example.test", "TestBrowser/1.0");
        assertNotNull(session);
        assertEquals("AP26DEMO0002", session.getRegNoOrEmail());
        assertEquals("STUDENT", session.getRole());
    }

    @Test
    @DisplayName("AP26DEMO0003 (Demo Student C) - Login OK with Kotha Vanthana on TN-3")
    public void testDemoStudentC_Allowed() {
        SessionData session = authService.loginUser("demo3@example.test", "TestBrowser/1.0");
        assertNotNull(session);
        assertEquals("AP26DEMO0003", session.getRegNoOrEmail());
    }

    @Test
    @DisplayName("AP26DEMO0004 (Demo Student D) - Login OK with Duggirala on TN-4")
    public void testDemoStudentD_Allowed() {
        SessionData session = authService.loginUser("demo4@example.test", "TestBrowser/1.0");
        assertNotNull(session);
        assertEquals("AP26DEMO0004", session.getRegNoOrEmail());
    }

    @Test
    @DisplayName("AP26DEMO0005 (Demo Student E) - Login OK with Mangalagiri on TN-4")
    public void testDemoStudentE_Allowed() {
        SessionData session = authService.loginUser("demo5@example.test", "TestBrowser/1.0");
        assertNotNull(session);
        assertEquals("AP26DEMO0005", session.getRegNoOrEmail());
    }

    @Test
    @DisplayName("AP26DEMO0006 (Demo Student F) - Denied with FEE_NOT_PAID (feeStatus=PENDING)")
    public void testDemoStudentF_FeePending() {
        StudentNotEligibleException ex = assertThrows(StudentNotEligibleException.class, () -> {
            authService.loginUser("demo6@example.test", "TestBrowser/1.0");
        });
        assertEquals("FEE_NOT_PAID", ex.getReasonCode());
    }

    @Test
    @DisplayName("AP26DEMO0007 (Demo Student G) - Denied with TRANSPORT_NOT_ENROLLED (transportActive=0)")
    public void testDemoStudentG_TransportInactive() {
        StudentNotEligibleException ex = assertThrows(StudentNotEligibleException.class, () -> {
            authService.loginUser("demo7@example.test", "TestBrowser/1.0");
        });
        assertEquals("TRANSPORT_NOT_ENROLLED", ex.getReasonCode());
    }

    @Test
    @DisplayName("Unknown Student - Denied with NOT_FOUND")
    public void testUnknownStudent_NotFound() {
        StudentNotEligibleException ex = assertThrows(StudentNotEligibleException.class, () -> {
            authService.loginUser("unknown@example.test", "TestBrowser/1.0");
        });
        assertEquals("NOT_FOUND", ex.getReasonCode());
    }

    @Test
    @DisplayName("Admin Login - Granted role ADMIN without student record")
    public void testAdminLogin() {
        SessionData session = authService.loginUser("admin@example.test", "TestBrowser/1.0");
        assertNotNull(session);
        assertEquals("ADMIN", session.getRole());
        assertEquals("admin@example.test", session.getRegNoOrEmail());
    }

    @Test
    @DisplayName("Logout & Session Revocation")
    public void testLogoutAndRevoke() {
        SessionData session = authService.loginUser("demo1@example.test", "TestBrowser/1.0");
        assertTrue(sessionStore.get(session.getSessionId()).isPresent());

        // Logout
        authService.logout(session.getSessionId());
        assertFalse(sessionStore.get(session.getSessionId()).isPresent());

        // Bulk revoke
        SessionData s1 = authService.loginUser("demo2@example.test", "Browser1");
        SessionData s2 = authService.loginUser("demo2@example.test", "Browser2");
        sessionStore.revokeAllForUser("AP26DEMO0002");

        assertFalse(sessionStore.get(s1.getSessionId()).isPresent());
        assertFalse(sessionStore.get(s2.getSessionId()).isPresent());
    }
}
