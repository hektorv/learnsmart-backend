# 📘 EPIC 9 — Profile Service Enhancements & Gap Closure

**System:** LearnSmart  
**Base microservice:** profile-service  
**Scope:** Address gaps and missing features identified during Epic 1 validation

---

## US-090 | User Registration Confirmation Email

**As** a new user,  
**I want** to receive a confirmation email after registration,  
**so that** I can verify my email address and activate my account.

### Endpoint
Enhancement to: `POST /auth/register`

### Acceptance Criteria

- After successful registration, the system sends a confirmation email to the provided address.
- The email includes:
  - welcome message
  - confirmation link with unique token
  - token expiration time (24 hours).
- The user account remains in `pending_verification` status until confirmed.
- The confirmation link redirects to:
  - `POST /auth/confirm?token={token}`
- If the token is valid:
  - account status changes to `active`
  - returns `HTTP 200 OK`.
- If the token is expired or invalid:
  - returns `HTTP 400 Bad Request`.
- Unverified accounts cannot access protected resources:
  - login attempts return `HTTP 403 Forbidden` with message "Email not verified".

---

## US-091 | Enhanced Password Validation

**As** a platform,  
**I want** to enforce strict password policies,  
**so that** user accounts are protected against weak credentials.

### Endpoint
Enhancement to: `POST /auth/register`

### Acceptance Criteria

- Password validation enforces:
  - minimum 8 characters ✅ (already implemented)
  - at least one uppercase letter
  - at least one lowercase letter
  - at least one number
  - at least one special character (e.g., `!@#$%^&*`).
- Validation errors return `HTTP 400 Bad Request` with detailed messages:
  - "Password must contain at least one uppercase letter"
  - "Password must contain at least one number"
  - etc.
- Password strength indicator is provided to the frontend (optional):
  - weak / medium / strong.
- Common passwords (from known lists) are rejected.

---

## US-092 | Course Progress Statistics

**As** an authenticated user,  
**I want** to view my course progress statistics,  
**so that** I can track my learning achievements.

### Endpoint
`GET /profiles/me` (enhanced response)

### Acceptance Criteria

- The profile response includes:
  - `activePlansCount` — number of active learning plans
  - `completedModulesCount` — number of completed modules across all plans
  - `totalStudyHours` — accumulated study time
  - `assessmentsCompleted` — number of completed assessment sessions
  - `currentStreak` — consecutive days of activity.
- Statistics are calculated by querying:
  - `planning-service` for plan/module data
  - `assessment-service` for assessment data
  - `tracking-service` for activity events.
- Statistics are cached for performance:
  - refresh interval: 5 minutes.
- If external services are unavailable:
  - return partial data with `null` for unavailable fields
  - do not fail the entire request.
- Response time remains under **500 ms**.

---

## US-093 | Skill Validation Against Catalog

**As** a student,  
**I want** my learning goals to reference valid skills from the catalog,  
**so that** the system can properly plan and track my progress.

### Endpoint
Enhancement to: `POST /profiles/me/goals`

### Acceptance Criteria

- When creating a goal, the system validates:
  - `domain` exists in `content-service`
  - `targetLevel` is valid for the specified skill.
- Validation is performed via synchronous call:
  - `GET /skills?domain={domain}` to `content-service`.
- If the skill does not exist:
  - returns `HTTP 400 Bad Request`
  - with message: "Skill '{skillName}' not found in domain '{domain}'".
- If the `content-service` is unavailable:
  - log warning
  - allow goal creation (graceful degradation)
  - flag goal as `pending_validation`.
- A background job periodically validates `pending_validation` goals.

---

## US-094 | Profile Update Audit Trail

**As** a compliance officer,  
**I want** all profile modifications to be audited,  
**so that** we can track changes for security and regulatory purposes.

### Endpoint
Enhancement to: `PUT /profiles/me`

### Acceptance Criteria

- Every profile update generates an audit event:
  - `event_type`: `PROFILE_UPDATED`
  - `user_id`
  - `changed_fields`: list of modified fields
  - `old_values`: previous values (JSON)
  - `new_values`: new values (JSON)
  - `timestamp`
  - `ip_address` (from request)
  - `user_agent`.
- Audit events are sent asynchronously to `tracking-service`:
  - `POST /events`.
