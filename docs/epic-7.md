# 📘 EPIC 7 — Security and API Gateway  
**System:** LearnSmart  
**Base components:** API Gateway, Security Layer  
**Scope:** Cross-cutting concern (applies to all microservices)

---

## US-060 | Configure CORS Policy

**As** the platform,  
**I want** to configure Cross-Origin Resource Sharing (CORS),  
**so that** the frontend application can securely communicate with backend services.

### Scope
API Gateway / Security Configuration

### Acceptance Criteria

- CORS is enabled at the gateway level.
- Allowed origin includes:
  - `http://localhost:5173`.
- Allowed HTTP methods:
  - GET, POST, PUT, PATCH, DELETE, OPTIONS.
- Allowed headers include:
  - Authorization
  - Content-Type
  - X-Requested-With.
- Credentials are allowed if required by authentication.
- Preflight (OPTIONS) requests are handled correctly.
- CORS configuration applies consistently across all routes.

---

## US-061 | Global Authentication and Route Protection

**As** the platform,  
**I want** to enforce global authentication and authorization rules at the gateway,  
**so that** access to microservices is consistently secured.

### Scope
API Gateway / Authentication Layer

### Acceptance Criteria

- Public routes are defined as:
  - `/auth/**`
- Public routes:
  - do not require authentication
  - do not trigger login redirection.
- All other routes are considered private:
  - `/**`
- Private routes require a valid access token (JWT).
- The gateway validates the token before routing the request.
- The access token is relayed (Token Relay) to downstream microservices.
- Unauthorized requests to private routes return:
  - `HTTP 401 Unauthorized`.
- Forbidden requests (insufficient roles/claims) return:
  - `HTTP 403 Forbidden`.
- Authentication rules are enforced uniformly for all microservices.

---
