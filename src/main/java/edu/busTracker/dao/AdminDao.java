package edu.busTracker.dao;

import edu.busTracker.db.ConnectionProvider;
import edu.busTracker.model.AdminUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@Repository
public class AdminDao extends GenericJdbcDao<AdminUser, String> {

    @Autowired
    public AdminDao(ConnectionProvider cp) { super(cp); }

    @Override protected String tableName() { return "admins"; }

    @Override
    protected RowMapper<AdminUser> rowMapper() {
        return rs -> new AdminUser(rs.getString("email"));
    }

    @Override
    protected String findByIdSql() { return "SELECT * FROM admins WHERE email = ?"; }

    @Override
    protected void bindId(PreparedStatement ps, String email) throws SQLException {
        ps.setString(1, email);
    }

    public boolean isAdmin(String email) {
        return findById(email).isPresent();
    }

    @Override
    public AdminUser save(AdminUser admin) {
        String sql = connectionProvider.isMySql()
                ? "INSERT IGNORE INTO admins(email) VALUES (?)"
                : "INSERT OR IGNORE INTO admins(email) VALUES (?)";
        execute(sql, ps -> ps.setString(1, admin.getEmail()));
        return admin;
    }

    @Override
    public void update(AdminUser admin) {}

    @Override
    public boolean deleteById(String email) {
        return execute("DELETE FROM admins WHERE email=?",
                ps -> ps.setString(1, email)) > 0;
    }
}
