package edu.busTracker.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Runs schema.sql and seed.sql on first startup if the DB is empty.
 * Idempotent — all statements use IF NOT EXISTS / INSERT OR IGNORE.
 */
@Component
public class DatabaseInitializer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final ConnectionProvider connectionProvider;

    @Autowired
    public DatabaseInitializer(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @PostConstruct
    public void initialize() {
        log.info("Initializing database (engine: {})...", connectionProvider.getDbType());
        try {
            if (connectionProvider.isMySql()) {
                log.info("Executing MySQL 8 schema and seed scripts...");
                runSqlFile("db/schema-mysql.sql");
                runSqlFile("db/seed-mysql.sql");
            } else {
                log.info("Executing SQLite schema and seed scripts...");
                runSqlFile("db/schema.sql");
                runSqlFile("db/seed.sql");
            }
            importGeocodedStops();
            log.info("Database initialization complete (engine: {}).", connectionProvider.getDbType());
        } catch (Exception e) {
            // Fatal — the app cannot run without a DB
            throw new RuntimeException("Database initialization failed: " + e.getMessage(), e);
        }
    }

    private void importGeocodedStops() {
        java.io.File csvFile = new java.io.File("data/generated/stops_geocoded.csv");
        if (!csvFile.exists()) {
            csvFile = new java.io.File("bus-tracking/data/generated/stops_geocoded.csv");
        }
        if (!csvFile.exists()) {
            log.debug("No stops_geocoded.csv found to import");
            return;
        }

        log.info("Importing geocoded stops from {}", csvFile.getPath());
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new java.io.FileInputStream(csvFile), StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // skip header
            String updateSql = "UPDATE stops SET latitude=?, longitude=?, geocode_status='AUTO', geocode_source=? WHERE name=? AND (geocode_status='PENDING' OR geocode_status='AUTO')";

            try (Connection conn = connectionProvider.getConnection();
                 PreparedStatement ps = conn.prepareStatement(updateSql)) {
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    String[] parts = line.split(",", -1);
                    if (parts.length >= 5 && "AUTO".equalsIgnoreCase(parts[4].trim())) {
                        String stopName = parts[0].replace("\"", "").trim();
                        try {
                            double lat = Double.parseDouble(parts[1].trim());
                            double lon = Double.parseDouble(parts[2].trim());
                            String src = parts[3].replace("\"", "").trim();

                            ps.setDouble(1, lat);
                            ps.setDouble(2, lon);
                            ps.setString(3, src);
                            ps.setString(4, stopName);
                            ps.executeUpdate();
                        } catch (NumberFormatException nfe) {
                            // skip invalid coordinate lines
                        }
                    }
                }
            }
            log.info("Geocoded stops imported successfully.");
        } catch (Exception e) {
            log.warn("Error importing geocoded stops: {}", e.getMessage());
        }
    }

    private void runSqlFile(String resourcePath) throws IOException, SQLException {
        log.debug("Running SQL file: {}", resourcePath);
        String sql = null;
        ClassPathResource resource = new ClassPathResource(resourcePath);
        if (resource.exists()) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                sql = reader.lines().collect(Collectors.joining("\n"));
            }
        } else {
            // Check filesystem relative to working directory or project root
            java.io.File file = new java.io.File(resourcePath);
            if (!file.exists()) {
                file = new java.io.File("bus-tracking/" + resourcePath);
            }
            if (!file.exists() && resourcePath.startsWith("db/")) {
                file = new java.io.File("data/" + resourcePath.substring(3));
            }
            if (!file.exists() && resourcePath.startsWith("db/")) {
                file = new java.io.File("bus-tracking/data/" + resourcePath.substring(3));
            }
            if (file.exists()) {
                sql = java.nio.file.Files.readString(file.toPath(), StandardCharsets.UTF_8);
            }
        }

        if (sql == null || sql.isBlank()) {
            log.warn("SQL file not found on classpath or filesystem: {} — skipping", resourcePath);
            return;
        }

        StringBuilder cleanSql = new StringBuilder();
        for (String line : sql.lines().toList()) {
            String trimmedLine = line.strip();
            if (!trimmedLine.startsWith("--")) {
                cleanSql.append(line).append("\n");
            }
        }

        try (Connection conn = connectionProvider.getConnection();
             Statement st = conn.createStatement()) {
            String[] statements = cleanSql.toString().split(";");
            for (String stmt : statements) {
                String trimmed = stmt.strip();
                if (!trimmed.isEmpty()) {
                    try {
                        st.execute(trimmed);
                    } catch (SQLException ex) {
                        log.debug("Database initialization notice on statement: {} -> {}", trimmed, ex.getMessage());
                    }
                }
            }
        }
        log.debug("SQL file executed successfully: {}", resourcePath);
    }
}
