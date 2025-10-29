# 🎟️ Event Service

## 📖 API Documentation

The Event Service includes **Javadoc-generated API documentation** for all public classes and methods.

* **Location:** [`docs/apidocs/index.html`](https://cs464-recycle-proj.github.io/event-service/apidocs/index.html)
* **Usage:** View endpoints, method signatures, and comments for developers integrating or contributing to the Event Service.
* **Example:** Open directly in your browser:

```bash
# From repo root
open docs/apidocs/index.html   # Mac/Linux
start docs\apidocs\index.html  # Windows
```

> 💡 Keep this folder updated by running: `mvn javadoc:javadoc`

---

### 🧭 Swagger / OpenAPI

Interactive REST API documentation is provided via **SpringDoc OpenAPI**.

* **Endpoints:**

  * OpenAPI JSON: `/v3/api-docs`
  * Swagger UI: `/swagger-ui.html`

Open locally:

```
http://localhost:8083/swagger-ui.html
```

---

## 📊 Test Coverage

The project includes **unit and integration tests** with coverage reports generated using **JaCoCo**.

* **Current coverage:** 92%

![Test Coverage](docs/coverage.png)

To regenerate coverage reports:

```bash
./mvnw clean test jacoco:report
open target/site/jacoco/index.html
```

---

## 📘 Description

**event-service** is a Spring Boot–based microservice responsible for **managing sustainability events** within the GreenLoop ecosystem.

Key features include:

* **Event CRUD operations** (admin-only create, update, delete)
* **Public event discovery** and search
* **Attendee management** (registration, deregistration, and attendance tracking)
* **QR Code check-ins** using **ZXing**
* **Event analytics** (open events, upcoming events, and participation stats)
* **User–Event relationships** tracking (upcoming and past events)

**Service Port:** `8083`
**Database:** Supabase (PostgreSQL) – Schema: `event_service`

---

## 🚀 Getting Started

### Prerequisites

* Docker & Docker Compose
* Java 21
* Maven (wrapper included)
* `.env` file with environment variables

### Environment Variables

Create `.env` in the `event-service` folder:

```env
DATABASE_URL=jdbc:postgresql://your-supabase-host.pooler.supabase.com:5432/postgres?sslmode=require
DATABASE_USERNAME=postgres.your_project_id
DATABASE_PASSWORD=your_database_password
```

See `.env.example` for a complete template.

---

## ⚙️ Configuration

### Application Properties

* **Server Port:** `server.port=8083`
* **Database:** PostgreSQL connection (from environment variables)
* **Hibernate:** `ddl-auto=update` (use `validate` in production)
* **Schema:** `event_service`
* **Actuator:** Health and info endpoints exposed (`/actuator/health`, `/actuator/info`)

---

## 🧪 Testing

```bash
# Run all tests
./mvnw test

# Run tests + generate coverage
./mvnw test jacoco:report

# Run specific test class
./mvnw test -Dtest=EventServiceTest

# Run with test profile
mvn test -Dspring.profiles.active=test
```

### Unit Tests

* `EventServiceTest` – Service layer unit tests with mocked repositories
* `EventControllerTest` – REST controller integration tests using MockMvc

---

## 🧩 Integration

This service integrates seamlessly with the **Gateway Service** and other GreenLoop microservices:

* Accepts authenticated requests with headers injected by Gateway:

  * `X-User-ID`
  * `X-User-Email`
  * `X-User-Role`
* Communicates with:

  * **User Service** – For validating users during event registration
  * **Gamification Service** – To award coins after attendance is marked

---

## 🔄 CI/CD Workflow

### Overview

1. **Build and Test**

   * JDK 21 setup, Maven caching
   * Compile and run unit tests
   * Upload test and coverage reports

2. **Code Quality**

   * Runs **Checkstyle** and **SpotBugs**
   * Performs **SonarCloud** analysis on `main` branch

3. **Docker Build**

   * Builds Docker image `greenloop-event-service:test`
   * Performs health check with `curl`
   * Pushes image on `main` branch

4. **Security Scans**

   * Uses **Trivy** and **OWASP Dependency Check**
   * Uploads scan reports as artifacts

---

## 🧱 Tech Stack

* **Java 21**
* **Spring Boot 3.5.6**
* **Spring Data JPA**
* **Supabase (PostgreSQL)**
* **ZXing (QR Code)**
* **Docker-ready**
* **Maven**
* **JaCoCo**, **Checkstyle**, **SpotBugs**, **SonarCloud**

---

## ⚙️ Static Analysis

### SpotBugs

```bash
./mvnw spotbugs:gui
./mvnw clean compile spotbugs:check
```

### Checkstyle

```bash
./mvnw checkstyle:check
```

### Javadoc & Coverage

```bash
./mvnw javadoc:javadoc
open target/site/apidocs/index.html

./mvnw clean test jacoco:report
open target/site/jacoco/index.html
```

---

## 📊 Monitoring

**Health Checks**

```bash
curl http://localhost:8083/actuator/health
curl http://localhost:8083/actuator/info
```

**Dockerfile Health Check**

```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1
```

---

## 🧠 Best Practices

* ✅ Javadoc for all public methods
* ✅ Consistent DTO and entity mapping
* ✅ Centralized exception handling
* ✅ Input validation & null checks
* ✅ Security-first design
* ✅ Code style via Checkstyle

---

## 👥 Contributing

1. Add Javadoc for new classes/methods
2. Write or update unit tests
3. Update README for new features
4. Follow the existing coding style
5. Maintain >80% test coverage

---

**GreenLoop Event Service** | Version 1.0 | Java 21 | Spring Boot 3.5.6