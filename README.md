# Spring Boot Kafka

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Kafka](https://img.shields.io/badge/Spring%20Kafka-3.x-brightgreen.svg)](https://spring.io/projects/spring-kafka)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)


# Legacy Version (old-main branch)

The [`old-main`](https://github.com/rahul-ghadge/spring-boot-kafka/tree/old-main) branch contains
the original implementation of this project, kept for reference.

| Component | Legacy (`old-main`) | Enterprise (`master`) |
|---|---|---|
| Java | 1.8 | 21 |
| Spring Boot | 2.3.2.RELEASE | 3.4.5 |
| Spring Kafka | ~2.5.x (managed) | 3.x |
| Dependencies | Web, Kafka only | Web, Kafka, Actuator, Validation, Lombok, MapStruct, Prometheus, OpenAPI |

The current `master` branch is a full enterprise rewrite targeting **Java 21**,
**Spring Boot 3.4.5**, and **Spring Kafka 3.x** with additional features such as
dead-letter queues, retry policies, Prometheus metrics, OpenAPI docs, and Docker support.
---

---

# New code base with DLQ and exception handling

An enterprise-grade Spring Boot application demonstrating production-ready Apache Kafka integration with multiple producers, multiple consumers, dead-letter queue (DLQ) support, retry policies, Prometheus metrics, and OpenAPI documentation.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
  - [Local (without Docker)](#local-without-docker)
  - [With Docker Compose](#with-docker-compose)
- [API Reference](#api-reference)
- [Kafka Topics](#kafka-topics)
- [Monitoring & Metrics](#monitoring--metrics)
- [Testing](#testing)
- [How to Contribute](#how-to-contribute)

---

## Overview

This project serves as a reference implementation for integrating Apache Kafka with a Spring Boot application following enterprise software engineering principles. It demonstrates real-world patterns such as:

- Reliable message delivery with manual offset acknowledgement
- Idempotent producers with `acks=all` for exactly-once guarantees
- Dead-Letter Queue routing after exhausted retries
- Distributed tracing via correlation IDs propagated from HTTP headers through to Kafka message headers
- Structured audit logging via a dedicated Kafka topic
- Prometheus-compatible metrics and Spring Actuator health checks

---

## Features

| Feature | Description |
|---|---|
| **Multiple Producers** | Typed producers for both plain-text `String` and JSON domain objects (`SuperHero`) |
| **Multiple Consumers** | Concurrent consumers with manual offset commit for reliable processing |
| **Dead-Letter Queue** | Failed messages are automatically routed to `.DLT` topics after retry exhaustion |
| **Retry with Backoff** | Exponential backoff retry (configurable attempts, interval, and multiplier) |
| **Idempotent Producer** | `enable.idempotence=true` with `acks=all` to prevent duplicate publishes |
| **Correlation ID Tracing** | HTTP `X-Correlation-Id` header propagated through MDC and Kafka message headers |
| **Audit Logging** | Every consumed message triggers an async structured audit event to the audit topic |
| **Prometheus Metrics** | Per-topic publish/consume success and failure counters via Micrometer |
| **Custom Health Check** | Spring Actuator health indicator querying Kafka cluster metadata |
| **OpenAPI Docs** | Swagger UI auto-generated from annotated controllers |
| **Bean Validation** | Jakarta Validation on all request DTOs with structured error responses |
| **Docker Support** | Multi-stage `Dockerfile` and full `docker-compose.yml` stack (Zookeeper, Kafka, Kafka UI, Prometheus) |

---

## Architecture

```
HTTP Client
    │
    ▼
┌─────────────────────────────────────────────────────────┐
│               Spring Boot Application                   │
│                                                         │
│  ┌────────────┐   ┌──────────────┐   ┌───────────────┐  │
│  │ REST       │──▶│ ProducerSvc  │──▶│ KafkaTemplate│  │
│  │ Controller │   │ (Metrics,    │   │ (String/JSON) │  │
│  │ (Validated)│   │  CorrelId)   │   └───────┬───────┘  │
│  └────────────┘   └──────────────┘           │          │
│                                              ▼          │
│                                    ┌─────────────────┐  │
│                                    │   Apache Kafka  │  │
│                                    │  (3 partitions) │  │
│                                    └────────┬────────┘  │
│                                             │           │
│  ┌──────────────┐   ┌─────────────┐         │           │
│  │  AuditSvc    │◀──│ ConsumerSvc │◀────────┘          │
│  │  (async DLQ) │   │ (Manual Ack │                     │
│  └──────────────┘   │  + Metrics) │                     │
│                     └──────┬──────┘                     │
│                            │ on failure                 │
│                            ▼                            │
│                     ┌────────────┐                      │
│                     │ DLQ Topic  │                      │
│                     │  (.DLT)    │                      │
│                     └────────────┘                      │
└─────────────────────────────────────────────────────────┘
```

---

## Prerequisites

| Requirement | Version |
|---|---|
| Java JDK | 17+ |
| Apache Maven | 3.8+ |
| Apache Kafka | 3.x |
| Docker & Docker Compose | 24+ (optional, for containerised stack) |

> **Note:** When running with Docker Compose, a standalone Kafka installation is not required — the broker is provided by the compose stack.

---

## Project Structure

```
spring-boot-kafka/
├── src/
│   ├── main/
│   │   ├── java/com/arya/kafka/
│   │   │   ├── SpringBootKafkaApplication.java   # Application entry point
│   │   │   ├── config/
│   │   │   │   ├── KafkaConsumerConfig.java      # Consumer factories, retry, DLQ
│   │   │   │   ├── KafkaProducerConfig.java      # Producer factories, idempotence
│   │   │   │   ├── KafkaTopicConfig.java         # Programmatic topic creation
│   │   │   │   ├── KafkaTopicProperties.java     # Type-safe topic name binding
│   │   │   │   ├── OpenApiConfig.java            # Swagger / OpenAPI metadata
│   │   │   │   └── WebMvcConfig.java             # HTTP interceptor registration
│   │   │   ├── controller/
│   │   │   │   ├── KafkaController.java          # Publish REST endpoints
│   │   │   │   └── KafkaMetricsController.java   # Operational metrics endpoint
│   │   │   ├── dto/
│   │   │   │   ├── ApiResponse.java              # Standard response envelope
│   │   │   │   └── SuperHeroRequest.java         # Validated request DTO
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java   # Centralised exception mapping
│   │   │   │   └── KafkaPublishException.java    # Domain-specific publish error
│   │   │   ├── health/
│   │   │   │   └── KafkaHealthIndicator.java     # Custom Actuator health check
│   │   │   ├── interceptor/
│   │   │   │   └── CorrelationIdInterceptor.java # MDC correlation ID seeding
│   │   │   ├── model/
│   │   │   │   └── SuperHero.java               # Kafka domain model
│   │   │   ├── service/
│   │   │   │   ├── AuditService.java            # Async audit event publisher
│   │   │   │   ├── ConsumerService.java         # All topic consumers + DLQ
│   │   │   │   └── ProducerService.java         # All topic producers + metrics
│   │   │   └── util/
│   │   │       └── MessagePayloadValidator.java  # Shared payload guard clauses
│   │   └── resources/
│   │       └── application.yml                  # Full multi-profile configuration
│   └── test/
│       ├── java/com/arya/kafka/
│       │   ├── config/KafkaIntegrationTest.java  # Embedded-broker integration tests
│       │   ├── controller/KafkaControllerTest.java # MockMvc slice tests
│       │   └── producer/ProducerServiceTest.java  # Mockito unit tests
│       └── resources/
│           └── application.yml                  # Test profile (embedded Kafka)
├── monitoring/
│   └── prometheus.yml                           # Prometheus scrape configuration
├── docker-compose.yml                           # Full local dev stack
├── Dockerfile                                   # Multi-stage image build
└── pom.xml
```

---

## Configuration

All Kafka configuration is externalised in `src/main/resources/application.yml`.  
Key properties can be overridden via environment variables:

| Environment Variable | Default | Description |
|---|---|---|
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker address(es) |
| `KAFKA_GROUP_ID` | `kafka-enterprise-group` | Consumer group ID |
| `SPRING_PROFILES_ACTIVE` | _(none)_ | Set to `docker` when running in containers |

### Retry Configuration

```yaml
kafka:
  retry:
    max-attempts: 3          # Number of retry attempts before DLQ routing
    initial-interval-ms: 1000
    multiplier: 2.0          # Exponential backoff multiplier
    max-interval-ms: 10000
```

### Topic Names

All topic names are bound to `KafkaTopicProperties` and resolved from:

```yaml
spring.kafka.topics:
  message: message-topic
  superhero: superhero-topic
  notification: notification-topic
  audit: audit-topic
  message-dlt: message-topic.DLT
  superhero-dlt: superhero-topic.DLT
```

---

## Running the Application

### Local (without Docker)

**1. Start Kafka and Zookeeper**

If you have a local Kafka installation:

```bash
# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka broker
bin/kafka-server-start.sh config/server.properties
```

**2. Build and run the application**

```bash
mvn clean package -DskipTests
java -jar target/spring-boot-kafka-2.0.0.jar
```

Or run directly via Maven:

```bash
mvn spring-boot:run
```

The application starts on **http://localhost:8080**.

---

### With Docker Compose

The compose file starts the complete stack — Zookeeper, Kafka, Kafka UI, Prometheus, and the application — in a single command.

```bash
# Build and start all services
docker-compose up --build

# Start in detached mode
docker-compose up -d --build

# Stop all services
docker-compose down
```

**Service URLs after startup:**

| Service | URL | Description |
|---|---|---|
| Spring Boot App | http://localhost:8080 | Application |
| Swagger UI | http://localhost:8080/swagger-ui.html | API documentation |
| Actuator Health | http://localhost:8080/actuator/health | Health check |
| Kafka UI | http://localhost:8090 | Topic and consumer group inspector |
| Prometheus | http://localhost:9090 | Metrics dashboard |

---

## API Reference

The full interactive API reference is available via **Swagger UI** at `/swagger-ui.html`.

### Publish a plain-text message

```http
GET /kafka/publish?message=Hello+Kafka
```

**Response:**
```json
{
  "success": true,
  "message": "Message published successfully",
  "timestamp": "2024-05-08T10:30:00Z"
}
```

---

### Publish a SuperHero event

```http
POST /kafka/publish
Content-Type: application/json

{
  "name": "Tony Stark",
  "superName": "Iron Man",
  "profession": "Business",
  "age": 50,
  "canFly": true
}
```

**Response:**
```json
{
  "success": true,
  "message": "SuperHero event published successfully",
  "timestamp": "2024-05-08T10:30:00Z"
}
```

---

### Publish a batch of messages

```http
POST /kafka/publish/batch
Content-Type: application/json

{
  "messages": ["message one", "message two", "message three"]
}
```

---

### Publish a notification

```http
POST /kafka/notify
Content-Type: application/json

"system alert: disk usage at 90%"
```

---

### Get Kafka metrics

```http
GET /kafka/metrics
```

**Response:**
```json
{
  "success": true,
  "message": "Kafka metrics retrieved successfully",
  "data": {
    "kafka.publish.success[message-topic]": 42.0,
    "kafka.publish.failure[message-topic]": 0.0,
    "kafka.consume.success[superhero-topic]": 38.0,
    "kafka.dlq.received[message-topic.DLT]": 2.0
  }
}
```

---

## Kafka Topics

| Topic | Partitions | Purpose |
|---|---|---|
| `message-topic` | 3 | Plain-text string messages |
| `superhero-topic` | 3 | JSON SuperHero domain events |
| `notification-topic` | 3 | System notification messages |
| `audit-topic` | 1 | Structured audit log entries |
| `message-topic.DLT` | 1 | Dead-letter queue for `message-topic` |
| `superhero-topic.DLT` | 1 | Dead-letter queue for `superhero-topic` |

> Topics are created automatically on application startup via `KafkaTopicConfig`. Set `KAFKA_AUTO_CREATE_TOPICS_ENABLE=false` on the broker (as configured in `docker-compose.yml`) to ensure only application-defined topics exist.

---

## Monitoring & Metrics

### Spring Actuator

| Endpoint | Description |
|---|---|
| `/actuator/health` | Application and Kafka cluster health |
| `/actuator/metrics` | All Micrometer metric names |
| `/actuator/prometheus` | Prometheus-format scrape endpoint |

### Custom Metrics

The following counters are exported to Prometheus:

| Metric | Tags | Description |
|---|---|---|
| `kafka.publish.success` | `topic` | Messages successfully enqueued by the producer |
| `kafka.publish.failure` | `topic` | Messages that failed to enqueue |
| `kafka.consume.success` | `topic` | Messages successfully processed by a consumer |
| `kafka.consume.failure` | `topic` | Messages that failed consumer processing |
| `kafka.dlq.received` | `topic` | Records arriving at a DLQ topic |

### Correlation ID Tracing

Every HTTP request automatically receives an `X-Correlation-Id` response header. Pass this header in your request to propagate a client-defined correlation ID:

```http
GET /kafka/publish?message=test
X-Correlation-Id: my-trace-id-12345
```

The same ID is embedded in Kafka message headers and all log statements, enabling end-to-end tracing from the REST request through to consumer processing.

---

## Testing

### Run all tests

```bash
mvn test
```

### Test coverage

| Test Class | Type | Description |
|---|---|---|
| `ProducerServiceTest` | Unit | Validates producer delegation and argument guards using Mockito |
| `KafkaControllerTest` | Web layer slice | Tests HTTP status codes, validation errors, and response envelopes via MockMvc |
| `KafkaIntegrationTest` | Integration | Round-trip publish/consume test using an embedded Kafka broker |

> Integration tests use `@EmbeddedKafka` — no external broker is required to run them.

---






## How to Contribute

1. Fork the repository and create a feature branch:
   ```bash
   git checkout -b feature/your-feature-name
   ```
2. Follow the existing code style (Lombok, package-private constructors, JavaDoc on public APIs).
3. Add or update tests for any new behaviour.
4. Open a pull request with a clear description of the change and its motivation.

---

## License

This project is licensed under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0).
