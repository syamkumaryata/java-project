package edu.busTracker.dao;

import edu.busTracker.db.ConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Abstract base class for JDBC DAOs.
 * Demonstrates:
 *   - Generic abstract class (Unit 5 Generics)
 *   - try-with-resources for JDBC (Unit 2 exception handling)
 *   - PreparedStatement everywhere (no string SQL injection) (Unit 4 JDBC)
 */
public abstract class GenericJdbcDao<T, ID> implements Repository<T, ID> {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final ConnectionProvider connectionProvider;

    protected GenericJdbcDao(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    protected abstract String tableName();
    protected abstract RowMapper<T> rowMapper();
    protected abstract String findByIdSql();
    protected abstract void bindId(PreparedStatement ps, ID id) throws SQLException;

    @Override
    public Optional<T> findById(ID id) {
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(findByIdSql())) {
            bindId(ps, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rowMapper().mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("findById failed on {}: {}", tableName(), e.getMessage(), e);
            throw new RuntimeException("Database error finding " + tableName(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<T> findAll() {
        String sql = "SELECT * FROM " + tableName();
        List<T> results = new ArrayList<>();
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                results.add(rowMapper().mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("findAll failed on {}: {}", tableName(), e.getMessage(), e);
            throw new RuntimeException("Database error listing " + tableName(), e);
        }
        return results;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM " + tableName();
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            log.error("count failed on {}: {}", tableName(), e.getMessage(), e);
            throw new RuntimeException("Database error counting " + tableName(), e);
        }
        return 0;
    }

    protected List<T> queryList(String sql, StatementBinder binder) {
        List<T> results = new ArrayList<>();
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (binder != null) binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(rowMapper().mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("queryList failed: {}", e.getMessage(), e);
            throw new RuntimeException("Database query error", e);
        }
        return results;
    }

    protected <R> List<R> queryListMapped(String sql, RowMapper<R> mapper, StatementBinder binder) {
        List<R> results = new ArrayList<>();
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (binder != null) binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("queryListMapped failed: {}", e.getMessage(), e);
            throw new RuntimeException("Database query error", e);
        }
        return results;
    }

    protected Optional<T> queryOne(String sql, StatementBinder binder) {
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (binder != null) binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(rowMapper().mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("queryOne failed: {}", e.getMessage(), e);
            throw new RuntimeException("Database query error", e);
        }
        return Optional.empty();
    }

    protected int execute(String sql, StatementBinder binder) {
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (binder != null) binder.bind(ps);
            return ps.executeUpdate();
        } catch (SQLException e) {
            log.error("execute failed: {}", e.getMessage(), e);
            throw new RuntimeException("Database update error", e);
        }
    }

    protected long executeInsert(String sql, StatementBinder binder) {
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (binder != null) binder.bind(ps);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
        } catch (SQLException e) {
            log.error("executeInsert failed: {}", e.getMessage(), e);
            throw new RuntimeException("Database insert error", e);
        }
        return -1;
    }

    protected void executeTransaction(TransactionBlock block) {
        try (Connection conn = connectionProvider.getConnection()) {
            conn.setAutoCommit(false);
            try {
                block.run(conn);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                log.error("Transaction rolled back: {}", e.getMessage(), e);
                throw new RuntimeException("Transaction failed", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database transaction error", e);
        }
    }

    @FunctionalInterface
    public interface StatementBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    @FunctionalInterface
    public interface TransactionBlock {
        void run(Connection conn) throws SQLException;
    }
}
