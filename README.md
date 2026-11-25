# College Bus Tracking System

A complete, production-grade web application for SRM University-AP's transport system.
Tracks buses **TN-2**, **TN-3**, and **TN-4** (Tenali region).

> **End-semester Java Project** by a team of 4–5 undergraduates.
> **Tech Stack:** Java 17+ · Spring Boot 3.3 · MySQL 8 (with SQLite fallback) · Redis · Leaflet 1.9.4 · Vanilla ES Modules

---

## Prerequisites

1. **Java Development Kit (JDK) 17+**
2. **Database:**
   - **MySQL 8.0** (installed locally or via Docker Compose)
   - *Automatic Fallback:* If MySQL is not running, the application automatically uses local SQLite (`bustrack.db`) with zero configuration.
3. **Redis 7** (via Docker Desktop or [Memurai](https://www.memurai.com/) for Windows)

---

## Quick Start

### 1. Start Services (MySQL 8 & Redis)
If using Docker Compose:
```bash
docker compose up -d
```
Or start your local **MySQL Server 8.0** service from Windows Services / MySQL Workbench.
Create the database in MySQL:
```sql
CREATE DATABASE bustrack CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
*(The application will automatically run `data/schema-mysql.sql` and `data/seed-mysql.sql` upon startup).*

### 2. Configure Environment (Optional)
By default, the application runs in **Demo Mode** (`app.auth.mode=demo`).
No Google OAuth credentials or external network setup are required to test or present.

To use real Google Sign-In:
1. Create an OAuth 2.0 Client ID in Google Cloud Console (Application type: Web application).
2. Set Authorized JavaScript origins to `http://localhost:8080`.
3. Set the environment variable:
   ```bash
   export GOOGLE_CLIENT_ID="your-client-id.apps.googleusercontent.com"
   # Windows PowerShell:
   $env:GOOGLE_CLIENT_ID="your-client-id.apps.googleusercontent.com"
   ```

### 3. Run the Application
Using Maven:
```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

Open your browser and navigate to:
**`http://localhost:8080`**

---

## Demo Accounts & Scenarios

| Register No. | Name | Email | Scenario / Expected Behavior |
|---|---|---|---|
| `AP26DEMO0001` | Demo Student A | `demo1@example.test` | **Login OK.** Bus TN-2, boarding Duggirala. Alternatives: TN-3, TN-4. |
| `AP26DEMO0002` | Demo Student B | `demo2@example.test` | **Login OK.** Bus TN-2, boarding Ravindrapadu. Alternative: TN-3. |
| `AP26DEMO0003` | Demo Student C | `demo3@example.test` | **Login OK.** Bus TN-3, boarding Kotha Vanthana. **No alternatives** (Empty state). |
| `AP26DEMO0004` | Demo Student D | `demo4@example.test` | **Login OK.** Missed-bus demo: alternatives TN-2, TN-3 approaching. |
| `AP26DEMO0005` | Demo Student E | `demo5@example.test` | **Login OK.** Bus TN-4, boarding Mangalagiri Old Bus Stand. No alternatives. |
| `AP26DEMO0006` | Demo Student F | `demo6@example.test` | **Denied: `FEE_NOT_PAID`**. Fee status is PENDING. |
| `AP26DEMO0007` | Demo Student G | `demo7@example.test` | **Denied: `TRANSPORT_NOT_ENROLLED`**. Transport is inactive. |
| — | Unknown User | `unknown@example.test` | **Denied: `NOT_FOUND`**. Student not registered. |
| *(Admin)* | Demo Admin | `admin@example.test` | **Admin Dashboard.** Full CRUD, simulation controls, data quality panel. |

---

## Running Tests

Execute the comprehensive test suite:
```bash
./mvnw test
```

---

## What is Real vs What is Simulated

| Feature | Real or Simulated | Notes |
|---|---|---|
| Bus routes, stops, and schedules | ✅ **Real** | Exact timetable data from university sheet (31-08-2026). |
| Bus registrations and drivers | ✅ **Real** | Exact vehicle registration numbers and driver phone numbers. |
| Stop coordinates & road paths | ⚙️ Geocoded / OSRM | Geocoded via OpenStreetMap Nominatim; fallbacks supported. |
| GPS live tracking | 🟡 **Simulated** | Clearly tagged `source = "SIMULATED"` with visible UI badges. |
| Student records | 🟡 **Synthetic** | Synthetic `AP26DEMO####` accounts for academic demonstration. |
| University Auth API | 🟡 **Mock** | Clearly tagged mock service embedded for standalone execution. |
