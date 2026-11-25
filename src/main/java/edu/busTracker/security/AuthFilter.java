package edu.busTracker.security;

import edu.busTracker.session.SessionData;
import edu.busTracker.session.SessionStore;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@Order(1)
public class AuthFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);

    private final SessionStore sessionStore;

    @Value("${app.session.cookie-name:CBTS_SID}")
    private String cookieName;

    @Autowired
    public AuthFilter(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getRequestURI();

        // Allow public and non-API endpoints
        if (!path.startsWith("/api/") ||
            path.startsWith("/api/config/public") ||
            path.startsWith("/api/auth/")) {
            chain.doFilter(request, response);
            return;
        }

        // Extract session cookie
        String sid = null;
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if (cookieName.equals(c.getName())) {
                    sid = c.getValue();
                    break;
                }
            }
        }

        if (sid == null || sid.isBlank()) {
            sendJsonError(res, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHENTICATED", "Authentication required");
            return;
        }

        Optional<SessionData> sessionOpt = sessionStore.get(sid);
        if (sessionOpt.isEmpty()) {
            sendJsonError(res, HttpServletResponse.SC_UNAUTHORIZED, "SESSION_EXPIRED", "Session expired or invalid");
            return;
        }

        SessionData session = sessionOpt.get();
        req.setAttribute("session", session);

        // Role-based check for admin endpoints
        if (path.startsWith("/api/admin/") && !"ADMIN".equalsIgnoreCase(session.getRole())) {
            sendJsonError(res, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Admin access required");
            return;
        }

        chain.doFilter(request, response);
    }

    private void sendJsonError(HttpServletResponse res, int status, String code, String message) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(String.format("{\"error\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
                code, message, java.time.Instant.now().toString()));
    }
}
