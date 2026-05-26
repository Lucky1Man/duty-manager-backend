# Session 3B — Observability (Monolith Repo)

**Goal:** Instrument the monolith (`duty-manager-backend`) with the same Actuator + Prometheus metrics and request logging as the microservices, and wire Prometheus + Grafana into its Docker Compose. After this session both architectures expose identical metrics and the Grafana dashboards are ready for side-by-side comparison during load tests.

**Repo:** `duty-manager-backend`
**Prerequisite:** Session 3A complete (microservices already instrumented and Grafana dashboard JSON exported).

---

## Step 1 — Actuator + Prometheus

### 1.1 — pom.xml

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

### 1.2 — application.yml

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

## Step 2 — RequestLoggingInterceptor

The monolith has a single codebase — one interceptor covers all endpoints.

### 2.1 — Create the interceptor

`src/main/java/.../interceptor/RequestLoggingInterceptor.java`:

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

### 2.2 — Register in WebMvcConfig

The monolith likely already has a `WebMvcConfigurer`. Add the interceptor registration there, or create a new config class:

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

### 2.3 — logback-spring.xml

`src/main/resources/logback-spring.xml`:

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

## Step 3 — prometheus/prometheus.yml

Create `prometheus/prometheus.yml` at the repo root. The monolith's Spring Boot app runs as a service named `app` in Docker Compose:

```yaml
global:
  scrape_interval: 5s

scrape_configs:
  - job_name: monolith
    static_configs:
      - targets: ['app:8080']
    metrics_path: /actuator/prometheus
```

---

## Step 4 — Grafana Provisioning

Copy the directory structure from `duty-manager-microservices`:

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

`grafana/provisioning/datasources/prometheus.yml` and `grafana/provisioning/dashboards/dashboard.yml` — identical to Session 3A.

`grafana/dashboards/dutydesk.json` — take the JSON exported from Session 3A and update the Prometheus job label:
- Change `job="duty-service"` (and other service jobs) to `job="monolith"` in panel queries, or use `job=~".+"` to match any job.
- Remove the cross-service call panel (panel 5 — `participant_service_call_seconds`) since there are no cross-service calls in the monolith.

The monolith dashboard should have 8 panels total (all except the cross-service timer).

---

## Step 5 — Add Prometheus + Grafana to docker-compose.yml

The monolith's `docker-compose.yml` likely defines two services: `app` and `db`. Append:

```yaml
  prometheus:
    image: prom/prometheus:latest
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml:ro
    expose:
      - "9090"
    depends_on:
      - app

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

Add `grafana-data:` to the `volumes:` section.

---

## Verification

After `docker compose up -d --build`:

1. `curl http://localhost:8080/actuator/prometheus` — should return Prometheus text format metrics
2. Prometheus at `http://localhost:9090` → Status → Targets — `monolith` job should be green
3. Grafana at `http://localhost:3000` (admin / admin) → DutyDesk dashboard should load with data

---

## Checklist

- [ ] Actuator + Prometheus deps in `pom.xml`
- [ ] `management.endpoints` config in `application.yml`
- [ ] `RequestLoggingInterceptor` created and registered
- [ ] `logback-spring.xml` created
- [ ] `prometheus/prometheus.yml` created with `monolith` job
- [ ] `grafana/provisioning/` directory structure created
- [ ] `grafana/dashboards/dutydesk.json` adapted (8 panels, `job="monolith"`)
- [ ] `prometheus` and `grafana` services added to `docker-compose.yml`
- [ ] `monolith` Prometheus target green
- [ ] Grafana dashboard loads and shows live data
