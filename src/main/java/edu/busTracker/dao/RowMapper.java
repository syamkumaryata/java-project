package edu.busTracker.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Functional interface for mapping a ResultSet row to a domain object.
 * Demonstrates: generic interface (Unit 5 Generics).
 */
@FunctionalInterface
public interface RowMapper<T> {
    T mapRow(ResultSet rs) throws SQLException;
}
