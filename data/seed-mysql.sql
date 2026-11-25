-- ============================================================
-- College Bus Tracking System — MySQL 8 Seed Data
-- ============================================================

USE bustrack;

-- ── Admin ────────────────────────────────────────────────────
INSERT IGNORE INTO admins(email) VALUES('admin@example.test');

-- ── Drivers ──────────────────────────────────────────────────
INSERT IGNORE INTO drivers(name, phone) VALUES('P Andand Babu', '90522 76465');
INSERT IGNORE INTO drivers(name, phone) VALUES('Salam',         '95509 31109');
INSERT IGNORE INTO drivers(name, phone) VALUES('Narendra',      '83330 56369');

-- ── Routes ───────────────────────────────────────────────────
INSERT IGNORE INTO routes(route_id, name, region) VALUES('TN-2', 'Tenali Route 2', 'Tenali');
INSERT IGNORE INTO routes(route_id, name, region) VALUES('TN-3', 'Tenali Route 3', 'Tenali');
INSERT IGNORE INTO routes(route_id, name, region) VALUES('TN-4', 'Tenali Route 4', 'Tenali');

-- ── Canonical Stops ──────────────────────────────────────────
-- TN-2 exclusive stops
INSERT IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Sai Baba Temple', 'PENDING', 0, NULL);

INSERT IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Tenali Bus Stand', 'PENDING', 0, NULL);

INSERT IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Pedavadlapudi', 'PENDING', 0, NULL);

INSERT IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Ratnalacheruvu', 'PENDING', 0, NULL);

-- TN-3 exclusive stops
INSERT IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Chinnaravuri Park', 'PENDING', 0, NULL);

INSERT IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Anjaneyaswamy Temple', 'PENDING', 0, NULL);

INSERT IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Billal Hotel', 'PENDING', 0, NULL);

INSERT IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Kotha Vanthana', 'PENDING', 0, 'Source timetable spelling: KothaVanthana (no space)');

-- TN-4 exclusive stops
INSERT IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Sivaji Chowk', 'PENDING', 0, NULL);

INSERT IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Mythri Hospital', 'PENDING', 0, 'Source timetable typo: Mythri Hospial');

INSERT IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Chakali Cheruvu', 'PENDING', 0, NULL);

INSERT IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Lakshmi Theater', 'PENDING', 0, NULL);

INSERT IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Kativaram', 'PENDING', 0, NULL);

INSERT IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Machikalapudi', 'PENDING', 0, NULL);

INSERT IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Mangalagiri Old Bus Stand', 'PENDING', 0, NULL);

-- Shared stops
INSERT IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Duggirala', 'PENDING', 0, 'ASSUMPTION – verify with transport office: TN-4 source says "Duggirala Gas Godown"');

INSERT IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('Ravindrapadu', 'PENDING', 0, 'TN-3 source name: "Ravindrapadu (Jenda Chettu)"');

-- Campus final stop
INSERT IGNORE INTO stops(name, geocode_status, is_campus, notes)
  VALUES('SRM University-AP (Campus)', 'PENDING', 1, 'Campus final destination stop');

-- ── Buses ────────────────────────────────────────────────────
INSERT IGNORE INTO buses(bus_number, registration_no, route_id, driver_id, active)
  VALUES('TN-2', 'AP39 UH 9613', 'TN-2',
         (SELECT driver_id FROM drivers WHERE phone='90522 76465'), 1);

INSERT IGNORE INTO buses(bus_number, registration_no, route_id, driver_id, active)
  VALUES('TN-3', 'AP39 UM 2725', 'TN-3',
         (SELECT driver_id FROM drivers WHERE phone='95509 31109'), 1);

INSERT IGNORE INTO buses(bus_number, registration_no, route_id, driver_id, active)
  VALUES('TN-4', 'AP39 UM 2726', 'TN-4',
         (SELECT driver_id FROM drivers WHERE phone='83330 56369'), 1);

