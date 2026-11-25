-- ============================================================
-- College Bus Tracking System — Database Schema
-- SQLite database accessed via plain JDBC
-- IMPORTANT: Run "PRAGMA foreign_keys = ON" on EVERY connection
--            (handled by ConnectionProvider)
-- Run this file once at first startup if the DB file is absent.
-- ============================================================

PRAGMA journal_mode = WAL;

-- ── Students ──────────────────────────────────────────────────
-- Primary key is registration number (e.g. AP26DEMO0001)
-- is_demo = 1 for synthetic accounts; never store real student data
CREATE TABLE IF NOT EXISTS students (
  reg_no          TEXT    PRIMARY KEY,
  email           TEXT    NOT NULL UNIQUE,
  name            TEXT    NOT NULL,
  gender          TEXT,
  batch           INTEGER,
  school          TEXT,
  program         TEXT,
  fee_status      TEXT    NOT NULL CHECK (fee_status IN ('PAID','PENDING','OVERDUE')),
  transport_active INTEGER NOT NULL DEFAULT 1 CHECK (transport_active IN (0,1)),
  is_demo         INTEGER NOT NULL DEFAULT 1
);

-- ── Drivers ───────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS drivers (
  driver_id INTEGER PRIMARY KEY AUTOINCREMENT,
  name      TEXT    NOT NULL,
  phone     TEXT    NOT NULL UNIQUE
);

-- ── Routes ────────────────────────────────────────────────────
-- route_id is the route code (e.g. 'TN-2'); design allows adding
-- G-1…G-24, V-1…V-34, TN-1 etc. by inserting rows only
CREATE TABLE IF NOT EXISTS routes (
  route_id        TEXT PRIMARY KEY,
  name            TEXT NOT NULL,
  region          TEXT NOT NULL,
  geometry_json   TEXT,            -- GeoJSON LineString, cached from OSRM
  geometry_source TEXT CHECK (geometry_source IN ('OSRM','STRAIGHT_LINE_FALLBACK'))
);

-- ── Stops ─────────────────────────────────────────────────────
-- Canonical stop table. Shared stops (e.g. Duggirala) have ONE row.
-- Coordinates populated by StopGeocoder tool or MANUAL admin edit.
CREATE TABLE IF NOT EXISTS stops (
  stop_id         INTEGER PRIMARY KEY AUTOINCREMENT,
  name            TEXT    NOT NULL UNIQUE,
  latitude        REAL,
  longitude       REAL,
  geocode_status  TEXT    NOT NULL DEFAULT 'PENDING'
                          CHECK (geocode_status IN ('AUTO','MANUAL','PENDING')),
  geocode_source  TEXT,            -- e.g. 'nominatim: <display_name>'
  is_campus       INTEGER NOT NULL DEFAULT 0 CHECK (is_campus IN (0,1)),
  notes           TEXT             -- assumptions, source typos, unverified matches
);

-- ── Route Stops ───────────────────────────────────────────────
-- Junction table: which stops does each route serve, in what order,
-- at what scheduled time (morning trip towards campus).
-- Rule enforced in code: campus stop must have the highest sequence.
CREATE TABLE IF NOT EXISTS route_stops (
  route_id       TEXT    NOT NULL REFERENCES routes(route_id),
  stop_id        INTEGER NOT NULL REFERENCES stops(stop_id),
  sequence       INTEGER NOT NULL,          -- 1-based, ascending order
  scheduled_time TEXT    NOT NULL,          -- 'HH:MM' IST 24h
  PRIMARY KEY (route_id, sequence),
  UNIQUE (route_id, stop_id)
);

-- ── Stop Aliases ──────────────────────────────────────────────
-- Maps student-list spellings → canonical stop names.
-- alias_normalized: lowercase, punctuation stripped, whitespace collapsed.
CREATE TABLE IF NOT EXISTS stop_aliases (
  alias_normalized TEXT PRIMARY KEY,
  stop_id          INTEGER NOT NULL REFERENCES stops(stop_id),
  confidence       TEXT    NOT NULL CHECK (confidence IN ('CONFIRMED','PROBABLE'))
);