- Audit events do not block the update operation.
- If tracking service fails:
  - log error
  - do not rollback the profile update.
- Administrators can query audit logs:
  - `GET /profiles/{userId}/audit-log`.

---

## US-095 | Soft Delete for Learning Goals

**As** a student,  
**I want** my learning goal history to be preserved,  
**so that** I can review past objectives and track my learning journey.

### Endpoint
Enhancement to: `DELETE /profiles/me/goals/{goalId}`

### Acceptance Criteria

- Goals are **soft deleted** instead of physically removed:
  - `isDeleted` flag set to `true`
  - `deletedAt` timestamp recorded.
- Deleted goals are excluded from default queries:
  - `GET /profiles/me/goals` returns only non-deleted goals.
- A dedicated endpoint allows viewing deleted goals:
  - `GET /profiles/me/goals?includeDeleted=true`.
- Deleted goals do not influence:
  - plan generation
  - recommendations.
- Administrators can permanently delete goals:
  - `DELETE /admin/goals/{goalId}?permanent=true`.
- Soft-deleted goals can be restored:
  - `POST /profiles/me/goals/{goalId}/restore`.

---

## US-096 | Goal Completion Tracking

**As** a student,  
**I want** to mark my learning goals as completed,  
**so that** the system records when I achieved my objectives.

### Endpoint
Enhancement to: `PUT /profiles/me/goals/{goalId}`

### Acceptance Criteria

- Add `status` field to `UserGoal` entity:
  - `ACTIVE`
  - `PAUSED`
  - `COMPLETED`
  - `ABANDONED`.
- Add `completedAt` timestamp field.
- When updating a goal status to `COMPLETED`:
  - `completedAt` is automatically set to current timestamp
  - `isActive` is set to `false`.
- Completed goals trigger:
  - tracking event: `GOAL_COMPLETED`
  - optional notification to the user.
- Statistics in profile include:
  - `completedGoalsCount`
  - `averageDaysToComplete`.
- Completed goals can be reopened:
  - changing status from `COMPLETED` to `ACTIVE` clears `completedAt`.

---

## US-097 | HTTP Status Code Standardization

**As** a developer,  
**I want** consistent HTTP status codes across all endpoints,  
**so that** error handling is predictable.

### Scope
All `profile-service` endpoints

### Acceptance Criteria

- Duplicate email on registration:
  - returns `HTTP 409 Conflict` (not generic `IllegalArgumentException`)
  - with structured error response:
    ```json
    {
      "status": 409,
      "error": "Conflict",
      "message": "Email already registered",
      "timestamp": "2026-02-03T15:38:00Z"
    }
    ```
- Resource not found:
  - returns `HTTP 404 Not Found` (not `IllegalArgumentException`).
- Validation errors:
  - return `HTTP 400 Bad Request` with field-level details.
- Unauthorized access:
  - returns `HTTP 401 Unauthorized`.
- Forbidden access (valid token, insufficient permissions):
  - returns `HTTP 403 Forbidden`.
- Implement global `@ControllerAdvice` exception handler.
- All exceptions are logged with correlation ID.

---

## US-098 | User Profile API Performance Optimization

**As** a user,  
**I want** fast profile access,  
**so that** my experience is smooth and responsive.

### Endpoint
`GET /profiles/me`

### Acceptance Criteria

- Implement database indexes:
  - `user_profiles(auth_user_id)` — unique index
  - `user_profiles(email)` — unique index
  - `user_goals(user_id, is_active)` — composite index.
- Add Redis caching layer:
  - cache key: `profile:{userId}`
  - TTL: 5 minutes
  - invalidate on profile update.
- Lazy loading for related entities:
  - goals and preferences loaded on demand.
- Response time consistently under **500 ms**:
  - measured at 95th percentile.
- Connection pooling configured:
  - min: 10
  - max: 50 connections.

---

## US-099 | Content Service - Rol CONTENT_CREATOR

**As** a content creator,  
**I want** to manage learning content without requiring full admin privileges,  
**so that** content authors can work independently.

### Endpoint
Enhancement to: `POST/PUT /content-items`, `POST/PUT /domains`

### Acceptance Criteria

- Create new role `CONTENT_CREATOR` in Keycloak configuration.
- Update `@PreAuthorize` annotations to accept:
  - `hasAnyRole('ADMIN', 'CONTENT_CREATOR')`
