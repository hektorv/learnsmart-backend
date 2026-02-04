# Tracking Service

Captures and analyzes user learning events (e.g., content views, exercises started/completed).

## Configuration

| Environment Variable | Default | Description |
|----------------------|---------|-------------|
| `PORT` | `8085` | Server port |
| `DB_HOST` | `localhost` | PostgreSQL Host |
| `DB_NAME` | `tracking_db` | Database Name |
| `DB_USER` | `postgres` | Database User |
| `DB_PASSWORD` | `postgres` | Database Password |
| `EUREKA_URL` | `http://localhost:8761/eureka/` | Discovery service URL |

## Dependencies
- **Data Store**: PostgreSQL

## API Endpoints

### Events
- `POST /events` - Track a new learning event
- `GET /events` - List raw events (Admin/Internal)

### Analytics
- `GET /analytics/users/{userId}/stats` - Get user learning statistics
- `GET /analytics/users/{userId}/activity` - Get user activity timeline
