# Epic 10: Architecture Hardening & Refactoring

**Goal**: Elevate the MVP codebase to production-grade robustness by addressing structural weaknesses and implementing strict referential integrity.

## User Stories / Technical Tasks

### US-10-01: Domain Referential Integrity (UUIDs)
*   **Problem**: Currently, `ProfileService` and `AI-Service` refer to Knowledge Domains by their string code (e.g., `react-dev`). This is brittle; renaming a domain in `ContentService` breaks all linked User Goals and Plans.
*   **Requirement**:
    *   Refactor `ContentService` to expose `domainId` (UUID) as the primary key for foreign references.
    *   Update `ProfileService` (Learning Goals) to store `domainId`.
    *   Update `AI-Service` to validate against `domainId`.
    *   Ensure validation endpoints accept UUIDs.

### US-10-02: [Placeholder for Future Refactors]
*   ...
