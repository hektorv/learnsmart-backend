# Profile Service

Manages user profiles, preferences, and study goals. Also handles user registration and synchronization with Keycloak.

## Configuration

| Environment Variable | Default | Description |
|----------------------|---------|-------------|
| `PORT` | `8081` | Server port |
| `DB_HOST` | `localhost` | PostgreSQL Host |
| `DB_NAME` | `profile_db` | Database Name |
| `DB_USER` | `postgres` | Database User |
| `DB_PASSWORD` | `postgres` | Database Password |
| `EUREKA_URL` | `http://localhost:8761/eureka/` | Discovery service URL |
| `HOSTNAME` | `localhost` | Hostname |

## Dependencies
- **Data Store**: PostgreSQL
- **Auth Provider**: Keycloak

## API Endpoints

### Authentication & User
- `POST /auth/register` - Register new user (Keycloak + Profile)
- `GET /profiles/me` - Get current user profile
- `PUT /profiles/me` - Update profile details
- `GET /profiles/{userId}` - Get public profile
- `GET /profiles/me/progress` - Get consolidated user progress

### Goals
- `GET /profiles/me/goals` - List user goals
- `POST /profiles/me/goals` - Create new goal
- `PUT /profiles/me/goals/{goalId}` - Update goal
- `PATCH /profiles/me/goals/{goalId}/progress` - Update goal progress
- `POST /profiles/me/goals/{goalId}/complete` - Mark goal as completed

### Audit Logs
- `GET /profiles/me/audit-logs` - Get current user audit logs
- `GET /profiles/{userId}/audit` - (Admin) Get user audit logs
- `GET /profiles/{userId}/goals/{goalId}/audit` - Get specific goal history

### Preferences
- `GET /profiles/me/preferences` - Get study preferences
- `PUT /profiles/me/preferences` - Update study preferences
