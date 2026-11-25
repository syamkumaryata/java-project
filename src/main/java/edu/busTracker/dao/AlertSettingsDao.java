package edu.busTracker.dao;

import edu.busTracker.db.ConnectionProvider;
import edu.busTracker.model.AlertSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class AlertSettingsDao extends GenericJdbcDao<AlertSettings, String> {

    @Autowired
    public AlertSettingsDao(ConnectionProvider cp) { super(cp); }

    @Override protected String tableName() { return "alert_settings"; }

    @Override
    protected RowMapper<AlertSettings> rowMapper() {
        return rs -> new AlertSettings(
            rs.getString("reg_no"),
            rs.getInt("enabled") == 1,
            rs.getInt("minutes_before"),
            rs.getInt("sound_enabled") == 1,
            rs.getString("last_fired_date"),
            rs.getString("updated_at")
        );
    }

    @Override
    protected String findByIdSql() { return "SELECT * FROM alert_settings WHERE reg_no = ?"; }

    @Override
    protected void bindId(PreparedStatement ps, String regNo) throws SQLException {
        ps.setString(1, regNo);
    }

    public Optional<AlertSettings> findByRegNo(String regNo) {
        return findById(regNo);
    }

    public void upsert(AlertSettings a) {
        String sql;
        if (connectionProvider.isMySql()) {
            sql = """
                INSERT INTO alert_settings(reg_no, enabled, minutes_before, sound_enabled,
                                           last_fired_date, updated_at)
                VALUES (?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    enabled = VALUES(enabled),
                    minutes_before = VALUES(minutes_before),
                    sound_enabled = VALUES(sound_enabled),
                    last_fired_date = VALUES(last_fired_date),
                    updated_at = VALUES(updated_at)
                """;
        } else {
            sql = """
                INSERT INTO alert_settings(reg_no, enabled, minutes_before, sound_enabled,
                                           last_fired_date, updated_at)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT(reg_no) DO UPDATE SET
                    enabled = excluded.enabled,
                    minutes_before = excluded.minutes_before,
                    sound_enabled = excluded.sound_enabled,
                    last_fired_date = excluded.last_fired_date,
                    updated_at = excluded.updated_at
                """;
        }
        execute(sql, ps -> {
            ps.setString(1, a.getRegNo());
            ps.setInt(2, a.isEnabled() ? 1 : 0);
            ps.setInt(3, a.getMinutesBefore());
            ps.setInt(4, a.isSoundEnabled() ? 1 : 0);
            ps.setString(5, a.getLastFiredDate());
            ps.setString(6, a.getUpdatedAt());
        });
    }

    @Override
    public AlertSettings save(AlertSettings a) { upsert(a); return a; }

    @Override
    public void update(AlertSettings a) { upsert(a); }

    @Override
    public boolean deleteById(String regNo) {
        return execute("DELETE FROM alert_settings WHERE reg_no=?",
                ps -> ps.setString(1, regNo)) > 0;
    }
}
