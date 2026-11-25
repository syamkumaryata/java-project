# Syllabus Mapping — College Bus Tracking System

> **Status: Phase 1 Complete**
> This document maps every Java syllabus requirement to real code in the project.

---

## Unit 1 — OOP: Classes, Objects, Inheritance, Abstract Classes, Interfaces

| Requirement | Implementation | File/Class | Viva Explanation |
|-------------|----------------|------------|------------------|
| Classes and objects | Model classes with fields and constructors | `model/Student.java`, `model/Bus.java`, etc. | Core domain entities encapsulating student, driver, bus, route state. |
| Default, parameterized, overloaded constructors | Each model class | `model/Person.java`, `model/Student.java` | Demonstrates multiple ways to instantiate objects depending on context. |
| Inheritance: abstract Person | `Student`, `Driver`, `AdminUser` extend `Person` | `model/Person.java` | Common personal identity attributes (name, email) are abstracted in Person. |
| Inheritance: abstract Vehicle | `Bus` extends `Vehicle` | `model/Vehicle.java`, `model/Bus.java` | Generalized vehicle attributes (registration, active status) extended by Bus. |
| Interfaces | `Trackable`, `Notifiable`, `LocationProvider`, `SessionStore`, `TransportAuthorizationClient` | `tracking/LocationProvider.java`, `session/SessionStore.java` | Decouples caller logic from concrete implementations (e.g., Redis vs In-Memory session). |

## Unit 2 — Polymorphism and Exceptions

| Requirement | Implementation | File/Class | Viva Explanation |
|-------------|----------------|------------|------------------|
| Method overloading | `DistanceCalculator.distance(...)`, `EtaCalculator.eta(...)` | `tracking/EtaCalculator.java`, `tracking/DistanceCalculator.java` | Same method name with different argument lists (e.g. Stop object vs stopId). |
| Method overriding | `toString()`, `describe()` in Student, Driver, Bus | `model/Student.java`, `model/Driver.java`, `model/Bus.java` | Subclasses provide specific implementations of abstract `describe()`. |
| Runtime polymorphism | `LocationProvider` holds Simulated or Device; `SessionStore` holds Redis or Memory | `tracking/LocationProvider.java`, `session/SessionStore.java` | Method calls dispatch dynamically to the runtime object's implementation. |
| Custom exception hierarchy | `TransportException` base with specific subtypes | `exception/TransportException.java` | Subclasses like `StudentNotEligibleException` carry HTTP status and error codes. |
| try/catch/finally, try-with-resources | DAO JDBC operations | `dao/GenericJdbcDao.java`, `db/ConnectionProvider.java` | Automatically closes Connections, Statements, and ResultSets safely without leaks. |
| Global exception handler | `@RestControllerAdvice` | `web/GlobalExceptionHandler.java` | Catches exceptions system-wide and formats standardized JSON error responses. |

## Unit 3 — Multithreading and Events

| Requirement | Implementation | File/Class | Viva Explanation |
|-------------|----------------|------------|------------------|
| Runnable | `SimulatedGpsWorker implements Runnable` | `tracking/SimulatedGpsWorker.java` | Background task executing periodic bus position interpolation along route. |
| Thread pool | `ScheduledExecutorService` | `tracking/SimulationManager.java` | Manages worker threads ticking at fixed 2-second intervals. |
| Synchronization | `synchronized` blocks in `LocationStore`, SSE registry | `tracking/LocationStore.java`, `tracking/SseBroadcaster.java` | Prevents race conditions during concurrent location updates and SSE broadcasts. |
| `ReentrantReadWriteLock` | Multi-field reads and updates | `tracking/LocationStore.java` | Allows multiple concurrent readers while ensuring exclusive write access. |
| Observer pattern / event handling | `EventPublisher`, `LocationEventListener` | `tracking/EventPublisher.java` | Decouples location producers from consumers (SSE broadcast, DB writer, alerts). |

## Unit 4 — JDBC and Collections

| Requirement | Implementation | File/Class | Viva Explanation |
|-------------|----------------|------------|------------------|
| JDBC PreparedStatement | All DAO operations | `dao/GenericJdbcDao.java` | Eliminates SQL injection risk and allows query plan caching in SQLite. |
| JDBC transactions | Multi-row atomic updates | `dao/GenericJdbcDao.java` | `setAutoCommit(false)`, `commit()`, and `rollback()` ensure ACID consistency. |
| Full CRUD | Students, Buses, Drivers, Allocations | `dao/` package | Complete Create, Read, Update, Delete methods for all core tables. |
| `ArrayList`/`List` | Route stop lists | `dao/RouteDao.java`, `model/Route.java` | Ordered list of stops representing bus travel order. |
| `Set`/`HashSet` | Passed-stop IDs, unique emitter set | `tracking/LocationStore.java` | O(1) membership check to determine if a stop has already been passed. |
| `Map`/`HashMap`/`ConcurrentHashMap` | Bus state map, alias lookup | `tracking/LocationStore.java` | High-throughput thread-safe map storing the latest position per bus ID. |
| `TreeMap` | Stops ordered by sequence | `dao/RouteDao.java` | Red-black tree naturally ordering stops by their integer sequence number. |
| `Iterator` with removal | Dead SSE emitters, expired sessions | `tracking/SseBroadcaster.java` | Safely removes disconnected clients during collection traversal. |

## Unit 5 — Generics and JavaFX

| Requirement | Implementation | File/Class | Viva Explanation |
|-------------|----------------|------------|------------------|
| Generic interface `Repository<T,ID>` | DAO layer | `dao/Repository.java` | Parameterized CRUD interface providing type safety for any entity type T and key ID. |
| Generic abstract class `GenericJdbcDao<T,ID>` | DAO layer | `dao/GenericJdbcDao.java` | Reusable JDBC helper methods (`queryList`, `executeInsert`) across all entity DAOs. |
| `RowMapper<T>` | Per-entity mapping functional interface | `dao/RowMapper.java` | Converts a `ResultSet` row into a strongly typed entity instance. |
| `ApiResponse<T>`, `Page<T>` | REST responses | `model/ApiResponse.java`, `model/Page.java` | Standard wrapper ensuring uniform API contracts for single items and pagination. |
| Generic methods | `earliest(List<T>)`, `firstMatch(List<T>, Predicate<T>)` | `util/CollectionUtils.java` | Generic algorithms with bounded type parameters (`<T extends Comparable<T>>`). |
| **JavaFX** | **NOT part of the web application.** | `javafx-admin/` (optional module) | Web frontend is built with standards-compliant HTML/CSS/JS; JavaFX is optional desktop module. |
