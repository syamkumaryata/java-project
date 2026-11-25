package edu.busTracker.dao;

import edu.busTracker.db.ConnectionProvider;
import edu.busTracker.model.Stop;
import edu.busTracker.model.StopAlias;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
public class StopDao extends GenericJdbcDao<Stop, Long> {

    @Autowired
    public StopDao(ConnectionProvider cp) { super(cp); }

    @Override
    protected String tableName() { return "stops"; }

    @Override
    protected RowMapper<Stop> rowMapper() {
        return rs -> {
            Double lat = (Double) rs.getObject("latitude");
            Double lon = (Double) rs.getObject("longitude");
            return new Stop(
                rs.getLong("stop_id"),
                rs.getString("name"),
                lat,
                lon,
                rs.getString("geocode_status"),
                rs.getString("geocode_source"),
                rs.getInt("is_campus") == 1,
                rs.getString("notes")
            );
        };
    }

    @Override
    protected String findByIdSql() { return "SELECT * FROM stops WHERE stop_id = ?"; }

    @Override
    protected void bindId(PreparedStatement ps, Long id) throws SQLException {
        ps.setLong(1, id);
    }

    public Optional<Stop> findByName(String name) {
        return queryOne("SELECT * FROM stops WHERE name = ?", ps -> ps.setString(1, name));
    }

    public Optional<Stop> findCampus() {
        return queryOne("SELECT * FROM stops WHERE is_campus = 1 LIMIT 1", null);
    }

    public List<Stop> findPendingOrAutoGeocoded() {
        return queryList("SELECT * FROM stops WHERE geocode_status IN ('PENDING','AUTO')", null);
    }

    public Optional<Stop> resolveAlias(String rawBoardingPoint) {
        String normalized = normalize(rawBoardingPoint);
        String sql = """
            SELECT s.* FROM stops s
            JOIN stop_aliases a ON s.stop_id = a.stop_id
            WHERE a.alias_normalized = ?
            LIMIT 1
            """;
        return queryOne(sql, ps -> ps.setString(1, normalized));
    }

    public List<StopAlias> findProbableAliases() {
        String sql = """
            SELECT a.alias_normalized, a.stop_id, s.name AS stop_name, a.confidence
            FROM stop_aliases a JOIN stops s ON s.stop_id = a.stop_id
            WHERE a.confidence = 'PROBABLE'
            """;
        return queryListMapped(sql, rs -> new StopAlias(
            rs.getString("alias_normalized"),
            rs.getLong("stop_id"),
            rs.getString("stop_name"),
            rs.getString("confidence")
        ), null);
    }

    @Override
    public Stop save(Stop stop) {
        String sql = """
            INSERT INTO stops(name, latitude, longitude, geocode_status, geocode_source, is_campus, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        long id = executeInsert(sql, ps -> bindStop(ps, stop));
        stop.setStopId(id);
        return stop;
    }

    @Override
    public void update(Stop stop) {
        String sql = """
            UPDATE stops SET name=?, latitude=?, longitude=?, geocode_status=?,
                geocode_source=?, is_campus=?, notes=?
            WHERE stop_id=?
            """;
        execute(sql, ps -> {
            bindStop(ps, stop);
            ps.setLong(8, stop.getStopId());
        });
    }

    @Override
    public boolean deleteById(Long id) {
        return execute("DELETE FROM stops WHERE stop_id=?", ps -> ps.setLong(1, id)) > 0;
    }

    public void updateCoordinates(Long stopId, double lat, double lon,
                                   String geocodeStatus, String geocodeSource) {
        execute("""
            UPDATE stops SET latitude=?, longitude=?, geocode_status=?, geocode_source=?
            WHERE stop_id=?
            """, ps -> {
            ps.setDouble(1, lat);
            ps.setDouble(2, lon);
            ps.setString(3, geocodeStatus);
            ps.setString(4, geocodeSource);
            ps.setLong(5, stopId);
        });
    }

    private void bindStop(PreparedStatement ps, Stop stop) throws SQLException {
        ps.setString(1, stop.getName());
        if (stop.getLatitude() != null) ps.setDouble(2, stop.getLatitude());
        else ps.setNull(2, Types.REAL);
        if (stop.getLongitude() != null) ps.setDouble(3, stop.getLongitude());
        else ps.setNull(3, Types.REAL);
        ps.setString(4, stop.getGeocodeStatus());
        ps.setString(5, stop.getGeocodeSource());
        ps.setInt(6, stop.isCampus() ? 1 : 0);
        ps.setString(7, stop.getNotes());
    }

    public static String normalize(String input) {
        if (input == null) return "";
        return input.toLowerCase()
                    .replaceAll("[^a-z0-9\\s]", "")
                    .replaceAll("\\s+", " ")
                    .strip();
    }
}
