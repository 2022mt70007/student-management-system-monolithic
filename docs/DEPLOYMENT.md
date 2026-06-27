# Student Management System — Monolith Deployment

This module (`monolith-service`) is a **single Spring Boot application** that mirrors the microservices-based SMS. It exists for **comparison** of architecture styles (security, scalability, maintainability, deployment complexity).

## Architecture comparison

| Aspect | Monolith (this repo) |
|--------|----------------------|
| Processes | **1 JVM** |
| Database | **1 MySQL** database |
| API port | **`8090`** |

## Features (parity with microservices)

- Role-based users: **Admin**, **Teacher**, **Student**
- JWT authentication (same claims: email, role, profileId)
- Invitation-based registration with email + 6–7 digit code
- BCrypt password hashing
- Academic structure: departments, classes, subjects
- Course and notification management
- Student/teacher dashboards
- Field encryption for phone/address (AES-GCM)
- Input validation and global exception handling
- Swagger UI at `/swagger-ui.html`

## API contract

The monolith exposes the **same REST paths** as the gateway-backed microservices deployment:

- `POST /api/auth/login`, `/api/auth/register/*`
- `/api/admin/**` (including `/api/admin/academic/**`)
- `/api/students/**`
- `/api/teachers/**`
- `/api/courses/**`
- `/api/notifications/**`

Point the React frontend at the monolith:

```env
VITE_API_BASE_URL=http://localhost:8090
```

## Prerequisites

- **JDK 17**
- **Maven 3.9+**
- **Docker** (optional, for containerized run)
- **Node.js 18+** (for frontend)

## Build

From the repository root:

```bash
mvn clean package -pl monolith-service -am -DskipTests
```

Artifact: `monolith-service/target/monolith-service-1.0.0-SNAPSHOT.jar`

## Run locally (Maven)

### 1. Start MySQL

Use Docker:

```bash
docker run -d --name sms-mysql \
  -e MYSQL_DATABASE=sms_monolith_db \
  -e MYSQL_USER=sms_user \
  -e MYSQL_PASSWORD=sms_pass \
  -e MYSQL_ROOT_PASSWORD=root_pass \
  -p 3306:3306 mysql:8.4
```

Optional MailHog for email testing:

```bash
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog:v1.0.1
```

### 2. Configure environment

Create `.env` or export variables:

```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=sms_monolith_db
DB_USER=sms_user
DB_PASSWORD=sms_pass
JWT_SECRET=StudentManagementSystemSecretKeyForJWT2024MustBeLongEnough
APP_ENCRYPTION_KEY=LocalDevEncryptionKeyChangeInProd
MAIL_HOST=localhost
MAIL_PORT=1025
MAIL_FROM=admin@sms.local
BOOTSTRAP_ADMIN_EMAIL=you@example.com
REGISTRATION_BASE_URL=http://localhost:5173/register
APP_SECURITY_ALLOWED_ORIGINS=http://localhost:5173,http://127.0.0.1:5173
```

### 3. Start the monolith

```bash
java -jar monolith-service/target/monolith-service-1.0.0-SNAPSHOT.jar
```

Or:

```bash
mvn spring-boot:run -pl monolith-service
```

API: **http://localhost:8090**  
Swagger: **http://localhost:8090/swagger-ui.html**

### 4. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173**

### 5. Bootstrap admin

On first startup, `BOOTSTRAP_ADMIN_EMAIL` receives a registration invitation. Check MailHog (**http://localhost:8025**) or admin logs for the code, then complete registration at `/register`.

## Run with Docker Compose (recommended)

From the repository root:

```bash
docker compose up -d --build
```

The Docker image builds from this repo's `pom.xml` (`common-lib` + `monolith-service` only).

Services:

| Container | Port | Purpose |
|-----------|------|---------|
| `sms-monolith` | **8090** | Unified API |
| `sms-monolith-mysql` | 3306 | Single database |
| `sms-monolith-mailhog` | 8025 | Test email UI |

Check health:

```bash
docker compose ps
docker logs sms-monolith --tail 30
```

## Database

- **Engine:** MySQL 8.4
- **Database name:** `sms_monolith_db`
- **Schema:** Hibernate `ddl-auto: update` (tables created automatically)

All former per-service tables coexist in one database (`users`, `students`, `teachers`, `admins`, `courses`, `departments`, `academic_classes`, `subjects`, `notifications`, etc.).

## Security

- **JWT** validated by `JwtAuthenticationFilter` (same path/role rules as API gateway)
- **BCrypt** for passwords (`BCryptPasswordEncoder`)
- **Public endpoints:** login, registration, Swagger
- **Internal `/internal` endpoints:** blocked from external JWT access (used only as in-process service methods in monolith)
- **CORS:** configured via `APP_SECURITY_ALLOWED_ORIGINS`

## Deploy to a VM (production-style)

1. Copy the project to the server.
2. Set production `.env` (SES SMTP, strong `JWT_SECRET`, domain in `REGISTRATION_BASE_URL` and CORS).
3. Build and run:

```bash
docker compose up -d --build
```

4. Build frontend with `VITE_API_BASE_URL=https://your-domain` and serve via nginx.
5. Proxy `/api/` to `http://127.0.0.1:8090`.

Unlike the microservices stack, you only maintain **one** backend container and **one** database.

## Module layout

```
monolith-service/
  src/main/java/com/sms/
    auth/          # Authentication & invitations
    admin/         # Admin orchestration & email
    student/       # Student profiles & dashboard
    teacher/       # Teacher profiles & dashboard
    course/        # Courses & academic structure
    notification/  # Notifications
    monolith/      # Main class, security filter, shared config
```

Feign clients were removed; cross-domain calls use direct service injection.

## Comparison notes (for reports)

| Topic | Microservices | Monolith |
|-------|---------------|----------|
| **Deploy complexity** | Higher (many images, Eureka, gateway) | Lower (one JAR) |
| **Resource usage** | Higher baseline RAM | Lower |
| **Independent scaling** | Per service | Whole app scales together |
| **Failure isolation** | Better | Single process failure affects all |
| **Code coupling** | Loose (HTTP contracts) | Tighter (shared JVM) |
| **Security surface** | Gateway + each service | Single security filter chain |
| **DB transactions** | Cross-service sagas needed | Single DB ACID transactions possible |

## Troubleshooting

| Issue | Fix |
|-------|-----|
| MySQL connection refused | Wait for `mysql` healthcheck; verify `DB_HOST` |
| CORS errors | Set `APP_SECURITY_ALLOWED_ORIGINS` to frontend URL |
| 401 on admin APIs | Login as ADMIN; pass `Authorization: Bearer <token>` |
| Email not sent | Check MailHog (local) or SES credentials (production) |
| Port conflict on 3306 | Monolith MySQL uses host port **3307** by default; stop other MySQL or change the mapping |

## Stop

```bash
docker compose down
```

To remove DB data:

```bash
docker compose down -v
```