- Content creators can:
  - create/edit content items
  - create/edit skills
  - manage skill-content associations.
- Content creators **cannot**:
  - delete domains
  - modify system-wide settings
  - manage user permissions.
- Audit all content creation events with creator ID.

---

## US-0100 | Content Versioning System

**As** an administrator,  
**I want** to maintain a version history of content,  
**so that** I can track changes and rollback if needed.

### Endpoint
Enhancement to: `PUT /content-items/{id}`

### Acceptance Criteria

- Add `ContentItemVersion` entity:
  - `id`, `contentItemId`, `version`, `title`, `description`, `metadata`
  - `createdAt`, `createdBy`
- Every content update creates a new version record.
- Maximum 10 versions per content item (older versions auto-archived).
- New endpoint: `GET /content-items/{id}/versions`
  - returns version history.
- New endpoint: `POST /content-items/{id}/revert/{versionId}`
  - restores previous version.
- Version changes trigger audit event.

---

## US-0101 | Skill-Based Content Filtering

**As** a student,  
**I want** to filter content items by skill,  
**so that** I can find relevant learning materials.

### Endpoint
Enhancement to: `GET /content-items`

### Acceptance Criteria

- Add `skillId` query parameter (in addition to `domainId`).
- If `skillId` provided:
  - query `content_item_skills` join table
  - return only items associated with that skill.
- Include `weight` in response:
  - indicates skill relevance (0.0-1.0).
- Support multiple skill IDs:
  - `skillId=uuid1,uuid2` returns items matching ANY skill.
- If skill doesn't exist:
  - return `HTTP 404 Not Found`.

---

## US-0102 | User Content Completion Tracking

**As** a student,  
**I want** to see which content I've completed,  
**so that** I can track my progress.

### Endpoint
Enhancement to: `GET /content-items`

### Acceptance Criteria

- Query `tracking-service` for user completion data:
  - `GET /events?userId={id}&type=CONTENT_COMPLETE`
- Enrich content item response with:
  - `completed`: boolean
  - `completedAt`: timestamp
  - `timeSpent`: minutes
- Implement caching to avoid N+1 queries:
  - batch query all completed items for user upfront.
- If tracking service fails:
  - return items without completion data (graceful degradation)
  - log warning.
- Response time target: < 500ms.

---

## US-0103 | Pedagogical Ordering

**As** a student,  
**I want** content ordered by pedagogical sequence,  
**so that** I learn in a logical progression.

### Endpoint
Enhancement to: `GET /skills`, `GET /content-items`

### Acceptance Criteria

- Add `displayOrder` field to `Skill` and `ContentItem` entities.
- Default ordering in GET endpoints:
  - `ORDER BY displayOrder ASC, createdAt ASC`.
- Allow overriding with `?sort=name` or `?sort=difficulty`.
- Administrators can set custom `displayOrder`:
  - `PUT /skills/{id}` accepts `displayOrder`
  - `PUT /content-items/{id}` accepts `displayOrder`.
- Skills with prerequisites appear after their dependencies.

---

## US-0104 | Domain Enrichment (Icon, Color, Metrics)

**As** a user,  
**I want** rich domain information with visual elements,  
**so that** the interface is more engaging.

### Endpoint
Enhancement to: `GET /domains`, `POST/PUT /domains`

### Acceptance Criteria

- Add fields to `Domain` entity:
  - `iconUrl`: URL to domain icon
  - `color`: hex color code (e.g., "#3B82F6")
  - `displayOrder`: integer for sorting.
- Update `GET /domains` response to include:
  - `skillCount`: number of skills in domain
  - aggregated from `COUNT(skills.id)`.
- Update DTOs to support new fields.
- Validate `color` format (regex: `^#[0-9A-F]{6}$`).
- Icon URLs must be HTTPS.

---

## US-0105 | Difficulty-Based Skill Filtering

**As** a user,  
**I want** to filter skills by difficulty level,  
**so that** I can find content appropriate to my level.

### Endpoint
Enhancement to: `GET /skills`

### Acceptance Criteria

- Add `level` query parameter:
  - accepts: `basic`, `intermediate`, `advanced`.
- Filter skills by `level` field in database.
- Support multiple levels:
  - `level=basic,intermediate` (OR logic).
