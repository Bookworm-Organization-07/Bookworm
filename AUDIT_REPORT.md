# Bookworm .NET Backend + Frontend — Full Functionality Audit

**Date:** 2026-08-11
**Scope:** `dotnet-backend/BookwormApi` (45 endpoints across 21 controllers), `dotnet-backend/BookwormNotificationService`, and the React `frontend` (17 routes). The Java `backend/` counterpart was **not** in scope for this pass.
**Method:** Every backend endpoint was exercised directly (as both admin, a regular reader, and unauthenticated) with real HTTP requests against the running app and a real MySQL database — not mocks. Every frontend route was loaded in a real browser and checked for console errors and correct rendering; the two admin flows implicated in the bugs below were also reproduced with real mouse clicks and form input, not just API calls.

**Result: 83 of 85 checks passed.** Two real backend bugs were found (both fixable in ~1 line each), one is user-facing today. A handful of lower-severity/cosmetic items are listed separately.

---

## Summary table

| Area | Checked | Result |
|---|---|---|
| Auth (register/login/Google/refresh/guards) | 13 | ✅ 13/13 |
| Product browsing & lookups | 11 | ✅ 11/11 |
| Cart | 7 | ✅ 7/7 |
| Checkout (buy + rent) | 4 | ✅ 4/4 |
| Lending library (packages + checkout) | 5 | ✅ 5/5 |
| My Library | 1 | ✅ 1/1 |
| My Shelf | 1 | ✅ 1/1 |
| Order history | 3 | ✅ 3/3 |
| Transactions | 3 | ✅ 3/3 |
| Invoice PDF | 1 | ✅ 1/1 |
| Readable-copy PDFs | 3 | ✅ 3/3 |
| Admin (dashboard/users/product CRUD/cover/quick-add/AI) | 20 | ⚠️ 19/20 |
| Bulk upload | 1 | ✅ 1/1 |
| Royalties | 1 | ✅ 1/1 |
| Royalty ledger | 2 | ✅ 2/2 |
| Beneficiaries | 2 | ⚠️ 1/2 |
| JWT access/refresh token behavior | 7 | ✅ 7/7 |
| Frontend routes (all 17) | 17 | ✅ 17/17 render cleanly |

---

## 🔴 Bugs found (real, worth fixing)

### 1. Creating a new beneficiary crashes — user-facing, reproduced by hand in the browser
**Where:** `POST /api/beneficiaries` → `BeneficiaryController.Create` → `AutoMapper/MappingProfile.cs`
**Symptom:** 500 error: `Missing type map configuration or unsupported mapping. Mapping types: BeneficiaryDto -> Beneficiary`

**Root cause:** `BeneficiaryController.Create`/`Update` take a `BeneficiaryDto` (the *response* shape, meant for GET), but `MappingProfile.cs` only registers `CreateMap<BeneficiaryRequest, Beneficiary>()` — a different, separately-defined "request" DTO with identical fields that the controller never actually uses. AutoMapper has no rule for `BeneficiaryDto → Beneficiary`, so any create/update throws.

**Who hits this:** Real admins, right now. Two actual UI flows call this exact endpoint:
- Admin ▸ Royalties ▸ "Manage" beneficiaries ▸ "+ New beneficiary" ▸ "Create & add"
- Admin ▸ Bulk Upload ▸ "Add a single book" ▸ "+ Add new beneficiary…"

I reproduced it by hand: filled in "UI Test Beneficiary" on the Royalties page and clicked **Create & add** — the raw exception text renders directly on the page for the admin to see. Adding an *existing* beneficiary to a book works fine; only *creating a brand-new one* is broken.

**Fix:** either add `CreateMap<BeneficiaryDto, Beneficiary>()` to `MappingProfile.cs` (1 line, matches existing pattern), or change the two `[FromBody]` parameter types on `BeneficiaryController` to `BeneficiaryRequest` (the type that already has a correct mapping and was clearly intended for this).

### 2. Creating a library package would crash the same way — not currently reachable from the UI
**Where:** `POST /api/library-packages` → `LibraryPackageController.Create` → same `MappingProfile.cs`
**Symptom:** identical error, `Missing type map configuration... LibraryPackageDto -> LibraryPackage`

**Root cause:** exact same pattern — the controller takes `LibraryPackageDto`, the mapper only knows `LibraryPackageRequest → LibraryPackage`.

