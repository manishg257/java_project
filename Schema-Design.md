# Smart Clinic Schema Design

## MySQL Database Design

**Rationale:**  
Core operational data with strict relationships and constraints belongs in relational tables (`patients`, `doctors`, `appointments`, `admin`) to ensure integrity and easy reporting; status codes and foreign keys enforce consistency for scheduling and authentication flows.

---

### Table: `patients`

| Column         | Type               | Constraints                                   |
| -------------- | ------------------ | --------------------------------------------- |
| id             | BIGINT UNSIGNED    | PK, AUTO_INCREMENT, NOT NULL                  |
| name           | VARCHAR(100)       | NOT NULL                                      |
| email          | VARCHAR(120)       | NOT NULL, UNIQUE                              |
| password_hash  | VARCHAR(255)       | NOT NULL                                      |
| phone          | CHAR(10)           | NOT NULL, UNIQUE                              |
| address        | VARCHAR(255)       | NOT NULL                                      |
| created_at     | DATETIME           | NOT NULL, DEFAULT CURRENT_TIMESTAMP           |

**Notes:**  
Unique email and phone avoid duplicate accounts; lengths mirror typical validation rules, while phone is fixed 10 digits validated at the app layer.

---

### Table: `doctors`

| Column         | Type               | Constraints                                   |
| -------------- | ------------------ | --------------------------------------------- |
| id             | BIGINT UNSIGNED    | PK, AUTO_INCREMENT, NOT NULL                  |
| name           | VARCHAR(100)       | NOT NULL                                      |
| email          | VARCHAR(120)       | NOT NULL, UNIQUE                              |
| specialty      | VARCHAR(100)       | NOT NULL                                      |
| phone          | CHAR(10)           | NOT NULL, UNIQUE                              |
| active         | TINYINT(1)         | NOT NULL, DEFAULT 1                           |
| created_at     | DATETIME           | NOT NULL, DEFAULT CURRENT_TIMESTAMP           |

**Notes:**  
Unique email/phone for contact; active flag supports soft disable without deletion.

---

### Table: `admin`

| Column         | Type               | Constraints                                   |
| -------------- | ------------------ | --------------------------------------------- |
| id             | BIGINT UNSIGNED    | PK, AUTO_INCREMENT, NOT NULL                  |
| name           | VARCHAR(100)       | NOT NULL                                      |
| email          | VARCHAR(120)       | NOT NULL, UNIQUE                              |
| password_hash  | VARCHAR(255)       | NOT NULL                                      |
| role           | ENUM('SUPER_ADMIN','STAFF') | NOT NULL, DEFAULT 'STAFF'               |
| created_at     | DATETIME           | NOT NULL, DEFAULT CURRENT_TIMESTAMP           |

**Notes:**  
Admins manage schedules/users; enum roles document permissions at the DB layer while business rules enforce authorization in code.

---

### Table: `appointments`

| Column           | Type               | Constraints                                   |
| ---------------- | ------------------ | --------------------------------------------- |
| id               | BIGINT UNSIGNED    | PK, AUTO_INCREMENT, NOT NULL                  |
| doctor_id        | BIGINT UNSIGNED    | NOT NULL, FK → doctors(id) ON DELETE RESTRICT |
| patient_id       | BIGINT UNSIGNED    | NOT NULL, FK → patients(id) ON DELETE CASCADE |
| appointment_time | DATETIME           | NOT NULL                                      |
| status           | TINYINT            | NOT NULL, DEFAULT 0                           |
| reason           | VARCHAR(255)       | NULL                                          |

**Indexes:**
- INDEX `idx_appt_doctor_time` (doctor_id, appointment_time) to check overlaps per doctor.

**Notes:**  
ON DELETE CASCADE for patient ensures appointments are removed if a patient is purged; doctors use RESTRICT to avoid orphaning schedules inadvertently; overlap rules enforced in application/service layer using the composite index for performance.

---

**Optional supporting tables (if needed later):**

- `doctor_availability` (`id`, `doctor_id`, `day_of_week`, `start_time`, `end_time`) to model schedules and prevent overlaps by time windows; can be added when implementing advanced scheduling.

---

## MongoDB Collection Design

**Rationale:**  
Flexible data like prescriptions, notes, and optional fields fit better in a document model, evolving without schema migrations and allowing nested metadata.

---

### Collection: `prescriptions`

**Purpose:**  
Store medication instructions tied to an appointment, with optional doctor notes, refill info, and nested pharmacy metadata; links back to MySQL via `appointmentId` (relational FK concept preserved as reference).

**Example document:**

```json
{
  "_id": "ObjectId(\"64b1f2e9c8e4ab12cd345678\")",
  "patientName": "John Smith",
  "appointmentId": 51,
  "medication": "Amoxicillin",
  "dosage": "500mg",
  "doctorNotes": "Take 1 capsule every 8 hours after food for 7 days. Recheck if symptoms persist.",
  "refillCount": 1,
  "pharmacy": {
    "name": "CityCare Pharmacy",
    "location": "Main Street, Downtown",
    "contact": "+1-555-123-4567"
  },
  "tags": ["antibiotic", "ENT"],
  "createdAt": "2025-08-30T10:22:45Z"
}
```

**Design notes:**  
- Store `patientName` for quick readability and reporting.
- `appointmentId` links to the SQL appointment while avoiding embedding entire patient/doctor objects that change frequently.
- `pharmacy` is embedded to snapshot context at prescription time.
- `tags` enable ad-hoc filtering (e.g., by drug class).

**Index suggestions:**
- Index on `appointmentId` for quick join-by-id lookups from SQL to Mongo.
- Consider text or multikey indexes on `medication` and `tags` for search use cases.
- TTL index not needed for prescriptions, but could be used for transient logs in other collections if added later.

---

## Justification: SQL vs Mongo Split

- **SQL** (`patients`, `doctors`, `appointments`, `admin`):  
  Strong consistency, constraints, joins, and reporting benefit from normalized tables; data integrity (unique emails, referential keys) is critical for identity, scheduling, and admin flows.

- **MongoDB** (`prescriptions`):  
  Variable, semi-structured data with optional attributes and nested metadata fits document modeling; schema can evolve per medical needs without migrations, while keeping a simple reference to SQL `appointmentId` for traceability.

---

## Commit Instructions

Create the file at repo root:
```sh
echo "<paste markdown here>" > schema-design.md
```

Stage, commit, push:
```sh
git add schema-design.md
git commit -m "Added schema design for Smart Clinic"
git push origin main
```

This structure meets the lab requirements: at least 4 MySQL tables with columns, types, PK/FK, constraints, plus one MongoDB collection with a realistic JSON document and reasoning for the hybrid approach.
