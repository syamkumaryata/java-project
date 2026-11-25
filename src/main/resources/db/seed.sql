-- ============================================================
-- College Bus Tracking System — Seed Data
-- Source: SRM University-AP transport timetable, Tenali routes
--         sheet dated 31-08-2026
-- Synthetic demo students use prefix AP26DEMO (never real students)
-- ============================================================

-- ── Admin ────────────────────────────────────────────────────
INSERT OR IGNORE INTO admins(email) VALUES('admin@example.test');

-- ── Drivers ──────────────────────────────────────────────────
-- Spellings kept VERBATIM from the timetable source sheet.
-- Note: "Andand" may be a typo for "Anand" — see DATA_NOTES.md
INSERT OR IGNORE INTO drivers(name, phone) VALUES('P Andand Babu', '90522 76465');
INSERT OR IGNORE INTO drivers(name, phone) VALUES('Salam',         '95509 31109');
INSERT OR IGNORE INTO drivers(name, phone) VALUES('Narendra',      '83330 56369');

-- ── Routes ───────────────────────────────────────────────────
INSERT OR IGNORE INTO routes(route_id, name, region) VALUES('TN-2', 'Tenali Route 2', 'Tenali');
INSERT OR IGNORE INTO routes(route_id, name, region) VALUES('TN-3', 'Tenali Route 3', 'Tenali');
INSERT OR IGNORE INTO routes(route_id, name, region) VALUES('TN-4', 'Tenali Route 4', 'Tenali');

-- ── Canonical Stops ──────────────────────────────────────────
-- Coordinates: PENDING — run StopGeocoder tool (see README).
-- Do NOT invent coordinates.
-- Shared stops (Duggirala, Ravindrapadu, campus) have ONE row each.

-- TN-2 exclusive stops
INSERT OR IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Sai Baba Temple', 'PENDING', 0, NULL);

INSERT OR IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Tenali Bus Stand', 'PENDING', 0, NULL);

INSERT OR IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Pedavadlapudi', 'PENDING', 0, NULL);

INSERT OR IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Ratnalacheruvu', 'PENDING', 0, NULL);

-- TN-3 exclusive stops
INSERT OR IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Chinnaravuri Park', 'PENDING', 0, NULL);

INSERT OR IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Anjaneyaswamy Temple', 'PENDING', 0, NULL);

INSERT OR IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Billal Hotel', 'PENDING', 0, NULL);

-- Source timetable spelling: "KothaVanthana" (no space)
INSERT OR IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Kotha Vanthana', 'PENDING', 0,
         'Source timetable spelling: KothaVanthana (no space)');

-- TN-4 exclusive stops
INSERT OR IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Sivaji Chowk', 'PENDING', 0, NULL);

-- Source timetable typo: "Mythri Hospial"
INSERT OR IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Mythri Hospital', 'PENDING', 0,
         'Source timetable typo: Mythri Hospial');

INSERT OR IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Chakali Cheruvu', 'PENDING', 0, NULL);

INSERT OR IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Lakshmi Theater', 'PENDING', 0, NULL);

INSERT OR IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Kativaram', 'PENDING', 0, NULL);

INSERT OR IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Machikalapudi', 'PENDING', 0, NULL);

INSERT OR IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Mangalagiri Old Bus Stand', 'PENDING', 0, NULL);

-- Shared: TN-2 (07:40), TN-3 (07:40), TN-4 (07:45 as "Duggirala Gas Godown")
INSERT OR IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Duggirala', 'PENDING', 0,
         'ASSUMPTION – verify with transport office: TN-4 source says "Duggirala Gas Godown" — treated as the same canonical stop');

-- Shared: TN-2 (07:55), TN-3 (07:55 as "Ravindrapadu (Jenda Chettu)")
INSERT OR IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Ravindrapadu', 'PENDING', 0,
         'TN-3 source name: "Ravindrapadu (Jenda Chettu)" — treated as same canonical stop');

-- Campus — final stop of ALL routes; is_campus = 1
-- Source typo on TN-4 sheet: "SMR University-AP"
INSERT OR IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('SRM University-AP (Campus)', 'PENDING', 1,
         'Source typo on TN-4 timetable: "SMR University-AP". This is the campus final stop (is_campus=1). 08:45 may be reporting time, not bus arrival — confirm with transport office.');

-- ── Buses ────────────────────────────────────────────────────
INSERT OR IGNORE INTO buses(bus_number, registration_no, route_id, driver_id, active)
  VALUES('TN-2', 'AP39 UH 9613', 'TN-2',
         (SELECT driver_id FROM drivers WHERE phone='90522 76465'), 1);

INSERT OR IGNORE INTO buses(bus_number, registration_no, route_id, driver_id, active)
  VALUES('TN-3', 'AP39 UM 2725', 'TN-3',
         (SELECT driver_id FROM drivers WHERE phone='95509 31109'), 1);

