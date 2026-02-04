# Discovery Service (Eureka)

Service registry for the LearnSmart microservices architecture. Enables dynamic discovery and load balancing between services.

## Configuration

| Environment Variable | Default | Description |
|----------------------|---------|-------------|
| `PORT` | `8761` | Server port |
| `EUREKA_HOST` | `localhost` | Hostname for Eureka instance |
| `EUREKA_SELF_PRESERVATION` | `false` | Enable self-preservation mode (prevents mass eviction in case of network partition) |

## Running Locally

```bash
mvn spring-boot:run
```

Access dashboard at: http://localhost:8761