- Include difficulty distribution in response metadata:
  - `{"basic": 12, "intermediate": 8, "advanced": 5}`.
- Default behavior (no filter) returns all levels.

---

## US-0106 | HTTP 409 for Duplicate Resources

**As** a developer,  
**I want** proper HTTP 409 Conflict responses,  
**so that** API behavior is RESTful and predictable.

### Scope
All `content-service` endpoints

### Acceptance Criteria

- Duplicate domain code:
  - throw `DuplicateResourceException`
  - caught by `@ControllerAdvice`
  - returns `HTTP 409 Conflict` with message.
- Duplicate skill code within same domain:
  - returns `HTTP 409 Conflict`.
- Global exception handler includes:
  - `status`: 409
  - `error`: "Conflict"
  - `message`: descriptive text
  - `timestamp`
  - `path`: request URI.

---

## US-0107 | Automatic Replanning Triggers

**As** the planning system,  
**I want** to automatically trigger replanning based on student behavior,  
**so that** learning paths remain optimal without manual intervention.

### Endpoints
Background jobs / Event listeners

### Acceptance Criteria

- Implement event listeners for:
  - `MODULE_COMPLETED` event from tracking-service
  - `ASSESSMENTFAILED` (3+ attempts) from assessment-service
  - `GOAL_UPDATED` from profile-service
- Scheduled job checks for inactivity:
  - runs daily
  - identifies users with no activity for 7+ days
  - triggers replanning for affected active plans
- Each trigger calls `POST /plans/{id}/replan` with appropriate reason
- Replanning is logged in `PlanReplanHistory` with trigger source
- User receives notification (email/in-app) about plan update
- User can disable automatic replanning in preferences

---

## US-0108 | User Replan Approval Workflow

**As** a student,  
**I want** to review and approve/reject replanning suggestions,  
**so that** I remain in control of my learning path.

### Endpoints
- `GET /plans/{id}/replan-suggestions`
- `POST /plans/{id}/replan-suggestions/{suggestionId}/approve`
- `POST /plans/{id}/replan-suggestions/{suggestionId}/reject`

### Acceptance Criteria

- When replanning is triggered, create `PlanReplanSuggestion` entity:
  - status: `pending`, `approved`, `rejected`
  - proposed plan structure (JSON)
  - change summary from AI
- User receives notification of pending suggestion
- Approval applies the suggested plan
- Rejection keeps current plan, logs reason
- Suggestions expire after 7 days (auto-reject)
- User can view comparison: current vs. proposed plan

---

## US-0109 | Progress Calculation and Indicators

**As** a student,  
**I want** to see my progress percentage and next steps,  
**so that** I stay motivated and know what to do next.

### Endpoints
Enhancement to: `GET /plans/{planId}/modules`, `GET /plans/{planId}`

### Acceptance Criteria

- Calculate module progress:
  - `progressPercentage` = (completed activities / total activities) * 100
- Calculate plan progress:
  - `overallProgress` = (completed modules / total modules) * 100
  - or weighted by estimated hours
- Response includes:
  - `currentModuleId`: first module with status != completed
  - `nextActivityId`: first pending activity in current module
  - `estimatedCompletionDate`: based on study preferences and remaining hours
- Cache calculations (5 min TTL)
- Update on activity completion events

---

## US-0110 | Activity Completion Timestamps

**As** a system,  
**I want** to track when activities are completed and time spent,  
**so that** I can measure engagement and improve recommendations.

### Endpoint
Enhancement to: `PATCH /plans/{planId}/activities/{activityId}`

### Acceptance Criteria

- Add fields to `PlanActivity`:
  - `startedAt`: timestamp
  - `completedAt`: timestamp
  - `actualMinutesSpent`: integer
- When activity status changes to `in_progress`:
  - set `startedAt` to current time
- When status changes to `completed`:
  - set `completedAt` to current time
  - calculate `actualMinutesSpent` = (completedAt - startedAt) in minutes
- Send tracking event to tracking-service:
  - `ACTIVITY_COMPLETED` with metadata
- Update module/plan status automatically:
  - if all module activities completed → module = completed
  - if all plan modules completed → plan = completed

---

## US-0111 | Skill Prerequisite Validation in Planning

