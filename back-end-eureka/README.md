# Discovery Service (Eureka)

Service registry for the LearnSmart microservices architecture. Enables dynamic discovery and load balancing between services.

## Configuration

These environment variables are specific to the Eureka Discovery Server.

| Environment Variable | Default | Description |
|----------------------|---------|-------------|
| `PORT` | `8761` | Server port |
| `EUREKA_HOST` | `localhost` | Hostname for Eureka instance (used for peer replication) |
| `EUREKA_SELF_PRESERVATION` | `false` | Enable self-preservation mode (prevents mass eviction in case of network partition) |

## Client Configuration (All Other Services)

All other microservices (Profile, Content, Planning, Assessment, Tracking, Gateway) must be configured to register with this Eureka server using the following environment variable:

| Environment Variable | Default | Description |
|----------------------|---------|-------------|
| `EUREKA_URL` | `http://localhost:8761/eureka/` | URL where services register themselves |

## Running Locally

```bash
mvn spring-boot:run
```

Access dashboard at: http://localhost:8761