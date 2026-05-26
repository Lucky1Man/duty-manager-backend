# Session 2 — duty-service

**Goal:** Implement the largest and most complex microservice — `duty-service` — which owns templates, execution facts, and testimonies. Wire it into the existing Docker Compose and Nginx gateway from Session 1. By the end of this session the full business workflow is operational across all three services.

**Estimated tokens:** ~30,000–40,000
**Prerequisite:** Session 1 complete. `auth-service` and `participant-service` running.

---

## Overview

`duty-service` is the core business domain. It is the most direct translation of the monolith's logic. The key architectural difference from the monolith is:

- It does **not** own participant data — it calls `participant-service` to resolve executor info
- It validates incoming JWTs itself (shared secret) but does not issue them
- It owns three tables: `templates`, `execution_facts`, `testimonies`

This service accounts for roughly half of all business logic in the system.

---

## Step 1 — Project Scaffold

**`duty-service/pom.xml`**

Dependencies:
- `spring-boot-starter-web`
- `spring-boot-starter-security`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson`
- `postgresql`
- `lombok`
- `modelmapper`
- `springdoc-openapi-starter-webmvc-ui`

**`duty-service/src/main/resources/application.yml`**
```yaml
server:
  port: 8083
spring:
  datasource:
    url: jdbc:postgresql://duty-db:5432/duty_db
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
app:
  jwt:
    secret: ${JWT_SECRET}
  participant-service:
    url: ${PARTICIPANT_SERVICE_URL:http://participant-service:8082}
```

**`duty-service/Dockerfile`**
Same multi-stage Maven build pattern as auth-service and participant-service.

---

## Step 2 — Database Schema

**`init-db/duty-db-init.sql`**

```sql
CREATE TABLE templates (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(500),
    version     BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_templates_name_trgm ON templates USING GIN (name gin_trgm_ops);

CREATE TABLE execution_facts (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    start_time  TIMESTAMP NOT NULL,
    finish_time TIMESTAMP,
    executor_id UUID NOT NULL,             -- FK to participant-service, NOT enforced by DB
    template_id UUID REFERENCES templates(id),
    description VARCHAR(500) NOT NULL,
    version     BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_ef_start_time  ON execution_facts (start_time);
CREATE INDEX idx_ef_finish_time ON execution_facts (finish_time);

CREATE TABLE testimonies (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    execution_fact_id UUID NOT NULL REFERENCES execution_facts(id),
    witness_id        UUID NOT NULL,       -- FK to participant-service, NOT enforced by DB
    testified_at      TIMESTAMP NOT NULL
);
```

**Thesis note:** `executor_id` and `witness_id` are UUID columns pointing to participant-service's DB. There is no DB-level foreign key — referential integrity is enforced at the application layer. This is the canonical microservice trade-off: no cross-service joins, no enforced FK constraints. Document this explicitly in Chapter 2 (Design).

---

## Step 3 — Entity Layer

Package: `com.dutydesk.duty.entity`

**`Template.java`**
Direct copy from monolith except: remove the `@OneToMany facts` collection (no lazy-load of cross-service data). Keep `@Version` for optimistic locking.

**`ExecutionFact.java`**
Key change from monolith:
- Remove `@ManyToOne Participant executor` → replace with `UUID executorId` (plain column)
- Remove `@ManyToOne Template template` → keep but make nullable, store `templateId` as plain UUID if template is deleted
- Keep `@OneToMany Set<Testimony> testimonies`
- Keep `@Version`

**`Testimony.java`**
- Remove `@ManyToOne Participant witness` → replace with `UUID witnessId`
- Keep `@ManyToOne ExecutionFact executionFact`

---

## Step 4 — Repository Layer

Package: `com.dutydesk.duty.repository`

**`TemplateRepository.java`**
- `findByNameContainingIgnoreCase(String name, Pageable pageable)` — fuzzy search
- `findAll(Pageable pageable)`

**`ExecutionFactRepository.java`**
All queries copied from monolith, adapted to work without Participant join:
- `getAllFinishedInRange(LocalDateTime from, LocalDateTime to, Pageable pageable)`
- `getAllFinishedInRangeForParticipant(LocalDateTime from, LocalDateTime to, UUID executorId, Pageable pageable)`
- `getAllActiveInRange(...)`, `getAllActiveInRangeForParticipant(...)`
- `getAllInRange(...)`, `getAllInRangeForParticipant(...)`

**`TestimonyRepository.java`**
- `findByExecutionFactId(UUID executionFactId, Pageable pageable)`

---

## Step 5 — Participant Service Client

Package: `com.dutydesk.duty.client`

**`ParticipantServiceClient.java`**

This is the central new piece that does not exist in the monolith.

```java
@Component
public class ParticipantServiceClient {

    private final RestClient restClient;

    public ParticipantServiceClient(@Value("${app.participant-service.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public ParticipantDTO getParticipant(UUID participantId) {
        return restClient.get()
                .uri("/api/v1/participants/{id}", participantId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new ParticipantNotFoundException(participantId);
                })
                .body(ParticipantDTO.class);
    }
}
```

**`dto/external/ParticipantDTO.java`**
Read-only projection: `id`, `email`, `firstName`, `lastName`, `role` — matches `GetParticipantDTO` shape from participant-service.

**Thesis note:** Every `GetExecutionFactDTO` assembly now requires a REST call to participant-service to resolve the executor's name/email. In the monolith this was a single JPA join. This round-trip overhead is one of the key measurements in Chapter 4.

**Optimization option (discuss in thesis but do not implement yet):** Cache `ParticipantDTO` responses with a short TTL to reduce cross-service calls under load.

---

## Step 6 — Service Layer

Package: `com.dutydesk.duty.service`

### `TemplateServiceImpl.java`
Direct port from monolith. No cross-service calls needed. Keep fuzzy search, pagination, CRUD.

### `ExecutionFactServiceImpl.java`
Port from monolith with the following changes:

1. **`recordExecutionFact`** — no longer calls `participantRepository.getReferenceById()`. Sets `executorId` directly from the DTO. Validates that the executor exists by calling `participantServiceClient.getParticipant(executorId)` once at record time.

2. **`mapEntityToGetDTO`** — must now call `participantServiceClient.getParticipant(fact.getExecutorId())` to populate `executorEmail` and `executorName` on the DTO. This is the hot path for read queries.

3. **`finishExecution`** — validates ownership by comparing `authentication.getName()` (email from JWT) against `participantServiceClient.getParticipant(fact.getExecutorId()).getEmail()`.

4. **`deleteExecutionFact`** — same ownership check pattern.

5. All date-range and pagination logic is identical to the monolith.

### `TestimonyServiceImpl.java`
Port from monolith. `testifyExecutionFact` sets `witnessId` from `authentication` (resolve email → call participant-service to get UUID).

---

## Step 7 — DTO Layer

Package: `com.dutydesk.duty.dto`

| DTO | Changes from monolith |
|-----|-----------------------|
| `GetExecutionFactDTO.java` | Add `executorEmail`, `executorName` fields (populated from participant-service call) |
| `RecordExecutionFactDTO.java` | No change |
| `GetTemplateDTO.java` | No change |
| `CreateTemplateDTO.java` | No change |
| `UpdateTemplateDTO.java` | No change |
| `GetTestimonyDTO.java` | Add `witnessEmail` (populated from participant-service call) |

---

## Step 8 — Controller Layer

Package: `com.dutydesk.duty.controller`

**`TemplateController.java`** — direct port from monolith, all endpoints identical.

**`ExecutionFactController.java`** — direct port from monolith, all endpoints identical. The complexity is hidden in the service layer.

---

## Step 9 — Security

**`config/SecurityConfig.java`**
Same JWT validation filter chain as participant-service. duty-service validates tokens but does not issue them. Role-based access rules identical to monolith.

**`config/JwtAuthenticationFilter.java`**
Identical to participant-service — copy directly.

---

## Step 10 — Exception Handling & Utilities

**`exception/ServiceException.java`** — port from monolith
**`exception/ParticipantNotFoundException.java`** — new, thrown when participant-service returns 404
**`exception/GlobalExceptionHandler.java`** — add handler for `ParticipantNotFoundException` → 422 Unprocessable Entity
**`service/TimeService.java` / `impl/TimeServiceImpl.java`** — direct port
**`config/TimeZoneFilter.java`** — direct port

---

## Step 11 — Wire Into Docker Compose & Nginx

**Update `docker-compose.yml`** (uncomment duty-service block):
```yaml
duty-service:
  build: ./duty-service
  environment:
    JWT_SECRET: ${JWT_SECRET}
    PARTICIPANT_SERVICE_URL: http://participant-service:8082
  depends_on:
    duty-db:
      condition: service_healthy
    participant-service:
      condition: service_started
```

**Update `gateway/nginx.conf`** — uncomment the duty-service location blocks:
```nginx
location /api/v1/templates {
    proxy_pass http://duty-service:8083;
}
location /api/v1/execution-facts {
    proxy_pass http://duty-service:8083;
}
```

---

## Step 12 — Smoke Test Checklist

- [ ] `docker-compose up` starts all 5 containers cleanly (3 services + 3 DBs + gateway)
- [ ] `POST /api/v1/templates` creates a template
- [ ] `GET /api/v1/templates` returns it
- [ ] `POST /api/v1/execution-facts` records a fact (requires valid participant UUID from participant-service)
- [ ] `GET /api/v1/execution-facts?from=...` returns facts with executor email populated (proves participant-service call works)
- [ ] `POST /api/v1/execution-facts/{id}/testimonies` testifies successfully
- [ ] `POST /api/v1/execution-facts/finished` marks a fact finished
- [ ] `DELETE /api/v1/execution-facts/{id}` deletes an untestified fact
- [ ] Verify `duty-service` logs show outbound HTTP calls to `participant-service`

---

## Deliverables at End of Session 2

1. `duty-service/` — fully working Spring Boot app, all business workflows operational
2. `init-db/duty-db-init.sql` — schema for duty domain
3. Updated `docker-compose.yml` — full 5-service stack
4. Updated `gateway/nginx.conf` — all routes wired

**Full end-to-end workflow is now functional across all three microservices.**

**Not included in Session 2:** Metrics instrumentation, request logging interceptor, Gatling tests (Session 3).
