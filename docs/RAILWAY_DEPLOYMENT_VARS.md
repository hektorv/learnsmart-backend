# Railway Deployment Variables

Copy the content of each block intoland paste it into the "Raw Editor" of the corresponding Service in Railway.

## 1. Shared Database (PostgreSQL)
*Create a PostgreSQL service first. Railway provides these variables automatically to other services if you link them, OR you can manually copy them from the Postgres "Connect" tab.*
*Common variables provided by Railway Postgres:*
- `PGHOST`
- `PGUSER`
- `PGPASSWORD`
- `PGPORT`
- `PGDATABASE`

---

## 2. Discovery Service (Eureka)
**Service Name:** `eureka`

```env
PORT=8761
EUREKA_HOST=eureka-production.up.railway.app
# Note: In Railway, use the specific internal domain or public domain if needed. 
# Ideally: EUREKA_HOST=${{RAILWAY_PUBLIC_DOMAIN}}
```

---

## 3. API Gateway
**Service Name:** `gateway`

```env
# Networking
PORT=8762
EUREKA_URL=http://${{Eureka.RAILWAY_PRIVATE_DOMAIN}}:8761/eureka/
HOSTNAME=${{RAILWAY_PRIVATE_DOMAIN}}

# Security & CORS
KEYCLOAK_INTERNAL_URL=http://${{Keycloak.RAILWAY_PRIVATE_DOMAIN}}:8080
KEYCLOAK_PUBLIC_URL=https://${{Keycloak.RAILWAY_PUBLIC_DOMAIN}}
FRONTEND_URL=https://your-frontend-app.up.railway.app
# Generate a random secret for gateway client in Keycloak
KEYCLOAK_CLIENT_SECRET=CHANGE_ME_FROM_KEYCLOAK_ADMIN
```

---

## 4. Profile Service
**Service Name:** `profile-service`

```env
PORT=8081
EUREKA_URL=http://${{Eureka.RAILWAY_PRIVATE_DOMAIN}}:8761/eureka/
HOSTNAME=${{RAILWAY_PRIVATE_DOMAIN}}
KEYCLOAK_INTERNAL_URL=http://${{Keycloak.RAILWAY_PRIVATE_DOMAIN}}:8080

# Database Config (Linking to Shared Postgres)
# Use Railway Variable References if possible, or copy values
DB_HOST=${{Postgres.PGHOST}}
DB_PORT=${{Postgres.PGPORT}}
DB_NAME=profile_db
DB_USER=${{Postgres.PGUSER}}
DB_PASSWORD=${{Postgres.PGPASSWORD}}
```

---

## 5. Content Service
**Service Name:** `content-service`

```env
PORT=8082
EUREKA_URL=http://${{Eureka.RAILWAY_PRIVATE_DOMAIN}}:8761/eureka/
HOSTNAME=${{RAILWAY_PRIVATE_DOMAIN}}
KEYCLOAK_INTERNAL_URL=http://${{Keycloak.RAILWAY_PRIVATE_DOMAIN}}:8080

# Database Config
DB_HOST=${{Postgres.PGHOST}}
DB_PORT=${{Postgres.PGPORT}}
DB_NAME=content_db
DB_USER=${{Postgres.PGUSER}}
DB_PASSWORD=${{Postgres.PGPASSWORD}}
```

---

## 6. Planning Service
**Service Name:** `planning-service`

```env
PORT=8083
EUREKA_URL=http://${{Eureka.RAILWAY_PRIVATE_DOMAIN}}:8761/eureka/
HOSTNAME=${{RAILWAY_PRIVATE_DOMAIN}}
KEYCLOAK_INTERNAL_URL=http://${{Keycloak.RAILWAY_PRIVATE_DOMAIN}}:8080

# Database Config
DB_HOST=${{Postgres.PGHOST}}
DB_PORT=${{Postgres.PGPORT}}
DB_NAME=planning_db
DB_USER=${{Postgres.PGUSER}}
DB_PASSWORD=${{Postgres.PGPASSWORD}}
```

