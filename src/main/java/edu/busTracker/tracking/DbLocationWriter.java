package edu.busTracker.tracking;

import edu.busTracker.db.ConnectionProvider;
import edu.busTracker.model.BusLocation;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Writes latest bus location to the database, throttled to every 10 seconds.
 * Decision D-12: Reduces SQLite write contention while maintaining persistence.
 */
@Component
public class DbLocationWriter implements LocationEventListener {

    private static final Logger log = LoggerFactory.getLogger(DbLocationWriter.class);

    private final ConnectionProvider connectionProvider;
    private final EventPublisher eventPublisher;
    private final Map<Long, Long> lastWrittenMs = new ConcurrentHashMap<>();

    @Autowired
    public DbLocationWriter(ConnectionProvider connectionProvider, EventPublisher eventPublisher) {
        this.connectionProvider = connectionProvider;
        this.eventPublisher = eventPublisher;
    }

    @PostConstruct
    public void init() {
        eventPublisher.register(this);
    }

    @Override
    public void onLocationUpdate(LocationEvent event) {
        BusLocation loc = event.getLocation();
        if (loc == null || loc.getBusId() == null) return;

        long now = System.currentTimeMillis();
        Long last = lastWrittenMs.get(loc.getBusId());
        // Throttled to 10 seconds per bus
        if (last != null && (now - last) < 10000) {
            return;
        }

        lastWrittenMs.put(loc.getBusId(), now);
        writeToDb(loc);
    }

    private void writeToDb(BusLocation loc) {
        String sql = """
            INSERT INTO bus_locations(bus_id, latitude, longitude, speed_kmph, heading_deg, recorded_at, source)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(bus_id) DO UPDATE SET
                latitude = excluded.latitude,
                longitude = excluded.longitude,
                speed_kmph = excluded.speed_kmph,
                heading_deg = excluded.heading_deg,
                recorded_at = excluded.recorded_at,
                source = excluded.source
            """;

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, loc.getBusId());
            ps.setDouble(2, loc.getLatitude());
            ps.setDouble(3, loc.getLongitude());
            ps.setObject(4, loc.getSpeedKmph());
            ps.setObject(5, loc.getHeadingDeg());
            ps.setString(6, loc.getRecordedAt() != null ? loc.getRecordedAt() : Instant.now().toString());
            ps.setString(7, loc.getSource());
            ps.executeUpdate();
        } catch (Exception e) {
            log.warn("Failed to persist bus location: {}", e.getMessage());
        }
    }
}
