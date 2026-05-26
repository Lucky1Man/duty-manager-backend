# Session 1 — auth-service, participant-service, Infrastructure

**Goal:** Create the new `duty-manager-microservices` repository, scaffold both simpler services, and set up the shared deployment infrastructure (Docker Compose + Nginx). By the end of this session the two services must boot, authenticate, and be reachable through the gateway.

**Estimated tokens:** ~25,000–30,000
**Prerequisite:** Have `duty-manager-backend` (monolith) open for reference.

---

## Step 1 — Repository & Top-Level Structure

Create a new directory `duty-manager-microservices/` with the following layout:

```
duty-manager-microservices/
├── auth-service/
├── participant-service/
├── duty-service/               ← placeholder only this session
├── gateway/
│   └── nginx.conf
├── docker-compose.yml
└── init-db/
    ├── auth-db-init.sql
    └── participant-db-init.sql
```

No Maven parent. Each service is a fully independent Spring Boot project with its own `pom.xml`. This makes the "independent deployability" point explicit for the thesis.

---

## Step 2 — auth-service

### Purpose
Issues and validates JWT tokens. Owns the `credentials` table (email + hashed password + role). No other service has access to raw passwords.

### Files to create

**`auth-service/pom.xml`**
Dependencies: `spring-boot-starter-web`, `spring-boot-starter-security`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `jjwt-api/impl/jackson`, `postgresql`, `lombok`, `springdoc-openapi-starter-webmvc-ui`

**`auth-service/src/main/resources/application.yml`**
```yaml
server:
  port: 8081
spring:
  datasource:
    url: jdbc:postgresql://auth-db:5432/auth_db
  jpa:
    hibernate:
      ddl-auto: validate
app:
  jwt:
    secret: ${JWT_SECRET}
    expiration-ms: 64800000   # 18 hours
```

**`auth-service/Dockerfile`**
Standard multi-stage: Maven build → slim JRE runtime image.

**Java classes (package `com.dutydesk.auth`):**

