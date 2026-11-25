package edu.busTracker.dao;

import edu.busTracker.db.ConnectionProvider;
import edu.busTracker.model.Route;
import edu.busTracker.model.RouteStop;
import edu.busTracker.model.Stop;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Repository
public class RouteDao extends GenericJdbcDao<Route, String> {

    @Autowired
    public RouteDao(ConnectionProvider cp) { super(cp); }

    @Override
    protected String tableName() { return "routes"; }

    @Override
    protected RowMapper<Route> rowMapper() {
        return rs -> {
            Route r = new Route();
            r.setRouteId(rs.getString("route_id"));
            r.setName(rs.getString("name"));
            r.setRegion(rs.getString("region"));
            r.setGeometryJson(rs.getString("geometry_json"));
            r.setGeometrySource(rs.getString("geometry_source"));
            return r;
        };
    }

    @Override
    protected String findByIdSql() { return "SELECT * FROM routes WHERE route_id = ?"; }

    @Override
    protected void bindId(PreparedStatement ps, String id) throws SQLException {
        ps.setString(1, id);
    }

    public Optional<Route> findByIdWithStops(String routeId) {
        Optional<Route> routeOpt = findById(routeId);
        if (routeOpt.isEmpty()) return Optional.empty();
        Route route = routeOpt.get();
        route.setStops(loadStopsForRoute(routeId));
        return Optional.of(route);
    }

    public List<RouteStop> loadStopsForRoute(String routeId) {
        String sql = """
            SELECT rs.route_id, rs.sequence, rs.scheduled_time,
                   s.stop_id, s.name, s.latitude, s.longitude,
                   s.geocode_status, s.geocode_source, s.is_campus, s.notes
            FROM route_stops rs
            JOIN stops s ON s.stop_id = rs.stop_id
            WHERE rs.route_id = ?
            ORDER BY rs.sequence
            """;
        TreeMap<Integer, RouteStop> bySeq = new TreeMap<>();
        List<RouteStop> raw = queryListMapped(sql, rs -> {
            Stop stop = new Stop(
                rs.getLong("stop_id"), rs.getString("name"),
                (Double) rs.getObject("latitude"), (Double) rs.getObject("longitude"),
                rs.getString("geocode_status"), rs.getString("geocode_source"),
                rs.getInt("is_campus") == 1, rs.getString("notes")
            );
            return new RouteStop(routeId, stop, rs.getInt("sequence"), rs.getString("scheduled_time"));
        }, ps -> ps.setString(1, routeId));
        for (RouteStop rs : raw) bySeq.put(rs.getSequence(), rs);
        return new ArrayList<>(bySeq.values());
    }

    public boolean stopIsOnRoute(String routeId, Long stopId) {
        String sql = "SELECT COUNT(*) FROM route_stops WHERE route_id=? AND stop_id=?";
        try (var conn = connectionProvider.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, routeId);
            ps.setLong(2, stopId);
            try (var rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error checking stop on route", e);
        }
    }

    public void updateGeometry(String routeId, String geometryJson, String geometrySource) {
        execute("UPDATE routes SET geometry_json=?, geometry_source=? WHERE route_id=?", ps -> {
            ps.setString(1, geometryJson);
            ps.setString(2, geometrySource);
            ps.setString(3, routeId);
        });
    }

    @Override
    public Route save(Route route) {
        execute("""
            INSERT INTO routes(route_id, name, region, geometry_json, geometry_source)
            VALUES (?, ?, ?, ?, ?)
            """, ps -> {
            ps.setString(1, route.getRouteId());
            ps.setString(2, route.getName());
            ps.setString(3, route.getRegion());
            ps.setString(4, route.getGeometryJson());
            ps.setString(5, route.getGeometrySource());
        });
        return route;
    }

    @Override
    public void update(Route route) {
        execute("""
            UPDATE routes SET name=?, region=?, geometry_json=?, geometry_source=?
            WHERE route_id=?
            """, ps -> {
            ps.setString(1, route.getName());
            ps.setString(2, route.getRegion());
            ps.setString(3, route.getGeometryJson());
            ps.setString(4, route.getGeometrySource());
            ps.setString(5, route.getRouteId());
        });
    }

    @Override
    public boolean deleteById(String id) {
        return execute("DELETE FROM routes WHERE route_id=?", ps -> ps.setString(1, id)) > 0;
    }
}
