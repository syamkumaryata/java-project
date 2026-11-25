# API Documentation — College Bus Tracking System

All endpoints return JSON. Errors use format:
```json
{
  "error": "ERROR_CODE",
  "message": "Human readable error message",
  "timestamp": "2026-11-16T11:00:00Z"
}
```
All `/api/**` endpoints require a valid session cookie (`CBTS_SID`) except those marked (Public). Mutating endpoints (POST, PUT, DELETE) require header `X-CSRF-Token`.

---

## 1. Auth & Public Config

### `GET /api/config/public` (Public)
Returns public application configuration.
- Response:
```json
{
  "authMode": "demo",
  "googleClientId": "",
  "simulatedGps": true
}
```

### `POST /api/auth/google` (Public)
Verifies Google Identity Services ID token, authenticates student or admin, sets `CBTS_SID` cookie.
- Request Body:
```json
{
  "credential": "<Google ID Token>"
}
```

### `POST /api/auth/demo` (Public, Demo Mode Only)
Authenticates as one of the pre-seeded demo accounts.
- Request Body:
```json
{
  "email": "demo1@example.test"
}
```

### `POST /api/auth/logout`
Terminates current session in Redis and clears session cookie.

### `GET /api/me`
Returns current user profile, role, and CSRF token.
- Response:
```json
{
  "regNo": "AP26DEMO0001",
  "name": "Demo Student A",
  "email": "demo1@example.test",
  "role": "STUDENT",
  "csrfToken": "<token>"
}
```

---

## 2. Student Endpoints

### `GET /api/student/dashboard`
Returns student dashboard overview (bus, driver, boarding stop, live status).

### `GET /api/student/profile`
Returns full student profile details.

### `GET /api/student/route`
Returns ordered sequence of stops for the allocated route with scheduled times and coordinates.

### `GET /api/student/alerts`
Returns student's alert settings.

### `PUT /api/student/alerts`
Updates alert configuration.
- Request Body:
```json
{
  "enabled": true,
  "minutesBefore": 10,
  "soundEnabled": true
}
```

### `GET /api/student/alternatives`
Finds eligible alternative buses that serve the student's boarding stop.

### `GET /api/tracking/bus/{busId}`
Returns latest position and ETA for the requested bus (must be allocated or eligible alternative).

### `GET /api/stream/tracking` (SSE)
Server-Sent Events stream for live tracking updates.

---

## 3. Admin Endpoints (`role = ADMIN`)

### CRUD:
- `/api/admin/students` (GET, POST, PUT, DELETE)
- `/api/admin/buses` (GET, POST, PUT, DELETE)
- `/api/admin/drivers` (GET, POST, PUT, DELETE)
- `/api/admin/allocations` (GET, POST, PUT, DELETE)
- `/api/admin/routes` (GET)
- `/api/admin/stops/{id}` (GET, PUT)

### Data Quality & Simulation:
- `GET /api/admin/data-quality`
- `POST /api/admin/simulation/start`
- `POST /api/admin/simulation/stop`
- `POST /api/admin/simulation/speed`
- `POST /api/admin/simulation/bus/{busId}/delay`
- `POST /api/admin/sessions/revoke`

---

## 4. Mock University Transport API (clearly marked MOCK)

### `GET /mock-university-api/v1/students/by-email?email=...`
Header: `X-API-Key: demo-api-key-change-in-prod`
Response:
```json
{
  "mock": true,
  "regNo": "AP26DEMO0001",
  "name": "Demo Student A",
  "email": "demo1@example.test",
  "enrolledInTransport": true,
  "feeStatus": "PAID",
  "busNumber": "TN-2",
  "boardingStop": "Duggirala",
  "destination": "SRM University-AP (Campus)",
  "academicYear": "2026-27"
}
```