---

## 7. Assessment Service
**Service Name:** `assessment-service`

```env
PORT=8084
EUREKA_URL=http://${{Eureka.RAILWAY_PRIVATE_DOMAIN}}:8761/eureka/
HOSTNAME=${{RAILWAY_PRIVATE_DOMAIN}}
KEYCLOAK_INTERNAL_URL=http://${{Keycloak.RAILWAY_PRIVATE_DOMAIN}}:8080

# Database Config
DB_HOST=${{Postgres.PGHOST}}
DB_PORT=${{Postgres.PGPORT}}
DB_NAME=assessment_db
DB_USER=${{Postgres.PGUSER}}
DB_PASSWORD=${{Postgres.PGPASSWORD}}
```

---

## 8. Tracking Service
**Service Name:** `tracking-service`

```env
PORT=8085
EUREKA_URL=http://${{Eureka.RAILWAY_PRIVATE_DOMAIN}}:8761/eureka/
HOSTNAME=${{RAILWAY_PRIVATE_DOMAIN}}
KEYCLOAK_INTERNAL_URL=http://${{Keycloak.RAILWAY_PRIVATE_DOMAIN}}:8080

# Database Config
DB_HOST=${{Postgres.PGHOST}}
DB_PORT=${{Postgres.PGPORT}}
DB_NAME=tracking_db
DB_USER=${{Postgres.PGUSER}}
DB_PASSWORD=${{Postgres.PGPASSWORD}}
```

---

## 9. AI Service
**Service Name:** `ai-service`

```env
PORT=8000
EUREKA_URL=http://${{Eureka.RAILWAY_PRIVATE_DOMAIN}}:8761/eureka/
KEYCLOAK_INTERNAL_URL=http://${{Keycloak.RAILWAY_PRIVATE_DOMAIN}}:8080
OPENAI_API_KEY=sk-YOUR_KEY_HERE
OPENAI_MODEL=gpt-3.5-turbo
```

---

## 10. Keycloak
**Service Name:** `keycloak`

```env
PORT=8080
KC_DB=postgres
KC_DB_URL=jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/keycloak
KC_DB_USERNAME=${{Postgres.PGUSER}}
KC_DB_PASSWORD=${{Postgres.PGPASSWORD}}
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=strongpassword123
KC_HOSTNAME=${{RAILWAY_PUBLIC_DOMAIN}}
KC_PROXY=edge
```

---

## 🛠 Troubleshooting & Critical Networking Fixes (TFM "Gold Mine")

### The "Eureka Connectivity" Problem (Gateway 500)
If your Gateway returns `500` or `Connection Timeout` when calling microservices, but Auth works fine, the issue is **Railway Internal Networking**.

1.  **The Problem**: By default, Eureka registers services using their **Container IP** (`10.x.x.x`). In Railway's distributed architecture, these IPs are often not reachable between different deployments (Network Isolation).
2.  **The Symptoms**:
    *   Eureka shows services as UP.
    *   Gateway logs error: `io.netty.channel.ConnectTimeoutException: ... 10.x.y.z:808X`.
3.  **The Solution**: Force services to register using their **Private DNS Hostname** (`.railway.internal`), which Railway's internal DNS always resolves correctly.

**Required Variables for ALL Microservices:**
| Variable | Value | Explanation |
| :--- | :--- | :--- |
| `EUREKA_INSTANCE_PREFER_IP_ADDRESS` | `false` | Stop using raw IPs. |
| `EUREKA_INSTANCE_HOSTNAME` | `${{RAILWAY_PRIVATE_DOMAIN}}` | Use the internal DNS name provided by Railway. |
| `EUREKA_INSTANCE_NON_SECURE_PORT` | `${PORT}` | Explicitly register the correct port. |

### Case Sensitivity in Variables
Railway variables like `${{ServiceName.VAR}}` are **Case Sensitive**.
*   If service is named `Keycloak`, `${{keycloak.VAR}}` will fail (resolves to empty string).
*   Always match the service name exactly as shown in the Dashboard.
