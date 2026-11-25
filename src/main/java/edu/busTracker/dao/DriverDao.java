package edu.busTracker.dao;

import edu.busTracker.db.ConnectionProvider;
import edu.busTracker.model.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@Repository
public class DriverDao extends GenericJdbcDao<Driver, Long> {

    @Autowired
    public DriverDao(ConnectionProvider cp) { super(cp); }

    @Override
    protected String tableName() { return "drivers"; }

    @Override
    protected RowMapper<Driver> rowMapper() {
        return rs -> new Driver(rs.getLong("driver_id"), rs.getString("name"), rs.getString("phone"));
    }

    @Override
    protected String findByIdSql() { return "SELECT * FROM drivers WHERE driver_id = ?"; }

    @Override
    protected void bindId(PreparedStatement ps, Long id) throws SQLException {
        ps.setLong(1, id);
    }

    @Override
    public Driver save(Driver driver) {
        String sql = "INSERT INTO drivers(name, phone) VALUES (?, ?)";
        long id = executeInsert(sql, ps -> {
            ps.setString(1, driver.getName());
            ps.setString(2, driver.getPhone());
        });
        driver.setDriverId(id);
        return driver;
    }

    @Override
    public void update(Driver driver) {
        execute("UPDATE drivers SET name=?, phone=? WHERE driver_id=?", ps -> {
            ps.setString(1, driver.getName());
            ps.setString(2, driver.getPhone());
            ps.setLong(3, driver.getDriverId());
        });
    }

    @Override
    public boolean deleteById(Long id) {
        return execute("DELETE FROM drivers WHERE driver_id=?", ps -> ps.setLong(1, id)) > 0;
    }
}
