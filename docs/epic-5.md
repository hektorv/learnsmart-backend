# 📘 EPIC 5 — Learning Tracking and Analytics  
**System:** LearnSmart  
**Base microservice:** tracking-service

---

## US-040 | Record Learning Event

**As** the system / platform,  
**I want** to record significant user interaction events,  
**so that** data is stored for learning behavior analysis and adaptivity improvements.

### Endpoint
`POST /events`

### Acceptance Criteria

- The system can record events of type:
  - PAGE_VIEW
  - CONTENT_START
  - CONTENT_COMPLETE
  - EVALUATION_START
  - EVALUATION_END
  - PLAN_GENERATED
  - ACTIVITY_COMPLETE.
- Each event includes:
  - type
  - timestamp
  - user_id
  - entity_id
  - entity_type
  - metadata (JSON).
- Events are stored asynchronously:
  - they do not block the main operation.
- Event payloads are validated before storage.
- Events are immutable once created.
- Supports high concurrency:
  - 1000+ events per second.
- Returns receipt confirmation:
  - `HTTP 202 Accepted`.

---

## US-041 | Query Events

**As** an administrator or analyst,  
**I want** to query events filtered by user, type, or entity,  
**so that** I can analyze usage patterns and make data-driven decisions.

### Endpoint
`GET /events`

### Acceptance Criteria

- Only users with role `ADMIN` or `ANALYST` can query events.
- Available filters:
  - user_id
  - event type
  - entity_id
  - entity_type
  - date range.
- Supports cursor-based pagination for large datasets.
- Allows sorting by timestamp:
  - ascending
  - descending.
- Allows exporting results in:
  - CSV
  - JSON formats.
- Maximum limit of **10,000 records** per query.
- Includes basic aggregations:
  - count by event type
  - count by day.
- A regular user can only view their own events.

---
