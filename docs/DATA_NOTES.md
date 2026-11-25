# Data Notes — College Bus Tracking System

> **Source:** SRM University-AP transport timetable, Tenali routes sheet, as of 31-08-2026.
> Routes in scope for Version 1: TN-2, TN-3, TN-4.

---

## 1. Driver Name Anomaly

- **Driver for TN-2:** Listed in the timetable as **"P Andand Babu"**.
- This may be a typo for "P Anand Babu".
- **Action:** The name has been kept verbatim from the source sheet. The transport office should confirm the correct spelling before go-live.

---

## 2. Stop Name Assumptions

### 2.1 Duggirala (shared stop — TN-2, TN-3, TN-4)

| Route | Source Name in Timetable | Treatment |
|-------|--------------------------|-----------|
| TN-2 | Duggirala | Canonical |
| TN-3 | Duggirala | Canonical |
| TN-4 | **Duggirala Gas Godown** | **ASSUMPTION:** Treated as the same canonical stop "Duggirala". |

> ⚠️ **Verify with transport office.** The assumption is stored in `stops.notes` and flagged in the admin Data Quality panel.

### 2.2 Ravindrapadu (shared stop — TN-2, TN-3)

| Route | Source Name | Treatment |
|-------|-------------|-----------|
| TN-2 | Ravindrapadu | Canonical |
| TN-3 | **Ravindrapadu (Jenda Chettu)** | Treated as canonical stop "Ravindrapadu". |

### 2.3 Kotha Vanthana (TN-3)

- Source timetable spelling: **"KothaVanthana"** (no space).
- Canonical name stored as "Kotha Vanthana" (with space, more readable).
- Stored in `stops.notes`.

---

## 3. Source Typos

| Field | Source Value | Corrected Value | Location |
|-------|-------------|-----------------|----------|
| Stop name, TN-4 | "Mythri Hospial" | "Mythri Hospital" | stops.name, stops.notes |
| Final stop, TN-4 sheet | "SMR University-AP" | "SRM University-AP (Campus)" | stops.notes |

---

## 4. Open Question — 08:45 Arrival at Campus

All three routes arrive at "SRM University-AP (Campus)" at **08:45**.

- TN-3 departs Ravindrapadu at 07:55 → arrives campus at 08:45 = **50 minutes**.
- This seems long for the distance and may indicate that 08:45 is a **reporting time** (when classes begin) rather than an actual bus arrival time.
- **Action:** Confirm with the transport office whether 08:45 is the bus's scheduled arrival or the university's reporting time.

---

## 5. Unresolved Boarding Points

The following student boarding points appear in the student allocation list but **cannot be matched** to any stop in the timetable. They must NOT be seeded or guessed.

| Alias (from student list) | Route | Status |
|--------------------------|-------|--------|
| TOWN CHURCH | TN-3 | ❌ Unresolved |
| VARDHAN MART | TN-4 | ❌ Unresolved |
| ASN PETROL BUNK (GANDHI CHOUK) | TN-4 | ❌ Unresolved |
| LINGARAO CENTER | TN-4 | ❌ Unresolved |

> **Action:** The team must resolve these with the transport office. Students boarding at these points cannot be assigned an allocation until the stops are confirmed and added to the timetable.

---

## 6. PROBABLE Aliases (require verification)

| Alias (normalized) | Mapped to | Confidence |
|-------------------|-----------|------------|
| tenali park | Chinnaravuri Park | PROBABLE |
| lakshmi takies | Lakshmi Theater | PROBABLE |

These are flagged in the admin Data Quality panel.

---

## 7. Missing Data

| Data | Status |
|------|--------|
| Coordinates of all stops | PENDING — run `StopGeocoder` tool (see README) |
| Road geometry for TN-2, TN-3, TN-4 | PENDING — computed from OSRM after geocoding |
| Live GPS from buses | Not available — simulated GPS used (`source = SIMULATED`) |
| Bus capacity / vehicle model | Not in source data — not modelled |