-- ── Buses ─────────────────────────────────────────────────────
-- bus_number is the route code shown as "Bus TN-2" in the UI.
-- registration_no is the vehicle's number plate (unique).
CREATE TABLE IF NOT EXISTS buses (
  bus_id          INTEGER PRIMARY KEY AUTOINCREMENT,
  bus_number      TEXT    NOT NULL UNIQUE,       -- 'TN-2'
  registration_no TEXT    NOT NULL UNIQUE,       -- 'AP39 UH 9613'
  route_id        TEXT    NOT NULL REFERENCES routes(route_id),
  driver_id       INTEGER NOT NULL REFERENCES drivers(driver_id),
  active          INTEGER NOT NULL DEFAULT 1
);

-- ── Student Allocations ───────────────────────────────────────
-- Links a student to their bus and boarding stop for an academic year.
-- Rule enforced in code: boarding_stop_id must be on the bus's route
-- and must NOT be the campus stop (InvalidBoardingStopException).
CREATE TABLE IF NOT EXISTS student_allocations (
  allocation_id    INTEGER PRIMARY KEY AUTOINCREMENT,
  reg_no           TEXT    NOT NULL REFERENCES students(reg_no),
  bus_id           INTEGER NOT NULL REFERENCES buses(bus_id),
  boarding_stop_id INTEGER NOT NULL REFERENCES stops(stop_id),
  academic_year    TEXT    NOT NULL,             -- '2026-27'
  active           INTEGER NOT NULL DEFAULT 1,
  UNIQUE (reg_no, academic_year)
);

-- ── Bus Locations ─────────────────────────────────────────────
-- Latest position per bus (upserted by SimulatedGpsWorker / DeviceLocationProvider).
-- source MUST be 'SIMULATED' for simulated GPS — never present simulated as real.
CREATE TABLE IF NOT EXISTS bus_locations (
  bus_id      INTEGER PRIMARY KEY REFERENCES buses(bus_id),
  latitude    REAL    NOT NULL,
  longitude   REAL    NOT NULL,
  speed_kmph  REAL,
  heading_deg REAL,
  recorded_at TEXT    NOT NULL,   -- ISO-8601 UTC
  source      TEXT    NOT NULL CHECK (source IN ('SIMULATED','DEVICE'))
);

-- ── Alert Settings ────────────────────────────────────────────
-- One row per student. minutes_before must be 5, 10, or 15.
-- last_fired_date prevents repeated alerts on the same day.
CREATE TABLE IF NOT EXISTS alert_settings (
  reg_no            TEXT    PRIMARY KEY REFERENCES students(reg_no),
  enabled           INTEGER NOT NULL DEFAULT 0,
  minutes_before    INTEGER NOT NULL DEFAULT 10 CHECK (minutes_before IN (5,10,15)),
  sound_enabled     INTEGER NOT NULL DEFAULT 1,
  last_fired_date   TEXT,          -- 'YYYY-MM-DD'
  updated_at        TEXT    NOT NULL
);

-- ── Admins ────────────────────────────────────────────────────
-- Emails that get role ADMIN after Google verification.
-- Sessions are NOT stored here — they live in Redis.
CREATE TABLE IF NOT EXISTS admins (
  email TEXT PRIMARY KEY
);

-- ── Indexes ───────────────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_route_stops_route   ON route_stops(route_id);
CREATE INDEX IF NOT EXISTS idx_route_stops_stop    ON route_stops(stop_id);
CREATE INDEX IF NOT EXISTS idx_allocations_reg     ON student_allocations(reg_no);
CREATE INDEX IF NOT EXISTS idx_allocations_bus     ON student_allocations(bus_id);
CREATE INDEX IF NOT EXISTS idx_students_email      ON students(email);
CREATE INDEX IF NOT EXISTS idx_stop_aliases_stop   ON stop_aliases(stop_id);
CREATE INDEX IF NOT EXISTS idx_buses_route         ON buses(route_id);
