# 🚀 Spring Boot Microservices Project

A production-style Microservices architecture built using Spring Boot and Spring Cloud.

This project demonstrates service discovery, centralized configuration, inter-service communication using Feign Client, and environment-based configuration management.

---

## 🏗️ Architecture Overview

This project follows a distributed microservices architecture:

- **Config Server** – Centralized configuration management
- **Eureka Server** – Service registry & discovery
- **Item Service** – Manages item data
- **Order Service** – Manages orders and communicates with Item Service via Feign Client

---

## 📦 Tech Stack

- Java 17+
- Spring Boot
- Spring Cloud
- Eureka Server
- Spring Cloud Config Server
- OpenFeign
- MySQL (Production profile)
- Maven


## ⚙️ Microservices Details

### 🔹 Config Server
- Runs on port `8888`
- Fetches configuration from GitHub config repository
- Provides externalized configuration to services

### 🔹 Eureka Server
- Runs on port `8761`
- Handles service registration and discovery

### 🔹 Item Service
- Registers with Eureka
- Exposes item-related REST APIs
- Loads configuration from Config Server
- Supports environment-based profiles (dev / prod)

### 🔹 Order Service
- Registers with Eureka
- Uses Feign Client to call Item Service
- Loads configuration from Config Server

### 🌍 Profiles

Two environment profiles are supported:

### ✅ Dev (Default)
Used for local development.

### ✅ Prod
Used for production with MySQL configuration.

---

## 🗄️ Database Configuration (Production Example)
```yaml
spring:
  datasource:
    url: jdbc:mysql://prod-db:3306/itemsdb
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: prod_user
    password: prod_pass
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

logging:
  level:
    root: INFO
 ```


▶️ How To Run The Project

1️⃣ Start Config Server
cd config-server
mvn spring-boot:run

2️⃣ Start Eureka Server
cd eureka-server
mvn spring-boot:run

3️⃣ Start Item Service
cd item-service
mvn spring-boot:run

4️⃣ Start Order Service
cd order-service
mvn spring-boot:run

🧪 Testing

🔹 Eureka Dashboard
http://localhost:8761

You should see registered services:
ITEM-SERVICE
ORDER-SERVICE

🔹 Test APIs (Example)
http://localhost:{service-port}/api/...


You can test using:
Postman
Browser (GET APIs)

👩‍💻 Author
Ritika Khanduri
