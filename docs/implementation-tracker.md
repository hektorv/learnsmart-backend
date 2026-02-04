# Implementación & Trazabilidad - LearnSmart Backend

Este documento rastrea el progreso de implementación mapeado a las Historias de Usuario (US) definidas en `user-stories.md`.

## Estada Actual
**Última actualización:** 01 Febrero 2026
**Microservicios Iniciados:** Todos (Verificados) (`profile`, `content`, `planning`, `assessment`, `tracking`, `ai`, `gateway`, `eureka`, `keycloak`)
**Infraestructura:** `back-end-eureka`, `gateway`

---

## 🏗 Infraestructura Base

- [x] **Service Discovery (Eureka)**
  - Configuración Docker-friendly (`application.yml`)
  - Puerto: 8761
- [x] **API Gateway**
  - Mapeo de rutas a microservicios (`lb://*`)
  - Configuración CORS global
  - Puerto: 8762

---

## 👤 ÉPICA 1: Gestión de Usuario y Perfil (`profile-service`)

### Feature 1.1 — Registro y autenticación
- **US-001 – Registro de usuario**
  - [x] **Endpoint**: `POST /auth/register` (AuthController)
  - [x] **Lógica**: Simulación de ID externo (Keycloak), creación de registro en `user_profiles`.
  - [x] **Validación**: `@Valid`, `@NotBlank`, `@Email`.

### Feature 1.2 — Gestión del perfil
- **US-002 – Consultar mi perfil**
  - [x] **Endpoint**: `GET /profiles/me`
  - [x] **Header**: Uso de `X-User-Id` para contexto usuario.
- **US-003 – Actualizar mi perfil**
  - [x] **Endpoint**: `PUT /profiles/me`
  - [x] **Datos**: Nombre, Locale, Timezone, Año nacimiento.

### Feature 1.3 — Gestión de objetivos
- **US-004 – Definir objetivos educativos**
  - [x] **Endpoint**: `POST /profiles/me/goals`
  - [x] **Entidad**: `UserGoal` con campos (domain, targetLevel, intensity...)
- **US-005 – Actualizar o desactivar objetivos**
  - [x] **Endpoint**: `PUT /profiles/me/goals/{id}`
  - [x] **Endpoint**: `DELETE /profiles/me/goals/{id}`

### Checklist Técnico (Profile Service)
- [x] **Estructura Proyecto**: Spring Boot 3.4.5, Java 21. `pom.xml` independiente.
- [x] **Base de Datos**: PostgreSQL driver.
- [x] **Schema**: `schema.sql` (ddl) idempotente.
- [x] **JPA**: Entidades `UserProfile`, `UserGoal` + Repositorios.
- [x] **Observabilidad**: Cliente Eureka configurado.
- [x] **Tests**: Tests de integración (Ejecutados y Verificados en Docker).
  - Verificado flujo end-to-end: Register -> Get Profile via Gateway.


---

## 📚 ÉPICA 2: Contenido (`content-service`)
- [x] **Estructura Base**: Spring Boot, JPA, Postgres.
- [x] **API**:
  - `GET /domains` (Implemented)
  - `GET /content-items` (Implemented)
  - [x] `GET/PUT /skills/{id}/prerequisites` (Epic 8) + Cycle Check (US-013).
- [x] **Datos (Seed Data)**:
  - Dominios: `Backend`, `Frontend`.
  - Habilidades: `Java`, `Spring`, `React`, `Hooks`.
  - Contenido: Artículos y Videos de ejemplo.

## 📅 ÉPICA 3: Planificación (`planning-service`)
- [x] **Estructura Base**: Spring Boot, JPA, Postgres.
- [x] **Integración**:
  - Cliente Feign para `profile-service` (Token Relay activo).
  - Cliente Feign para `content-service` (Token Relay activo).
  - Cliente Feign para `ai-service`.
- [x] **API**:
  - `POST /plans`: Generación orquestada y persistencia verificada.
  - `POST /plans`: Generación orquestada y persistencia verificada.
  - [x] `POST /plans/{id}/replan` (Lógica real implementada US-080).

## 🎓 ÉPICA 4: Evaluación (`assessment-service`)
- [x] **Estructura Base**: Spring Boot, JPA, Postgres.
- [x] **API**:
  - `POST /assessments/session`: Crear sesión.
  - `POST /assessments/session/{id}/next-item`: Implementado (Mock random). ⚠️ Falta IA (Epic 8).
  - `POST /assessments/session/{id}/response`: Evaluar respuesta.
- [x] **Datos (Seed Data)**:
  - Preguntas cargadas para `Java Basics` y `React Basics`.
  - Opciones y Feedback configurados.

## 📊 ÉPICA 5: Tracking (`tracking-service`)
- [x] **Estructura Base**: Spring Boot, JPA, Postgres.
- [x] **API**:
  - `POST /events`: Registro implementado (Asíncrono US-086).
  - `GET /events`: Consulta histórica.

