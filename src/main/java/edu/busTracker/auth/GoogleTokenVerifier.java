package edu.busTracker.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import edu.busTracker.exception.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Cryptographically verifies Google Identity Services ID tokens on backend.
 */
@Component
public class GoogleTokenVerifier {

    private static final Logger log = LoggerFactory.getLogger(GoogleTokenVerifier.class);

    private final String clientId;
    private final List<String> allowedDomains;

    public GoogleTokenVerifier(
            @Value("${app.google.client-id:}") String clientId,
            @Value("${app.auth.allowed-google-domains:}") String allowedDomainsStr) {
        this.clientId = clientId;
        if (allowedDomainsStr != null && !allowedDomainsStr.isBlank()) {
            this.allowedDomains = Arrays.stream(allowedDomainsStr.split(","))
                    .map(String::strip).filter(s -> !s.isEmpty()).toList();
        } else {
            this.allowedDomains = Collections.emptyList();
        }
    }

    public GoogleIdToken.Payload verify(String idTokenString) {
        if (clientId == null || clientId.isBlank()) {
            throw new TransportException("OAUTH_NOT_CONFIGURED", "Google Client ID is not configured on server", 500);
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(clientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                log.warn("Invalid Google ID token supplied");
                throw new TransportException("INVALID_TOKEN", "Google ID token signature verification failed", 401);
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
                throw new TransportException("EMAIL_NOT_VERIFIED", "Google email is not verified", 401);
            }

            if (!allowedDomains.isEmpty()) {
                String hd = payload.getHostedDomain();
                if (hd == null || !allowedDomains.contains(hd)) {
                    throw new TransportException("DOMAIN_NOT_ALLOWED", "Google account domain not authorized", 403);
                }
            }

            return payload;
        } catch (TransportException te) {
            throw te;
        } catch (Exception e) {
            log.error("Error during Google token verification: {}", e.getMessage());
            throw new TransportException("AUTH_FAILED", "Failed to verify Google token: " + e.getMessage(), 401);
        }
    }
}
