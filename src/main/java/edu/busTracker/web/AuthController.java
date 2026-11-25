package edu.busTracker.web;

import edu.busTracker.auth.AuthService;
import edu.busTracker.auth.GoogleTokenVerifier;
import edu.busTracker.dao.StudentDao;
import edu.busTracker.exception.TransportException;
import edu.busTracker.model.Student;
import edu.busTracker.session.SessionData;
import edu.busTracker.session.SessionStore;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final GoogleTokenVerifier tokenVerifier;
    private final SessionStore sessionStore;
    private final StudentDao studentDao;

    @Value("${app.auth.mode:demo}")
    private String authMode;

    @Value("${app.session.cookie-name:CBTS_SID}")
    private String cookieName;

    @Autowired
    public AuthController(AuthService authService, GoogleTokenVerifier tokenVerifier,
                          SessionStore sessionStore, StudentDao studentDao) {
        this.authService = authService;
        this.tokenVerifier = tokenVerifier;
        this.sessionStore = sessionStore;
        this.studentDao = studentDao;
    }

    private void attachSessionCookie(HttpServletResponse response, String sessionId, boolean secure) {
        Cookie cookie = new Cookie(cookieName, sessionId);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setSecure(secure);
        // Note: SameSite=Lax can be added via header or response cookie
        response.addCookie(cookie);
        response.addHeader("Set-Cookie", cookieName + "=" + sessionId + "; Path=/; HttpOnly; SameSite=Lax" + (secure ? "; Secure" : ""));
    }

    private void clearSessionCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        response.addHeader("Set-Cookie", cookieName + "=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");
    }

    @PostMapping("/auth/google")
    public ResponseEntity<?> loginGoogle(@RequestBody Map<String, String> body,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        String credential = body.get("credential");
        if (credential == null || credential.isBlank()) {
            throw new TransportException("INVALID_REQUEST", "Missing Google credential", 400);
        }

        var payload = tokenVerifier.verify(credential);
        String email = payload.getEmail();

        SessionData session = authService.loginUser(email, request.getHeader("User-Agent"));
        attachSessionCookie(response, session.getSessionId(), request.isSecure());

        return ResponseEntity.ok(Map.of(
            "role", session.getRole(),
            "csrfToken", session.getCsrfToken()
        ));
    }

    @PostMapping("/auth/demo")
    public ResponseEntity<?> loginDemo(@RequestBody Map<String, String> body,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        if (!"demo".equalsIgnoreCase(authMode)) {
            throw new TransportException("DEMO_DISABLED", "Demo mode is disabled in production", 403);
        }

        String email = body.get("email");
        if (email == null || email.isBlank()) {
            throw new TransportException("INVALID_REQUEST", "Missing email parameter", 400);
        }

        SessionData session = authService.loginUser(email, request.getHeader("User-Agent"));
        attachSessionCookie(response, session.getSessionId(), request.isSecure());

        return ResponseEntity.ok(Map.of(
            "role", session.getRole(),
            "csrfToken", session.getCsrfToken()
        ));
    }

    @GetMapping("/auth/demo-accounts")
    public ResponseEntity<List<Student>> getDemoAccounts() {
        if (!"demo".equalsIgnoreCase(authMode)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(studentDao.findDemoStudents());
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String sid = extractSessionId(request);
        if (sid != null) {
            authService.logout(sid);
        }
        clearSessionCookie(response);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        String sid = extractSessionId(request);
        if (sid == null) {
            return ResponseEntity.status(401).body(Map.of("error", "UNAUTHENTICATED", "message", "No active session"));
        }

        Optional<SessionData> sessionOpt = sessionStore.get(sid);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "SESSION_EXPIRED", "message", "Session has expired"));
        }

        SessionData s = sessionOpt.get();
        Map<String, Object> res = new HashMap<>();
        res.put("role", s.getRole());
        res.put("csrfToken", s.getCsrfToken());

        if ("STUDENT".equals(s.getRole())) {
            Optional<Student> studentOpt = studentDao.findById(s.getRegNoOrEmail());
            studentOpt.ifPresent(student -> {
                res.put("student", student);
                res.put("regNo", student.getRegNo());
                res.put("name", student.getName());
                res.put("email", student.getEmail());
            });
        } else {
            res.put("email", s.getRegNoOrEmail());
        }

        return ResponseEntity.ok(res);
    }

    private String extractSessionId(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (cookieName.equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}
