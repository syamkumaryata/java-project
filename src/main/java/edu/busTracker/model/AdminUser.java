package edu.busTracker.model;

/**
 * Represents a system administrator.
 * Extends Person (Unit 1 — inheritance).
 */
public class AdminUser extends Person {

    public AdminUser() {
        super();
    }

    public AdminUser(String email) {
        super("Admin", email);
    }

    @Override
    public String describe() {
        return "AdminUser{email='" + email + "'}";
    }

    @Override
    public String toString() {
        return "AdminUser{email='" + email + "'}";
    }
}
