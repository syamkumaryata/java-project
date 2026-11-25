package edu.busTracker.db;

import edu.busTracker.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Provides database connections supporting both MySQL 8 and SQLite.
 *
 * Configured via:
 *   app.db.type: mysql | sqlite (default: mysql)
 *
 * When app.db.type=mysql:
 *   Uses spring.datasource.url, spring.datasource.username, spring.datasource.password.
 *   If MySQL is offline/unreachable on startup, it cleanly logs instructions and falls back
 *   to local SQLite so the application remains functional and presentable.
 *
 * When app.db.type=sqlite:
 *   Uses app.db.path with WAL mode and PRAGMA foreign_keys = ON.
 */
@Component
public class ConnectionProvider {

    private static final Logger log = LoggerFactory.getLogger(ConnectionProvider.class);

    private String dbType;
    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final String sqlitePath;

    public ConnectionProvider(
            @Value("${app.db.type:mysql}") String configuredType,
            @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/bustrack?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8}") String mysqlUrl,
            @Value("${spring.datasource.username:root}") String mysqlUser,
            @Value("${spring.datasource.password:root}") String mysqlPass,
            @Value("${app.db.path:${user.home}/bus-tracker-data/bustrack.db}") String dbPath) {

        this.sqlitePath = dbPath;
        String requestedType = (configuredType != null) ? configuredType.trim().toLowerCase() : "mysql";

        if ("mysql".equals(requestedType)) {
            // Check if MySQL driver is present
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                log.warn("MySQL JDBC driver not found on classpath; falling back to SQLite");
                requestedType = "sqlite";
            }
        }

        if ("mysql".equals(requestedType)) {
            // Test connection to verify MySQL server availability
            boolean mysqlReachable = false;
            try (Connection testConn = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass)) {
                mysqlReachable = true;
                log.info("Successfully connected to MySQL database: {}", mysqlUrl);
            } catch (SQLException e) {
                log.warn("Could not connect to MySQL at {} ({}).", mysqlUrl, e.getMessage());
                log.warn("Falling back to local SQLite so the application runs without disruption. To use MySQL, ensure MySQL Server 8.0 service is started.");
            }

            if (mysqlReachable) {
                this.dbType = "mysql";
                this.jdbcUrl = mysqlUrl;
                this.username = mysqlUser;
                this.password = mysqlPass;
                return;
            } else {
                requestedType = "sqlite";
            }
        }

        // Setup SQLite mode
        this.dbType = "sqlite";
        String resolvedPath = dbPath;
        if (resolvedPath.startsWith("~")) {
            resolvedPath = System.getProperty("user.home") + resolvedPath.substring(1);
        }
        Path path = Paths.get(resolvedPath);
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
        } catch (IOException e) {
            throw new ExternalServiceException("SQLite",
                    "Cannot create database directory: " + path.getParent(), e);
        }
        this.jdbcUrl = "jdbc:sqlite:" + path.toAbsolutePath();
        this.username = null;
        this.password = null;
        log.info("Initialized SQLite database path: {}", path.toAbsolutePath());
    }

    /**
     * Returns true if MySQL is the active database engine.
     */
    public boolean isMySql() {
        return "mysql".equalsIgnoreCase(this.dbType);
    }

    /**
     * Returns the active database engine name ('mysql' or 'sqlite').
     */
    public String getDbType() {
        return this.dbType;
    }

    /**
     * Returns a new connection.
     * Close it in a try-with-resources block.
     */
    public Connection getConnection() throws SQLException {
        if (isMySql()) {
            return DriverManager.getConnection(jdbcUrl, username, password);
        } else {
            Connection conn = DriverManager.getConnection(jdbcUrl);
            try (Statement st = conn.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
                st.execute("PRAGMA journal_mode = WAL");
                st.execute("PRAGMA busy_timeout = 5000");
            }
            return conn;
        }
    }

    /** Returns the JDBC URL (for diagnostics/admin display). */
    public String getJdbcUrl() {
        return jdbcUrl;
    }
}