| Class | Notes |
|-------|-------|
| `AuthServiceApplication.java` | `@SpringBootApplication` entry point |
| `entity/Credential.java` | `id (UUID)`, `email`, `passwordHash`, `role (String)` — minimal, no Participant relationship |
| `repository/CredentialRepository.java` | `findByEmail(String)` |
| `service/JwtService.java` | Copied from monolith, generates + validates JWT |
| `service/AuthenticationService.java` | Loads credential, validates password, returns signed JWT |
| `controller/AuthController.java` | `POST /api/v1/auth/jwt` — single endpoint |
| `controller/CredentialController.java` | `POST /api/v1/credentials` (called internally by participant-service when registering a new user), `POST /api/v1/credentials/{email}/password` |
| `config/SecurityConfig.java` | Permit all on auth endpoints, no JWT filter needed (this service issues tokens, it doesn't validate them for its own endpoints) |
| `config/AppConfig.java` | `PasswordEncoder` bean |
| `dto/AuthRequestDTO.java` | `email`, `password` |
| `dto/AuthResponseDTO.java` | `token`, `expiresAt` |
| `dto/CreateCredentialDTO.java` | `email`, `password`, `role` — called by participant-service |
| `exception/ExceptionResponse.java` | `timestamp`, `message`, `status` |
| `exception/GlobalExceptionHandler.java` | `@RestControllerAdvice` |

**`init-db/auth-db-init.sql`**
```sql
CREATE TABLE credentials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);
```

### Key design decision
`participant-service` calls `auth-service` via internal REST when registering a new participant, so that passwords never leave the auth domain. Document this inter-service call in the thesis as an example of the added network hop cost.

---

## Step 3 — participant-service

### Purpose
Manages participant profiles (name, email, metadata). Does NOT store passwords — delegates all credential work to `auth-service`. Owns the `participants` table.

### Files to create

**`participant-service/pom.xml`**
Dependencies: same web/security/jpa stack + `jjwt` for token validation (does not issue, only validates) + `lombok` + `springdoc`.

**`participant-service/src/main/resources/application.yml`**
```yaml
server:
  port: 8082
spring:
  datasource:
    url: jdbc:postgresql://participant-db:5432/participant_db
app:
  jwt:
    secret: ${JWT_SECRET}       # same secret — shared via env var
  auth-service:
    url: ${AUTH_SERVICE_URL:http://auth-service:8081}
```

**`participant-service/Dockerfile`**
Same multi-stage pattern as auth-service.

**Java classes (package `com.dutydesk.participant`):**

| Class | Notes |
|-------|-------|
| `ParticipantServiceApplication.java` | Entry point |
| `entity/Participant.java` | `id (UUID)`, `email`, `firstName`, `lastName`, `role (String)` — no password field |
| `repository/ParticipantRepository.java` | `findByEmail`, `findById`, pageable `findAll` |
| `service/ParticipantService.java` | Interface |
| `service/impl/ParticipantServiceImpl.java` | Register (saves profile + calls auth-service to create credential), getParticipant, getParticipants, changePassword (delegates to auth-service) |
| `client/AuthServiceClient.java` | `RestClient`-based HTTP client: `POST /api/v1/credentials`, `POST /api/v1/credentials/{email}/password` |
| `controller/ParticipantController.java` | Same endpoints as monolith: `POST /`, `GET /{identifier}`, `GET /`, `POST /{identifier}/password` |
| `config/SecurityConfig.java` | JWT validation filter — reads JWT, sets `SecurityContext`, but does not issue tokens |
| `config/JwtAuthenticationFilter.java` | Copied from monolith, validates incoming JWT using shared secret |
| `dto/RegisterParticipantDTO.java` | `email`, `password`, `firstName`, `lastName`, `role` |
| `dto/GetParticipantDTO.java` | `id`, `email`, `firstName`, `lastName`, `role` |
| `dto/ChangePasswordDTO.java` | `oldPassword`, `newPassword` |
| `exception/ExceptionResponse.java` | Same structure as auth-service |
| `exception/GlobalExceptionHandler.java` | `@RestControllerAdvice` |

**`init-db/participant-db-init.sql`**
```sql
CREATE TABLE participants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(50) NOT NULL
);
```

### Key design decision
`AuthServiceClient` is the first cross-service call introduced. This is a thesis talking point: registering one user now requires two HTTP round trips instead of one in-process call. Measure and document this overhead in Chapter 4.

---

## Step 4 — Nginx Gateway

**`gateway/nginx.conf`**

Route by path prefix:
```
/api/v1/auth/**          → auth-service:8081
/api/v1/participants/**  → participant-service:8082
/api/v1/templates/**     → duty-service:8083  (placeholder)
/api/v1/execution-facts/** → duty-service:8083  (placeholder)
/swagger-ui/**           → per-service (discussed in thesis as microservices UI challenge)
```

Single `location` block per service, `proxy_pass` with `proxy_set_header Host`, `X-Real-IP`, `X-Forwarded-For`.

---

## Step 5 — Docker Compose

**`docker-compose.yml`** — microservices profile only (monolith profile lives in the original repo).

Services:
- `auth-db`: `postgres:16`, volume `auth-db-data`, init script `auth-db-init.sql`
- `participant-db`: `postgres:16`, volume `participant-db-data`, init script `participant-db-init.sql`
- `duty-db`: `postgres:16`, placeholder, init script added in Session 2
- `auth-service`: build `./auth-service`, env `JWT_SECRET`, depends on `auth-db`
- `participant-service`: build `./participant-service`, env `JWT_SECRET`, `AUTH_SERVICE_URL`, depends on `participant-db` + `auth-service`
- `duty-service`: placeholder commented out, added in Session 2
- `gateway`: `nginx:alpine`, mounts `./gateway/nginx.conf`, exposes port `80`

Health checks on all DB containers before services start (`pg_isready`).

---

## Step 6 — Smoke Test Checklist

Before closing Session 1, verify manually:

- [ ] `docker-compose up auth-db participant-db auth-service participant-service gateway` starts cleanly
- [ ] `POST /api/v1/credentials` via gateway creates a credential row in auth-db
- [ ] `POST /api/v1/auth/jwt` returns a valid JWT
- [ ] `POST /api/v1/participants` with the JWT creates a participant row in participant-db
- [ ] `GET /api/v1/participants/{id}` returns the created participant
- [ ] Invalid JWT returns 401 from participant-service

---

## Deliverables at End of Session 1

1. `auth-service/` — fully working Spring Boot app, builds and runs
2. `participant-service/` — fully working, communicates with auth-service
3. `gateway/nginx.conf` — routing config
4. `docker-compose.yml` — boots auth + participant stack
5. `init-db/auth-db-init.sql` and `init-db/participant-db-init.sql`

**Not included in Session 1:** duty-service, Gatling tests, metrics instrumentation (Session 3).
