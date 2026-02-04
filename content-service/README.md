# Content Service

Manages educational content, domains, skills, and learning resources.

## Configuration

| Environment Variable | Default | Description |
|----------------------|---------|-------------|
| `PORT` | `8082` | Server port |
| `DB_HOST` | `localhost` | PostgreSQL Host |
| `DB_NAME` | `content_db` | Database Name |
| `DB_USER` | `postgres` | Database User |
| `DB_PASSWORD` | `postgres` | Database Password |
| `EUREKA_URL` | `http://localhost:8761/eureka/` | Discovery service URL |
| `HOSTNAME` | `localhost` | Hostname for Eureka registration |
| `KEYCLOAK_INTERNAL_URL` | `http://keycloak:8080` | Internal URL for S2S Keycloak communication |

## Dependencies
- **Data Store**: PostgreSQL

## API Endpoints

### Content Items
- `GET /content-items` - List content items
- `POST /content-items` - Create content item
- `GET /content-items/{id}` - Get content details
- `PUT /content-items/{id}` - Update content item
- `DELETE /content-items/{id}` - Delete content item
- `POST /content-items/generate` - Generate content via AI
- `POST /content-items/{id}/skills` - Link skills to content

### Domains
- `GET /domains` - List domains
- `POST /domains` - Create domain
- `GET /domains/{id}` - Get domain details

### Skills
- `GET /skills` - List skills
- `POST /skills` - Create skill
- `PUT /skills/{id}` - Update skill
- `GET /skills/{id}/prerequisites` - Get skill prerequisites
- `PUT /skills/{id}/prerequisites` - Set skill prerequisites
