package edu.busTracker.session;

import java.io.Serializable;
import java.time.Instant;

/**
 * Server-side session object stored in Redis (or In-Memory for tests).
 * The frontend never accesses session fields directly — only via session ID cookie.
 */
public class SessionData implements Serializable {

    private String sessionId;
    private String regNoOrEmail;
    private String role;           // "STUDENT" | "ADMIN"
    private String csrfToken;
    private String userAgentHash;
    private String createdAt;
    private String lastSeen;

    public SessionData() {}

    public SessionData(String sessionId, String regNoOrEmail, String role,
                       String csrfToken, String userAgentHash) {
        this.sessionId = sessionId;
        this.regNoOrEmail = regNoOrEmail;
        this.role = role;
        this.csrfToken = csrfToken;
        this.userAgentHash = userAgentHash;
        String now = Instant.now().toString();
        this.createdAt = now;
        this.lastSeen = now;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getRegNoOrEmail() { return regNoOrEmail; }
    public void setRegNoOrEmail(String regNoOrEmail) { this.regNoOrEmail = regNoOrEmail; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getCsrfToken() { return csrfToken; }
    public void setCsrfToken(String csrfToken) { this.csrfToken = csrfToken; }

    public String getUserAgentHash() { return userAgentHash; }
    public void setUserAgentHash(String userAgentHash) { this.userAgentHash = userAgentHash; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getLastSeen() { return lastSeen; }
    public void setLastSeen(String lastSeen) { this.lastSeen = lastSeen; }
}
