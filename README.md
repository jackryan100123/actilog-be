# Actilog (Spring Boot)

A robust backend application built with **Spring Boot 3.x**, focused on secure user management, role-based access control (RBAC), and daily activity logging for analysts.

---

## 🚀 Key Features

### 🔐 Secure Authentication
- Implements **JWT (JSON Web Token)** with dedicated **access** and **refresh** token mechanisms.

### 🧩 Role-Based Access Control (RBAC)
- Supports a hierarchical role system:

SUPER_ADMIN > ADMIN > ANALYST 


### 👥 User Management
- Full **CRUD operations** for administrators to register, update, and delete users.

### 🔒 Account Controls
- Admins can toggle user status between **ACTIVE** and **INACTIVE** to instantly restrict access.

### 📝 Daily Activity Tracking
- Designed for analysts to log:
   - Case details
   - Tools used
   - Task status

### 📊 Analytics & Logging
- Dedicated endpoints for admins to:
   - View paginated activity logs
   - Access summary statistics

### 🚫 Token Blacklisting
- Secure logout functionality by blacklisting tokens **in-memory** until expiration.

### 📘 API Documentation
- Integrated **Swagger / OpenAPI** for easy testing of endpoints.

---

## 🛠️ Tech Stack

- **Framework:** Spring Boot 3.x
- **Security:** Spring Security 6.x
- **JWT Library:** JJWT (Java JWT)
- **Database:** Spring Data JPA (Hibernate)
- **API Documentation:** SpringDoc OpenAPI (Swagger)
- **Utilities:** Lombok, Jakarta Validation

---

## 📋 Prerequisites

- **Java:** JDK 17 or higher
- **Build Tool:** Maven 3.6+
- **Database:** Any SQL database supported by JPA (e.g., MySQL, PostgreSQL)

---

## ⚙️ Configuration

Set the following properties in your `application.properties` or `application.yml` file:

```properties
# JWT Configuration
jwt.secret=your_base64_encoded_secret_key_here
jwt.access-expiration=3600000   # 1 hour in ms
jwt.refresh-expiration=86400000 # 24 hours in ms

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/your_db
spring.datasource.username=your_username
spring.datasource.password=your_password


```

## 🔑 Initial Setup & Roles

The system is configured with a **hierarchical role structure**, where higher roles inherit permissions from lower ones.

### Default Super Admin
- On first run, the system initializes a **SUPER_ADMIN** account.

### Role Capabilities

#### SUPER_ADMIN
- Manage roles
- Manage all system users

#### ADMIN
- Register users
- Manage activities

#### ANALYST
- Create and manage their own daily activities

---

## 🔌 Core API Endpoints

### 🔐 Authentication (`/auth`)

- **POST `/auth/login`**  
  Authenticate and receive an access token and refresh cookie.

- **POST `/auth/refresh`**  
  Refresh an expired access token using the refresh cookie.

- **POST `/auth/logout`**  
  Invalidate the current token.

---

### 👨‍💼 Admin Operations (`/admin`)

- **GET `/admin/users`**  
  List all system users  
  *(SUPER_ADMIN, ADMIN, MANAGER)*

- **POST `/admin/register`**  
  Register a new user  
  *(SUPER_ADMIN, ADMIN)*

- **PUT `/admin/users/{id}/status`**  
  Toggle user account status.

---

### 📝 Daily Activities (`/daily-activities`)

- **POST `/daily-activities`**  
  Create a new activity log *(ANALYST role)*

- **GET `/daily-activities`**  
  View activities with pagination and filtering.

---

## 🛡️ Security Implementation

The project uses a **stateless `JwtAuthenticationFilter`** that intercepts incoming requests and performs:

### Token Validation
- Expiration check
- Signature verification
- Token type validation *(Access vs Refresh)*

### Blacklist Check
- Ensures logged-out tokens cannot be reused

### User Status Check
- Automatically rejects requests from users marked as **INACTIVE**

---

## 📖 API Documentation

Once the application is running, access Swagger UI at:

```powershell

http://localhost:8080/swagger-ui/index.html
```