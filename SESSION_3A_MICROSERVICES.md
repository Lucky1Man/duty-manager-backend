# Session 3A — Observability (Microservices Repo)

**Goal:** Instrument all three microservices with Actuator + Prometheus metrics, add a Micrometer timer around the cross-service HTTP call in `duty-service`, add request logging to `duty-service`, and wire Prometheus + Grafana into Docker Compose. After this session Grafana is live and all thesis metrics are being collected.

**Repo:** `duty-manager-microservices`
**Prerequisite:** Sessions 1 and 2 complete. Full stack boots and passes manual smoke test.

---

## Step 1 — Actuator + Prometheus in All Three Services

### 1.1 — pom.xml (repeat for all three services)

Add to `auth-service/pom.xml`, `participant-service/pom.xml`, `duty-service/pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

### 1.2 — application.yml (repeat for all three services)

Add to each service's `application.yml`:

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

---

## Step 2 — Micrometer Timer on ParticipantServiceClient

This is the most thesis-critical instrumentation. It measures exactly how much time `duty-service` spends on cross-service HTTP calls — this becomes the "microservice tax" figure in Chapter 4.

In `duty-service`, inject `MeterRegistry` into `ParticipantServiceClient` and wrap the HTTP call:

```java
@Component
public class ParticipantServiceClient {

    private final RestClient restClient;
    private final MeterRegistry meterRegistry;

    public ParticipantDTO getParticipant(UUID participantId) {
        return meterRegistry.timer("participant.service.call", "operation", "getParticipant")
            .record(() -> restClient.get()
                .uri("/api/v1/participants/{id}", participantId)
                .retrieve()
                .body(ParticipantDTO.class));
    }
}
```

This exposes `participant_service_call_seconds` in Prometheus. During Scenario B (read-heavy load tests), the ratio of this timer to the total `http_server_requests_seconds` for the same endpoint is the overhead percentage.

---

## Step 3 — RequestLoggingInterceptor in duty-service

### 3.1 — Create the interceptor

`duty-service/src/main/java/.../interceptor/RequestLoggingInterceptor.java`:

```java
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger("REQUEST_LOG");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        long duration = System.currentTimeMillis() - (long) request.getAttribute("startTime");
        String principal = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName).orElse("anonymous");
        log.info("{\"endpoint\":\"{} {}\",\"status\":{},\"duration_ms\":{},\"user\":\"{}\"}",
                request.getMethod(), request.getRequestURI(),
                response.getStatus(), duration, principal);
    }
}
```

### 3.2 — Register in WebMvcConfig

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RequestLoggingInterceptor requestLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLoggingInterceptor);
    }
}
```

### 3.3 — logback-spring.xml

`duty-service/src/main/resources/logback-spring.xml` — route `REQUEST_LOG` to stdout so Docker captures it:

```xml
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{ISO8601} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <logger name="REQUEST_LOG" level="INFO" additivity="false">
        <appender-ref ref="CONSOLE"/>
    </logger>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

---

## Step 4 — prometheus/prometheus.yml

Create `prometheus/prometheus.yml` at the repo root:

```yaml
global:
  scrape_interval: 5s

scrape_configs:
  - job_name: auth-service
    static_configs:
      - targets: ['auth-service:8081']
    metrics_path: /actuator/prometheus

  - job_name: participant-service
    static_configs:
      - targets: ['participant-service:8082']
    metrics_path: /actuator/prometheus

  - job_name: duty-service
    static_configs:
      - targets: ['duty-service:8083']
    metrics_path: /actuator/prometheus
```

---

## Step 5 — Grafana Provisioning

### Directory structure to create:

```
grafana/
├── provisioning/
│   ├── datasources/
│   │   └── prometheus.yml
│   └── dashboards/
│       └── dashboard.yml
└── dashboards/
    └── dutydesk.json
```

`grafana/provisioning/datasources/prometheus.yml`:
```yaml
apiVersion: 1
datasources:
  - name: Prometheus
    type: prometheus
    url: http://prometheus:9090
    isDefault: true
```

`grafana/provisioning/dashboards/dashboard.yml`:
```yaml
apiVersion: 1
providers:
  - name: DutyDesk
    folder: DutyDesk
    type: file
    options:
      path: /var/lib/grafana/dashboards
```

`grafana/dashboards/dutydesk.json` — dashboard with these panels (build in the Grafana UI first, then export as JSON):

| # | Panel | PromQL |
|---|-------|--------|
| 1 | Throughput (req/s) | `rate(http_server_requests_seconds_count[1m])` |
| 2 | p50 Latency | `histogram_quantile(0.5, rate(http_server_requests_seconds_bucket[1m]))` |
| 3 | p95 Latency | `histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[1m]))` |
| 4 | p99 Latency | `histogram_quantile(0.99, rate(http_server_requests_seconds_bucket[1m]))` |
| 5 | Cross-service call p95 | `histogram_quantile(0.95, rate(participant_service_call_seconds_bucket[1m]))` |
| 6 | JVM Heap | `jvm_memory_used_bytes{area="heap"}` |
| 7 | CPU Usage | `process_cpu_usage` |
| 8 | DB Pool Active | `hikaricp_connections_active` |
| 9 | Error Rate | `rate(http_server_requests_seconds_count{status=~"5.."}[1m])` |

---

## Step 6 — Add Prometheus + Grafana to docker-compose.yml

Append to the `services:` block in `docker-compose.yml`:

```yaml
  prometheus:
    image: prom/prometheus:latest
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml:ro
    expose:
      - "9090"
    depends_on:
      - auth-service
      - participant-service
      - duty-service

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      GF_SECURITY_ADMIN_PASSWORD: admin
      GF_USERS_ALLOW_SIGN_UP: "false"
    volumes:
      - grafana-data:/var/lib/grafana
      - ./grafana/provisioning:/etc/grafana/provisioning:ro
      - ./grafana/dashboards:/var/lib/grafana/dashboards:ro
    depends_on:
      - prometheus
```

Add `grafana-data:` to the `volumes:` section at the bottom.

---

## Verification

After `docker compose up -d --build`:

1. `curl http://localhost:80/actuator/prometheus` — proxied via Nginx? If not, hit each service directly via `docker exec` or expose ports temporarily for testing
2. Prometheus at `http://localhost:9090` → Status → Targets — all 3 jobs should be green
3. Grafana at `http://localhost:3000` (admin / admin) → DutyDesk dashboard should load with data

---

## Checklist

- [ ] Actuator + Prometheus deps in all 3 `pom.xml` files
- [ ] `management.endpoints` config in all 3 `application.yml` files
- [ ] Micrometer timer in `ParticipantServiceClient` — `participant_service_call_seconds` visible in Prometheus
- [ ] `RequestLoggingInterceptor` created and registered in `duty-service`
- [ ] `logback-spring.xml` created in `duty-service`
- [ ] `prometheus/prometheus.yml` created
- [ ] `grafana/provisioning/` directory structure created
- [ ] `grafana/dashboards/dutydesk.json` created with all 9 panels
- [ ] `prometheus` and `grafana` services added to `docker-compose.yml`
- [ ] All 3 Prometheus targets green
- [ ] Grafana dashboard loads and shows live data
