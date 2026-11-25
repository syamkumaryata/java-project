package edu.busTracker.university;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Mock university transport endpoint clearly marked MOCK.
 * Served under /mock-university-api/** with X-API-Key authentication.
 */
@RestController
@RequestMapping("/mock-university-api/v1")
public class MockUniversityController {

    private static final Logger log = LoggerFactory.getLogger(MockUniversityController.class);

    private final MockTransportAuthorizationClient mockClient;

    @Value("${app.university-api.api-key}")
    private String expectedApiKey;

    @Autowired
    public MockUniversityController(MockTransportAuthorizationClient mockClient) {
        this.mockClient = mockClient;
    }

    @GetMapping("/students/by-email")
    public ResponseEntity<?> getStudentByEmail(
            @RequestParam("email") String email,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey) {

        log.info("[MOCK UNIVERSITY API] Received student lookup request for: {}", email);

        if (apiKey == null || !apiKey.equals(expectedApiKey)) {
            log.warn("[MOCK UNIVERSITY API] Unauthorized access attempt with invalid API key");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mock", true, "error", "UNAUTHORIZED", "message", "Invalid X-API-Key"));
        }

        try {
            TransportAuthorization auth = mockClient.lookup(email);
            return ResponseEntity.ok(auth);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mock", true, "error", "NOT_FOUND", "message", e.getMessage()));
        }
    }
}
