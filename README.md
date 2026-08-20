# Bookworm

Digital content marketplace — CDAC PG-DAC group project. An online bookstore/library platform with product catalog, cart & checkout, a lending library with beneficiary assignments, an admin dashboard, invoicing, and notification email delivery.

The backend exists in **two parallel implementations** that are drop-in replacements for each other (same routes, same JSON shapes, same auth, same business rules) against the **same MySQL database**:

| Component | Stack | Location |
|---|---|---|
| Frontend | React 19 + Vite + Tailwind CSS | [`frontend/`](frontend) |
| Backend (Java) | Spring Boot (Java 17) | [`backend/`](backend) |
| Backend (.NET) | ASP.NET Core (.NET 9) + a standalone notification microservice | [`dotnet-backend/`](dotnet-backend) |
| Database | MySQL 8, schema + seed data | [`database/`](database) |

Only one backend needs to run at a time — both talk to the same schema.

## Repository layout

```
frontend/          React SPA (Vite, Tailwind)
backend/            Spring Boot REST API (Java 17, Maven)
dotnet-backend/      ASP.NET Core REST API (.NET 9) + BookwormNotificationService (invoice emails)
database/           MySQL schema + seed SQL, run in numbered order
docker/             MySQL init scripts used by docker-compose
docker-compose.yml  Full .NET-stack container setup (mysql + api + notification + frontend)
SETUP.md            Full local Windows setup walkthrough (Java backend + frontend, bare metal)
GitHub-Workflow.md  Team git workflow conventions
```

## Quick start (local, bare metal)

Full step-by-step instructions — including seeded login accounts and how to reset the database before a demo — are in [`SETUP.md`](SETUP.md). Summary:

1. Install JDK 17, MySQL 8, and Node 20+.
2. Load the database, **in order**:
   ```bash
   mysql -u root -p < database/00_create_user.sql
   mysql -u root -p < database/01_schema.sql
   mysql -u root -p < database/02_seed.sql
   mysql -u bookworm_app -p < database/03_seed_bulk_catalog.sql
   mysql -u bookworm_app -p < database/04_product_covers.sql
   ```
3. Run a backend (pick one):
   ```bash
   # Java / Spring Boot
   cd backend && mvnw.cmd spring-boot:run

   # .NET
   cd dotnet-backend/BookwormApi && dotnet run
   cd dotnet-backend/BookwormNotificationService && dotnet run   # optional, invoice emails
   ```
4. Run the frontend:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
5. Open the printed URL (usually `http://localhost:5173`).

## Quick start (Docker)

`docker-compose.yml` at the repo root brings up MySQL, the .NET API, the notification service, and the frontend together:

```bash
docker compose up --build
```

Then open `http://localhost:8082`.

## Documentation

- [`SETUP.md`](SETUP.md) — full local setup walkthrough
- [`dotnet-backend/README.md`](dotnet-backend/README.md) — .NET backend architecture
- [`GitHub-Workflow.md`](GitHub-Workflow.md) — branching/PR conventions
- [`AUDIT_REPORT.md`](AUDIT_REPORT.md) — project audit notes
