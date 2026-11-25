# Test Plan — College Bus Tracking System

## 1. Automated Tests (JUnit 5 & Mockito)

### 1.1 DAO & Database Invariants
- `testStudentCrud()`: Insert, read, update, delete student records.
- `testForeignKeyEnforced()`: Inserting an allocation with an invalid stop or bus fails with SQLite FK violation.
- `testCampusLastStopRule()`: Validates that `SRM University-AP (Campus)` always has the highest sequence number on each route.
- `testBoardingStopNotCampusRule()`: Allocation with campus stop as boarding stop throws `InvalidBoardingStopException`.
- `testBoardingStopOnRouteRule()`: Boarding stop not belonging to the allocated bus route throws `InvalidBoardingStopException`.

### 1.2 Authentication & Authorization
- `testDemoAccountsAllowed()`: Accounts AP26DEMO0001 to AP26DEMO0005 log in successfully.
- `testFeePendingDenied()`: AP26DEMO0006 denied with `FEE_NOT_PAID`.
- `testTransportInactiveDenied()`: AP26DEMO0007 denied with `TRANSPORT_NOT_ENROLLED`.
- `testUnknownUserDenied()`: Non-existent email denied with `NOT_FOUND`.
- `testSessionExpiry()`: Requests with expired or missing session return HTTP 401.
- `testRedisOutageGraceful503()`: When Redis is unreachable, return friendly HTTP 503 rather than stack trace.

### 1.3 Alternative Bus Finder
- `testSameStopMatchesOnly()`: AP26DEMO0001 at Duggirala sees TN-3 and TN-4.
- `testNoAlternatives()`: AP26DEMO0003 at Kotha Vanthana sees 0 alternatives (correct empty state).
- `testPassedBusExcluded()`: A bus that has already passed the student's stop is excluded from suggestions.

---

## 2. Manual Acceptance Checklist

- [ ] App starts on port 8080 without errors.
- [ ] Database automatically creates `bustrack.db` and seeds tables.
- [ ] Demo login dropdown functions seamlessly with all test scenarios.
- [ ] Student dashboard displays allocated bus, driver contact, and route.
- [ ] Map renders Leaflet tiles and route stops correctly.
- [ ] SSE stream connects and moves bus marker smoothly.
- [ ] Arrival alert sounds alarm tone via Web Audio API.
- [ ] Missed bus action shows eligible alternative buses in real time.
- [ ] Admin panel performs CRUD operations with validation.