INSERT OR IGNORE INTO buses(bus_number, registration_no, route_id, driver_id, active)
  VALUES('TN-4', 'AP39 UM 2726', 'TN-4',
         (SELECT driver_id FROM drivers WHERE phone='83330 56369'), 1);

-- ── Route Stops ───────────────────────────────────────────────
-- All times are IST 24h from the source timetable (morning trip, towards campus).
-- Campus stop MUST have the highest sequence on every route (enforced in code).

-- TN-2 (7 stops, campus at seq 7)
INSERT OR IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-2', (SELECT stop_id FROM stops WHERE name='Sai Baba Temple'),           1, '07:25');
INSERT OR IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-2', (SELECT stop_id FROM stops WHERE name='Tenali Bus Stand'),          2, '07:30');
INSERT OR IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-2', (SELECT stop_id FROM stops WHERE name='Duggirala'),                 3, '07:40');
INSERT OR IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-2', (SELECT stop_id FROM stops WHERE name='Ravindrapadu'),              4, '07:55');
INSERT OR IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-2', (SELECT stop_id FROM stops WHERE name='Pedavadlapudi'),             5, '08:00');
INSERT OR IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-2', (SELECT stop_id FROM stops WHERE name='Ratnalacheruvu'),            6, '08:05');
INSERT OR IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-2', (SELECT stop_id FROM stops WHERE name='SRM University-AP (Campus)'),7, '08:45');

-- TN-3 (7 stops, campus at seq 7)
INSERT OR IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-3', (SELECT stop_id FROM stops WHERE name='Chinnaravuri Park'),         1, '07:10');
INSERT OR IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-3', (SELECT stop_id FROM stops WHERE name='Anjaneyaswamy Temple'),      2, '07:15');
INSERT OR IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-3', (SELECT stop_id FROM stops WHERE name='Billal Hotel'),              3, '07:20');
INSERT OR IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-3', (SELECT stop_id FROM stops WHERE name='Kotha Vanthana'),            4, '07:25');
INSERT OR IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-3', (SELECT stop_id FROM stops WHERE name='Duggirala'),                 5, '07:40');
INSERT OR IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-3', (SELECT stop_id FROM stops WHERE name='Ravindrapadu'),              6, '07:55');
INSERT OR IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-3', (SELECT stop_id FROM stops WHERE name='SRM University-AP (Campus)'),7, '08:45');

-- TN-4 (9 stops, campus at seq 9)
INSERT OR IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-4', (SELECT stop_id FROM stops WHERE name='Sivaji Chowk'),              1, '07:10');
INSERT OR IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-4', (SELECT stop_id FROM stops WHERE name='Mythri Hospital'),           2, '07:15');
INSERT OR IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-4', (SELECT stop_id FROM stops WHERE name='Chakali Cheruvu'),           3, '07:20');
INSERT OR IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-4', (SELECT stop_id FROM stops WHERE name='Lakshmi Theater'),           4, '07:25');
INSERT OR IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-4', (SELECT stop_id FROM stops WHERE name='Kativaram'),                 5, '07:30');
INSERT OR IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-4', (SELECT stop_id FROM stops WHERE name='Duggirala'),                 6, '07:45');
INSERT OR IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-4', (SELECT stop_id FROM stops WHERE name='Machikalapudi'),             7, '07:50');
INSERT OR IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-4', (SELECT stop_id FROM stops WHERE name='Mangalagiri Old Bus Stand'), 8, '08:10');
INSERT OR IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-4', (SELECT stop_id FROM stops WHERE name='SRM University-AP (Campus)'),9, '08:45');

-- ── Stop Aliases ─────────────────────────────────────────────
INSERT OR IGNORE INTO stop_aliases(alias_normalized, stop_id, confidence)
  VALUES('saibaba temple main road',
         (SELECT stop_id FROM stops WHERE name='Sai Baba Temple'), 'CONFIRMED');

INSERT OR IGNORE INTO stop_aliases(alias_normalized, stop_id, confidence)
  VALUES('tenali main bus stand',
         (SELECT stop_id FROM stops WHERE name='Tenali Bus Stand'), 'CONFIRMED');

INSERT OR IGNORE INTO stop_aliases(alias_normalized, stop_id, confidence)
  VALUES('revendrapadu',
         (SELECT stop_id FROM stops WHERE name='Ravindrapadu'), 'CONFIRMED');

INSERT OR IGNORE INTO stop_aliases(alias_normalized, stop_id, confidence)
  VALUES('pedavaddalamudi',
         (SELECT stop_id FROM stops WHERE name='Pedavadlapudi'), 'CONFIRMED');

INSERT OR IGNORE INTO stop_aliases(alias_normalized, stop_id, confidence)
  VALUES('anjayanaswami templechinaravuru road',
         (SELECT stop_id FROM stops WHERE name='Anjaneyaswamy Temple'), 'CONFIRMED');

