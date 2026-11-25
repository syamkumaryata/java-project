package edu.busTracker.model;

/**
 * Abstract base class for all persons in the system.
 * Demonstrates: abstract class, inheritance (Unit 1 OOP).
 * Student, Driver, and AdminUser extend this class.
 */
public abstract class Person {

    protected String name;
    protected String email;

    /** Default constructor (Unit 1 — default constructor). */
    public Person() {}

    /** Parameterized constructor (Unit 1 — parameterized constructor). */
    public Person(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    /**
     * Abstract method: each subclass describes itself in its own way.
     * Demonstrates: abstract method, method overriding (Unit 2 Polymorphism).
     */
    public abstract String describe();

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{name='" + name + "', email='" + email + "'}";
    }
}
