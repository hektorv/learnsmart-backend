# 📉 Technical Debt & Future Improvements

This document outlines known limitations in the current MVP architecture, their architectural implications, and proposed solutions for a production-grade evolution. This serves as a transparency record for the Master's Thesis (TFM).

## 1. Distributed Data Consistency (The "Orphan User" Problem)

### ⚠️ The Issue
The User Registration flow involves two distinct systems:
1.  **Identity Provider (Keycloak)**: Creates the Auth User.
2.  **Profile Service (PostgreSQL)**: Creates the Domain Profile.

Currently, these operations are **not atomic**. If Step 1 succeeds but Step 2 fails (e.g., DB constraint violation), the system is left in an inconsistent state: a user exists in Keycloak who cannot log in effectively because they lack a profile in the backend.

### 🛠 Proposed Solution: SAGA Pattern (Compensation)
Implement a **Choreography-based SAGA**:
1.  `ProfileService` attempts to creates the User in Keycloak.
2.  **Happy Path**: If successful, it saves the Profile to DB.
3.  **Compensation Path**: If DB save fails, `ProfileService` catches the exception and explicitly calls Keycloak to **DELETE** the user created in Step 1.
4.  **Advanced**: Use an Event Bus (RabbitMQ/Kafka) to handle these rollbacks asynchronously for higher resilience.

---

## 2. Service Discovery & Networking

### ⚠️ The Issue
The current Eureka implementation relies on **Application-Side DNS Registration** logic (`preferIpAddress: false`, manual Hostname overrides) to work within the Railway overlay network. This couples the application code/config to the specific deployment platform quirks.

### 🛠 Proposed Solution: Service Mesh
Replace application-level discovery (Eureka) with a **Service Mesh** (Linkerd or Istio) or Platform-Native Discovery (Kubernetes Service Discovery).
*   **Why**: Removes networking logic from the code. The infrastructure handles routing, retries, and circuit breaking transparently (Sidecar Pattern).

---

## 3. Observability & Logging

### ⚠️ The Issue
Logs are currently unstructured (or raw JSON) scattered across individual container streams. Debugging requires manual correlation of timestamps and 500 errors.

### 🛠 Proposed Solution: Centralized Distributed Tracing
1.  **Distributed Tracing**: Implement **OpenTelemetry** + **Zipkin/Jaeger**. Pass a `Trace-ID` across Gateway -> Service A -> Service B to visualize the full request lifecycle.
2.  **Log Aggregation**: Ship logs to **Grafana Loki** or **ELK Stack** to search across all services instantly.

---

## 4. Hardcoded Secrets & Configuration

### ⚠️ The Issue
Secrets (Client Secrets, Database Passwords) are injected via Environment Variables manually managed in the Deployment Dashboard.

### 🛠 Proposed Solution: Secret Management Vault
Integrate **HashiCorp Vault** or **AWS Secrets Manager**. Applications fetch secrets at runtime securely, enabling automatic rotation of credentials without redeploying.


