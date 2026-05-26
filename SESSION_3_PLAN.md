# Session 3 — Observability, Load Testing & Final Comparison

**Goal:** Instrument both the monolith and the microservices stack with metrics and request logging. Build and run the Gatling load test suite against both architectures. Produce the comparison data tables and charts required for Chapter 4 of the thesis.

**Estimated tokens:** ~20,000–25,000
**Prerequisite:** Sessions 1 and 2 complete. Full microservices stack boots cleanly.

---

## Part A — Observability (applies to BOTH repos)

These changes must be made in **both** `duty-manager-backend` (monolith) and all three services in `duty-manager-microservices`.

---

### A1 — Spring Actuator + Micrometer + Prometheus

**Add to `pom.xml` in each service:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-actuator-autoconfigure</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

**Add to each `application.yml`:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, metrics, prometheus
  metrics:
    distribution:
      percentiles-histogram:
        http.server.requests: true
      percentiles:
        http.server.requests: 0.5, 0.95, 0.99
```

**Metrics automatically exposed:**
- `http_server_requests_seconds` — latency histogram per endpoint + status
- `jvm_memory_used_bytes` — JVM heap
- `hikaricp_connections_active` — DB connection pool
- `process_cpu_usage` — CPU %

**Prometheus scrape config (`prometheus.yml`):**
Add a `prometheus/` directory to both repos:
```yaml
scrape_configs:
  - job_name: monolith
    static_configs:
      - targets: ['app:8080']
    metrics_path: /actuator/prometheus
    scrape_interval: 5s

  # For microservices repo:
  - job_name: auth-service
    static_configs:
      - targets: ['auth-service:8081']
  - job_name: participant-service
    static_configs:
      - targets: ['participant-service:8082']
  - job_name: duty-service
    static_configs:
      - targets: ['duty-service:8083']
```

Add `prometheus` and `grafana` services to `docker-compose.yml` in both repos.

---

### A2 — Request Performance Logging Interceptor

Add to the monolith and to `duty-service` (the service handling the most traffic).

**`RequestLoggingInterceptor.java`**

```java
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger("REQUEST_LOG");
    private static final String START_TIME_ATTR = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        long duration = System.currentTimeMillis() - (long) request.getAttribute(START_TIME_ATTR);
        String principal = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName).orElse("anonymous");
        log.info("{\"endpoint\":\"{} {}\",\"status\":{},\"duration_ms\":{},\"user\":\"{}\"}",
                request.getMethod(), request.getRequestURI(),
                response.getStatus(), duration, principal);
    }
}
```

**Register in `WebMvcConfig.java`:**
```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLoggingInterceptor);
    }
}
```

**Logback config** (`logback-spring.xml`): route `REQUEST_LOG` logger to a separate `requests.log` file in JSON format for easy parsing.

---

### A3 — Grafana Dashboard

**`grafana/dashboards/dutydesk-comparison.json`**

Panels to configure:
1. **Throughput** — `rate(http_server_requests_seconds_count[1m])` — one line per architecture
2. **p50 Latency** — `histogram_quantile(0.5, rate(http_server_requests_seconds_bucket[1m]))`
3. **p95 Latency** — `histogram_quantile(0.95, ...)`
4. **p99 Latency** — `histogram_quantile(0.99, ...)`
5. **JVM Heap Used** — `jvm_memory_used_bytes{area="heap"}`
6. **CPU Usage** — `process_cpu_usage`
7. **DB Pool Active Connections** — `hikaricp_connections_active`
8. **Error Rate** — `rate(http_server_requests_seconds_count{status=~"5.."}[1m])`

Export this dashboard JSON and commit it to both repos. Provision it automatically via Grafana volume mount.

---

## Part B — Gatling Load Tests

Location: `duty-manager-load-tests/` — a separate directory (can live in the microservices repo under `load-tests/` for simplicity).

### B1 — Project Structure

```
load-tests/
├── pom.xml                          (Gatling Maven plugin)
├── src/
│   └── test/
│       └── scala/
│           └── dutydesk/
│               ├── Feeders.scala    (test data)
│               ├── Protocols.scala  (base HTTP config)
│               ├── ScenarioA.scala  (write-heavy)
│               ├── ScenarioB.scala  (read-heavy)
│               ├── ScenarioC.scala  (mixed realistic)
│               └── DutyDeskSimulation.scala (runs all)
└── results/
    ├── monolith/
    └── microservices/
