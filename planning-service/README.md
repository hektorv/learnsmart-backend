# Planning Service

Core service responsible for generating, managing, and updating personalized learning plans.

## Configuration

| Environment Variable | Default | Description |
|----------------------|---------|-------------|
| `PORT` | `8083` | Server port |
| `DB_HOST` | `localhost` | PostgreSQL Host |
| `DB_NAME` | `planning_db` | Database Name |
| `DB_USER` | `postgres` | Database User |
| `DB_PASSWORD` | `postgres` | Database Password |
| `EUREKA_URL` | `http://localhost:8761/eureka/` | Discovery service URL |
| `HOSTNAME` | `localhost` | Hostname for Eureka registration |
| `KEYCLOAK_INTERNAL_URL` | `http://keycloak:8080` | Internal URL for S2S Keycloak communication |

## Dependencies
- **Data Store**: PostgreSQL
- **External Services**: Content Service, Profile Service, AI Service (via Eureka/Feign)

## API Endpoints

### Learning Plans
- `POST /plans` - Create a new learning plan
- `GET /plans` - List learning plans (optional userId param)
- `GET /plans/{id}` - Get plan details
- `PATCH /plans/{id}` - Update plan status
- `POST /plans/diagnostics` - Create plan from diagnostic result
- `POST /plans/{id}/replan` - Trigger replanning
- `GET /plans/{id}/replan-triggers` - List active triggers
- `GET /plans/certificates` - List user certificates

### Plan Modules & Activities
- `GET /plans/{planId}/modules` - List modules
- `PATCH /plans/{planId}/modules/{moduleId}` - Update module status
- `GET /plans/{planId}/activities` - List activities
- `PATCH /plans/{planId}/activities/{activityId}` - Update activity status
- `POST /plans/{planId}/modules/{moduleId}/activities` - Add custom activity