-- ── Route Stops ───────────────────────────────────────────────
-- TN-2
INSERT IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-2', (SELECT stop_id FROM stops WHERE name='Sai Baba Temple'),           1, '07:25');
INSERT IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-2', (SELECT stop_id FROM stops WHERE name='Tenali Bus Stand'),          2, '07:30');
INSERT IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-2', (SELECT stop_id FROM stops WHERE name='Duggirala'),                 3, '07:40');
INSERT IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-2', (SELECT stop_id FROM stops WHERE name='Ravindrapadu'),              4, '07:55');
INSERT IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-2', (SELECT stop_id FROM stops WHERE name='Pedavadlapudi'),             5, '08:00');
INSERT IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-2', (SELECT stop_id FROM stops WHERE name='Ratnalacheruvu'),            6, '08:05');
INSERT IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-2', (SELECT stop_id FROM stops WHERE name='SRM University-AP (Campus)'),7, '08:45');

-- TN-3
INSERT IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-3', (SELECT stop_id FROM stops WHERE name='Chinnaravuri Park'),         1, '07:10');
INSERT IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-3', (SELECT stop_id FROM stops WHERE name='Anjaneyaswamy Temple'),      2, '07:15');
INSERT IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-3', (SELECT stop_id FROM stops WHERE name='Billal Hotel'),              3, '07:20');
INSERT IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-3', (SELECT stop_id FROM stops WHERE name='Kotha Vanthana'),            4, '07:25');
INSERT IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-3', (SELECT stop_id FROM stops WHERE name='Duggirala'),                 5, '07:40');
INSERT IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-3', (SELECT stop_id FROM stops WHERE name='Ravindrapadu'),              6, '07:55');
INSERT IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-3', (SELECT stop_id FROM stops WHERE name='SRM University-AP (Campus)'),7, '08:45');

-- TN-4
INSERT IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-4', (SELECT stop_id FROM stops WHERE name='Sivaji Chowk'),              1, '07:10');
INSERT IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-4', (SELECT stop_id FROM stops WHERE name='Mythri Hospital'),           2, '07:15');
INSERT IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-4', (SELECT stop_id FROM stops WHERE name='Chakali Cheruvu'),           3, '07:20');
INSERT IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-4', (SELECT stop_id FROM stops WHERE name='Lakshmi Theater'),           4, '07:25');
INSERT IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-4', (SELECT stop_id FROM stops WHERE name='Kativaram'),                 5, '07:30');
INSERT IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-4', (SELECT stop_id FROM stops WHERE name='Duggirala'),                 6, '07:45');
INSERT IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-4', (SELECT stop_id FROM stops WHERE name='Machikalapudi'),             7, '07:50');
INSERT IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-4', (SELECT stop_id FROM stops WHERE name='Mangalagiri Old Bus Stand'), 8, '08:10');
INSERT IGNORE INTO route_stops(route_id, stop_id, sequence, scheduled_time)
  VALUES('TN-4', (SELECT stop_id FROM stops WHERE name='SRM University-AP (Campus)'),9, '08:45');

-- ── Stop Aliases ─────────────────────────────────────────────
INSERT IGNORE INTO stop_aliases(alias_normalized, stop_id, confidence)
  VALUES('saibaba temple main road',
         (SELECT stop_id FROM stops WHERE name='Sai Baba Temple'), 'CONFIRMED');

INSERT IGNORE INTO stop_aliases(alias_normalized, stop_id, confidence)
  VALUES('tenali main bus stand',
         (SELECT stop_id FROM stops WHERE name='Tenali Bus Stand'), 'CONFIRMED');

INSERT IGNORE INTO stop_aliases(alias_normalized, stop_id, confidence)
  VALUES('revendrapadu',
         (SELECT stop_id FROM stops WHERE name='Ravindrapadu'), 'CONFIRMED');

INSERT IGNORE INTO stop_aliases(alias_normalized, stop_id, confidence)
  VALUES('pedavaddalamudi',
         (SELECT stop_id FROM stops WHERE name='Pedavadlapudi'), 'CONFIRMED');

INSERT IGNORE INTO stop_aliases(alias_normalized, stop_id, confidence)
  VALUES('anjayanaswami templechinaravuru road',
         (SELECT stop_id FROM stops WHERE name='Anjaneyaswamy Temple'), 'CONFIRMED');

INSERT IGNORE INTO stop_aliases(alias_normalized, stop_id, confidence)
  VALUES('bilal hotelnew bridge',
         (SELECT stop_id FROM stops WHERE name='Billal Hotel'), 'CONFIRMED');

INSERT IGNORE INTO stop_aliases(alias_normalized, stop_id, confidence)
  VALUES('kotta vantena',
         (SELECT stop_id FROM stops WHERE name='Kotha Vanthana'), 'CONFIRMED');

