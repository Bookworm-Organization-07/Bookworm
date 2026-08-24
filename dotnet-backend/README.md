# Bookworm .NET Backend

An ASP.NET Core (.NET 9, running under the .NET 10 SDK via roll-forward) reimplementation of the
Java Spring Boot backend in `../backend`. It talks to the **same MySQL database** and is a
drop-in replacement for the Java backend from the frontend's point of view: same routes, same
JSON shapes, same auth mechanics, same business rules.

`../backend` and `../frontend` are untouched reference/legacy code — nothing in this folder
modifies them.

This folder holds **two** independently deployable projects, not one:

```
dotnet-backend/
  BookwormApi/                  the main backend (everything below), port 8080
  BookwormNotificationService/  standalone microservice, port 8090 - owns invoice email
                                  delivery only, no database of its own
```

## Architecture

```
BookwormApi/
  Controllers/     19 REST controllers, 1:1 with the Java controllers (routes/verbs/status codes)
  Models/          EF Core entities, Fluent-API mapped to the exact existing MySQL column/table names
  DTO/             Request/response DTOs (mirrors the JSON shapes the frontend already reads)
  AutoMapper/      Entity <-> DTO mapping profile
  Repository/      Generic repository (IGenericRepository<TEntity,TKey>) for simple CRUD entities
  Service/
    Generic/       Generic CRUD service built on the generic repository (ProductType, Genere,
                    Language, Beneficiary, LibraryPackage - simple lookup/reference data)
    (flat)         Entity-specific business services: checkout, library checkout, cart, product
                    (delete guard, search, patch semantics), my-library, admin user (cascade
                    delete), beneficiary assignment, bulk Excel import, invoice PDFs, email,
                    JWT/auth, the Java microservice demo client, and the optional AI helper
  Middleware/      Global exception-handling middleware (ArgumentException -> 400,
                    InvalidOperationException -> 409, unhandled -> 500, all bodies {"message":...})
  Data/            BookwormDbContext
  Migrations/       EF Core migration (see note below - not auto-applied)
```

## Database

The schema is owned by `../database/*.sql` (already run once to create `bookworm_delta`). This
backend connects to that **same database** - it never creates or alters schema at startup.
`Migrations/InitialCreate` is generated for documentation/reference (and so you could bootstrap a
throwaway dev database with `dotnet ef database update` if you ever needed one from scratch), but
`Program.cs` does not call it automatically, since the schema is shared with the Java backend and
must stay exactly as the SQL scripts define it.

Connection settings live in `appsettings.json` (`ConnectionStrings:Default`), with the same
`DB_USERNAME` / `DB_PASSWORD` environment-variable override pattern the Java backend uses
(defaults: `bookworm_app` / `Bookworm@2026`).

## Mandatory requirements, where to find them

- **Structured logging** - Serilog (console + rolling file under `logs/`), `ILogger<T>` throughout.
- **JWT Bearer auth** - `Service/JwtService.cs`, wired in `Program.cs`; same claims as Java
  (`sub`, `role` = `ROLE_ADMIN`/`ROLE_USER`, `iat`, `exp`, HS256).
- **Microsoft.Extensions.AI** - `Service/BookDescriptionAiService.cs`, an optional admin-only
  "draft a short description" helper (`POST /api/products/{id}/ai-short-description`). Disabled
  (returns `503`) unless `Ai:Endpoint` / `Ai:ApiKey` are configured; not used by the frontend and
  not required for any core flow.
- **Global exception middleware** - `Middleware/ExceptionHandlingMiddleware.cs`.
- **Validation** - service-layer checks replicate the Java backend's exact messages (the Java
  source has no Bean Validation annotations - it's all manual `IllegalArgumentException`/
  `IllegalStateException` checks), plus light Data Annotations on request DTOs.
- **Generic CRUD** - `Repository/Generic`, `Service/Generic` (used by `ProductType`, `Genere`,
  `Language`, `Beneficiary`, `LibraryPackage`); everything else is entity-specific per requirement.
- **AutoMapper** - `AutoMapper/MappingProfile.cs`.
- **Java microservice communication** - `Service/JavaMicroserviceClient.cs` +
  `Controllers/ProxyController.cs` (`GET /api/proxy/java-product-types`), calling the Java
  backend's public `GET /api/product-types`. Purely a demo call - nothing in this backend's core
  functionality depends on the Java backend being reachable.