**As** the planning system,  
**I want** to validate skill prerequisites when generating plans,  
**so that** students learn foundational skills before advanced ones.

### Endpoint
Enhancement to: AI service `POST /v1/plans/generate`

### Acceptance Criteria

- Fetch skill graph from content-service:
  - `GET /skills/{id}/prerequisites`
- Include prerequisite data in AI request:
  - `skillGraph`: map of skill → prerequisites[]
- AI response respects prerequisite order:
  - prerequisite skills appear in earlier modules
- Validation post-generation:
  - verify no skill appears before its prerequisites
  - if violation found, log warning and attempt re-ordering
- Option to override for advanced users

---

## US-0112 | Diagnostic-Based Level Detection

**As** the planning system,  
**I want** to determine student level via diagnostic assessment,  
**so that** generated plans match their actual capability.

### Endpoints
- `POST /plans/diagnostics` (existing)
- Enhancement to: `POST /plans`

### Acceptance Criteria

- Before generating first plan:
  - system checks if user has `initialMastery` data
  - if not, redirect to diagnostic test
- Diagnostic test evaluates target domain skills:
  - adaptive difficulty based on responses
  - generates `skillId → masteryLevel (0-100)` map
- Store results in assessment-service:
  - `user_skill_mastery` table
-AI plan generation includes mastery data:
  - skip modules for high-mastery skills
  - add reinforcement for low-mastery skills
- User can retake diagnostic to update level

---

## US-0113 | Session Management Enhancements

**As** a student,  
**I want** robust session management with duration limits and resume capability,  
**so that** my assessment experience is smooth and flexible.

### Endpoints
Enhancement to: `POST /assessments/sessions`, `GET /assessments/sessions/{sessionId}`

### Acceptance Criteria

- Add `maxDurationMinutes` field to `AssessmentSession` (default: 30)
- Add `expiresAt` calculated field: `startedAt + maxDurationMinutes`
- Validation on session creation:
  - check for existing active session for user
  - if exists, return `HTTP 409 Conflict` with option to resume or cancel
- New endpoint: `POST /assessments/sessions/{sessionId}/cancel`
  - marks session as `cancelled`
- Session expiration handling:
  - middleware checks `expiresAt` on every request
  - if expired, return `HTTP 410 Gone` with message
- On session creation, return:
  - `firstItem`: first adaptive question
  - `remainingTimeMs`: calculated time left

---

## US-0114 | IRT/CAT Algorithm Implementation

**As** the assessment system,  
**I want** to use Item Response Theory (IRT) for mastery calculation,  
**so that** skill levels are scientifically accurate.

### Scope
`assessment-service`, `ai-service`

### Acceptance Criteria

- Replace heuristic mastery update (+0.1/-0.05) with IRT model:
  - Implement 3-parameter logistic (3PL) model
  - parameters: difficulty ($b$), discrimination ($a$), guessing ($c$)
- Store IRT parameters in `AssessmentItem`:
  - `difficulty`: double
  - `discrimination`: double
  - `guessing`: double (default: 0.25 for 4-option MCQ)
- Calculate mastery ($\theta$) using maximum likelihood estimation (MLE)
- Update mastery after each response using Bayesian update
- AI item selection uses $\theta$ to select optimal difficulty:
  - item difficulty $≈ \theta ± 0.5$
- Termination criteria:
  - standard error of $\theta < 0.3$
  - or 20 items administered

---

## US-0115 | Item Deduplication in Sessions

**As** the assessment system,  
**I want** to prevent repeating items within a session,  
**so that** assessments are fair and varied.

### Endpoint
Enhancement to: `GET /assessments/sessions/{sessionId}/next-item`

### Acceptance Criteria

- Track presented items in `AssessmentSession`:
  - new field: `presentedItemIds`: List<UUID>
- Before calling AI service:
  - include `presentedItemIds` in request
  - or implement post-filter
- AI service excludes presented items from selection
- If all items exhausted:
  - automatically finalize session with status `completed`
  - return `HTTP 200` with `{hasNext: false, sessionComplete: true}`

---

## US-0116 | Session Progress Indicators

**As** a student,  
**I want** to see my progress through the assessment,  
**so that** I know how much remains.

### Endpoints
Enhancement to: `GET /assessments/sessions/{sessionId}/next-item`, `GET /assessments/sessions/{sessionId}`

