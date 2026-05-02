# ESM – English School Management System
## Enterprise Architecture Document

---

## 1. High-Level Architecture Diagram

```mermaid
flowchart TB
    subgraph Client["Client Layer"]
        WebApp["LMS Web App<br/>(Angular)"]
        Mobile["Mobile App"]
    end

    subgraph Gateway["API Gateway (Spring Cloud Gateway)"]
        RateLimit["Rate Limiter"]
        Auth["JWT Validation"]
        Routing["Routing"]
    end

    subgraph Services["Microservices"]
        CourseMS["Course Microservice<br/>:8083"]
        EnrollmentMS["Enrollment Microservice<br/>:8084"]
        UserMS["User Service<br/>:8085"]
    end

    subgraph EventBus["Event Bus (Kafka/RabbitMQ)"]
        Topic1["course.events"]
        Topic2["enrollment.events"]
    end

    subgraph Data["Data Layer"]
        Redis["Redis Cache"]
        CourseDB[(PostgreSQL<br/>Courses DB)]
        EnrollmentDB[(PostgreSQL<br/>Enrollments DB)]
    end

    subgraph Ops["Observability"]
        ELK["ELK Stack<br/>Logging"]
        Prometheus["Prometheus<br/>Metrics"]
        Grafana["Grafana<br/>Dashboards"]
    end

    WebApp --> Gateway
    Mobile --> Gateway
    Gateway --> RateLimit --> Auth --> Routing
    Routing --> CourseMS
    Routing --> EnrollmentMS
    Routing --> UserMS
    CourseMS --> Redis
    CourseMS --> CourseDB
    EnrollmentMS --> Redis
    EnrollmentMS --> EnrollmentDB
    CourseMS <--> EventBus
    EnrollmentMS <--> EventBus
    CourseMS --> ELK
    EnrollmentMS --> ELK
    CourseMS --> Prometheus
    EnrollmentMS --> Prometheus
    Prometheus --> Grafana
```

---

## 2. Course Microservice – Data Flow

```mermaid
flowchart LR
    subgraph CourseMS["Course Microservice"]
        Controller["REST Controller"]
        Service["Course Service"]
        Cache["Redis Cache"]
        Repo["JPA Repository"]
    end
    subgraph DB["Database"]
        Courses[(Courses)]
        Modules[(Modules)]
        Lessons[(Lessons)]
        Categories[(Categories)]
        Reviews[(Reviews)]
        Instructors[(Instructors)]
    end
    Controller --> Service
    Service --> Cache
    Service --> Repo
    Repo --> DB
```

---

## 3. Enrollment Microservice – Data Flow

```mermaid
flowchart LR
    subgraph EnrollmentMS["Enrollment Microservice"]
        Ctrl["Enrollment Controller"]
        Svc["Enrollment Service"]
        EventPub["Event Publisher"]
        Repo["JPA Repository"]
    end
    subgraph DB["Database"]
        Enrollments[(Enrollments)]
        Progress[(Progress)]
        Certificates[(Certificates)]
    end
    subgraph Events["Event Bus"]
        Kafka["Kafka Topics"]
    end
    Ctrl --> Svc
    Svc --> EventPub
    Svc --> Repo
    Repo --> DB
    EventPub --> Kafka
```

---

## 4. Technology Stack

| Component | Technology |
|-----------|------------|
| API Gateway | Spring Cloud Gateway |
| Course Service | Spring Boot 3.2, Spring Data JPA |
| Enrollment Service | Spring Boot 3.2, Spring Data JPA |
| Database | PostgreSQL (prod), H2 (dev) |
| Cache | Redis |
| Message Broker | Kafka (prod) / RabbitMQ (alt) |
| Auth | Spring Security + JWT |
| Docs | OpenAPI 3.0 / Swagger |
| Containerization | Docker, Kubernetes |
| CI/CD | GitHub Actions |
| Monitoring | Prometheus, Grafana |
| Logging | ELK (Elasticsearch, Logstash, Kibana) |
| Frontend | Angular 21 |

---

## 5. API Versioning

All APIs use path-based versioning: `/api/v1/...`
