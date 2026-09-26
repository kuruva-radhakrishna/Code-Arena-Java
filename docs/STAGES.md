# Build stages

Each stage is implemented and committed independently, with tests, before moving to the next.

| # | Stage | Status | Notes |
|---|-------|--------|-------|
| 0 | Scaffold: monorepo layout, root docs, CI skeleton, Docker Compose (Mongo) | ✅ done | |
| 1 | Backend foundation: Spring Boot app, MongoDB, `User` model, JWT auth (register/login/me), Spring Security, tests | ✅ done | see `backend/README.md` |
| 2 | Problems module: entity + read endpoints (list/detail/discussions) + tests | ✅ done | create/update/delete land in Stage 6 (Admin) |
| 3 | Contests module: entity + leaderboard logic + tests | ✅ done | no explicit "join" step — a leaderboard entry is created on first submission (Stage 4) |
| 4 | Submissions module: entity, run/submit endpoints, compiler client, verdict logic + tests | pending | |
| 5 | AI module: Gemini-based review/debug endpoints (mocked in tests) | pending | |
| 6 | Admin module: role-gated management endpoints | pending | |
| 7 | Compiler service (Java): execution engine for C/C++/Java/Python + Dockerfile + tests | pending | |
| 8 | Frontend adaptation: JWT auth instead of session cookies, updated API base URLs/env vars | pending | |
| 9 | Dockerization: backend/compiler/frontend Dockerfiles + full docker-compose stack | pending | |
| 10 | Deploy configs: `render.yaml`, `vercel.json`, MongoDB Atlas setup docs | pending | |
| 11 | Polish: README overhaul, final smoke-test pass | pending | |

## Design decisions vs. the original app

- **Auth**: JWT bearer tokens instead of Passport session cookies + `connect-mongo`, since the frontend
  (Vercel) and backend (Render) live on different domains.
- **Database**: MongoDB Atlas for the deployed backend; a local Mongo container for dev via Docker Compose.
- **Bugs intentionally fixed, not preserved**: hardcoded session secret, auth middleware returning 200 instead
  of 401 on failure, `getProblemById` returning 500 instead of 404, admin delete endpoints referencing an
  unimported model (always failed), problem/contest validators that were written but never invoked, the
  `Submission.language` enum missing `"java"`, `Contest.leaderBoard[].lastSubmission` written but undeclared
  in the schema, the compiler service hanging on an unrecognized language, and the complete absence of
  execution timeouts (the Java compiler service enforces a process timeout and force-kill). Also fixed: the
  original `/register` endpoint accepted a client-supplied `role`, letting anyone self-register as admin — the
  rewrite always creates new accounts as `USER`. Also: the original `getProblemById` returned every test
  case, including hidden ones, to any caller — `GET /api/problems/{id}` now only ever returns test cases
  marked public. And the original had two overlapping listing endpoints (`/problems/all` and `/problems/`)
  where the "all" one let any logged-in user bypass the anti-cheat filtering meant to hide problems currently
  locked in a live contest — the rewrite has a single `GET /api/problems` endpoint instead; the contest-lock
  filtering itself is added in Stage 3 once the Contest entity exists (now done).
- **Leaderboard storage**: the original embedded a full 2D array of submission copies on each leaderboard
  entry (and wrote a `lastSubmission` field never declared in its schema). The rewrite instead keeps only
  per-problem points, per-problem first-solved timestamps, and the latest submission timestamp — enough to
  render and rank the leaderboard without duplicating submission history. Ties are broken by earliest last
  submission, matching the original's intent (finishing a given score sooner ranks higher).
- **Shared `Discussion` type**: the original duplicated an identical comment-thread subdocument schema on
  both `Problem` and `Contest`; the rewrite defines it once and reuses it. The original also never exposed a
  create-discussion endpoint for contests (only for problems) — the rewrite preserves that asymmetry rather
  than inventing new scope.
- **Verification**: this environment has no local JDK/Maven/Docker, and downloads from GitHub's release-asset
  CDN are blocked by network policy. Toolchains are declared in Dockerfiles/CI, not installed on the host —
  every stage's tests run via GitHub Actions, which provides Java, Maven, and Docker out of the box.