### Acceptance Criteria

- Response includes:
  - `currentQuestionNumber`: count of answered items + 1
  - `estimatedTotalQuestions`: based on CAT convergence (adaptive estimate)
  - `progressPercentage`: (currentQuestion / estimatedTotal) * 100
- Update `estimatedTotal` dynamically as session progresses
- Show `remainingTimeMs` on each response
- On final item:
  - indicate `isFinalItem: true`

---

## US-0117 | On-Demand Additional Feedback

**As** a student,  
**I want** to request alternative explanations for feedback,  
**so that** I can understand concepts from different angles.

### Endpoint
- `POST /assessments/responses/{responseId}/feedback/regenerate`

### Acceptance Criteria

- Student can request new feedback for any saved response
- Request includes optional parameter:
  - `style`: "simpler", "more-detailed", "analogy", "example"
- System calls AI service with:
  - previous feedback
  - requested style
  - item context
- New feedback is appended to `UserItemResponse` as `additionalFeedback`: List<String>
- Limit: 3 regenerations per response
- Track regeneration count to prevent abuse

---

## US-0118 | Mastery Trend Analysis

**As** a student,  
**I want** to see how my skill mastery evolves over time,  
**so that** I can track my learning trajectory.

### Endpoint
Enhancement to: `GET /users/{userId}/skill-mastery`

### Acceptance Criteria

- Add `UserSkillMasteryHistory` entity:
  - `userId`, `skillId`, `mastery`, `timestamp`
  - records historical mastery snapshots
- On mastery update, save history entry
- Response includes `trend` field:
  - "improving": last 3 assessments show increase
  - "stable": variance < 0.05
  - "declining": last 3 assessments show decrease
- Calculate trend using linear regression over last 30 days
- Response includes:
  - `attemptCount`: total assessments for skill
  - `lastAssessmentDate`: most recent evaluation

---

## US-0119 | Low Mastery Skill Highlighting

**As** a student,  
**I want** to see which skills need attention,  
**so that** I can focus my study efforts.

### Endpoint
Enhancement to: `GET /users/{userId}/skill-mastery`

### Acceptance Criteria

- Response includes `needsAttention`: boolean
  - true if `mastery < 0.5` and `attempts >= 2`
- Sort results by:
  - needsAttention (true first)
  - then by mastery ASC
- Add filter parameter: `?needsAttention=true`
  - returns only skills requiring focus
- Response includes `recommendedActions`:
  - "review-content": links to related content items
  - "practice-more": suggests assessment retry
  - "adjust-plan": recommends replanning

---

## US-0120 | Peer Comparison Analytics

**As** a student,  
**I want** to compare my skill levels with anonymized peer averages,  
**so that** I can contextualize my progress.

### Endpoint
- `GET /users/{userId}/skill-mastery/peer-comparison`

### Acceptance Criteria

- Calculate anonymized peer averages:
  - group users by similar profile (age group, domain)
  - compute average mastery per skill (exclude outliers)
- Response includes for each skill:
  - `userMastery`: student's level
  - `peerAverage`: anonymized average
  - `percentile`: student's ranking (0-100)
- Privacy safeguards:
  - require minimum 10 peers per comparison
  - if fewer, return `null` for peer data
- Cache peer averages (1 hour TTL)
- Response includes disclaimer about data anonymization

---

## US-0121 | Subtopic Mastery Breakdown

**As** a student,  
**I want** to see mastery broken down by subtopics within a skill,  
**so that** I can identify specific knowledge gaps.

### Endpoint
- `GET /users/{userId}/skill-mastery/{skillId}/subtopics`

### Acceptance Criteria

- Extend`AssessmentItem` to include:
  - `subtopicTags`: List<String>
- Calculate mastery per subtopic:
  - filter responses by subtopic tag
  - apply IRT algorithm per subset
- Response includes array of:
  - `subtopic`: name
  - `mastery`: level (0-1)
  - `itemCount`: assessments in this subtopic
- Sort by mastery ASC (weakest first)
- Requires minimum 3 items per subtopic for reliable estimate

---

## US-0122 | Positive Reinforcement for Correct Answers

**As** a student,  
**I want** encouraging feedback when I answer correctly,  
**so that** I feel motivated to continue learning.