INSERT IGNORE INTO stop_aliases(alias_normalized, stop_id, confidence)
  VALUES('tenali park',
         (SELECT stop_id FROM stops WHERE name='Chinnaravuri Park'), 'PROBABLE');

INSERT IGNORE INTO stop_aliases(alias_normalized, stop_id, confidence)
  VALUES('duggirala',
         (SELECT stop_id FROM stops WHERE name='Duggirala'), 'CONFIRMED');

INSERT IGNORE INTO stop_aliases(alias_normalized, stop_id, confidence)
  VALUES('lakshmi takies',
         (SELECT stop_id FROM stops WHERE name='Lakshmi Theater'), 'PROBABLE');

-- ── Demo Students ────────────────────────────────────────────
INSERT IGNORE INTO students(reg_no, email, name, fee_status, transport_active, is_demo)
  VALUES('AP26DEMO0001', 'demo1@example.test', 'Demo Student A', 'PAID',    1, 1);
INSERT IGNORE INTO students(reg_no, email, name, fee_status, transport_active, is_demo)
  VALUES('AP26DEMO0002', 'demo2@example.test', 'Demo Student B', 'PAID',    1, 1);
INSERT IGNORE INTO students(reg_no, email, name, fee_status, transport_active, is_demo)
  VALUES('AP26DEMO0003', 'demo3@example.test', 'Demo Student C', 'PAID',    1, 1);
INSERT IGNORE INTO students(reg_no, email, name, fee_status, transport_active, is_demo)
  VALUES('AP26DEMO0004', 'demo4@example.test', 'Demo Student D', 'PAID',    1, 1);
INSERT IGNORE INTO students(reg_no, email, name, fee_status, transport_active, is_demo)
  VALUES('AP26DEMO0005', 'demo5@example.test', 'Demo Student E', 'PAID',    1, 1);
INSERT IGNORE INTO students(reg_no, email, name, fee_status, transport_active, is_demo)
  VALUES('AP26DEMO0006', 'demo6@example.test', 'Demo Student F', 'PENDING', 1, 1);
INSERT IGNORE INTO students(reg_no, email, name, fee_status, transport_active, is_demo)
  VALUES('AP26DEMO0007', 'demo7@example.test', 'Demo Student G', 'PAID',    0, 1);

-- ── Student Allocations ───────────────────────────────────────
INSERT IGNORE INTO student_allocations(reg_no, bus_id, boarding_stop_id, academic_year, active)
  VALUES('AP26DEMO0001',
         (SELECT bus_id FROM buses WHERE bus_number='TN-2'),
         (SELECT stop_id FROM stops WHERE name='Duggirala'),
         '2026-27', 1);

INSERT IGNORE INTO student_allocations(reg_no, bus_id, boarding_stop_id, academic_year, active)
  VALUES('AP26DEMO0002',
         (SELECT bus_id FROM buses WHERE bus_number='TN-2'),
         (SELECT stop_id FROM stops WHERE name='Ravindrapadu'),
         '2026-27', 1);

INSERT IGNORE INTO student_allocations(reg_no, bus_id, boarding_stop_id, academic_year, active)
  VALUES('AP26DEMO0003',
         (SELECT bus_id FROM buses WHERE bus_number='TN-3'),
         (SELECT stop_id FROM stops WHERE name='Kotha Vanthana'),
         '2026-27', 1);

INSERT IGNORE INTO student_allocations(reg_no, bus_id, boarding_stop_id, academic_year, active)
  VALUES('AP26DEMO0004',
         (SELECT bus_id FROM buses WHERE bus_number='TN-4'),
         (SELECT stop_id FROM stops WHERE name='Duggirala'),
         '2026-27', 1);

INSERT IGNORE INTO student_allocations(reg_no, bus_id, boarding_stop_id, academic_year, active)
  VALUES('AP26DEMO0005',
         (SELECT bus_id FROM buses WHERE bus_number='TN-4'),
         (SELECT stop_id FROM stops WHERE name='Mangalagiri Old Bus Stand'),
         '2026-27', 1);

INSERT IGNORE INTO student_allocations(reg_no, bus_id, boarding_stop_id, academic_year, active)
  VALUES('AP26DEMO0006',
         (SELECT bus_id FROM buses WHERE bus_number='TN-3'),
         (SELECT stop_id FROM stops WHERE name='Billal Hotel'),
         '2026-27', 1);
