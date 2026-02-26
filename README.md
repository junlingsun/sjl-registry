# SJL Registry

SJL Registry is a lightweight, modular Service Registry and Discovery system built with Spring Boot.

It provides service registration, deregistration, discovery, and long-polling change notifications. The project is designed as an educational and extensible foundation for understanding how service registries such as Eureka, Consul, or Nacos work internally.

---

## Table of Contents

- Overview
- Architecture
- Project Structure
- Features
- Getting Started
- API Reference
- Service Lifecycle
- Design Decisions
- Comparison with Production Registries
- Roadmap
- License

---

## Overview

SJL Registry implements a simple client–server registry model:

- Services register themselves with the registry server
- Clients query the registry to discover services
- Clients may monitor registry changes via long polling
- Services deregister when shutting down

The system is structured as a multi-module Maven project.

---

## Architecture

```
+------------------+        HTTP         +------------------+
|  Service Client  |  <--------------->  |  Registry Server |
+------------------+                     +------------------+
         |                                         |
         | register/remove/discover                |
         |                                         |
         +---------------- Uses -------------------+
                           |
                    registry-common
```

The architecture separates client logic, shared models, and server implementation.

---

## Project Structure

```
sjl-registry
├── registry-client
├── registry-common
├── registry-server
└── pom.xml
```

### registry-server

Spring Boot application exposing REST endpoints for:

- Service registration
- Service removal
- Service discovery
- Service monitoring (long polling)

Layered structure:

- controller
- service
- dao
- entity
- mapper

---

### registry-client

Client SDK used by services to:

- Register on startup
- Discover other services
- Monitor registry updates
- Deregister on shutdown

---

### registry-common

Shared components:

- `Service` entity
- `RegistryEntity`
- Unified response wrapper `R`
- HTTP utilities
- File utilities

---

## Features

- Service registration
- Service deregistration
- Service discovery
- Long-polling registry change notifications
- Modular Maven architecture
- Spring Boot REST API
- MyBatis persistence layer
- Asynchronous request handling using `DeferredResult`

---

## Getting Started

### Prerequisites

- Java 8 or higher
- Maven 3.6 or higher
- Optional: MySQL if persistence is configured

---

### Build

From the project root:

```bash
mvn clean install
```

---

### Run Registry Server

```bash
cd registry-server
mvn spring-boot:run
```

Or:

```bash
java -jar target/registry-server-*.jar
```

Default address (configurable in `application.yaml`):

```
http://localhost:8080
```

---

## API Reference

Base path:

```
/registry
```

### Register Service

POST `/registry/register`

Request:

```json
{
  "serviceName": "user-service",
  "ip": "127.0.0.1",
  "port": 8081,
  "metadata": {}
}
```

---

### Remove Service

POST `/registry/remove`

Request:

```json
{
  "serviceName": "user-service",
  "ip": "127.0.0.1",
  "port": 8081
}
```

---

### Discover Service

POST `/registry/discover`

Request:

```json
{
  "serviceName": "user-service"
}
```

Response:

```json
{
  "code": 200,
  "message": "success",
  "data": [...]
}
```

---

### Monitor Registry Changes

POST `/registry/monitor`

- Uses Spring `DeferredResult`
- Implements long-polling
- Returns when registry state changes

---

## Service Lifecycle

1. Service starts
2. Client registers service
3. Other services discover via registry
4. Service shuts down and deregisters
5. Monitoring clients receive updates

---

## Design Decisions

### Long Polling via DeferredResult

Instead of constant polling, the registry uses `DeferredResult` to:

- Reduce CPU usage
- Reduce unnecessary network traffic
- Provide near real-time updates

---

### Unified Response Wrapper

All API responses are wrapped in `R`:

```java
public class R {
    private int code;
    private String message;
    private Object data;
}
```

Benefits:

- Standardized API responses
- Consistent error handling
- Easier extensibility

---

## Comparison with Production Registries

| Feature                  | SJL Registry | Eureka | Consul |
|--------------------------|-------------|--------|--------|
| Service Registration     | Yes         | Yes    | Yes    |
| Service Discovery        | Yes         | Yes    | Yes    |
| Health Checks            | No          | Yes    | Yes    |
| Heartbeat Mechanism      | No          | Yes    | Yes    |
| Distributed Cluster      | No          | Yes    | Yes    |
| Key-Value Store          | No          | No     | Yes    |

SJL Registry is intentionally lightweight and focused on clarity and learning.

---

## Roadmap

### Reliability
- Add heartbeat mechanism
- Add TTL expiration
- Auto-remove inactive services

### Scalability
- Distributed cluster support
- Leader election
- In-memory cache optimization

### Observability
- Metrics integration
- Structured logging
- Tracing support

### DevOps
- Dockerfile
- Docker Compose setup
- GitHub Actions CI pipeline
- Integration tests
- OpenAPI documentation

---

## Example Usage

```bash
curl -X POST http://localhost:8080/registry/register \
     -H "Content-Type: application/json" \
     -d '{"serviceName":"order-service","ip":"127.0.0.1","port":9001}'
```

---
