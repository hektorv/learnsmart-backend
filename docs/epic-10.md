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

### US-10-05: Skill Referential Integrity (UUIDs)
*   **Problem**: The `/content/skills` endpoint currently exposes `code` as a lookup/filtering parameter, which may lead to ambiguity or brittle references if domain names/codes change.
*   **Requirement**:
    *   Enforce `domainId` (UUID) as the primary and mandatory foreign key for creating and filtering Skills.
    *   Deprecate/Remove reliance on domain `code` in the `SkillController` APIs (`GET /skills`, `POST /skills`).
    *   Ensure all clients (Planning Service, Frontend) use `domainId` when interacting with Skills.

*   **Acceptance Criteria (Validate via Unit/Integration Tests):**
    - [ ] **AC-5.1 (Create - Success)**: `POST /skills` returns `201 CREATED` when a valid `domainId` (UUID) is provided in the payload.
    - [ ] **AC-5.2 (Create - Failure)**: `POST /skills` returns `400 BAD REQUEST` if `domainId` is missing or null.
    - [ ] **AC-5.3 (Create - Integrity)**: `POST /skills` returns `404 NOT FOUND` if the provided `domainId` does not exist in the database.
    - [ ] **AC-5.4 (Read - Filter)**: `GET /skills?domainId={uuid}` returns only skills associated with that specific domain ID.
    - [ ] **AC-5.5 (Refactor - Cleanup)**: The `ContentItemController` logic that looked up domains by `code` is removed/replaced with `findById(uuid)`.
    - [ ] **AC-5.6 (Unit Test)**: Mock `DomainService` to return empty optional, verify that `createSkill` throws expected exception before saving.

### US-10-06: AI Skill Discovery (Taxonomy Generation)
*   **Problem**: Manually populating a new domain with skills is slow and error-prone.
*   **Requirement**:
    *   Implement `POST /v1/contents/skills` in `ai-service` to generate a list of skills for a given topic/domain.
    *   Implement Orchestrator `POST /domains/{id}/skills/generate` in `content-service`.
    *   **Scope**: Generates "Acquired Skills" (The nodes of the graph).

### US-10-07: AI Prerequisite Linking (Graph Generation)
*   **Problem**: Defining the pedagogical dependency graph (prerequisites) manually is complex.
*   **Requirement**:
    *   Implement `POST /v1/contents/skills/prerequisites` in `ai-service` to determine dependencies between a list of skills.
    *   Implement Orchestrator `POST /domains/{id}/skills/link` in `content-service`.
    *   **Scope**: Generates "Needed Skills" (The edges of the graph).

### US-10-08: AI Assessment Item Generation (Content-Based)
*   **Problem**: Lessons are generated but lack associated assessment items for evaluation.
*   **Requirement**:
    *   Implement `POST /v1/contents/assessment-items` enhancement in `ai-service` to accept `contextText` (lesson body).
    *   Implement `POST /content-items/{id}/assessments/generate` in `content-service` to generate items based on lesson content.
    *   **Scope**: Generates assessment items deterministically from lesson body (US-10-02 alignment).

### US-10-09: AI Skill Tagging (Content Analysis)
*   **Problem**: Generated content items are not automatically linked to relevant skills.
*   **Requirement**:
    *   Implement `POST /v1/contents/skill-tags` in `ai-service` to analyze content and suggest relevant skills.
    *   Implement `POST /content-items/{id}/skills/auto-link` in `content-service` to auto-associate skills.
    *   **Scope**: Analyzes lesson body and links to existing domain skills.

### US-10-02: [Placeholder for Future Refactors]
*   ...