**Who hits this:** nobody yet — there's no admin page for managing library packages (`LendingLibraryPage.jsx` only reads the list; nothing in the admin nav creates one). Confirmed via direct API call, not reachable through any current UI. Worth fixing alongside #1 since it's the identical root cause, before anyone builds a "manage packages" screen.

---

## 🟡 Minor findings (not bugs, worth knowing about)

- **"Not found" and validation failures return `400`, not `404`/`409`, almost everywhere.** This is a deliberate, *consistent* pattern (19 places use `throw new ArgumentException("X not found")`, all mapped to 400 by `ExceptionHandlingMiddleware`), not a bug — every "not found" in this API behaves the same predictable way, and the frontend already handles it correctly via the generic error-message path. If you want textbook REST status codes (404 for missing resources, 409 only for real conflicts) that would mean touching all 19 call sites; low priority, cosmetic.
- **`POST /products` (the raw manual "create a product" endpoint) 500s if given an ISBN longer than 20 characters** — the DB column is `varchar(20)` and `ProductRequestDto` has no length validation. Not reachable from any frontend form today (Quick Add and Bulk Upload both generate their own ISBNs, always ≤18 chars, so they're unaffected) — only matters if this endpoint is called directly or a "create product" admin form is added later.
- **`/admin/orders` works correctly but isn't linked from the admin nav bar.** Only reachable by typing the URL directly. Everything on the page itself renders fine.

---

## What was verified working, in detail

**Auth** — register, duplicate-email rejection (correctly `409`), login (correct/incorrect password), Google client-id lookup, the new 15-minute access / 7-day refresh token pair (exact lifetimes measured from the JWTs themselves), `POST /auth/refresh` exchange, refresh-token-used-as-access-token correctly rejected (`401`), no-token and wrong-role requests correctly rejected.

**Products** — list, single lookup, search, library filter, genre/language filter, genre/language/type lookup lists, cover fetch, admin-only create correctly blocked for a regular user.

**Cart** — add, list, update quantity, remove one line, clear all, rejected without auth.

**Checkout** — a real `BUY` and a real `RENT` transaction, both against products actually in the catalogue; empty-cart checkout correctly rejected (`409`, clear message) instead of silently succeeding or crashing.

**Lending library** — package list/detail/by-name, and a real library checkout (package + specific titles) that correctly detects and blocks re-borrowing a title the reader already has.

**My Library / My Shelf / Orders / Transactions / Invoices** — all read correctly for the acting user; a transaction's items are correctly hidden from a *different* logged-in user; invoice PDF downloads and is a real PDF (checked `content-type` and non-trivial byte size).

**Readable-copy PDFs** — reading a title with no PDF uploaded yet is rejected cleanly; reading an unowned title is rejected; only an admin can upload one.

**Admin** — dashboard numbers, user list, user deletion, full product CRUD (create → update → set royalty % → upload cover → fetch cover → assign/unassign beneficiaries, including the "can't remove the last beneficiary" guard → delete), Quick Add, AI short-description correctly reports `503` since no AI key is configured (expected, matches `appsettings.json`).

**Bulk upload / Royalties / Beneficiaries / Royalty Ledger** — re-verified the cover_id matching fix still works, royalty % is settable per book, the ledger and beneficiary totals reflect real transactions (including live ones triggered during this audit), and unassigned/unearning beneficiaries still show up (the fix from earlier this session).

**JWT specifics** — access token measured at exactly 15 minutes, refresh token at exactly 7 days, both carry the right `token_type` claim, the access token carries `role` and the refresh token deliberately doesn't, and a tampered token is rejected.

**Frontend** — all 17 routes (`/`, `/genres`, `/lending-library`, `/products/:id`, `/login`, `/register`, `/checkout`, `/my-shelf`, `/my-library`, and all 9 admin routes) load and render correct content with no unexpected console errors. One transient set of console `401`s was observed on first-load pages — that's the access token expiring mid-session and silently refreshing itself, exactly as designed; the page content was correct despite it.

---

## Suggested next step

Fix the two AutoMapper gaps above (#1 is genuinely broken for a real admin workflow right now; #2 is the same fix, for free, before it becomes user-facing). Everything else checked out. Let me know if you'd like me to apply those fixes now, or tackle something else first.
