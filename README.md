# Machine Events Backend 🚀

A high-performance, thread-safe backend service to ingest machine events, deduplicate/update them correctly, and expose analytical statistics over time windows.

This project was built with a strong focus on **correctness under concurrency**, **clear business rules**, and **performance guarantees**.

---

## 1. Architecture

The application follows a clean layered Spring Boot architecture:

     Controller → Service → Repository → Database


### Components
- **Controller**
  - Exposes REST APIs for event ingestion and statistics queries
- **Service Layer**
  - `EventIngestionService`: validation, dedupe/update, persistence
  - `StatsService`: time-window statistics computation
  - `TopDefectLinesService`: aggregation queries
- **Repository**
  - JPA repositories backed by MySQL
- **Database**
  - Single `machine_events` table with indexed fields for fast queries

This separation keeps business logic isolated, testable, and scalable.

---

## 2. Dedupe / Update Logic

Each event is uniquely identified by `eventId`.

### Decision Rules
1. **Same payload (same hash)**  
   → Event is **deduplicated** (ignored)

2. **Different payload + newer receivedTime**  
   → Existing event is **updated**

3. **Different payload + older receivedTime**  
   → Incoming event is **ignored**

### Payload Comparison
- A deterministic `payloadHash` is computed from relevant fields:

       machineId, factoryId, lineId, durationMs, defectCount, eventTime
- Hash comparison avoids deep object comparisons and keeps ingestion fast.

### Why this works
- Ensures **idempotency**
- Handles **out-of-order delivery**
- Prevents race-condition corruption

---

## 3. Thread Safety

The system is thread-safe under concurrent ingestion due to:

### Database Guarantees
- `event_id` is a **PRIMARY KEY**
- Atomic `SELECT → UPDATE/INSERT` within transactions

### Application Guarantees
- Ingestion logic wrapped in `@Transactional`
- Update decisions made **after reading latest persisted state**
- No in-memory shared mutable state

### Verification
A dedicated **concurrent ingestion test** (`ConcurrentIngestionTest`) fires multiple threads ingesting the same eventId and asserts:
- Only **one final record exists**
- No duplicate inserts
- Correct update behavior

---

## 4. Data Model

### Table: `machine_events`

| Column        | Type        | Notes |
|--------------|------------|------|
| event_id     | VARCHAR PK | Unique event identifier |
| machine_id   | VARCHAR    | Indexed |
| factory_id   | VARCHAR    | Indexed |
| line_id      | VARCHAR    | Indexed |
| event_time   | TIMESTAMP  | Used for stats windows |
| received_time| TIMESTAMP  | Used for update decisions |
| duration_ms  | INT        | Must be positive |
| defect_count | INT        | `-1` means unknown |
| payload_hash | VARCHAR    | For dedupe detection |

---

## 5. Performance Strategy (1000 events < 1 second)

Key optimizations applied:

- **Batch ingestion endpoint** (`POST /events/batch`)
- Minimal JSON parsing & validation
- Indexed database columns
- Hash-based deduplication (O(1))
- No per-event locks
- Single transaction per batch

### Result
✅ **1000 events ingested in under 1 second on a standard laptop**

(See `BENCHMARK.md` for measured results)

---

## 6. Edge Cases & Assumptions

Handled explicitly:

- ❌ **Future `eventTime`** → rejected
- ❌ **Negative `durationMs`** → rejected
- ❌ **Missing mandatory fields** → rejected
- ⚠️ `defectCount = -1` → ignored in defect totals
- **Time window logic**
- `start` → inclusive
- `end` → exclusive

Example:

         [start, end)


---

## 7. Setup & Run Instructions

### Prerequisites
- Java 17+
- Maven
- MySQL (local)

### Run locally
```bash
git clone <repo-url>
cd machine-events-backend
mvn spring-boot:run
```

API Endpoints

Ingest events

        POST /events/batch

Query stats

        GET /events/stats?machineId=M-001&start=...&end=...

##8. What I Would Improve With More Time

- Add Kafka for async ingestion

- Add Redis for hot stats caching

- Pagination for large stats queries

- Rate limiting & authentication

- More advanced benchmarking (JMH)

- Schema migration with Flyway


Why This Project Matters

This backend demonstrates:

- Correctness under concurrency

- Real-world ingestion patterns

- Clean domain-driven design

- Production-ready validation and stats logic
