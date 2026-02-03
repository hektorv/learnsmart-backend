# 📘 EPIC 1 — User Management and Profile  
**System:** LearnSmart  
**Base microservice:** profile-service  
**Authentication:** Keycloak (OIDC)

---

## US-001 | User Registration

**As** a new user,  
**I want** to register on the platform,  
**so that** my authentication account and internal learning profile are automatically created.

### Endpoint
`POST /auth/register`

### Acceptance Criteria

- The user registers by providing:
  - email
  - password
  - first name
  - last name
- The system creates an account in **Keycloak** using the provided credentials.
- An internal profile is automatically generated and linked to the `keycloak_user_id`.
- The user receives a registration confirmation email.
- If the email already exists, the system responds with:
  - `HTTP 409 Conflict`
  - a descriptive error message.
- The password meets minimum security policies:
  - minimum 8 characters
  - at least one uppercase letter
  - at least one number.
- The process is **transactional**:
  - if internal profile creation fails, the Keycloak account is rolled back.

---

## US-002 | View Profile

**As** an authenticated user,  
**I want** to view my profile,  
**so that** I can see my personal data, preferences, and current learning status.

### Endpoint
`GET /profiles/me`

### Acceptance Criteria

- Requires a valid JWT token.
- Returns:
  - first name
  - email
  - registration date
  - preferences (language, time zone, etc.).
- Includes the user’s active learning goals.
- Includes basic statistics:
  - courses in progress
  - completed courses.
- A user can only access their own profile.
- If the token is invalid or expired:
  - returns `HTTP 401 Unauthorized`.
- Response time is under **500 ms**.

---

## US-003 | Update Profile

**As** an authenticated user,  
**I want** to update my profile,  
**so that** I can modify my preferences and personal information.

### Endpoint
`PUT /profiles/me`

### Acceptance Criteria

- The user can modify:
  - first name
  - last name
  - language
  - time zone.
- Changes are correctly persisted.
- Email **cannot** be modified through this endpoint.
- Fields are validated:
  - length
  - format.
- Returns the updated profile.
- An audit event is recorded with the modified fields.
- If validation errors occur:
  - returns `HTTP 400 Bad Request` with details.

---

## US-004 | Create Learning Goal

**As** a student,  
**I want** to create a learning goal,  
**so that** I can define objectives that allow the system to personalize my experience.

### Endpoint
`POST /profiles/me/goals`

### Acceptance Criteria

- The student defines:
  - title
  - description
  - target skill
  - due date (optional).
- The goal is associated with the authenticated user.
- The initial state is `active`.
- The referenced skill is validated against the catalog.
- The goal appears immediately in the user profile.
- There is no limit on active goals per user.
- Returns the created goal with its assigned ID.

---

## US-005 | Update or Deactivate Learning Goal

**As** a student,  
**I want** to update or deactivate a learning goal,  
**so that** I can adjust my objectives based on progress or changing interests.

### Endpoint
`PUT /profiles/me/goals/{goalId}`

### Acceptance Criteria

- The student can modify:
  - title
  - description
  - due date
  - status (`active`, `inactive`, `completed`).
- Only the goal owner can modify it.
- When a goal is deactivated:
  - it no longer influences recommendations.
- When marked as `completed`:
  - the completion date is recorded.
- Goals are not physically deleted (history is preserved).
- Returns the updated goal.
- If the goal does not exist or does not belong to the user:
  - returns `HTTP 404 Not Found`.

---
