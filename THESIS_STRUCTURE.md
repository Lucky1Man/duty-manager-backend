# Master's Thesis Report Structure

**Title:** Дослідження ефективності монолітної та мікросервісної архітектури на прикладі системи DutyDesk
**English:** A Study of the Efficiency of Monolithic and Microservice Architectures Using the DutyDesk System as a Case Study

**Student:** Цяцяк Орест Володимирович
**Degree:** Master's (Магістр), Specialty 126 — Information Systems and Technologies
**Institution:** НЛТУ України
**Year:** 2026

---

## Front Matter

### Title Page
University, institute, department, work type (МАГІСТЕРСЬКА РОБОТА), topic, student info, supervisor, grade fields, signatures.

### Abstract (Анотація)
Two versions: Ukrainian and English. ~250 words each. Covers: problem statement, research object and subject, methods used, key results and conclusions. Mentions quantitative results (e.g., response time difference under load, deployment complexity metrics).

### Table of Contents (Зміст)
Auto-generated with page numbers. All sections down to subsection level.

### List of Abbreviations (Перелік скорочень)
API, REST, JWT, JPA, ORM, DB, JVM, CPU, RAM, CI/CD, DDD, SLA, RPS, DDL, UML, DTO, HTTP, HTTPS, JSON, GCP, NLTU, etc.

---

## Introduction (Вступ) — ~3 pages

- Relevance of the topic: explosion of distributed systems, cloud-native trend, need for evidence-based architecture decisions
- Research object: software architecture patterns for web information systems
- Research subject: performance, scalability, and operational characteristics of monolithic vs microservice architectures
- Goal: to experimentally compare efficiency metrics of both architectures on a real-world system
- Tasks:
  1. Analyze existing literature and prior comparisons
  2. Develop a working monolithic version of DutyDesk
  3. Design and implement the microservice decomposition
  4. Instrument both versions for performance measurement
  5. Conduct load testing and collect comparative data
  6. Analyze and draw conclusions
- Methods: empirical benchmarking, load testing, statistical analysis of results, UML modeling
- Novelty: comparison on a real system with domain-specific load (not synthetic), with decomposition justified by DDD
- Practical value: guidelines for architecture selection in similar task-management systems

---

## Chapter 1 — Theoretical Foundations and Literature Review (~20 pages)

### 1.1 Overview of Software Architecture Patterns
- Definition of software architecture, key quality attributes (performance, scalability, maintainability, deployability)
- Historical evolution: from mainframes to monoliths to SOA to microservices
- When architecture patterns matter: system size, team size, traffic profile

### 1.2 Monolithic Architecture
- Definition and characteristics: single deployable unit, shared database, in-process communication
- Subtypes: layered (n-tier), modular monolith, big ball of mud
- Advantages: simple development, easy debugging, no network overhead, ACID transactions across the whole domain
- Disadvantages: scaling entire app for one bottleneck, deployment risk, technology lock-in, growing complexity at scale
- Real-world examples where monolith is the right choice

### 1.3 Microservice Architecture
- Definition and characteristics: independent deployable services, service-per-database, inter-service communication via network
- Core principles: single responsibility, decentralized data management, design for failure
- Communication patterns: synchronous REST/gRPC, asynchronous messaging (Kafka, RabbitMQ)
- Service discovery, API gateway, circuit breaker patterns
- Advantages: independent scaling, technology heterogeneity, fault isolation, team autonomy
- Disadvantages: network latency, distributed transactions complexity, operational overhead, eventual consistency
- Real-world examples (Netflix, Amazon, Uber)

### 1.4 Comparison of Architectures in Academic and Industry Literature
- Review of key papers: Fowler & Lewis (2014), Newman (2015), Dragoni et al. (2017)
- Industry case studies: Amazon, Netflix migration stories
- Existing benchmarks: limitations of synthetic benchmarks vs domain-specific tests
- Gap in literature: lack of comparison on task-management/duty-tracking domain systems
- Conclusion: need for empirical evidence on a concrete system → motivation for this thesis

### 1.5 Performance Metrics and Measurement Methodology
- Definitions: throughput (req/s), latency (avg, p95, p99), error rate, resource utilization (CPU %, RAM MB)
- Load testing concepts: virtual users, ramp-up, steady state, spike test
- Tools overview: Gatling, JMeter, k6 — rationale for choosing Gatling (JVM-native, code-based scenarios, rich HTML reports)
- Observability stack: Prometheus (metrics scraping), Grafana (visualization), structured JSON logging
- Statistical validity: number of test runs, warm-up period, outlier handling

