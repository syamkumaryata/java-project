package edu.busTracker.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class PublicConfigController {

    @Value("${app.auth.mode:demo}")
    private String authMode;

    @Value("${app.google.client-id:}")
    private String googleClientId;

    @GetMapping("/public")
    public Map<String, Object> getPublicConfig() {
        return Map.of(
            "authMode", authMode,
            "googleClientId", googleClientId,
            "simulatedGps", true
        );
    }
}
