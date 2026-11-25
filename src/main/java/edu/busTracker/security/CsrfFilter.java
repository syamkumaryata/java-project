package edu.busTracker.security;

import edu.busTracker.session.SessionData;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
@Order(2)
public class CsrfFilter implements Filter {

    private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "DELETE", "PATCH");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getRequestURI();
        String method = req.getMethod();

        // Only enforce on mutating state-changing API requests
        if (!path.startsWith("/api/") || !MUTATING_METHODS.contains(method.toUpperCase())) {
            chain.doFilter(request, response);
            return;
        }

        // Exclude public auth endpoints from CSRF check
        if (path.startsWith("/api/auth/") || path.startsWith("/api/config/")) {
            chain.doFilter(request, response);
            return;
        }

        SessionData session = (SessionData) req.getAttribute("session");
        if (session != null) {
            String csrfHeader = req.getHeader("X-CSRF-Token");
            if (csrfHeader == null || !csrfHeader.equals(session.getCsrfToken())) {
                res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                res.setContentType("application/json");
                res.setCharacterEncoding("UTF-8");
                res.getWriter().write(String.format("{\"error\":\"INVALID_CSRF\",\"message\":\"Missing or invalid X-CSRF-Token header\",\"timestamp\":\"%s\"}",
                        java.time.Instant.now().toString()));
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