### 1.6 Technology Stack Used
- Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA
- PostgreSQL, Docker, Nginx
- Micrometer + Prometheus + Grafana for metrics
- Gatling for load testing
- Angular + Angular Material (frontend, out of scope for performance tests)

---

## Chapter 2 — System Analysis and Design (~18 pages)

### 2.1 Domain Analysis of DutyDesk
- Problem domain: organizational duty tracking — what it is, who uses it, why it matters
- Stakeholders: administrators, participants (executors)
- Core business processes: assign duty template → record execution fact → testify execution → admin confirms
- Data flows: sequence diagrams for main workflows

### 2.2 Requirements Specification
- Functional requirements (table): participant management, template CRUD, execution fact lifecycle, testimony, role-based access
- Non-functional requirements: response time < 500ms under 100 concurrent users, 99.9% availability, JWT-secured, timezone-aware
- Constraints: PostgreSQL as DB, REST API, stateless backend

### 2.3 Domain Model and Bounded Contexts
- Entity analysis: Participant, Role, Template, ExecutionFact, Testimony
- ER diagram
- Identification of bounded contexts using DDD:
  - **Auth context**: authentication, JWT lifecycle, credential management
  - **Participant context**: user registration, profile, password management
  - **Duty context**: templates, execution facts, testimonies — core business domain
- Context map showing relationships between bounded contexts
- Justification of why these are the natural microservice split points

### 2.4 Monolithic Architecture Design
- Layered architecture: Controller → Service → Repository → DB
- Package structure and component diagram
- Single shared PostgreSQL schema
- Security filter chain design
- Deployment model: single Docker container + one DB container

### 2.5 Microservice Architecture Design
- Service decomposition based on bounded contexts:
  - `auth-service`: POST /api/v1/auth/jwt, credential validation
  - `participant-service`: CRUD /api/v1/participants
  - `duty-service`: /api/v1/templates, /api/v1/execution-facts
- Inter-service communication: synchronous REST (duty-service calls participant-service for executor info)
- API gateway: Nginx routing rules
- Database-per-service: three separate PostgreSQL schemas
- Deployment model: 5 containers (3 services + 3 DBs + 1 Nginx)
- Comparison of deployment complexity (table: config lines, containers, network policies)

### 2.6 Database Design
- Schema: participants, roles, templates, execution_facts, testimonies
- Indexes: full-text search index on templates.name (already present), indexes on execution_facts date columns
- Migration strategy for splitting shared DB into per-service schemas

---

## Chapter 3 — Implementation (~25 pages)

### 3.1 Monolithic Implementation
- Project structure walkthrough (Maven module, package layout)
- Entity layer: JPA entities, optimistic locking with @Version
- Repository layer: Spring Data JPA, custom JPQL queries for date-range searches
- Service layer: business logic, ModelMapper configuration, role-based DTO mapping
- Security: JWT filter chain, stateless session, role-based endpoint authorization
- Exception handling: global @RestControllerAdvice, structured error responses
- Timezone handling: TimeZoneFilter, UTC normalization in service layer
- Key code listings: RecordExecutionFact flow end-to-end, finishExecution with ownership check

### 3.2 Observability Implementation (Change #1 from plan)
- Adding Spring Actuator + Micrometer dependencies
- Prometheus endpoint configuration
- Custom metrics: request counter per endpoint+role, response time histogram
- Grafana dashboard setup: panels for throughput, latency percentiles, JVM heap, DB pool
- Code listing: metrics configuration bean

### 3.3 Request Performance Logging Interceptor (Change #2 from plan)
- HandlerInterceptor implementation: preHandle / afterCompletion hooks
- Structured JSON log format: timestamp, endpoint, method, duration_ms, status, user_role
- Integration with SLF4J + Logback
- Code listing: interceptor class and WebMvcConfigurer registration

### 3.4 Domain Boundary Refactoring (Change #3 from plan)
- Package reorganization into auth / participant / duty domains
- Interface contracts at domain boundaries
- Before/after package structure comparison
- Explanation of which changes would be needed to extract each domain into a separate service

### 3.5 Microservice Implementation
- auth-service: extracted authentication logic, standalone Spring Boot app
- participant-service: extracted participant management, own DB schema
- duty-service: templates + execution facts + testimonies, calls participant-service via RestClient
- Nginx gateway configuration: routing rules, upstream definitions
- Docker Compose profiles: monolith vs microservices
- Inter-service call code listing: how duty-service fetches participant data

### 3.6 Load Testing Implementation (Change #4 from plan)
- Gatling scenario design for main workflows:
  - Scenario A: record execution fact (write-heavy)
  - Scenario B: get execution facts by date range (read-heavy)
  - Scenario C: mixed realistic load (70% read / 30% write)
