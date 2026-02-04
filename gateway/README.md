# API Gateway

Entry point for the LearnSmart backend. Handles routing, authentication, and load balancing for all microservices.

## Configuration

| Environment Variable | Default | Description |
|----------------------|---------|-------------|
| `PORT` | `8762` | Server port |
| `HOSTNAME` | `localhost` | Hostname for Gateway instance |
| `EUREKA_URL` | `http://localhost:8761/eureka/` | Discovery service URL |
| `KEYCLOAK_CLIENT_SECRET` | *(Required)* | Client secret for 'api-gateway' client in Keycloak |
| `ROUTE_TABLES_ENABLED` | `true` | Enable dynamic routing based on discovery |

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