### Endpoint
Enhancement to: `POST /assessments/sessions/{sessionId}/responses`

### Acceptance Criteria

- When `isCorrect = true`, generate positive feedback:
  - Call AI service with `feedbackType: "positive-reinforcement"`
  - AI returns encouraging message (e.g., "Great job! You've mastered...")
- Vary feedback to avoid repetition:
  - rotate between different tones (enthusiastic, supportive, factual)
- For mastery > 0.8 on skill:
  - add achievement message: "You're becoming an expert in {skill}!"
- Include optional "did you know?" fact related to topic
- Keep feedback concise (< 50 words)

---

## US-0123 | Event Payload Validation

**As** the tracking system,  
**I want** to validate event payloads before storage,  
**so that** only well-formed data enters the analytics pipeline.

### Endpoint
Enhancement to: `POST /events`

### Acceptance Criteria

- Define JSON schema per `eventType`:
  - `CONTENT_START`: requires `{contentItemId, startTime}`
  - `CONTENT_COMPLETE`: requires `{contentItemId, completionTime, timeSpentMs}`
  - `EVALUATION_START/END`: requires `{sessionId, ...}`
- Validate payload against schema before save
- If invalid:
  - return `HTTP 400 Bad Request` with error details
  - do NOT save to database
- Log validation failures for monitoring
- Support optional fields via flexible schema

---

## US-0124 | Role-Based Event Access Control

**As** a system administrator,  
**I want** event querying restricted by role,  
**so that** user privacy is protected.

### Endpoint
Enhancement to: `GET /events`

### Acceptance Criteria

- Add `@PreAuthorize` to query endpoint:
  - `hasAnyRole('ADMIN', 'ANALYST')` for unrestricted access
- Regular users (`ROLE_USER`):
  - automatically filter events by their `userId`
  - cannot query other users' data
- Response includes access scope indicator:
  - `accessScope`: "all" (admin) or "self" (user)
- Audit trail for admin queries:
  - log who queried what data

---

## US-0125 | Cursor-Based Pagination

**As** an analyst,  
**I want** cursor-based pagination for large event datasets,  
**so that** queries remain performant.

### Endpoint
Enhancement to: `GET /events`

### Acceptance Criteria

- Add cursor parameter: `?cursor={base64EncodedTimestamp}`
- Response includes:
  - `events`: array of results
  - `nextCursor`: opaque token for next page
  - `hasMore`: boolean
- Cursor encodes:
  - last event timestamp + ID for disambiguation
- Page size configurable (default: 100, max: 1000)
- Backward compatible with offset pagination

---

## US-0126 | Event Export Functionality

**As** an analyst,  
**I want** to export query results in CSV or JSON,  
**so that** I can analyze data in external tools.

### Endpoints
- `GET /events/export?format=csv`
- `GET /events/export?format=json`

### Acceptance Criteria

- Accept same filters as `GET /events`
- CSV format:
  - headers: id, userId, eventType, entityType, entityId, occurredAt, payload
  - flatten JSON payload to separate columns if possible
- JSON format:
  - array of event objects
- Limit export to 10,000 records
- For larger datasets:
  - return `HTTP 413 Payload Too Large`
  - suggest narrowing filters or using pagination
- Async export for large results (>5K):
  - return job ID immediately
  - poll `/events/export-jobs/{jobId}` for status
  - download via GET when ready

---

## US-0127 | Event Aggregations

**As** an analyst,  
**I want** pre-computed aggregations,  
**so that** I can quickly understand usage patterns.

### Endpoint
- `GET /events/aggregations`

### Acceptance Criteria

- Aggregation types:
  - `by-event-type`: count of events per type
  - `by-day`: count of events per day (last 30 days)
  - `by-user`: top 10 active users
- Filters:
  - `dateRange`: from/to
  - `userId`: scope to specific user
  - `eventType`: filter by type
- Response format:
```json
{
  "aggregation": "by-event-type",
  "data": [
    {"eventType": "CONTENT_COMPLETE", "count": 1523},
    {"eventType": "PAGE_VIEW", "count": 8742}
  ]
}
```
- Cache aggregations (15 min TTL)
- Refresh on demand via `?refresh=true`

---

## US-0128 | Query Result Limits

**As** the tracking system,  
**I want** to enforce query result limits,  
**so that** database performance remains stable.

