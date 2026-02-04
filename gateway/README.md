# API Gateway

Entry point for the LearnSmart backend. Handles routing, authentication, and load balancing for all microservices.

## Global Configuration (All Services)

These variables are common across most microservices to enable connectivity.

| Environment Variable | Default | Description |
|----------------------|---------|-------------|
| `EUREKA_URL` | `http://localhost:8761/eureka/` | URL of the Request Discovery Server |
| `HOSTNAME` | `localhost` | Hostname of the service instance (for Eureka registration) |
| `KEYCLOAK_INTERNAL_URL` | `http://keycloak:8080` | Internal URL for S2S Keycloak communication (e.g. `http://keycloak-internal.railway.internal:8080`) |

## Service-Specific Configuration

Below are the environment variables required for each specific service to start correctly.

### 1. API Gateway
| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `8762` | Server Port |
| `KEYCLOAK_CLIENT_SECRET` | *(Required)* | Client Secret for 'api-gateway' client |
| `ROUTE_TABLES_ENABLED` | `true` | Enable dynamic routing |
| `FRONTEND_URL` | `http://localhost:5173` | Allowed Origin for CORS (e.g. `https://my-app.up.railway.app`) |
| `KEYCLOAK_PUBLIC_URL` | `http://localhost:8080` | Public URL for User Redirects (e.g. `https://keycloak-production.up.railway.app`) |
| `KEYCLOAK_INTERNAL_URL` | `http://keycloak:8080` | Internal URL for S2S Keycloak communication (e.g. `http://keycloak-internal.railway.internal:8080`) |
| `KC_HOSTNAME` | *(Required)* | Hostname for Keycloak itself (e.g. `keycloak-production.up.railway.app`). Crucial for `iss` claim consistency. |

### 2. Discovery Service (Eureka)
| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `8761` | Server Port |
| `EUREKA_HOST` | `localhost` | Hostname for Eureka instance |

### 3. Profile Service
| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `8081` | Server Port |
| `DB_HOST` | `localhost` | Database Host (e.g., `profile-db` in Docker) |
| `DB_NAME` | `profile_db` | Database Name |
| `DB_USER` | `postgres` | Database User |
| `DB_PASSWORD` | `postgres` | Database Password |

### 4. Content Service
| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `8082` | Server Port |
| `DB_HOST` | `localhost` | Database Host |
| `DB_NAME` | `content_db` | Database Name |
| `DB_USER` | `postgres` | Database User |
| `DB_PASSWORD` | `postgres` | Database Password |

### 5. Planning Service
| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `8083` | Server Port |
| `DB_HOST` | `localhost` | Database Host |
| `DB_NAME` | `planning_db` | Database Name |
| `DB_USER` | `postgres` | Database User |
| `DB_PASSWORD` | `postgres` | Database Password |

### 6. Assessment Service
| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `8084` | Server Port |
| `DB_HOST` | `localhost` | Database Host |
| `DB_NAME` | `assessment_db` | Database Name |
| `DB_USER` | `postgres` | Database User |
| `DB_PASSWORD` | `postgres` | Database Password |

### 7. Tracking Service
| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `8085` | Server Port |
| `DB_HOST` | `localhost` | Database Host |
| `DB_NAME` | `tracking_db` | Database Name |
| `DB_USER` | `postgres` | Database User |
| `DB_PASSWORD` | `postgres` | Database Password |

### 8. AI Service (Python)
| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `8000` | Server Port (FastAPI) |
| `OPENAI_API_KEY` | *(Required)* | OpenAI API Key |
| `OPENAI_MODEL` | `gpt-3.5-turbo` | LLM Model to use |

### 9. Keycloak (Identity Provider)
| Variable | Default | Description |
|----------|---------|-------------|
| `KC_DB` | `postgres` | Database Vendor |
| `KC_DB_URL` | `jdbc:postgresql://...` | Database Connection URL |
| `KC_DB_USERNAME` | `keycloak` | Database User |
| `KC_DB_PASSWORD` | `password` | Database Password |
| `KEYCLOAK_ADMIN` | `admin` | Admin Username |
| `KEYCLOAK_ADMIN_PASSWORD` | `admin` | Admin Password |

## Key Routes

- `/auth/**` -> Profile Service (Auth)
- `/profiles/**` -> Profile Service
- `/content/**` -> Content Service
- `/planning/**` -> Planning Service
- `/assessment/**` -> Assessment Service
- `/tracking/**` -> Tracking Service

## Running Locally

```bash
mvn spring-boot:run
```