INSERT OR IGNORE INTO stop_aliases(alias_normalized, stop_id, confidence)
  VALUES('bilal hotelnew bridge',
         (SELECT stop_id FROM stops WHERE name='Billal Hotel'), 'CONFIRMED');

INSERT OR IGNORE INTO stop_aliases(alias_normalized, stop_id, confidence)
  VALUES('kotta vantena',
         (SELECT stop_id FROM stops WHERE name='Kotha Vanthana'), 'CONFIRMED');

INSERT OR IGNORE INTO stop_aliases(alias_normalized, stop_id, confidence)
  VALUES('tenali park',
         (SELECT stop_id FROM stops WHERE name='Chinnaravuri Park'), 'PROBABLE');

INSERT OR IGNORE INTO stop_aliases(alias_normalized, stop_id, confidence)
  VALUES('duggirala',
         (SELECT stop_id FROM stops WHERE name='Duggirala'), 'CONFIRMED');

INSERT OR IGNORE INTO stop_aliases(alias_normalized, stop_id, confidence)
  VALUES('lakshmi takies',
         (SELECT stop_id FROM stops WHERE name='Lakshmi Theater'), 'PROBABLE');

-- ── Demo Students (synthetic — AP26DEMO prefix, example.test emails) ──
INSERT OR IGNORE INTO students(reg_no, email, name, fee_status, transport_active, is_demo)
  VALUES('AP26DEMO0001', 'demo1@example.test', 'Demo Student A', 'PAID',    1, 1);
INSERT OR IGNORE INTO students(reg_no, email, name, fee_status, transport_active, is_demo)
  VALUES('AP26DEMO0002', 'demo2@example.test', 'Demo Student B', 'PAID',    1, 1);
INSERT OR IGNORE INTO students(reg_no, email, name, fee_status, transport_active, is_demo)
  VALUES('AP26DEMO0003', 'demo3@example.test', 'Demo Student C', 'PAID',    1, 1);
INSERT OR IGNORE INTO students(reg_no, email, name, fee_status, transport_active, is_demo)
  VALUES('AP26DEMO0004', 'demo4@example.test', 'Demo Student D', 'PAID',    1, 1);
INSERT OR IGNORE INTO students(reg_no, email, name, fee_status, transport_active, is_demo)
  VALUES('AP26DEMO0005', 'demo5@example.test', 'Demo Student E', 'PAID',    1, 1);
INSERT OR IGNORE INTO students(reg_no, email, name, fee_status, transport_active, is_demo)
  VALUES('AP26DEMO0006', 'demo6@example.test', 'Demo Student F', 'PENDING', 1, 1);
INSERT OR IGNORE INTO students(reg_no, email, name, fee_status, transport_active, is_demo)
  VALUES('AP26DEMO0007', 'demo7@example.test', 'Demo Student G', 'PAID',    0, 1);

-- ── Student Allocations ───────────────────────────────────────
INSERT OR IGNORE INTO student_allocations(reg_no, bus_id, boarding_stop_id, academic_year, active)
  VALUES('AP26DEMO0001',
         (SELECT bus_id FROM buses WHERE bus_number='TN-2'),
         (SELECT stop_id FROM stops WHERE name='Duggirala'),
         '2026-27', 1);

INSERT OR IGNORE INTO student_allocations(reg_no, bus_id, boarding_stop_id, academic_year, active)
  VALUES('AP26DEMO0002',
         (SELECT bus_id FROM buses WHERE bus_number='TN-2'),
         (SELECT stop_id FROM stops WHERE name='Ravindrapadu'),
         '2026-27', 1);

INSERT OR IGNORE INTO student_allocations(reg_no, bus_id, boarding_stop_id, academic_year, active)
  VALUES('AP26DEMO0003',
         (SELECT bus_id FROM buses WHERE bus_number='TN-3'),
         (SELECT stop_id FROM stops WHERE name='Kotha Vanthana'),
         '2026-27', 1);

INSERT OR IGNORE INTO student_allocations(reg_no, bus_id, boarding_stop_id, academic_year, active)
  VALUES('AP26DEMO0004',
         (SELECT bus_id FROM buses WHERE bus_number='TN-4'),
         (SELECT stop_id FROM stops WHERE name='Duggirala'),
         '2026-27', 1);

INSERT OR IGNORE INTO student_allocations(reg_no, bus_id, boarding_stop_id, academic_year, active)
  VALUES('AP26DEMO0005',
         (SELECT bus_id FROM buses WHERE bus_number='TN-4'),
         (SELECT stop_id FROM stops WHERE name='Mangalagiri Old Bus Stand'),
         '2026-27', 1);

INSERT OR IGNORE INTO student_allocations(reg_no, bus_id, boarding_stop_id, academic_year, active)
  VALUES('AP26DEMO0006',
         (SELECT bus_id FROM buses WHERE bus_number='TN-3'),
         (SELECT stop_id FROM stops WHERE name='Billal Hotel'),
         '2026-27', 1);
