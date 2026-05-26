# Thesis Implementation Plan

**Topic:** Дослідження ефективності монолітної та мікросервісної архітектури на прикладі системи DutyDesk
**English:** A study of the efficiency of monolithic and microservice architectures using the DutyDesk system as a case study

---

## Current State

The project is a complete Spring Boot monolith: single deployable JAR, single PostgreSQL DB, 4 REST controllers (Auth, Participant, Template, ExecutionFact), JWT-secured, deployed via Docker. It is functionally complete as DutyDesk v1.

---

## What the Thesis Requires

The topic is about **comparing efficiency** of monolithic vs microservice architectures. To make that comparison academically valid, the project needs:

- A measurable baseline for the monolith
- A clear decomposition showing where services would split
- Real performance data under load
- Demonstrable deployment and operational differences

---

## 5 Most Significant Changes

### 1. Metrics & Observability Infrastructure (Spring Actuator + Micrometer + Prometheus)

Add `spring-boot-actuator` and `micrometer-registry-prometheus` to expose `/actuator/metrics` and `/actuator/prometheus` endpoints.

**What it gives:** CPU usage, memory, JVM heap, DB connection pool stats, and per-endpoint response time histograms — the **quantitative backbone** of the comparison chapter.

**Why it matters:** Without this, the thesis cannot back architectural claims with real numbers.

**Deliverable:** Prometheus scrape config + Grafana dashboard showing monolith baseline metrics.

---

### 2. Request-Level Performance Logging Interceptor

Add a `HandlerInterceptor` that records each request's endpoint, HTTP method, duration (ms), HTTP status code, and authenticated role into structured JSON logs.

**What it gives:** A raw dataset for analysis tables and graphs comparing monolith throughput vs microservices under different load profiles.

**Why it matters:** Provides the per-request evidence needed for the Analysis of Results chapter (Chapter 4).

**Deliverable:** Log output parseable by standard tools (e.g., jq, Kibana, or simple CSV export for charts).

---

### 3. Domain Boundary Refactoring (Bounded Context Preparation)

Reorganize the service layer into explicit domain packages:

- `auth` — JWT issuance, credential validation
- `participant` — user registration, password management
- `duty` — templates, execution facts, testimonies

Each domain gets clearly separated interfaces with no direct cross-domain service calls (use events or explicit DTOs at boundaries).

**What it gives:** A concrete architectural argument showing exactly which code lines become service boundaries, what data each microservice would own, and where inter-service communication overhead would appear.

**Why it matters:** This is the structural core of the thesis — the monolith decomposition design that justifies the microservice comparison.

**Deliverable:** Refactored package structure + architecture diagram showing bounded contexts.

---

### 4. Load Testing Suite (Gatling)

Add Gatling scenarios under `src/test/gatling` (or a separate Maven module) covering the main workflows:

- Ramp-up to 50 / 100 / 200 concurrent users
- Record execution fact
- Get execution facts by date range
- Testify an execution fact
- Template search (fuzzy)

Run these scenarios against the monolith to establish baseline, then against the decomposed microservice version for comparison.

**What it gives:** The performance comparison graphs required in Chapter 4 — response time percentiles, throughput (req/s), error rates under load.

**Why it matters:** Transforms qualitative architectural claims into measurable, reproducible results.

**Deliverable:** Gatling HTML reports for monolith and microservice runs side by side.

---

### 5. Multi-Profile Docker Compose (Monolith vs Microservices Simulation)

Extend the existing `Dockerfile` with a `docker-compose.yml` supporting two profiles:

**`monolith` profile:**
- Single app container
- Single PostgreSQL DB

**`microservices` profile:**
- Three app containers: `auth-service`, `participant-service`, `duty-service`
- Each service with its own DB schema (schema-per-service pattern)
- Nginx container as API gateway routing requests to correct service

**What it gives:** A concrete demonstration of deployment complexity difference — number of containers, config files, startup time, memory footprint, inter-service network calls.

**Why it matters:** Closes the operational overhead comparison chapter with reproducible numbers (startup time, total memory usage, config volume).

**Deliverable:** `docker-compose.monolith.yml` and `docker-compose.microservices.yml` with documented resource comparison table.

---

## Implementation Order

| Step | Change | Reason |
|------|--------|--------|
| 1 | Metrics & Observability | Measurement infrastructure must exist before any test runs |
| 2 | Request Logging Interceptor | Provides per-request data to complement aggregate metrics |
| 3 | Domain Boundary Refactoring | Architectural narrative; required before splitting into containers |
| 4 | Load Testing Suite | Generates comparison data; runs against both architectures |
| 5 | Multi-Profile Docker Compose | Closes deployment chapter; depends on domain split from step 3 |