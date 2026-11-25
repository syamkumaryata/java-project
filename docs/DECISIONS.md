# Architecture and Design Decisions

## Decision Log

| # | Decision | Rationale |
|---|----------|-----------|
| D-01 | Package root `edu.busTracker` | Matches syllabus convention for the institution's Java projects. |
| D-02 | Plain JDBC (no JPA/Hibernate) | Explicitly required by the syllabus; demonstrates `PreparedStatement`, `ResultSet`, transactions. |
| D-03 | SQLite for all app data, Redis for sessions only | SQLite requires zero infrastructure for the demo; Redis provides proper session TTL and revocation. |
| D-04 | Demo mode is default when `GOOGLE_CLIENT_ID` is not set | Allows the team to run and present the app without OAuth credentials. The real Google path is fully implemented. |
| D-05 | Simulated GPS always tagged `source = "SIMULATED"` | Prevents any screen from ever presenting simulated movement as real GPS. The badge is enforced in the DB, API and UI. |
| D-06 | TN-4 "Duggirala Gas Godown" treated as canonical stop "Duggirala" | The timetable uses a local landmark name for the same location. Stored in `stops.notes` with an "ASSUMPTION – verify with transport office" note. |
| D-07 | `app.alternatives.allow-cross-route-boarding=true` by default | The source data contains no per-bus capacity or boarding restrictions. This flag is off by default in production but on for the demo, documented here and in DATA_NOTES. |
| D-08 | Bus capacity and vehicle model not modelled | Not present in the source timetable data. Adding them would require inventing data, which is prohibited. |
| D-09 | Bus number = route code (e.g. "TN-2"), displayed as "Bus TN-2" | Source timetable uses route codes as bus identifiers; registration numbers are separate. |
| D-10 | One `ConnectionProvider` per operation (short-lived connections) | Prevents SQLite `SQLITE_BUSY` errors under concurrent reads/writes; WAL mode allows concurrent reads. |
| D-11 | RedisSessionStore default with automatic InMemorySessionStore fallback | Production defaults to RedisSessionStore; if Redis/Docker is unreachable, SessionConfig gracefully falls back to InMemorySessionStore for zero-dependency standalone execution and academic evaluation. |
| D-12 | Simulated GPS worker ticks every 2 s, DB write throttled to every 10 s | Reduces SQLite write pressure while keeping SSE updates smooth. |
| D-13 | CSRF token is per-session, returned at login | Per-session token stored in session hash and validated on mutating requests. |
| D-14 | Rate limit on `/api/auth/*` is in-memory per IP | Simple enough for demo presentation; prevents brute force attacks. |
| D-15 | Alternative Bus Finder shows buses with `SAME_STOP` match only | The spec allows only buses that actually serve the student's boarding stop. No proximity guessing. |
