# Case Management System - Dockerized Setup (IST)

This project consists of a **Spring Boot** backend and a **PostgreSQL** database with **pgAdmin**, segregated into two logical stacks sharing a common Docker network.

## 🏗 Architecture & Timezone

* **Timezone:** Asia/Kolkata (IST)
* **Network Name:** `case-net`
* **Infrastructure:** PostgreSQL + pgAdmin
* **Application:** Spring Boot (Multi-stage Docker build)

---

## 🚀 Getting Started

### 1. Prerequisites

* Docker & Docker Desktop installed.
* Ports **8080** (Application), **5433** (Database External), and **5050** (pgAdmin) must be available.

---

### 2. Startup Sequence

#### Step A: Start Infrastructure (Database & Network)

```powershell
docker-compose -f infra/docker-compose.infra.yaml up -d
```

#### Step B: Start Application  
```powershell
docker-compose -f docker-compose.app.yaml up -d --build
```

## 🛠 Management Commands

| Action | Command |
| --- | --- |
| View Application Logs | `docker logs -f springboot_app` |
| Stop Application | `docker-compose -f docker-compose.app.yaml down` |
| Stop Everything | `docker-compose -f docker-compose.app.yaml down; docker-compose -f infra/docker-compose.infra.yaml down` |
| Remove Unused Networks | `docker network prune` |

---

## 🔗 Connection Details

### Internal (Container-to-Container)

* **Database Host:** `db`
* **Database Port:** `5432`
* **Network:** `case-net`

---

### External (Laptop-to-Container)

* **Swagger UI:** http://localhost:8080/swagger-ui.html
* **pgAdmin UI:** http://localhost:5050  
  **Login:** `admin@admin.com` / `admin`
* **Local DB Tool:** `localhost:5433`

---

## 🐳 pgAdmin Setup

1. Open http://localhost:5050
2. Click **Add New Server**
3. Use the following connection details:
   * **Host:** `db`
   * **Port:** `5432`
   * **Maintenance DB:** `case_management`
   * **Username:** `admin`
   * **Password:** `admin123`

## 🕒 Timezone Verification

```powershell
docker exec springboot_app java -XshowSettings:properties -version
```
## Expected response
```powershell
user.timezone = Asia/Kolkata
```

##project structure 
```powershell
project-root/
├── infra/
│   └── docker-compose.infra.yaml
├── docker-compose.app.yaml
├── Dockerfile
├── README.md
└── src/
```