- **A second, real microservice** - `BookwormNotificationService/` (its own project, own port
  8090, no database connection). It owns invoice email delivery end to end. `Service/EmailService.cs`
  in the main API no longer sends mail itself - it POSTs the rendered PDF + transaction details to
  `POST /api/notifications/invoice-email` on this service over `HttpClient`
  (`Program.cs` -> `AddHttpClient<IEmailService, EmailService>`) and lets it handle SMTP. Unlike the
  Java demo call, this one is load-bearing for a real feature (invoice emails), but still not a hard
  dependency: if the notification service is down, `EmailService` catches the failure, logs a
  warning, and checkout still completes - verified by stopping the service mid-test and confirming
  checkout still returned `200`.

## Running the Java backend + frontend

See `../SETUP.md` for the full walkthrough. Short version:

```bash
cd backend
mvnw.cmd spring-boot:run
```

```bash
cd frontend
npm install
npm run dev
```

Java listens on **port 8080**.

## Running the .NET backend + frontend

```bash
cd dotnet-backend/BookwormApi
dotnet run
```

This also listens on **port 8080** by default (`Properties/launchSettings.json`), so:

```bash
cd frontend
npm install
npm run dev
```

...just works, unchanged - the Vite dev proxy already targets `http://localhost:8080` for `/api`.

Swagger UI is available at `http://localhost:8080/swagger` in Development mode, useful for
poking endpoints directly (e.g. testing admin-only routes with a bearer token) without the UI.

### Optional: the notification microservice

Not required for the app to work - checkout succeeds without it, invoice emails are just skipped.
Run it if you want to see the real cross-service call or actually receive invoice emails:

```bash
cd dotnet-backend/BookwormNotificationService
dotnet run
```

Listens on **port 8090** by default; `BookwormApi/appsettings.json`'s `NotificationService:BaseUrl`
already points there, so no config change is needed to connect the two. It also has its own Swagger
UI at `http://localhost:8090/swagger`. Mail credentials (optional, same Gmail App Password caveat as
the Java backend) go in *this* service's `appsettings.json`/`Mail__Username`/`Mail__Password` env
vars now, not the main API's - it's the one actually sending mail.

## Switching between the two backends

Because both backends default to the same port, **switching is just "stop one, start the
other"** - no frontend or Vite config change needed in the common case:

```bash
# Run Java:
cd backend && mvnw.cmd spring-boot:run

# ...or run .NET instead:
cd dotnet-backend/BookwormApi && dotnet run
```

Both read/write the same `bookworm_delta` database, so data created under one backend (a new
user, a purchase, an uploaded cover) is immediately visible under the other.

### Running both at once (to demo the Java microservice call)

The `ProxyController` demo (`GET /api/proxy/java-product-types`) calls the Java backend directly,
so if you want to show it working, run both backends simultaneously on different ports:

```bash
# Terminal 1 - Java on 8081 instead of its default 8080:
cd backend
mvnw.cmd spring-boot:run --server.port=8081

# Terminal 2 - .NET on its default 8080:
cd dotnet-backend/BookwormApi
dotnet run
```

`appsettings.json`'s `JavaBackend:BaseUrl` already defaults to `http://localhost:8081`, matching
this setup - no code change needed, just start Java on that alternate port for the demo. Revert
to the single-backend-at-a-time setup afterwards for normal use.

If instead you want both backends reachable on their *own* default ports at the same time (e.g.
to A/B test manually), point the frontend's Vite proxy at whichever one you're testing by editing
the `target` in `frontend/vite.config.js` - the one config-file edit the assignment brief calls
out as acceptable, and the only thing that ever needs touching in `frontend/`.

## Known pre-existing behavior (not a .NET-specific gap)

The frontend's invoice download link (`<a href="/api/invoice/{id}">`) is a plain anchor with no
`Authorization` header attached, but the endpoint requires a signed-in user on **both** backends
(this matches the Java backend's `SecurityConfig`, which does not list `/api/invoice/**` as
public). This isn't something introduced by the .NET port - it behaves identically on the Java
backend today. Fetching it via the app's own authenticated API client (rather than a raw link
navigation) would need a small frontend change if this is ever worth fixing; out of scope here
since `frontend/` must stay untouched.

## Seeded accounts

Same as Java (from `database/02_seed.sql`):

| Email | Password | Role |
|---|---|---|
| admin@bookworm.com | Admin@123 | Admin |
| reader@bookworm.com | User@123 | Reader |
