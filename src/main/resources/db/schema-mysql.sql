-- ============================================================
-- College Bus Tracking System — MySQL 8 Database Schema
-- Complete relational schema for MySQL 8 with InnoDB engine,
-- foreign keys, unique constraints, and indexes.
-- ============================================================

CREATE DATABASE IF NOT EXISTS bustrack
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE bustrack;

-- ── Students ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS students (
  reg_no           VARCHAR(64)  PRIMARY KEY,
  email            VARCHAR(191) NOT NULL UNIQUE,
  name             VARCHAR(255) NOT NULL,
  gender           VARCHAR(20),
  batch            INT,
  school           VARCHAR(255),
  program          VARCHAR(255),
  fee_status       VARCHAR(20)  NOT NULL DEFAULT 'PAID',
  transport_active TINYINT(1)   NOT NULL DEFAULT 1,
  is_demo          TINYINT(1)   NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Drivers ───────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS drivers (
  driver_id INT PRIMARY KEY AUTO_INCREMENT,
  name      VARCHAR(255) NOT NULL,
  phone     VARCHAR(50)  NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Routes ────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS routes (
  route_id        VARCHAR(32)  PRIMARY KEY,
  name            VARCHAR(255) NOT NULL,
  region          VARCHAR(100) NOT NULL,
  geometry_json   LONGTEXT,
  geometry_source VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Stops ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS stops (
  stop_id        INT PRIMARY KEY AUTO_INCREMENT,
  name           VARCHAR(191) NOT NULL UNIQUE,
  latitude       DOUBLE,
  longitude      DOUBLE,
  geocode_status VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
  geocode_source VARCHAR(255),
  is_campus      TINYINT(1)   NOT NULL DEFAULT 0,
  notes          TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Route Stops ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS route_stops (
  route_id       VARCHAR(32) NOT NULL,
  stop_id        INT         NOT NULL,
  sequence       INT         NOT NULL,
  scheduled_time VARCHAR(20) NOT NULL,
  PRIMARY KEY (route_id, sequence),
  UNIQUE KEY uq_route_stop (route_id, stop_id),
  CONSTRAINT fk_rs_route FOREIGN KEY (route_id) REFERENCES routes(route_id) ON DELETE CASCADE,
  CONSTRAINT fk_rs_stop  FOREIGN KEY (stop_id) REFERENCES stops(stop_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Stop Aliases ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS stop_aliases (
  alias_normalized VARCHAR(191) PRIMARY KEY,
  stop_id          INT          NOT NULL,
  confidence       VARCHAR(20)  NOT NULL,
  CONSTRAINT fk_sa_stop FOREIGN KEY (stop_id) REFERENCES stops(stop_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Buses ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS buses (
  bus_id          INT PRIMARY KEY AUTO_INCREMENT,
  bus_number      VARCHAR(64) NOT NULL UNIQUE,
  registration_no VARCHAR(64) NOT NULL UNIQUE,
  route_id        VARCHAR(32) NOT NULL,
  driver_id       INT         NOT NULL,
  active          TINYINT(1)  NOT NULL DEFAULT 1,
  CONSTRAINT fk_b_route  FOREIGN KEY (route_id)  REFERENCES routes(route_id) ON DELETE CASCADE,
  CONSTRAINT fk_b_driver FOREIGN KEY (driver_id) REFERENCES drivers(driver_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Student Allocations ───────────────────────────────────────
CREATE TABLE IF NOT EXISTS student_allocations (
  allocation_id    INT PRIMARY KEY AUTO_INCREMENT,
  reg_no           VARCHAR(64) NOT NULL,
  bus_id           INT         NOT NULL,
  boarding_stop_id INT         NOT NULL,
  academic_year    VARCHAR(32) NOT NULL,
  active           TINYINT(1)  NOT NULL DEFAULT 1,
  UNIQUE KEY uq_student_year (reg_no, academic_year),
  CONSTRAINT fk_sa_student FOREIGN KEY (reg_no) REFERENCES students(reg_no) ON DELETE CASCADE,
  CONSTRAINT fk_sa_bus     FOREIGN KEY (bus_id) REFERENCES buses(bus_id) ON DELETE CASCADE,
  CONSTRAINT fk_sa_stop_b  FOREIGN KEY (boarding_stop_id) REFERENCES stops(stop_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Bus Locations ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bus_locations (
  bus_id      INT PRIMARY KEY,
  latitude    DOUBLE      NOT NULL,
  longitude   DOUBLE      NOT NULL,
  speed_kmph  DOUBLE,
  heading_deg DOUBLE,
  recorded_at VARCHAR(64) NOT NULL,
  source      VARCHAR(20) NOT NULL,
  CONSTRAINT fk_bl_bus FOREIGN KEY (bus_id) REFERENCES buses(bus_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Alert Settings ────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS alert_settings (
  reg_no          VARCHAR(64) PRIMARY KEY,
  enabled         TINYINT(1)  NOT NULL DEFAULT 0,
  minutes_before  INT         NOT NULL DEFAULT 10,
  sound_enabled   TINYINT(1)  NOT NULL DEFAULT 1,
  last_fired_date VARCHAR(20),
  updated_at      VARCHAR(64) NOT NULL,
  CONSTRAINT fk_as_student FOREIGN KEY (reg_no) REFERENCES students(reg_no) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Admins ────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS admins (
  email VARCHAR(191) PRIMARY KEY
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