## 🤖 ÉPICA 6: Inteligencia Artificial (`ai-service`)
- [x] **Estructura Base**: Python (FastAPI).
- [x] **Integración LLM**:
  - Configuración OpenAI API Key verificada.
  - Endpoints `/plans/generate` y `/assessments/next-item` funcionales.
  - **Validado**: Lógica real (OpenAI) con fallback a Mock si no hay Key.
  - **Seguridad**: Prompts incluyen instrucciones anti-injection.
  - **Refinement**: Generación de lecciones incluye paso de auto-refinamiento.

## 🔒 Seguridad & Gateway
- [x] **CORS**: Habilitado para `http://localhost:5173` (Frontend).
- [x] **CORS**: Habilitado para `http://localhost:5173` (Frontend) en `application.yml`.
- [x] **Auth Global**:
  - Rutas Públicas: `/auth/**` permitidas en `SecurityConfig`.
  - Rutas Privadas: `/**` autenticadas vía `oauth2ResourceServer`.
  - Token Relief: Filtro `TokenRelay` activo en rutas de microservicios.

---

## 🛠 ÉPICA 8: Technical Refinement & Gap Closure
*Epic creada post-validación inicial para abordar deuda técnica.*

- [x] **US-080: Real AI Replanning**
  - Reemplazar stub en `LearningPlanServiceImpl` con llamada real a AI Service.
- [x] **US-081: Explicit Prerequisies API**
  - Añadir endpoints específicos en `SkillController` para gestión de grafo.
- [x] **US-082: OAS/DDL Consistency**
  - Auditoría final de contratos vs código.
- [x] **US-083: Adaptive Assessment (Real AI)**
  - Implementar selección de items vía AI (`getNextItem`).
- [x] **US-084: AI Feedback Generation**
  - Integrar llamada a AI para feedback contextual (`submitResponse`).
- [x] **US-085: Strict AI Configuration**
  - Eliminar fallback automático a Mock; requerir flag explícito o API Key válida.
- [x] **US-086: Async Event Tracking**
  - Hacer `createEvent` asíncrono.
- [x] **US-087: Skill Dependency Cycle Detection**
  - Validar grafo DAG en updates.
- [x] **US-088: Domain Status Filtering**
  - Filtrar dominios no publicados.

---

## 🧩 ÉPICA 9: Gap Closure & Enhancements (New)
*Gaps identificados durante la validación integral de Febrero 2026.*

### Profile Service Gaps
- [x] **US-094**: User Audit Trail ✅ (Implemented - Commit: a00f738)
- [x] **US-095**: Soft Delete for Learning Goals (Deferred - Not prioritized)
- [x] **US-096**: Goal Completion Tracking ✅ (Implemented - Commit: pending)
- [ ] **US-097**: Goal Progress Calculation
- [ ] **US-098**: Goal Recommendations

### Content Service Gaps
- [ ] **US-099**: CONTENT_CREATOR Role
- [ ] **US-0100**: Content Versioning
- [ ] **US-0101**: Skill-based Content Filtering
- [ ] **US-0102**: User Completion Tracking Integration
- [ ] **US-0103**: Pedagogical Ordering
- [ ] **US-0104**: Domain Enrichment (UI/UX)
- [ ] **US-0105**: Difficulty-based Skill Filtering
- [ ] **US-0106**: Proper HTTP 409 for Duplicates

### Planning Service Gaps
- [x] **US-0107**: Automatic Replanning Triggers ✅ (Implemented - Commit: pending)
- [ ] **US-0108**: Skill Prerequisite Validation
- [ ] **US-0109**: Plan Versioning
- [ ] **US-0110**: Activity Completion Timestamps (Already implemented in tracking-service)
- [ ] **US-0111**: Certificate Generation
- [ ] **US-0111**: Skill Prerequisite Validation in Planning
- [ ] **US-0112**: Diagnostic-based Level Detection

### Assessment Service Gaps
- [ ] **US-0113**: Session Management Enhancements
- [ ] **US-0114**: IRT/CAT Algorithm Implementation
- [ ] **US-0115**: Item Deduplication in Sessions
- [ ] **US-0116**: Session Progress Indicators
- [ ] **US-0117**: On-Demand Additional Feedback
- [ ] **US-0118**: Mastery Trend Analysis
- [ ] **US-0119**: Low Mastery Skill Highlighting
- [ ] **US-0120**: Peer Comparison Analytics
- [ ] **US-0121**: Subtopic Mastery Breakdown
- [ ] **US-0122**: Positive Reinforcement

### Tracking Service Gaps
- [x] **US-0123**: Event Payload Validation
- [ ] **US-0124**: Role-Based Event Access Control
- [ ] **US-0125**: Cursor-Based Pagination
- [ ] **US-0126**: Event Export (CSV/JSON)
- [ ] **US-0127**: Event Aggregations
- [ ] **US-0128**: Query Result Limits
- [ ] **US-0129**: Performance Monitoring

### AI Service Gaps
- [ ] **US-0130**: AI Performance Monitoring
- [ ] **US-0131**: Follow-Up Feedback Requests
