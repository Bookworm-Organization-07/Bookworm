# Bookworm — GitHub Workflow & Repository Management

**Repository:** https://github.com/Bookworm-Organization-07/Bookworm
**Prepared for:** Team Lead / Repository Owner (Aniket) and all 9 team members
**Purpose:** A professional, beginner-friendly Git workflow for the remainder of the project, through the final presentation.

This document replaces informal notes in `docs/Conventions.md` and `docs/Git-Commands.md` with a single authoritative reference. It keeps what your team already got right and fixes what's missing or risky.

---

## 1. Repository Analysis (current state, as of this review)

### 1.1 Structure

Root of `develop` (the default branch):

```
Bookworm/
├── .gitignore
├── README.md
├── backend-java/     ← real Maven project, properly pushed
├── frontend/         ← placeholder only (just README.md, no React code)
├── database/         ← placeholder only (just README.md, no SQL scripts)
└── docs/             ← BRD, Conventions.md, Git-Commands.md, Database.md
```

**Missing entirely from GitHub right now:**
- `dotnet-backend/` — the ASP.NET Core backend does not exist in the repo at all yet.
- Real content in `frontend/` and `database/` — both folders are still just placeholder `README.md` files; the actual React app and SQL scripts haven't been pushed.
- ER diagram — not present anywhere in the repo.
- `Books Data/` — not present.
- `LICENSE` — none set.
- `.github/` folder — doesn't exist (no PR template, issue template, or CODEOWNERS).

**Action:** Before anything else in this document matters, the team needs to push the real `frontend/`, `database/`, `dotnet-backend/`, the ER diagram, and `Books Data/` into the repo (see §9, Action Checklist). A "professional GitHub workflow" wrapped around an incomplete repo won't read as complete at presentation time.

### 1.2 Branches

| Branch | Protected? | Status |
|---|---|---|
| `develop` | Yes | Default branch, 11 commits ahead of `main`, active |
| `main` | Yes | Essentially empty (just `.gitignore` + 2-line README) — nothing has been released to it yet |
| `feature/stakeholders` | No (correct) | Active feature branch, 1 open PR against `develop` |
| `master` | No | **Stale/unused leftover** — should be deleted |

