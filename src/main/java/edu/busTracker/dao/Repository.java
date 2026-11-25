package edu.busTracker.dao;

import java.util.List;
import java.util.Optional;

/**
 * Generic CRUD repository interface.
 * Demonstrates: generic interface with bounded type parameter (Unit 5 Generics).
 */
public interface Repository<T, ID> {

    Optional<T> findById(ID id);
    List<T> findAll();
    T save(T entity);
    void update(T entity);
    boolean deleteById(ID id);
    long count();
}
