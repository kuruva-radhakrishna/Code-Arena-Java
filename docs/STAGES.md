# Build stages

Each stage is implemented and committed independently, with tests, before moving to the next.

| # | Stage | Status | Notes |
|---|-------|--------|-------|
| 0 | Scaffold: monorepo layout, root docs, CI skeleton, Docker Compose (Mongo) | ✅ done | |
| 1 | Backend foundation: Spring Boot app, MongoDB, `User` model, JWT auth (register/login/me), Spring Security, tests | ✅ done | see `backend/README.md` |
| 2 | Problems module: entity + CRUD/filter endpoints + tests | pending | |
| 3 | Contests module: entity + join/leaderboard logic + tests | pending | |
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
  rewrite always creates new accounts as `USER`.
- **Verification**: this environment has no local JDK/Maven/Docker, and downloads from GitHub's release-asset
  CDN are blocked by network policy. Toolchains are declared in Dockerfiles/CI, not installed on the host —
  every stage's tests run via GitHub Actions, which provides Java, Maven, and Docker out of the box.
