# Running Bookworm Delta locally (Windows)

This is the checklist for getting the project running on a fresh Windows
machine from the backend zip, the frontend zip, and the `database/`
folder.

## 1. Install prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| Java JDK | 17 | [Eclipse Temurin 17](https://adoptium.net/) is a good free build. During install, make sure "Set JAVA_HOME" / "Add to PATH" is checked. |
| MySQL Server | 8.x | Community edition. During the Windows installer, you will be asked to set a **root password** — remember it, you'll use it once in step 2. |
| Node.js | 20 LTS or newer | Includes `npm`. Get it from [nodejs.org](https://nodejs.org/). |

You do **not** need to install Maven — the backend ships with a wrapper
(`mvnw.cmd`) that downloads the right Maven version automatically the
first time you run it.

Verify each install in Command Prompt:

```bash

java -version
mysql --version
node -v
npm -v

```

## 2. Set up MySQL

Open **Command Prompt** (or MySQL Workbench's "Run SQL Script") and run
the five files in `database/`, **in this order**:

```bash

mysql -u root -p < database\00_create_user.sql
mysql -u root -p < database\01_schema.sql
mysql -u root -p < database\02_seed.sql
mysql -u bookworm_app -p < database\03_seed_bulk_catalog.sql
mysql -u bookworm_app -p < database\04_product_covers.sql

```
(The first three prompt for the root password you set during MySQL
install. The last two log in as `bookworm_app` instead - its password
is `Bookworm@2026`, the one `00_create_user.sql` just set up.)

- **`00_create_user.sql`** creates a dedicated MySQL login
  (`bookworm_app` / `Bookworm@2026`) that the backend uses instead of
  root. This is the important part for a multi-machine team: everyone's
  MySQL root password is different (set individually during install),
  but everyone runs this same script, so the app's own login is
  identical on every machine and `application.properties` never needs
  editing per teammate.
- **`01_schema.sql`** drops and recreates the `bookworm_delta` database
  and all its tables.
- **`02_seed.sql`** inserts starter data: 2 users, a handful of authors/
  publishers/products, and one lending-library package.
- **`03_seed_bulk_catalog.sql`** adds the rest of the real catalogue (59
  more books) plus the cover images for 20 of them baked in directly.
- **`04_product_covers.sql`** adds 32 more cover images on top of those
  20 - built up over development by correcting bad filename-matches,
  sourcing official covers online for well-known titles, and extracting
  a few straight from their own PDF ebook's cover page. **Don't skip
  this one** - without it you'll only see 20 of 67 books with real
  covers instead of 52. Safe to re-run any time; it always leaves
  exactly the same 52 covers in place, never duplicates.

Skipping straight to **step 03** without **04** is the #1 way to end up
confused about "why do half my books have no cover" - both are part of
the standard setup now, not just `03`.

If you ever need to wipe and start over — including right before your
final presentation, so old test data doesn't show up in the Admin
Dashboard — see **"Resetting the database before your presentation"**
further down this file for the full step-by-step (it also covers all
five scripts).

## 3. Run the backend

```
bash

cd backend
mvnw.cmd spring-boot:run

```

First run takes a few minutes (downloading dependencies). Wait for:

```
Tomcat started on port 8080 (http) with context path '/'
Started BookwormApplication in ... seconds
```

If it instead fails immediately with a MySQL connection error, MySQL
isn't running — start it from **Services** (search "Services" in the
Start menu → find "MySQL80" → Start), then try again.

## 4. Run the frontend

In a **second** Command Prompt window:

```bash

cd frontend

npm install
npm run dev

```

Open the URL it prints (usually `http://localhost:5173`).

## 5. Sign in

Seeded accounts (from `02_seed.sql`):

----------------------------------------------
| Email               | Password    | Role   |
|---------------------|-------------|--------|
| admin@bookworm.com  | Admin@123   | Admin  |
| reader@bookworm.com | User@123    | Reader |
----------------------------------------------

Or register a new account from the app itself, or use the "Sign in with
Google" button on the login page — that one works out of the box, no
setup needed on your end (the Google client id is already baked into
`application.properties`).

## Optional: invoice emails

After a purchase/rental/borrow, the app tries to email an invoice PDF to
the buyer. This is **off by default** in the zip (`spring.mail.username`/
`password` are blank) — checkout still works completely normally either
way, it just skips the email step silently. To turn it on for your own
machine: a Gmail account with an **App Password** (Google Account >
Security > App passwords — a normal Gmail password will not work here),
then set it via the `MAIL_USERNAME` / `MAIL_PASSWORD` environment
variables rather than editing `application.properties` directly, so you
never end up with your own credentials sitting in a file you might hand
to someone else later.

## Admin features

Sign in as `admin@bookworm.com` for: **Dashboard** (revenue, books sold,
bestseller), **Products** (cover image upload/replace per book),
**Bulk Upload** (a whole spreadsheet of books at once, or one book by
hand — both can bring cover images along automatically, matched to each
book by comparing image filenames to titles), **Royalties** (view/edit
each book's royalty %, plus the full calculation history), and **Users**
(view accounts, delete one if needed).

## Resetting the database before your presentation

Every purchase, rental, borrow, or registration anyone does while testing
writes a real row into the database — the Admin Dashboard's revenue and
"books bought" numbers are a live sum of that data, not a fixed demo
number. If you (or a teammate) test the app between now and presentation
day, do this reset **right before you present**, not earlier, so nothing
you test afterward leaks back in.

### 1. Stop everything that's running

Close the Command Prompt windows running the backend(s) — Java
(`mvnw.cmd spring-boot:run`) and/or .NET (`dotnet run` in
`BookwormApi`/`BookwormNotificationService`). You don't need to stop the
frontend (`npm run dev`), it holds no database connection — but do stop
whichever backend(s) you'll be presenting with, so nothing is mid-request
against the database while it's being dropped and recreated.

### 2. Run all four reset scripts, in this exact order

Open Command Prompt, `cd` to the project root (the folder containing
`database\`), and run:

```bash
mysql -u bookworm_app -p < database\01_schema.sql
mysql -u bookworm_app -p < database\02_seed.sql
mysql -u bookworm_app -p < database\03_seed_bulk_catalog.sql
mysql -u bookworm_app -p < database\04_product_covers.sql
```

Each one prompts for a password — enter `Bookworm@2026` each time (that's
`bookworm_app`'s password, not your MySQL root password). You do **not**
need to re-run `00_create_user.sql` — that only creates the
`bookworm_app` login itself, which already exists from your first-time
setup.

What each step does:
- **`01_schema.sql`** drops the entire `bookworm_delta` database and
  recreates every table empty. This is the step that actually wipes all
  test purchases, registrations, cart contents, royalty history, etc.
- **`02_seed.sql`** puts back the 2 starter accounts (`admin@bookworm.com`
  / `reader@bookworm.com`) and the base catalogue/lending package.
- **`03_seed_bulk_catalog.sql`** puts back the rest of the 67-book
  catalogue and 20 of its cover images.
- **`04_product_covers.sql`** puts back the other 32 cover images (52
  total). **Don't skip this one** — leaving it out is the single most
  likely way to walk on stage with half your books showing "No cover"
  that were fine a minute ago.

If any of the four reports a MySQL error instead of returning silently
to the prompt, stop and re-check the password and that MySQL is running
(Services → "MySQL80" or similar) before continuing to the next script.

### 3. Verify it actually reset

Start whichever backend you're presenting with, then in a browser or via
`curl`:

1. Sign in as `admin@bookworm.com` / `Admin@123` and open the **Dashboard**
   page — it should show **₹0.00 revenue, 0 books bought, no bestseller**.
   If it shows anything else, the reset didn't take — repeat step 2.
2. Sign in as `reader@bookworm.com` / `User@123` and check **My Shelf**
   and **My Library** — both should be empty.
3. The storefront should show 52 of the 67 books with real cover art
   (the other 15 never had cover art available at all — that's expected,
   not a bug; see "Known cover gaps" below if asked about it).

**If you reset the database while a browser tab is still signed in from
before**, that tab's saved login now points at a user that no longer
exists (deleted by the reset), and you'll see odd errors on sign-out or
navigation. Fix: open DevTools console and run
`localStorage.clear(); location.reload();`, or just use a fresh/incognito
window — don't reuse a tab that was open before the reset.

### 4. After verifying, don't test checkout/registration again

Once you've confirmed the dashboard reads zero, avoid buying, renting,
borrowing, or registering anything else before you actually present —
each of those writes a new row and will move the numbers away from zero
again. Browsing the catalogue, viewing products, and signing in/out are
all safe (read-only, nothing to reset afterward).

### Known cover gaps (not a bug)

15 of the 67 books never had cover art available anywhere — not in the
`Book Covers` folder, not findable online, nothing to attach. If asked:
these are small self-published titles with no cover art ever supplied,
not a mapping failure. Every book that *does* have source art available
gets it correctly and reliably now.

## Demonstrating the Bulk Upload feature

Two files under `Books DATA/` exist specifically for this:

- **`Prod Master Table.xlsx`** — the real catalogue. Every row in it is
  already in the database, so re-uploading it on its own is a safe way
  to prove duplicates get skipped and nothing breaks (expect
  **Total 63, Created 0, Skipped 63, Failed 0**).
- **`Presentation Demo Upload.xlsx`** + **`Presentation Demo Covers/`**
  folder — built specifically to show the full range faculty will want
  to see in one run. Upload both together and expect exactly
  **Total 8, Created 3, Skipped 3, Failed 2**, every time, with a
  reason shown for every skipped/failed row. The 3 newly-created demo
  books also get real covers attached live, via the reliable `cover_id`
  match (not filename guessing) — a good moment to point out during the
  demo, since it's the actual thing this round of fixes was about.

Both are safe to re-run as many times as you like before the real
day — just remember the presentation-day reset (above) will remove
anything either of them created, so re-run the reset one final time
after your last rehearsal.

## Packaging the zip (for whoever is sending this)

Before zipping, delete these folders — they're machine-specific build
output and will just make the zip huge / potentially wrong-platform:

- `backend/target/`
- `frontend/node_modules/`
- `frontend/dist/` (if it exists)
- `dotnet-backend/BookwormApi/bin/`, `dotnet-backend/BookwormApi/obj/`
- `dotnet-backend/BookwormNotificationService/bin/`,
  `dotnet-backend/BookwormNotificationService/obj/`
- `dotnet-backend/.vs/` (Visual Studio's own cache, if present)

Everything else — including `backend/.mvn/`, `backend/mvnw.cmd`,
`frontend/package-lock.json`, the whole `database/` folder (all 5
scripts), and `Books DATA/` (including the two presentation demo files
above) — should be included.

## Common Windows issues

- **"JAVA_HOME is not defined correctly" from `mvnw.cmd`** — the JDK
  installer didn't set it. Set it manually: Start → "Edit environment
  variables for your account" → New → Variable `JAVA_HOME`, value the
  JDK install path (e.g. `C:\Program Files\Eclipse Adoptium\jdk-17...`).
  Close and reopen Command Prompt afterwards.

- **Use `mvnw.cmd`, not `mvnw`** — `mvnw` (no extension) is the
  Mac/Linux shell script; Windows needs the `.cmd` one, which is
  already in the zip.

- **Port 8080 or 5173 already in use** — usually a leftover process
  from a previous run. Close the old Command Prompt window it was
  running in, or find and end the `java.exe` / `node.exe` process in
  Task Manager. Vite (frontend) will happily move to 5174/5175 on its
  own if 5173 is busy — the backend already allows those origins.

- **Extract the zip somewhere short**, e.g. `C:\bookworm-delta`, not
  deep inside a synced OneDrive/Desktop folder. `node_modules` creates
  very long file paths that can hit Windows' path-length limit, and
  OneDrive trying to sync `node_modules`/`target` while they're being
  written can cause odd file-lock errors.

- **Windows Defender / firewall prompt** on first backend or frontend
  run — click "Allow access" (it's just for other devices on your
  network to reach your local dev server; not required, but harmless
  to allow for "Private networks").

- **MySQL service not running** is the #1 cause of the backend failing
  to start — check Services (see step 3) before troubleshooting
  anything else.

---

# Running the .NET Backend (alternative to the Java backend)

`dotnet-backend/BookwormApi` is a second, independent implementation of the
same backend, written in ASP.NET Core. It talks to the **same MySQL
database** and is a drop-in replacement for the Java backend — same API
routes, same JSON shapes, same business rules, same seeded logins. You do
**not** need to run both at once; pick one.

The database setup in **step 2 above is shared by both backends** — do
that once, regardless of which backend you run.

## 1. Install prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| .NET SDK | 9 or 10 | [dotnet.microsoft.com/download](https://dotnet.microsoft.com/download). The project targets .NET 9 but is configured to roll forward and run fine on a .NET 10 SDK/runtime too — either satisfies it. |
| MySQL Server | 8.x | Same MySQL instance/database as the Java backend — see step 2 above. |
| Node.js | 20 LTS or newer | Only needed for the frontend (same as the Java setup). |

Verify:

```bash
dotnet --version
```

## 2. Set up MySQL

Already covered in **step 2** near the top of this file — run the four
`database\*.sql` scripts once. Both backends read/write the exact same
`bookworm_delta` database, so if you've already done this for the Java
backend, there is nothing further to do here.

## 3. Run the .NET backend

```bash
cd dotnet-backend\BookwormApi
dotnet run
```

First run downloads NuGet packages and takes a minute or two. Wait for:

```
Now listening on: http://localhost:8080
Application started. Press Ctrl+C to shut down.
```

It listens on **port 8080** — the same port the Java backend uses — so it
is a true drop-in swap. If it fails immediately with a MySQL connection
error, MySQL isn't running (see the "MySQL service not running" note
above).

By default it connects as `bookworm_app` / `Bookworm@2026`, same as the
Java backend. To use different credentials without editing any file, set
environment variables before running:

```bash
set DB_USERNAME=bookworm_app
set DB_PASSWORD=Bookworm@2026
dotnet run
```

### Testing the API directly (optional, before wiring up the frontend)

With the backend running, open **`http://localhost:8080/swagger`** in a
browser — a Swagger UI page listing every endpoint, where you can try
requests (login first via `POST /api/auth/login` to get a token, then use
the "Authorize" button to attach it for admin/authenticated endpoints).

Or from a second terminal:

```bash
curl http://localhost:8080/api/products
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"email\":\"admin@bookworm.com\",\"password\":\"Admin@123\"}"
```

### Optional: the notification microservice

`dotnet-backend/BookwormNotificationService` is a second, independently
deployable .NET service — it owns invoice email delivery, and nothing
else. It has no database connection of its own; the main API generates
the invoice PDF (that part needs the database) and hands it to this
service over HTTP, which just sends the email. This is a genuine service
split, not a demo stub: stop this service and checkout still succeeds
(confirmed by testing) — the main API logs a warning and moves on, the
same way it already behaved when mail was simply left unconfigured.

You do **not** need to run it for the app to work end-to-end — checkout,
purchases, and rentals all succeed without it, they just skip the invoice
email. Run it only if you want to see the actual cross-service call, or
to receive invoice emails for real:

```bash
cd dotnet-backend\BookwormNotificationService
dotnet run
```

Wait for `Now listening on: http://localhost:8090`. The main API already
points at this URL by default (`NotificationService:BaseUrl` in its
`appsettings.json`) — no config change needed on either side to make them
talk to each other.

To actually receive the email (optional — same Gmail App Password caveat
as the Java backend's invoice emails), set its Mail settings via
environment variables before running, rather than editing
`appsettings.json` directly. ASP.NET Core maps nested config keys from
environment variables using a double underscore (`__`), so `Mail:Username`
becomes `Mail__Username` — this works out of the box, no code change
needed:

```bash
set Mail__Username=youraddress@gmail.com
set Mail__Password=your16charapppassword
dotnet run
```

`appsettings.json` starts with both blank on purpose, same reasoning as
the Java backend: so no one's personal credentials ship in the zip.

## 4. Run the frontend

Same as the Java setup — **no changes needed**, since the .NET backend
listens on the same port 8080 that the frontend's Vite dev proxy already
targets:

```bash
cd frontend
npm install
npm run dev
```

Open the URL it prints (usually `http://localhost:5173`).

## 5. Sign in

Same seeded accounts as the Java backend (they're rows in the same
database):

| Email               | Password  | Role   |
|---------------------|-----------|--------|
| admin@bookworm.com  | Admin@123 | Admin  |
| reader@bookworm.com | User@123  | Reader |

## Switching between the Java and .NET backends

Because both default to port 8080, switching is just **stop one, start
the other** — no frontend or config change needed:

```bash
:: Java:
cd backend
mvnw.cmd spring-boot:run

:: ...or .NET instead:
cd dotnet-backend\BookwormApi
dotnet run
```

Both read/write the same database, so anything created under one backend
(a new user, a purchase, an uploaded cover) is immediately visible under
the other.

### Running both at once (to demo the .NET → Java microservice call)

The .NET backend includes a small demo endpoint,
`GET /api/proxy/java-product-types`, that calls the Java backend directly
over HTTP (`Services/JavaMicroserviceClient` + `Controllers/ProxyController`
in the .NET project) to demonstrate cross-service communication. To see it
actually reach the Java backend, run both at once on different ports:

```bash
:: Terminal 1 - Java on 8081 instead of its default 8080:
cd backend
mvnw.cmd spring-boot:run --server.port=8081

:: Terminal 2 - .NET on its default 8080:
cd dotnet-backend\BookwormApi
dotnet run
```

`dotnet-backend\BookwormApi\appsettings.json`'s `JavaBackend:BaseUrl`
already defaults to `http://localhost:8081`, matching this setup — no
code change needed. Then hit `http://localhost:8080/api/proxy/java-product-types`
and it will forward to the Java backend and return its response. Revert to
running one backend at a time afterwards for normal use.

## More detail

See `dotnet-backend\README.md` for the .NET project's internal structure
(controllers/services/repositories layout), which mandatory requirements
live where, and known pre-existing behavior notes (e.g. the invoice link
gap, which exists identically on the Java backend too).

## Packaging the zip — .NET-specific additions

In addition to the folders already listed above, also delete these
before zipping (machine-specific build output, same reasoning as
`backend/target/`):

- `dotnet-backend/BookwormApi/bin/`
- `dotnet-backend/BookwormApi/obj/`
- `dotnet-backend/BookwormNotificationService/bin/`
- `dotnet-backend/BookwormNotificationService/obj/`

Keep `dotnet-backend/BookwormApi/Migrations/` — those are source-controlled
C# files, not build output.
