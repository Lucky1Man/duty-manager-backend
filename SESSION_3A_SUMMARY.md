# Session 3A — Summary: Observability Stack (Microservices)

**Date:** 2026-05-26
**Repo:** `duty-manager-microservices`

---

## What was done

### 1. Prometheus metrics on all 3 services
Added `spring-boot-starter-actuator` and `micrometer-registry-prometheus` to `auth-service`, `participant-service`, and `duty-service` pom.xml files.

Configured each `application.yml` to expose the `/actuator/prometheus` endpoint and enable percentile histograms for `http.server.requests` (p50, p95, p99). Added `/actuator/**` to the permit-all list in each `SecurityConfig.java`.

### 2. Cross-service call timers
Added a Micrometer `Timer` around outbound HTTP calls to `participant-service` in two places:

- `duty-service/ParticipantServiceClient.getParticipant()` — emits `participant_service_call_seconds{operation="getParticipant"}`
- `auth-service/ParticipantServiceClient.getParticipantByEmail()` — emits `participant_service_call_seconds{operation="getParticipantByEmail"}`

Both callers contribute to the same metric name so Grafana can compare latency by `job` label (which service made the call).

### 3. Request logging in duty-service
Created `RequestLoggingInterceptor.java` — a Spring MVC `HandlerInterceptor` that logs one structured JSON line per request including method, URI, HTTP status, duration in ms, and authenticated user. Registered it via `WebMvcConfig.java`. Added `logback-spring.xml` to route the `REQUEST_LOG` logger to stdout so Docker captures it.

### 4. Prometheus scrape config
Created `prometheus/prometheus.yml` — scrapes all 3 services every 5 seconds via their internal Docker network hostnames.

### 5. Grafana dashboard
Created `grafana/provisioning/` with auto-provisioned Prometheus datasource and dashboard provider. Created `grafana/dashboards/dutydesk.json` with 9 panels:

| Panel | What it shows |
|---|---|
| Throughput (req/s) | Requests per second per service |
| p50 Latency | Median response time per service |
| p95 Latency | 95th percentile response time per service |
| p99 Latency | 99th percentile response time per service |
| Cross-Service Call Latency | p95 and avg of calls to participant-service, broken down by calling service (auth-service vs duty-service) |
| JVM Heap Used | Heap memory per service |
| CPU Usage | Process CPU per service |
| DB Pool Active Connections | HikariCP active connections per service |
| Error Rate (5xx) | Server error rate per service |

### 6. Docker Compose
Added `prometheus` and `grafana` services to `docker-compose.yml`. Grafana is exposed on port 3000 (admin / admin). Added `grafana-data` named volume for persistence.

---

## Verification

Run `docker compose up -d --build` and check:

1. `http://localhost:9090/targets` — all 3 Prometheus scrape jobs should be green
2. `http://localhost:3000` (admin / admin) — DutyDesk dashboard should load with live data

---

## Key design decisions

- **Same metric name for both callers** (`participant_service_call_seconds`) — Prometheus `job` label distinguishes which service fired the timer, keeping the Grafana query simple.
- **p50/p95/p99 percentiles** — averages hide tail latency. For the thesis load test comparison the p99 difference between monolith and microservices is the most interesting number.
- **Prometheus not exposed via Nginx** — internal only (`expose` not `ports`), Grafana reaches it via the Docker internal network.