```

**`pom.xml`** — uses `gatling-maven-plugin`, `gatling-charts-highcharts`, Scala 2.13.

**`Protocols.scala`**
```scala
object Protocols {
  val baseUrl: String = sys.env.getOrElse("TARGET_URL", "http://localhost:80")

  val httpProtocol = http
    .baseUrl(baseUrl)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .header("Authorization", "Bearer ${jwt}")
}
```

**`Feeders.scala`**
- `participantFeeder`: CSV with pre-seeded participant UUIDs and JWTs
- `templateFeeder`: CSV with pre-seeded template UUIDs
- `dateRangeFeeder`: generates `from` / `to` parameters covering last 30 days

---

### B2 — Scenario A: Write-Heavy (Record Execution Fact)

**Purpose:** Stress the write path — `POST /api/v1/execution-facts`. In microservices, this also triggers a participant-service lookup. This directly measures the cross-service overhead cost.

```scala
object ScenarioA {
  val scn = scenario("Write-Heavy: Record Execution Fact")
    .feed(participantFeeder.circular)
    .feed(templateFeeder.random)
    .exec(
      http("Record Execution Fact")
        .post("/api/v1/execution-facts")
        .body(StringBody("""{"executorId":"${executorId}","templateId":"${templateId}"}"""))
        .check(status.is(201))
        .check(jsonPath("$").saveAs("factId"))
    )
    .pause(100.milliseconds)
    .exec(
      http("Finish Execution Fact")
        .post("/api/v1/execution-facts/finished")
        .body(StringBody(""""${factId}""""))
        .check(status.is(200))
    )
}
```

---

### B3 — Scenario B: Read-Heavy (Get Execution Facts by Date Range)

**Purpose:** Stress the read path — `GET /api/v1/execution-facts?from=...`. In microservices, resolving each fact's executor name requires N participant-service calls (N = page size). This is the N+1 query problem manifested as N+1 HTTP calls.

```scala
object ScenarioB {
  val scn = scenario("Read-Heavy: Get Execution Facts by Date Range")
    .feed(dateRangeFeeder.circular)
    .exec(
      http("Get Execution Facts")
        .get("/api/v1/execution-facts")
        .queryParam("from", "${from}")
        .queryParam("to", "${to}")
        .queryParam("pageSize", "50")
        .check(status.is(200))
    )
    .pause(200.milliseconds)
    .exec(
      http("Get Templates")
        .get("/api/v1/templates")
        .check(status.is(200))
    )
}
```

---

### B4 — Scenario C: Mixed Realistic Load (70/30)

**Purpose:** Simulate real-world usage. 70% read operations, 30% writes. Most representative for the thesis comparison because it reflects actual DutyDesk usage patterns.

```scala
object ScenarioC {
  val scn = scenario("Mixed Realistic Load")
    .randomSwitch(
      70.0 -> exec(ScenarioB.readSteps),
      30.0 -> exec(ScenarioA.writeSteps)
    )
}
```

---

### B5 — Load Profiles

**`DutyDeskSimulation.scala`**

Three load profiles, each run separately, results saved to `results/monolith/` or `results/microservices/`:

```scala
setUp(
  ScenarioC.scn.inject(
    rampUsers(50).during(60.seconds),     // ramp to 50 users over 1 min
    constantUsersPerSec(50).during(5.minutes),  // sustain 5 min
    rampUsersPerSec(50).to(100).during(60.seconds), // ramp to 100
    constantUsersPerSec(100).during(5.minutes),
    rampUsersPerSec(100).to(200).during(60.seconds), // ramp to 200
    constantUsersPerSec(200).during(5.minutes)
  )
).protocols(Protocols.httpProtocol)
 .assertions(
   global.responseTime.percentile(95).lt(2000),
   global.successfulRequests.percent.gt(99.0)
 )
```

---

### B6 — Running Tests

**Against monolith:**
```bash
# Start monolith
cd duty-manager-backend
docker-compose up -d

# Run tests
cd load-tests
TARGET_URL=http://localhost:8080 mvn gatling:test -Dgatling.simulationClass=dutydesk.DutyDeskSimulation
mv target/gatling/* results/monolith/
```

**Against microservices:**
```bash
# Start microservices
cd duty-manager-microservices
docker-compose up -d

# Run tests (gateway on port 80)
cd load-tests
TARGET_URL=http://localhost:80 mvn gatling:test -Dgatling.simulationClass=dutydesk.DutyDeskSimulation
mv target/gatling/* results/microservices/
```

---

## Part C — Comparison Data Collection

### C1 — Metrics to Record Per Run

For each scenario (A, B, C) × each architecture (monolith, microservices) × each load level (50, 100, 200 users):

| Metric | Source |
|--------|--------|
| Throughput (req/s) | Gatling HTML report |
| p50 response time (ms) | Gatling HTML report |
| p95 response time (ms) | Gatling HTML report |
| p99 response time (ms) | Gatling HTML report |
| Error rate (%) | Gatling HTML report |
| Peak JVM heap (MB) | Grafana / Prometheus |
| Avg CPU usage (%) | Grafana / Prometheus |
| DB pool saturation | Grafana / Prometheus |
| Inter-service call latency (ms) | Grafana — duty-service client metrics |

**Run each scenario 3 times. Report the median run.**

### C2 — Deployment Complexity Table (manual, for thesis)

| Metric | Monolith | Microservices |
|--------|----------|---------------|
| Docker containers | 2 (app + db) | 6 (3 services + 3 DBs + gateway) |
| docker-compose lines | ~30 | ~120 |
| application.yml files | 1 | 3 |
| Total config lines | ~80 | ~280 |
| Boot time (all services ready) | measure | measure |
| Total RAM at idle (MB) | measure | measure |
| Endpoints to check on failure | 1 | 3+ |

### C3 — Inter-Service Overhead Isolation

To isolate the cross-service call overhead specifically:

1. Add a custom Micrometer timer around `ParticipantServiceClient.getParticipant()` in duty-service
2. Record avg, p95, p99 of that timer during Scenario B runs
3. Express as percentage of total request time → this is the "microservice tax" figure for the thesis

---

## Part D — Final Checklist Before Thesis Writing

### Monolith repo (`duty-manager-backend`)
- [ ] Actuator + Prometheus endpoint working
- [ ] Request logging interceptor producing JSON logs
- [ ] Grafana dashboard provisioned via Docker Compose
- [ ] All existing endpoints still passing manual smoke test

### Microservices repo (`duty-manager-microservices`)
- [ ] All 3 services instrumented with Actuator + Prometheus
- [ ] duty-service has request logging interceptor
- [ ] Grafana dashboard provisioned (same panels, different data sources)
- [ ] Inter-service call timer metric present in Prometheus

### Load tests
- [ ] All 3 scenarios run cleanly against monolith
- [ ] All 3 scenarios run cleanly against microservices
- [ ] Results saved in `results/monolith/` and `results/microservices/`
- [ ] 3 runs completed per scenario per architecture

### Thesis data
- [ ] Comparison tables filled in (C1 and C2 above)
- [ ] Inter-service overhead percentage calculated (C3)
- [ ] Grafana screenshots taken during peak load for both architectures
- [ ] Gatling HTML reports archived as Appendix D and E

---

## Deliverables at End of Session 3

1. `prometheus/prometheus.yml` — scrape configs for both repos
2. `grafana/dashboards/dutydesk-comparison.json` — provisioned dashboard
3. `RequestLoggingInterceptor.java` — in monolith and duty-service
4. `load-tests/` — complete Gatling project with all 3 scenarios
5. `results/monolith/` and `results/microservices/` — Gatling HTML reports
6. Raw metrics data exported from Prometheus for both architectures
7. Filled comparison tables ready to paste into Chapter 4