`main` and `master` both existing is a leftover from repo creation (GitHub's older default branch name vs. the newer one, before `develop` was set as default). Having three long-lived branch names (`main`, `master`, `develop`) is confusing for a 9-person beginner team — delete `master`.

### 1.3 Pull request activity

Two PRs so far — both correctly targeting `develop`, not `main`:
- PR #1 (merged): `backend/feat/project-file-configuration` → `develop`
- PR #2 (open): `feature/stakeholders` → `develop`

This confirms the team is already using the right target branch for feature work. Good sign.

### 1.4 Repository settings (visible without admin access)

- Visibility: Public, owned by the `Bookworm-Organization-07` organization — correct setup (an Org, not a personal repo, is the right call for a 9-person team).
- Issues: enabled. Projects: enabled. Wiki: enabled (redundant with `docs/`, low priority to disable). Discussions: disabled (fine, not needed).
- No LICENSE, no topics.
- I could **not** verify the exact branch protection rules (required reviewers, force-push settings, status checks) — GitHub's API requires admin/write access to read that detail, which an unauthenticated check doesn't have. `develop` and `main` do show as "protected," but you (as repo Owner) need to confirm the specific rules match §3 below via **Settings → Branches**.
- I could not verify collaborator/team permissions either (same reason). Confirm all 9 members actually have **Write** access — only 3 usernames appear in commit/PR history so far, which may just mean the others haven't started pushing yet, or may mean they were never added.

---

## 2. Recommended Branch Strategy

**Recommendation: GitHub Flow with a `develop` integration branch** (sometimes called "GitHub Flow Lite" or "Git Flow without release branches"). This is deliberately *not* full Git Flow — full Git Flow's `release/*` branches exist to manage multiple parallel production versions, which this project doesn't have (there's one deployable target: the final presentation demo). Adding `release/*` branches would just be ceremony for a 9-person student team.

This is also, almost exactly, what your own `docs/Conventions.md` already describes — this section formalizes it.

```
main                    ← always stable, demo-ready, tagged at release time
  ↑ PR only, when develop is stable
develop                 ← default branch, integration point for all finished features
  ↑ PR only, one per feature
feature/<name>          ← one branch per task, branched from develop
hotfix/<name>           ← rare, only for urgent fixes discovered close to presentation day
```

| Branch type | Branched from | Merges into | Who creates it | Protected? |
|---|---|---|---|---|
| `main` | — (already exists) | — | Repo owner (once, at setup) | **Yes** |
| `develop` | — (already exists) | — | Repo owner (once, at setup) | **Yes** |
| `feature/*` | `develop` | `develop` (via PR) | Whoever is doing that task | No |
| `hotfix/*` | `main` | **both** `main` and `develop` (via PR) | Whoever finds the urgent bug, with lead's sign-off | No |

### Naming convention

Keep your existing purpose-based names (`feature/auth`, `feature/product`, `feature/cart`, etc.) for work that spans the full stack — that's the common case and your team already listed sensible ones in `Conventions.md`.

**Add this refinement** now that the project has three separate codebases: when a task is scoped to *one* stack only, prefix it so reviewers immediately know which folder to expect changes in:

```
feature/backend-<short-desc>      → backend-java/ only
feature/dotnet-<short-desc>       → dotnet-backend/ only
feature/frontend-<short-desc>     → frontend/ only
feature/db-<short-desc>           → database/ only
feature/docs-<short-desc>         → docs/ only
```

Example: `feature/dotnet-checkout`, `feature/frontend-cart-ui`, `feature/db-seed-data`. If a feature genuinely touches multiple stacks (e.g. "add cart" touches backend + frontend), the plain `feature/cart` name is fine — don't force a prefix where it doesn't fit.

`hotfix/<short-desc>` — e.g. `hotfix/login-500-error`.

---

## 3. Branch Protection Rules — exact settings to configure

Go to **Settings → Branches → Branch protection rules** for each of `main` and `develop`. Set both the same way except where noted.

| Setting | `main` | `develop` |
|---|---|---|
| Require a pull request before merging | ✅ On | ✅ On |
| Required approvals | 1 (2 if you want extra rigor for release) | 1 |
| Dismiss stale approvals on new commits | ✅ On | ✅ On |
| Require conversation resolution before merging | ✅ On | ✅ On |
| Require status checks to pass | Off for now — turn on once CI exists (see §9) | Off for now |
| Require linear history | Off (not needed — see §4 on merge strategy) | Off |
| Do not allow bypassing settings (include administrators) | ✅ On | ✅ On |
| Allow force pushes | ❌ Off | ❌ Off |
| Allow deletions | ❌ Off | ❌ Off |

**"Include administrators" matters more than it looks** — without it, anyone with Admin role (which might be several of you in a student org) can push straight to `main`/`develop` and skip review entirely, silently defeating the whole point of this document. Turn it on for both.

`feature/*` and `hotfix/*` branches: **no protection rules** — contributors need to push freely to their own branches.

### Direct answers to your specific questions

- **Should direct pushes to `main` be disabled?** Yes — enforced by "require PR before merging" above.
- **Should direct pushes to `develop` be allowed?** No — same rule applies to `develop`. You said it yourself: *"all code is reviewed through Pull Requests before reaching develop."* That's the "require a pull request before merging" rule on `develop`.
- **Is branch protection currently correct?** `develop` and `main` both show as protected via the API, which is the right starting point — but I can't confirm the specific rules above (approvals required, force-push blocked, etc.) without admin access. Check each one against the table above in Settings.

---

## 4. Merge Strategy

**Feature → `develop`: Squash and merge.**
Each feature branch usually accumulates messy commits ("wip", "fix typo", "actually fix it"). Squashing collapses all of that into one clean commit on `develop` with the PR title as the message. This is the easiest strategy for beginners to reason about, and makes `git revert` trivial if a feature turns out to be broken — one commit, one revert.

**`develop` → `main`: Merge commit (not squash, not rebase).**
By the time you're merging `develop` into `main`, `develop` already has clean, squashed, one-commit-per-feature history. A regular merge commit here preserves that structure and creates one clear "this is release v1.0" marker in `main`'s history, which you can tag (see §7, Stage 8).

**Rebase: don't use it on shared branches.** Rebasing `develop` or `main` requires force-pushing, which is exactly what branch protection above blocks — and it's a common way for beginners to lose teammates' work. The one place rebase is fine is *privately*, on your own feature branch, before you've pushed it anywhere (optional, not required).

**Repo setting to match:** Settings → General → Pull Requests:
- ✅ Allow squash merging (set as default)
- ✅ Allow merge commits
- ❌ Allow rebase merging (turn off — removes the temptation entirely)
- ✅ Automatically delete head branches (auto-cleans up feature branches after merge — with 9 people creating branches, this keeps the branch list from becoming clutter)
- ✅ Always suggest updating pull request branches (gives PR authors a one-click "update branch" button instead of needing to know merge/rebase commands)

---

## 5. Roles & Responsibilities

| Action | Who |
|---|---|
| Create `main` / `develop` | Repo owner (Aniket) — one-time, already done |
| Create a `feature/*` branch | Whoever is starting that task (self-service) |
| Create a `hotfix/*` branch | Whoever finds the urgent issue, ideally with a quick heads-up to the lead |
| Push commits | Only to your **own** feature/hotfix branch — never directly to `develop` or `main` |
| Open the Pull Request | The person who did the work (self-service) |
| Review the PR | At least one teammate other than the author — see CODEOWNERS note below |
| Approve the PR | The assigned reviewer(s) |
| Merge feature → `develop` | The reviewer, or the author once approved (GitHub allows this once required approvals are met) |
| Merge `develop` → `main` (release) | **Team Lead only** — keep this a deliberate, singular action, not something anyone can trigger |
| Resolve merge conflicts | The PR **author**, in their own feature branch, before the PR is merged (see §6) |

### Suggested reviewer assignment (CODEOWNERS)

With 3 codebases and 9 people, add a `.github/CODEOWNERS` file so GitHub automatically requests the right reviewers based on which folder a PR touches, instead of everyone guessing who should review what:

```
# .github/CODEOWNERS
/backend-java/    @github-username-1 @github-username-2
/dotnet-backend/  @github-username-3 @github-username-4
/frontend/        @github-username-5 @github-username-6
/database/        @github-username-7
/docs/            @Aniketpatil9767
```

Replace the placeholders with real GitHub usernames once you've decided who primarily owns each area. Doesn't have to be exclusive — list 2 people per area so review isn't a bottleneck if one person is busy.

---

## 6. Conflict Resolution

Resolve conflicts **locally, in the feature branch, before merging** — not in GitHub's web editor (fine for a one-line conflict, error-prone for anything bigger).

```bash
git checkout feature/your-branch
git fetch origin
git merge origin/develop
# Git will pause and list conflicted files
# Open each one, look for <<<<<<< / ======= / >>>>>>> markers, fix by hand
git add <the files you fixed>
git commit
git push origin feature/your-branch
```

The PR author resolves conflicts, since they understand their own change best. If the conflict is with something a teammate merged recently, a quick message to that teammate before resolving is good practice, but not a hard requirement.

---

## 7. Complete Workflow: Empty Repository → Final Merged Project

```
Repository Creation (Org, not personal account)
    ↓
Initial Project Setup (README, .gitignore, LICENSE, main branch)
    ↓
Create develop Branch (set as default branch)
    ↓
Configure Branch Protection (main + develop, per §3)
    ↓
Add .github/ (CODEOWNERS, PR template, issue template)
    ↓
Add All 9 Members to the Org/Team with Write Access
    ↓
Push Real Project Content (backend-java, dotnet-backend, frontend, database, docs, ER diagram)
    ↓
Assign Features (one feature/* per person/task)
    ↓
    ┌─── repeat per feature ───────────────────────────────┐
    │  Create feature/* branch from develop                │
    │  Commit changes locally (small, frequent commits)     │
    │  Push feature branch to origin                        │
    │  Open Pull Request → develop                           │
    │  Code Review (CODEOWNERS auto-requests reviewer)       │
    │  Resolve conflicts if any (§6)                          │
    │  Reviewer Approves                                       │
    │  Squash-merge into develop                                │
    │  Delete feature branch (automatic)                          │
    └───────────────────────────────────────────────────────┘
    ↓
Integration Testing on develop (everyone pulls latest, tests full app together)
    ↓
Open Pull Request: develop → main
    ↓
Final Review (Team Lead)
    ↓
Merge-commit develop → main
    ↓
Tag the Release (e.g. v1.0-presentation)
    ↓
Final Presentation Demo (running from main)
```

### Stage-by-stage detail with exact commands

#### Stage 1 — Repository creation
Already done. (For reference: created under the `Bookworm-Organization-07` org, which is correct — an org gives you Teams and centralized permission management that a personal-account repo doesn't.)

#### Stage 2 — Initial project setup
```bash
git clone https://github.com/Bookworm-Organization-07/Bookworm.git
cd Bookworm
# add/confirm README.md, .gitignore, LICENSE exist
git add .
git commit -m "chore: initial project setup"
git push origin main
```
(Already done for README/.gitignore; LICENSE is still missing — see §9.)

#### Stage 3 — Create `develop`
```bash
git checkout -b develop
git push -u origin develop
```
Then in **Settings → Branches**, set `develop` as the repository's default branch (already done).

#### Stage 4 — Branch protection
Configure both branches per the table in §3. Done via the web UI, no commands.

#### Stage 5 — Add all 9 members
**Settings → Collaborators and teams** (or, better, create an org Team called e.g. `bookworm-devs`, add all 9 there, and give the *team* Write access to the repo — easier to manage than 9 individual collaborator entries). Give the Team Lead Admin role individually; everyone else gets Write.

#### Stage 6 — Push real project content
This is the most urgent gap right now. Whoever owns each piece pushes it via a normal feature branch + PR (don't push these directly even though it's "just adding files" — set the precedent early that everything goes through review):
```bash
git checkout develop
git pull origin develop
git checkout -b feature/frontend-initial-import
# copy the real frontend/ code in, replacing the placeholder README
git add frontend/
git commit -m "feat: import frontend React app"
git push -u origin feature/frontend-initial-import
# open PR → develop, get it reviewed, merge
```
Repeat the same pattern for `database/` (SQL scripts), `dotnet-backend/` (the whole ASP.NET Core project), the ER diagram (probably belongs under `docs/`), and `Books Data/`.

> **Note on `dotnet-backend/`:** it's a full second backend with its own `bin/`/`obj/` build folders. Before pushing it, add a `.gitignore` entry (or confirm one exists) for `dotnet-backend/BookwormApi/bin/` and `dotnet-backend/BookwormApi/obj/` so build output never gets committed. Same idea as `backend-java`'s existing `.gitignore` excluding `target/`, and `frontend/node_modules/`.

#### Stage 7 — Feature branch cycle (the day-to-day loop)

**Starting a feature:**
```bash
git checkout develop
git pull origin develop
git checkout -b feature/your-feature-name
```

**While working (commit often, in small chunks):**
```bash
git add .
git commit -m "feat: add product search endpoint"
```
Use these commit prefixes (matches your existing `Conventions.md`, standardized slightly):
`feat:` new functionality · `fix:` bug fix · `refactor:` code change with no behavior change · `docs:` documentation only · `test:` tests only · `chore:` tooling/config/build files

**Keeping your branch up to date with develop (do this daily, or before opening a PR):**
```bash
git fetch origin
git merge origin/develop
# resolve any conflicts (§6), then:
git push origin feature/your-feature-name
```

**Pushing and opening the PR:**
```bash
git push -u origin feature/your-feature-name
```
Then on GitHub: **Compare & pull request** → base `develop`, compare `feature/your-feature-name` → fill in a description of what changed and why → **Create pull request**.

**Code review:** the assigned reviewer(s) (via CODEOWNERS or manually requested) read the diff, leave comments, request changes if needed. Author pushes more commits to the same branch to address feedback — they show up automatically on the same PR.

**Approve & merge:**
Once approved and all conversations are resolved, click **Squash and merge** on GitHub (button, not a command — this is exactly why we configured squash-merge as the default in §4). The feature branch auto-deletes (per the repo setting in §4).

**Sync your local machine afterward:**
```bash
git checkout develop
git pull origin develop
git branch -d feature/your-feature-name   # clean up your local copy too
```

#### Stage 8 — Integration testing on `develop`
Before releasing to `main`, everyone pulls `develop` and runs the full app together (both backend options + frontend + database) to catch integration issues that individual feature PRs couldn't catch alone. This is manual testing for this project — no CI is required to do this, though see §9 for an optional lightweight CI addition.

#### Stage 9 — Release: `develop` → `main`
```bash
git checkout main
git pull origin main
```
Then on GitHub: open a PR with base `main`, compare `develop`. Title it something like `Release: v1.0 for final presentation`. The Team Lead reviews and merges using **Create a merge commit** (not squash — see §4).

**Tag the release** (do this locally after the merge, or via GitHub's Releases UI):
```bash
git checkout main
git pull origin main
git tag -a v1.0-presentation -m "Final presentation release"
git push origin v1.0-presentation
```
This gives you a permanent, labeled snapshot of exactly what was demoed — useful if anything needs to be referenced or rolled back to afterward.

#### Stage 10 (rare) — Hotfix
Only if something breaks close to presentation day and can't wait for the normal feature → develop → main cycle:
```bash
git checkout main
git pull origin main
git checkout -b hotfix/fix-login-crash
# fix it, commit, push
git push -u origin hotfix/fix-login-crash
```
Open **two** PRs from this branch: one into `main` (to fix production immediately) and one into `develop` (so the fix isn't lost/overwritten by the next release). Team Lead reviews and merges both.

---

## 8. Quick Reference — All Commands in One Place

```bash
# One-time setup
git clone https://github.com/Bookworm-Organization-07/Bookworm.git
cd Bookworm
git config --global user.name "Your Name"
git config --global user.email "you@example.com"

# Start a new feature (every time)
git checkout develop
git pull origin develop
git checkout -b feature/your-feature-name

# While working
git add .
git commit -m "feat: description of what you did"

# Stay in sync with develop (daily, or before opening a PR)
git fetch origin
git merge origin/develop

# Push and open PR
git push -u origin feature/your-feature-name
# → open PR on GitHub, base = develop

# After your PR is merged
git checkout develop
git pull origin develop
git branch -d feature/your-feature-name

# Check where you are / what's changed
git status
git branch -a
git log --oneline -10
```

---

## 9. Action Checklist — before the final presentation

Roughly in priority order:

1. **Push the real project content**: `frontend/`, `database/`, `dotnet-backend/`, ER diagram, `Books Data/` — the repo currently doesn't reflect the finished local project.
2. **Delete the stale `master` branch** (Settings → Branches, or `git push origin --delete master`).
3. **Verify branch protection rules** on `main` and `develop` match §3 exactly (especially "include administrators" and "require pull request before merging").
4. **Confirm all 9 members have Write access** — only 3 usernames show activity so far.
5. **Add a `.gitignore` entry** for `dotnet-backend/BookwormApi/bin/` and `obj/` before pushing that project (and confirm `frontend/node_modules/` and `backend-java/target/` are already excluded, which they are).
6. **Add a LICENSE** (MIT is a reasonable default for an academic project).
7. **Add `.github/CODEOWNERS`**, and optionally a simple PR template (`.github/PULL_REQUEST_TEMPLATE.md`) with a short checklist: what changed, how it was tested, screenshots if UI.
8. **Expand `README.md`** beyond the current 2 lines — add: what the project is, the tech stack (Java + .NET + React + MySQL), how to run it (can link to `SETUP.md`), and the team member list.
9. Update repo merge-button settings per §4 (squash default, merge commits allowed, rebase disabled, auto-delete branches, suggest-update-branch on).
10. *(Optional, nice-to-have for presentation polish)* A minimal GitHub Actions workflow that just runs `mvn -B compile` / `dotnet build` / `npm run build` on every PR, so "require status checks" in §3 has something real to require. Not required to have a working demo, but it's an easy, genuine "we used CI" talking point for the presentation if there's time.