- Load profiles: 50 / 100 / 200 concurrent virtual users, 5-minute sustained load
- Test data setup: pre-seeded participants and templates
- Running tests against monolith and microservices with identical data

---

## Chapter 4 — Testing and Results Analysis (~20 pages)

### 4.1 Test Environment
- Hardware: spec of machine(s) used (CPU, RAM, disk)
- Docker resource limits applied per container (to simulate realistic constraints)
- Test isolation: fresh DB state before each test run, JVM warm-up period (1 min discard)
- Number of runs per scenario: 3 runs, median reported

### 4.2 Monolith Baseline Results
- Scenario A (write-heavy): throughput (req/s), p50/p95/p99 latency, error rate at 50/100/200 users
- Scenario B (read-heavy): same metrics
- Scenario C (mixed): same metrics
- Resource utilization: CPU%, JVM heap usage, DB connection pool saturation
- Grafana screenshot: throughput and latency graphs over time

### 4.3 Microservice Results
- Same scenarios and metrics as 4.2
- Additional metrics: inter-service call latency (duty→participant REST call overhead)
- Container-level resource breakdown: per-service CPU and RAM

### 4.4 Comparative Analysis
- Side-by-side tables: monolith vs microservices for each scenario and metric
- Throughput comparison chart (bar graph)
- Latency percentile comparison chart (p50/p95/p99)
- Resource utilization comparison: total RAM, total CPU
- Overhead of microservice inter-service calls as % of total request time
- Startup time comparison: monolith boot time vs all 3 services boot time

### 4.5 Operational Complexity Comparison
- Config volume: lines of configuration (docker-compose, nginx, application.yml) for each approach
- Deployment steps count: how many commands / steps to deploy each version
- Number of failure points: what can go wrong in each architecture
- Observability complexity: how many services to check when debugging an issue

### 4.6 Discussion
- Under what load conditions does the monolith outperform microservices (low/medium traffic)?
- At what load does horizontal scaling of individual microservices become beneficial?
- Trade-off summary: performance overhead vs operational independence
- Limitations of this study: single-machine test, no auto-scaling, synchronous inter-service calls only
- What the results mean for systems like DutyDesk (task-management domain)

---

## Conclusions (Висновки) — ~2 pages

- Summary of what was built and measured
- Answer to the research question: which architecture is more efficient and under what conditions?
- Quantitative summary: key numbers from comparison (e.g., "microservices introduced X% latency overhead at p99 under 200 users")
- Recommendation: monolith is appropriate for DutyDesk-scale systems; microservices justified only when independent scaling of duty vs auth domains is required
- Future work: async messaging between services, auto-scaling simulation, adding a message queue

---

## References (Список використаних джерел) — ~20 sources

Includes:
- Fowler, M., Lewis, J. (2014). Microservices. martinfowler.com
- Newman, S. (2015). Building Microservices. O'Reilly
- Dragoni, N. et al. (2017). Microservices: Yesterday, Today, and Tomorrow
- Spring Boot official documentation
- Gatling documentation
- Prometheus / Micrometer documentation
- Docker documentation
- NIST definitions of cloud computing
- PostgreSQL documentation
- 2-3 Ukrainian/local academic sources on software architecture
- Industry reports (ThoughtWorks Technology Radar, State of DevOps)

---

## Appendices (Додатки)

### Appendix A — UML Diagrams
- Class diagram: full entity + service + controller model
- Component diagram: monolith architecture
- Component diagram: microservice architecture
- Sequence diagrams: record execution fact flow (monolith vs microservices, showing network hop)
- Deployment diagram: Docker containers for both profiles

### Appendix B — Database Schema (DDL)
Full SQL DDL for all tables, indexes, constraints.

### Appendix C — Key Code Listings
- JWT filter chain
- ExecutionFactServiceImpl (most complex service)
- Metrics interceptor
- Gatling load test scenario
- Nginx gateway config
- Docker Compose files (both profiles)

### Appendix D — Test Results Raw Data
Tables of all Gatling run results (all 3 runs per scenario) before median calculation.

### Appendix E — Grafana Dashboard Screenshots
Full-page screenshots of dashboards during monolith and microservice test runs.

---

## Estimated Volume

| Section | Pages |
|---------|-------|
| Front matter + Abstract | 4 |
| Introduction | 3 |
| Chapter 1 — Theory | 20 |
| Chapter 2 — Design | 18 |
| Chapter 3 — Implementation | 25 |
| Chapter 4 — Results | 20 |
| Conclusions | 2 |
| References | 3 |
| Appendices | 15–20 |
| **Total** | **~110–115** |