### Endpoint
Enhancement to: `GET /events`

### Acceptance Criteria

- Enforce max 10,000 results per single query
- If query would exceed limit:
  - return first 10,000 results
  - include warning header: `X-Result-Truncated: true`
  - suggest using pagination or narrowing filters
- Track query performance:
  - log queries taking >5 seconds
  - add database indices on common filter fields:
    - `userId`, `eventType`, `occurredAt`, `entityId`

---

## US-0129 | Performance Monitoring

**As** a developer,  
**I want** to monitor tracking service performance,  
**so that** I can ensure it handles 1000+ events/sec.

### Scope
Monitoring infrastructure

### Acceptance Criteria

- Expose metrics endpoint: `/actuator/metrics`
- Track:
  - `events.created.count`: total events recorded
  - `events.created.rate`: events per second
  - `events.query.duration`: P95/P99 query latency
  - `events.async.queue.size`: async task queue depth
- Alert if:
  - async queue > 10,000 (backpressure)
  - event creation rate drops to 0 for >1 min (failure)
  - query P95 latency > 2 seconds
- Load testing documentation:
  - verify 1000 events/sec sustained for 1 hour
  - document database sizing recommendations

---

## US-0130 | Performance Monitoring for AI Service

**As** a system administrator,  
**I want** to monitor AI service response times and success rates,  
**so that** I can ensure SLA compliance.

### Scope
Monitoring infrastructure for `ai-service`

### Acceptance Criteria

- Expose metrics endpoint: `/metrics`
- Track per-endpoint metrics:
  - `ai.generate_plan.duration`: P50/P95/P99 latency
  - `ai.generate_plan.success_rate`: percentage of successful responses
  - `ai.replan.duration`
  - `ai.next_item.duration`
  - `ai.feedback.duration`
- SLA targets:
  - Plan generation:  < 8s (P95)
  - Replanning: < 5s (P95)
  - Next item: < 500ms (P95)
  - Feedback: < 2s (P95)
- Alert if:
  - Any endpoint P95 > 2x SLA target
  - Success rate < 95% for any endpoint
- Dashboard visualization (Grafana)
- Load testing documentation:
  - verify sustained throughput (10 req/sec min)

---

## US-0131 | Follow-Up Feedback Requests

**As** a student,  
**I want** to request alternative explanations for feedback,  
**so that** I can understand concepts from multiple perspectives.

### Endpoint
- `POST /v1/assessments/feedback/regenerate`

### Acceptance Criteria

- Accept same `FeedbackRequest` schema
- Add optional parameter:
  - `previousFeedback`: string
  - `requestedStyle`: "simpler" | "more-detailed" | "analogy" | "example"
- Include context from previous feedback in prompt
- Instruct LLM to:
  - provide different explanation approach
  - avoid repeating exact same content
  - maintain same pedagogical tone
- Limit: 3 regenerations per response (tracked in assessment-service)
- Response time < 2s

---

## Summary

This epic addresses **42 gaps** identified during validation of Epics 1-6:

### Epic 1 Gaps (Profile Service - 9 items)
US-090 through US-098

### Epic 2 Gaps (Content Service - 8 items)
US-099 through US-0106

### Epic 3 Gaps (Planning Service - 6 items)
US-0107 through US-0112

### Epic 4 Gaps (Assessment Service - 10 items)
US-0113 through US-0122

### Epic 5 Gaps (Tracking Service - 7 items)
US-0123 through US-0129

### Epic 6 Gaps (AI Service - 2 items) — **ÉPICA MÁS COMPLETA**
47. Performance monitoring for AI service missing (US-0130)
48. Follow-up feedback requests not available (US-0131)

**Priority:** HIGH  
**Estimated Effort:** 12-16 sprints  
**Dependencies:** 
- Epic 2 (Content Service) for skill validation and content relationships
- Epic 4 (Assessment Service) for mastery data and diagnostic tests
- Epic 5 (Tracking Service) for audit events, completion tracking, and activity events
- Epic 6 (AI Service) for enhanced planning logic, item selection, and feedback
- Keycloak for role management
- Notification system for user alerts
- Analytics infrastructure for peer comparisons and aggregations
- Monitoring stack (Prometheus/Grafana) for performance metrics
- OpenAI API or equivalent LLM provider
