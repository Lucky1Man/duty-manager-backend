# DutyDesk — Project Overview

> This document captures the full picture of what is being built, why, and how. Read this first before opening any other plan file.

---

## What This Project Is

**DutyDesk** is a browser-based system for managing organizational duty execution. Participants record execution facts (duties they performed), testify each other's work, and administrators manage duty templates and confirm completions.

**Student:** Цяцяк Орест Володимирович, ІСТ-41, НЛТУ України
**Supervisor:** доц. Павлюк У.В.
**Year:** 2026

---

## The Master's Thesis Topic

> «Дослідження ефективності монолітної та мікросервісної архітектури на прикладі системи DutyDesk»
> A Study of the Efficiency of Monolithic and Microservice Architectures Using the DutyDesk System as a Case Study

The thesis is not just about building DutyDesk — it is about **using DutyDesk as a vehicle to compare two architectures**. The system gets built twice: once as a monolith (this repo), once decomposed into microservices (new repo). Both are then load tested and the results are compared in Chapter 4.

The core research question: **under what conditions does each architecture perform better, and at what cost?**

---

## Current State of This Repo (Monolith)

**Repository:** `duty-manager-backend` | **Branch:** `basic-functionality`

This is a complete, working Spring Boot monolith. Core tech stack:

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA |
| Database | PostgreSQL (single shared schema) |
| Auth | JWT (jjwt), stateless, 18h expiry |
| Docs | OpenAPI / Swagger |
| Deploy | Docker |

**Domain entities:** `Participant`, `Role`, `Template`, `ExecutionFact`, `Testimony`

**4 REST controllers:** `AuthenticationController`, `ParticipantController`, `TemplateController`, `ExecutionFactController`

**What is NOT yet in this repo** (to be added in Session 3):
- Spring Actuator + Micrometer + Prometheus metrics
- Request performance logging interceptor
- Grafana dashboard
- Gatling load test suite

---

## The Two-Repo Strategy

| Repo | Purpose | Status |
|------|---------|--------|
| `duty-manager-backend` | Monolith — the baseline | Functionally complete, needs instrumentation |
| `duty-manager-microservices` | Microservices — the comparison target | Not yet created |

A third directory `load-tests/` (likely inside the microservices repo) will hold the Gatling scenarios that run against **both** architectures using only an environment variable to switch the target URL.

**Why not a shared Maven parent or a git branch?**
A separate repo was chosen because:
1. Both architectures must run simultaneously for load testing
2. Code duplication between services is itself a thesis data point (operational overhead)
3. It mirrors how real microservice migrations actually work

---

## Microservice Decomposition Plan

The monolith splits into 3 independent Spring Boot services based on DDD bounded contexts:

```
auth-service        (port 8081) — JWT issuance, credential storage
participant-service (port 8082) — user profiles, registration, password management
duty-service        (port 8083) — templates, execution facts, testimonies (core domain)
```

Plus infrastructure:
- **Nginx** — API gateway, routes by path prefix
- **3 × PostgreSQL** — one schema per service, no cross-DB foreign keys
- **Prometheus + Grafana** — metrics for comparison

**Key architectural difference from the monolith:**
`duty-service` no longer has a JPA join to `Participant`. Instead it stores `executorId` (UUID) and calls `participant-service` via HTTP (`RestClient`) to resolve executor info at query time. This cross-service HTTP call is the central overhead being measured in the thesis.

---

## 5 Key Code Changes (Thesis-Driven)

These changes close the gap between "functional system" and "thesis-ready system." Detailed plans are in `THESIS_PLAN.md`.

| # | Change | Which repo | Thesis section it feeds |
|---|--------|-----------|------------------------|
| 1 | Spring Actuator + Micrometer + Prometheus | Both | Chapter 4 — quantitative metrics |
| 2 | Request performance logging interceptor | Both | Chapter 4 — per-request dataset |
| 3 | Domain boundary refactoring (bounded contexts) | Monolith | Chapter 2 — decomposition design |
| 4 | Gatling load test suite | Standalone | Chapter 4 — comparison graphs |
| 5 | Multi-profile Docker Compose | Both | Chapter 4 — operational complexity |

---

## Implementation Sessions

Work is split into 3 focused sessions. Detailed plans are in `SESSION_1_PLAN.md`, `SESSION_2_PLAN.md`, `SESSION_3_PLAN.md`.

### Session 1 — auth-service + participant-service + Infrastructure (~25–30k tokens)
Create the microservices repo, scaffold `auth-service` and `participant-service`, wire up Nginx and Docker Compose. **Ends with:** two services booting and communicating through the gateway.

### Session 2 — duty-service (~30–40k tokens)
Implement the largest service — templates, execution facts, testimonies — with the `ParticipantServiceClient` REST calls replacing JPA joins. Wire into the full stack. **Ends with:** full end-to-end business workflow working across all 3 microservices.

### Session 3 — Observability + Load Tests (~20–25k tokens)
Instrument both repos with Actuator/Prometheus, add request logging, build Gatling scenarios, run tests against both architectures, collect comparison data. **Ends with:** filled data tables ready to write Chapter 4.

---

## Thesis Report Structure (Summary)

Full structure is in `THESIS_STRUCTURE.md`. High-level:

| Chapter | Content | Key output |
|---------|---------|------------|
| Introduction | Problem, goals, tasks, methods | Research questions defined |
| Chapter 1 | Theory: monolith, microservices, metrics methodology, tech stack | Literature review |
| Chapter 2 | Domain analysis, requirements, DDD bounded contexts, both architecture designs | UML diagrams, design decisions |
| Chapter 3 | Implementation of both versions, all 5 code changes explained | Code listings |
| Chapter 4 | Load test results, comparison tables, discussion of trade-offs | Data tables, Grafana screenshots |
| Conclusions | What was found, under what conditions each architecture wins | Quantitative summary |

**Estimated volume:** ~110–115 pages including appendices.

---

## Key Design Decisions Made

| Decision | Rationale |
|----------|-----------|
| Separate repo for microservices | Need both running simultaneously for load tests |
| No shared Maven parent | Sharing code obscures the architectural differences being studied |
| Synchronous REST between services | Simpler to measure overhead vs async; matches scope of a Master's thesis |
| No auth in duty-service DB | Passwords never leave auth domain — demonstrates data ownership principle |
| executor_id as plain UUID (no DB FK) | Cross-DB foreign keys are impossible; application-layer integrity is a microservices trade-off |
| Gatling for load testing | JVM-native, code-based scenarios, reproducible, rich HTML reports |
| Prometheus + Grafana | Industry standard, integrates with Spring via Micrometer, visual proof for thesis |

---

## Files in This Repo Related to Thesis Planning

| File | Contents |
|------|---------|
| `PROJECT_OVERVIEW.md` | This file — full picture |
| `THESIS_PLAN.md` | The 5 key code changes with deliverables |
| `THESIS_STRUCTURE.md` | Chapter-by-chapter report structure (~110 pages) |
| `SESSION_1_PLAN.md` | Implementation plan: auth-service, participant-service, infra |
| `SESSION_2_PLAN.md` | Implementation plan: duty-service |
| `SESSION_3_PLAN.md` | Implementation plan: observability, Gatling, data collection |
