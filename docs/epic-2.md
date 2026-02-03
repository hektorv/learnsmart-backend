# 📘 EPIC 2 — Content Management  
**System:** LearnSmart  
**Base microservice:** content-service

---

## US-010 | List Domains

**As** a user,  
**I want** to list the available knowledge domains,  
**so that** I can explore the thematic areas offered by the platform.

### Endpoint
`GET /domains`

### Acceptance Criteria

- The endpoint is accessible to authenticated users.
- Returns a list of domains including:
  - ID
  - name
  - description
  - icon/image
  - number of skills.
- Supports pagination:
  - page
  - size
  - total.
- Allows filtering by name:
  - partial match
  - case-insensitive.
- Allows sorting by:
  - name
  - creation date.
- Only domains with status `published` are returned.
- Response time is under **300 ms** for fewer than 100 domains.

---

## US-011 | Create Domain

**As** an administrator,  
**I want** to create a new knowledge domain,  
**so that** I can expand the catalog of available learning areas.

### Endpoint
`POST /domains`

### Acceptance Criteria

- Only users with role `ADMIN` can access this endpoint.
- Required fields:
  - name (unique)
  - description.
- Optional fields:
  - icon
  - color
  - display order.
- Domain name must be unique:
  - case-insensitive validation.
- The domain is created with status `draft` by default.
- Returns the created domain with its assigned ID.
- If the user lacks permissions:
  - returns `HTTP 403 Forbidden`.
- If the name already exists:
  - returns `HTTP 409 Conflict`.

---

## US-012 | List Skills

**As** a user,  
**I want** to list the skills of a domain,  
**so that** I can understand which competencies I can develop in a specific area.

### Endpoint
`GET /skills`

### Acceptance Criteria

- Requires the domain ID as a parameter.
- Returns a list of skills including:
  - ID
  - name
  - description
  - difficulty level
  - prerequisites.
- Supports pagination.
- Allows filtering by difficulty level:
  - basic
  - intermediate
  - advanced.
- Includes the count of content items associated with each skill.
- If the domain does not exist:
  - returns `HTTP 404 Not Found`.
- Default ordering follows the defined pedagogical sequence.

---

## US-013 | Manage Skill Prerequisites

**As** an administrator,  
**I want** to manage prerequisites between skills,  
**so that** I can define the logical learning order and dependencies between competencies.

### Endpoint
`GET /skills/{id}/prerequisites`  
`PUT /skills/{id}/prerequisites`

### Acceptance Criteria

- Only users with role `ADMIN` can manage prerequisites.
- Allows adding a skill as a prerequisite of another.
- Allows removing an existing prerequisite.
- Validates that dependency cycles are not created:
  - e.g. A → B → C → A.
- Validates that both skills exist.
- A skill may have multiple prerequisites.
- Returns the updated list of prerequisites for the skill.
- If a cycle is detected:
  - returns `HTTP 400 Bad Request` with an explanatory message.

---

## US-014 | List Content Items

**As** a student,  
**I want** to list the content items associated with a skill,  
**so that** I can access the learning materials that help me develop that competency.

### Endpoint
`GET /content-items`

### Acceptance Criteria

- Requires the skill ID as a parameter.
- Returns a list of content items including:
  - ID
  - title
  - type (video, text, exercise)
  - estimated duration
  - difficulty.
- Supports pagination and filtering by content type.
- Default ordering follows the pedagogical sequence.
- Indicates whether the user has completed each content item:
  - requires authentication.
- Only content with status `published` is shown.
- If the skill does not exist:
  - returns `HTTP 404 Not Found`.

---

## US-015 | Create / Edit Content

**As** an administrator or content creator,  
**I want** to create or edit learning content,  
**so that** the platform’s educational materials remain up to date.

### Endpoints
`POST /content-items`  
`PUT /content-items/{id}`

### Acceptance Criteria

- Only users with role `ADMIN` or `CONTENT_CREATOR` can access.
- Create:
  - required fields:
    - title
    - type
    - associated skill
    - content or URL.
- Edit:
  - any field can be modified except the ID.
- Supported content types:
  - VIDEO
  - TEXT
  - EXERCISE
  - QUIZ
  - EXTERNAL_RESOURCE.
- Validates that the associated skill exists.
- Supports content versioning:
  - maintains change history.
- Content is created with status `draft` and requires explicit publication.
- Returns the created or updated content with its ID.

---
