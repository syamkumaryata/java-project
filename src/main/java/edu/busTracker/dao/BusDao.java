package edu.busTracker.dao;

import edu.busTracker.db.ConnectionProvider;
import edu.busTracker.model.Bus;
import edu.busTracker.model.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class BusDao extends GenericJdbcDao<Bus, Long> {

    @Autowired
    public BusDao(ConnectionProvider cp) { super(cp); }

    @Override
    protected String tableName() { return "buses"; }

    private static final String SELECT_WITH_DRIVER = """
        SELECT b.bus_id, b.bus_number, b.registration_no, b.route_id, b.active,
               d.driver_id, d.name AS driver_name, d.phone AS driver_phone
        FROM buses b
        JOIN drivers d ON d.driver_id = b.driver_id
        """;

    @Override
    protected RowMapper<Bus> rowMapper() {
        return rs -> {
            Driver driver = new Driver(rs.getLong("driver_id"),
                                       rs.getString("driver_name"),
                                       rs.getString("driver_phone"));
            return new Bus(rs.getLong("bus_id"),
                           rs.getString("bus_number"),
                           rs.getString("registration_no"),
                           rs.getString("route_id"),
                           driver,
                           rs.getInt("active") == 1);
        };
    }

    @Override
    protected String findByIdSql() {
        return SELECT_WITH_DRIVER + " WHERE b.bus_id = ?";
    }

    @Override
    protected void bindId(PreparedStatement ps, Long id) throws SQLException {
        ps.setLong(1, id);
    }

    @Override
    public List<Bus> findAll() {
        return queryList(SELECT_WITH_DRIVER + " ORDER BY b.bus_number", null);
    }

    public Optional<Bus> findByBusNumber(String busNumber) {
        return queryOne(SELECT_WITH_DRIVER + " WHERE b.bus_number = ?",
                ps -> ps.setString(1, busNumber));
    }

    public List<Bus> findByRouteId(String routeId) {
        return queryList(SELECT_WITH_DRIVER + " WHERE b.route_id = ?",
                ps -> ps.setString(1, routeId));
    }

    public List<Bus> findActive() {
        return queryList(SELECT_WITH_DRIVER + " WHERE b.active = 1 ORDER BY b.bus_number", null);
    }

    @Override
    public Bus save(Bus bus) {
        String sql = """
            INSERT INTO buses(bus_number, registration_no, route_id, driver_id, active)
            VALUES (?, ?, ?, ?, ?)
            """;
        long id = executeInsert(sql, ps -> {
            ps.setString(1, bus.getBusNumber());
            ps.setString(2, bus.getRegistrationNo());
            ps.setString(3, bus.getRouteId());
            ps.setLong(4, bus.getDriver().getDriverId());
            ps.setInt(5, bus.isActive() ? 1 : 0);
        });
        bus.setBusId(id);
        return bus;
    }

    @Override
    public void update(Bus bus) {
        execute("""
            UPDATE buses SET bus_number=?, registration_no=?, route_id=?, driver_id=?, active=?
            WHERE bus_id=?
            """, ps -> {
            ps.setString(1, bus.getBusNumber());
            ps.setString(2, bus.getRegistrationNo());
            ps.setString(3, bus.getRouteId());
            ps.setLong(4, bus.getDriver().getDriverId());
            ps.setInt(5, bus.isActive() ? 1 : 0);
            ps.setLong(6, bus.getBusId());
        });
    }

    @Override
    public boolean deleteById(Long id) {
        return execute("DELETE FROM buses WHERE bus_id=?", ps -> ps.setLong(1, id)) > 0;
    }
}
