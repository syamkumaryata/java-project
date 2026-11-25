package edu.busTracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BusTrackerApplication {

    private static final Logger log = LoggerFactory.getLogger(BusTrackerApplication.class);

    public static void main(String[] args) {
        log.info("Starting College Bus Tracking System (SRM University-AP)...");
        SpringApplication.run(BusTrackerApplication.class, args);
        log.info("College Bus Tracking System is running at http://localhost:8080");
    }
}
