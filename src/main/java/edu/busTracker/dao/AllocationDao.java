package edu.busTracker.dao;

import edu.busTracker.db.ConnectionProvider;
import edu.busTracker.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class AllocationDao extends GenericJdbcDao<StudentAllocation, Long> {

    @Autowired
    public AllocationDao(ConnectionProvider cp) { super(cp); }

    @Override protected String tableName() { return "student_allocations"; }

    private static final String SELECT_FULL = """
        SELECT sa.allocation_id, sa.reg_no, sa.academic_year, sa.active,
               b.bus_id, b.bus_number, b.registration_no, b.route_id,
               d.driver_id, d.name AS driver_name, d.phone AS driver_phone,
               s.stop_id, s.name AS stop_name, s.latitude, s.longitude,
               s.geocode_status, s.geocode_source, s.is_campus, s.notes AS stop_notes
        FROM student_allocations sa
        JOIN buses b ON b.bus_id = sa.bus_id
        JOIN drivers d ON d.driver_id = b.driver_id
        JOIN stops s ON s.stop_id = sa.boarding_stop_id
        """;

    @Override
    protected RowMapper<StudentAllocation> rowMapper() {
        return rs -> {
            Driver driver = new Driver(rs.getLong("driver_id"),
                                       rs.getString("driver_name"),
                                       rs.getString("driver_phone"));
            Bus bus = new Bus(rs.getLong("bus_id"), rs.getString("bus_number"),
                              rs.getString("registration_no"), rs.getString("route_id"),
                              driver, true);
            Stop stop = new Stop(rs.getLong("stop_id"), rs.getString("stop_name"),
                                 (Double) rs.getObject("latitude"), (Double) rs.getObject("longitude"),
                                 rs.getString("geocode_status"), rs.getString("geocode_source"),
                                 rs.getInt("is_campus") == 1, rs.getString("stop_notes"));
            return new StudentAllocation(rs.getLong("allocation_id"), rs.getString("reg_no"),
                                         bus, stop, rs.getString("academic_year"),
                                         rs.getInt("active") == 1);
        };
    }

    @Override
    protected String findByIdSql() { return SELECT_FULL + " WHERE sa.allocation_id = ?"; }

    @Override
    protected void bindId(PreparedStatement ps, Long id) throws SQLException {
        ps.setLong(1, id);
    }

    @Override
    public List<StudentAllocation> findAll() {
        return queryList(SELECT_FULL + " ORDER BY sa.reg_no", null);
    }

    public Optional<StudentAllocation> findActiveByRegNo(String regNo) {
        return queryOne(SELECT_FULL + " WHERE sa.reg_no = ? AND sa.active = 1",
                ps -> ps.setString(1, regNo));
    }

    public List<StudentAllocation> findByBusId(Long busId) {
        return queryList(SELECT_FULL + " WHERE b.bus_id = ?", ps -> ps.setLong(1, busId));
    }

    @Override
    public StudentAllocation save(StudentAllocation a) {
        String sql = """
            INSERT INTO student_allocations(reg_no, bus_id, boarding_stop_id, academic_year, active)
            VALUES (?, ?, ?, ?, ?)
            """;
        long id = executeInsert(sql, ps -> {
            ps.setString(1, a.getRegNo());
            ps.setLong(2, a.getBus().getBusId());
            ps.setLong(3, a.getBoardingStop().getStopId());
            ps.setString(4, a.getAcademicYear());
            ps.setInt(5, a.isActive() ? 1 : 0);
        });
        a.setAllocationId(id);
        return a;
    }

    public void upsert(StudentAllocation a) {
        String sql;
        if (connectionProvider.isMySql()) {
            sql = """
                INSERT INTO student_allocations(reg_no, bus_id, boarding_stop_id, academic_year, active)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    bus_id = VALUES(bus_id),
                    boarding_stop_id = VALUES(boarding_stop_id),
                    active = VALUES(active)
                """;
        } else {
            sql = """
                INSERT INTO student_allocations(reg_no, bus_id, boarding_stop_id, academic_year, active)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT(reg_no, academic_year) DO UPDATE SET
                    bus_id = excluded.bus_id,
                    boarding_stop_id = excluded.boarding_stop_id,
                    active = excluded.active
                """;
        }
        execute(sql, ps -> {
            ps.setString(1, a.getRegNo());
            ps.setLong(2, a.getBus().getBusId());
            ps.setLong(3, a.getBoardingStop().getStopId());
            ps.setString(4, a.getAcademicYear());
            ps.setInt(5, a.isActive() ? 1 : 0);
        });
    }

    @Override
    public void update(StudentAllocation a) {
        execute("""
            UPDATE student_allocations SET bus_id=?, boarding_stop_id=?, academic_year=?, active=?
            WHERE allocation_id=?
            """, ps -> {
            ps.setLong(1, a.getBus().getBusId());
            ps.setLong(2, a.getBoardingStop().getStopId());
            ps.setString(3, a.getAcademicYear());
            ps.setInt(4, a.isActive() ? 1 : 0);
            ps.setLong(5, a.getAllocationId());
        });
    }

    @Override
    public boolean deleteById(Long id) {
        return execute("DELETE FROM student_allocations WHERE allocation_id=?",
                ps -> ps.setLong(1, id)) > 0;
    }
}
