# Student Management System — Monolith

Standalone **monolithic** SMS: one Spring Boot app, one MySQL database, same React frontend and API contract as the microservices version (for architecture comparison).

**This repo is independent** from the microservices project at `D:\NewProject`.

## Quick start (Docker)

```powershell
cd D:\NewProjectMonolithic
copy .env.example .env
# Edit .env — set BOOTSTRAP_ADMIN_EMAIL to your email

docker compose up -d --build
```

| Service | URL |
|---------|-----|
| API | http://localhost:8090 |
| Swagger | http://localhost:8090/swagger-ui.html |
| MailHog | http://localhost:8025 |
| MySQL (host) | `localhost:3307` / `sms_monolith_db` |

**Frontend:**

```powershell
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173** — Vite proxies `/api` to **8090**.

## Project layout

```
D:\NewProjectMonolithic\
├── common-lib/          # Shared DTOs, JWT, validation
├── monolith-service/    # Single Spring Boot application
├── frontend/            # React UI (configured for monolith)
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── docs/DEPLOYMENT.md
```

## Build (Maven)

```bash
mvn clean package -pl monolith-service -am -DskipTests
java -jar monolith-service/target/monolith-service-1.0.0-SNAPSHOT.jar
```

## Registration flow

1. First startup seeds bootstrap admin → email in MailHog
2. Register at http://localhost:5173/register
3. Admin creates students/teachers → registration emails use **5173** links (same frontend)

## Database connection (DBeaver / Workbench)

| Setting | Value |
|---------|--------|
| Host | `localhost` |
| Port | `3307` |
| Database | `sms_monolith_db` |
| User | `sms_user` |
| Password | `sms_pass` |
| SSL | **Off** (`useSSL=false`) |

## More documentation

See [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) for full build, run, deploy, and troubleshooting details.
