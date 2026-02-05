# Assessment Service

Manages assessments, quizzes, and evaluates user skill mastery levels.

## Configuration

| Environment Variable | Default | Description |
|----------------------|---------|-------------|
| `PORT` | `8084` | Server port |
| `DB_HOST` | `localhost` | PostgreSQL Host |
| `DB_NAME` | `assessment_db` | Database Name |
| `DB_USER` | `postgres` | Database User |
| `DB_PASSWORD` | `postgres` | Database Password |
| `EUREKA_URL` | `http://localhost:8761/eureka/` | Discovery service URL |
| `HOSTNAME` | `localhost` | Hostname for Eureka registration |
| `KEYCLOAK_INTERNAL_URL` | `http://keycloak:8080` | Internal URL for S2S Keycloak communication |

## Dependencies
- **Data Store**: PostgreSQL
- **External Services**: Content Service (for questions/skills), AI Service (via Eureka)

## API Endpoints

### Assessment Sessions
- `POST /assessments/sessions` - Start new assessment session
- `GET /assessments/sessions/{sessionId}` - Get session status
- `PUT /assessments/sessions/{sessionId}/status` - Update session status (e.g., complete)

### Questions & Responses
- `GET /assessments/sessions/{sessionId}/next-item` - Get next adaptive question
- `POST /assessments/sessions/{sessionId}/responses` - Submit answer
- `GET /assessments/sessions/{sessionId}/responses` - Review responses

### Mastery
- `GET /users/{userId}/skill-mastery` - Get user skill mastery levels

### Assessment Items
- `GET /assessment-items` - List assessment items
- `POST /assessment-items` - Create assessment item
- `GET /assessment-items/{id}` - Get item details
