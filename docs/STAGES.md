# Build stages

Each stage is implemented and committed independently, with tests, before moving to the next.

| # | Stage | Status | Notes |
|---|-------|--------|-------|
| 0 | Scaffold: monorepo layout, root docs, CI skeleton, Docker Compose (Mongo) | ✅ done | |
| 1 | Backend foundation: Spring Boot app, MongoDB, `User` model, JWT auth (register/login/me), Spring Security, tests | ✅ done | see `backend/README.md` |
| 2 | Problems module: entity + read endpoints (list/detail/discussions) + tests | ✅ done | create/update/delete land in Stage 6 (Admin) |
| 3 | Contests module: entity + leaderboard logic + tests | ✅ done | no explicit "join" step — a leaderboard entry is created on first submission (Stage 4) |
| 4 | Submissions module: entity, run/submit endpoints, compiler client, verdict logic + tests | ✅ done | see `backend/README.md` |
| 5 | AI module: Gemini-based review/debug/chat/authoring endpoints (mocked in tests) | ✅ done | see `backend/README.md` |
| 6 | Admin module: role-gated problem/contest CRUD | ✅ done | see `backend/README.md` |
| 7 | Compiler service (Java): execution engine for C/C++/Java/Python + tests | ✅ done | see `compiler/README.md`; its Dockerfile lands in Stage 9 |
| 8 | Frontend adaptation: JWT auth instead of session cookies, updated API base URLs/env vars | ✅ done | see `frontend/README.md`; kept the frontend structurally identical, only the data layer changed |
| 9 | Dockerization: backend/compiler/frontend Dockerfiles + full docker-compose stack | ✅ done | |
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
  every stage's tests run via GitHub Actions, which provides Java, Maven, and Docker out of the box. The CI
  workflow re-emits Maven `[ERROR]` lines and failing surefire reports as `::error::` annotations, since
  GitHub's log viewer otherwise requires signing in to see full step output even on a public repo.
- **Compiler contract fixed, not preserved**: the original compiler service had no response at all for an
  unrecognized language (the request just hung) and returned errors with an implicit HTTP 200. The rewrite's
  contract (`POST /api/execute` on the compiler service, implemented for real in Stage 7) always responds:
  `SUCCESS`/`COMPILATION_ERROR`/`RUNTIME_ERROR`/`TIME_LIMIT_EXCEEDED` with HTTP 200 for any well-formed
  request, and a real 4xx/5xx only for a malformed request or genuine internal failure.
- **Submission grading**: `WRONG_ANSWER` details (input/expected/actual) are only ever returned to the
  submitter when the failing test case is public — the original leaked hidden test-case content in the
  failure response for a graded submission, same class of bug as Stage 2's problem-detail fix.
- **Submission visibility**: the original's single-submission view (`GET /submissions/single/:id`) had *no
  authentication at all* — anyone could read anyone else's submitted source code by guessing/enumerating IDs,
  gated only by whether the underlying problem happened to be locked in a live contest. The rewrite requires
  authentication and keeps the contest-lock gating (a submission for a problem currently locked in a
  *different* live contest than the one it belongs to is hidden from everyone, not just the public).
- **AI endpoints require auth, not preserved as fully public**: the original had zero auth on any `/ai/*`
  route — anyone, logged in or not, could burn the app's Gemini quota for free. All AI endpoints now require
  authentication; the two admin-authoring ones (drafting a problem, writing a contest description) require
  the `ADMIN` role. The original's `/ai/review` also referenced a nonexistent `prob.description` field (the
  schema field is `problemDescription`), so the problem's actual text was silently never included in the
  prompt — not an issue in the rewrite since the field is just called `description` consistently. The
  original's separate, never-implemented `/ai/generate-problem` stub (a pure echo, no real AI call) was
  dropped rather than reproduced; `/ai/createProblem`'s real behavior is what became `/api/ai/problem-draft`.
- **Gemini access via plain REST, not a client SDK**: calls Google's Generative Language REST API
  (`POST /v1beta/models/{model}:generateContent`) directly through a small `GeminiClient` interface, the same
  pattern used for the compiler service — deliberately avoiding a dependency on a Java GenAI SDK whose exact
  API surface couldn't be verified against real Maven Central artifacts the way the plain REST contract could.
- **Admin validators actually run**: the original wrote `validateProblem`/`validateContest` functions that were
  never invoked by any controller — problem/contest creation only ever got Mongoose's schema-level checks
  (and contest updates got none at all). The rewrite validates every create/update through Bean Validation on
  the request DTO plus explicit service-level checks: topics must be in the allowed list, a contest's
  problem ids must reference problems that actually exist (the original never checked this, so a contest
  could reference a deleted or nonexistent problem), and `endTime` must be after `startTime`.
- **Admin delete cascade actually works**: the original's `deleteProblem`/`deleteContest` referenced a
  `Submission` model that was never imported in `AdminController.js` — the cascade line threw a
  `ReferenceError` every time, so both admin-delete endpoints always returned 500 in practice. The rewrite's
  `AdminService` genuinely deletes a problem's submissions and clears `contestId` on a deleted contest's
  submissions.
- **Full test-case visibility for the owning admin**: `AdminProblemResponse` (used only for create/update,
  where the requester is verified to be the problem's own creator) includes every test case, unlike
  `ProblemDetailResponse` (Stage 2), which only ever shows the public ones.
- **Compiler service isolation and timeouts**: each execution runs in its own freshly created temp directory
  (deleted afterward), so concurrent submissions never collide — the original wrote every submission into one
  shared `codes/`/`inputs/` folder and had to generate a unique Java class name per submission to work around
  it; here submitted Java code just defines `public class Main` directly. `ProcessRunner` enforces a
  configurable wall-clock timeout on every run step (the original had none — an infinite loop hung forever)
  and drains stdout/stderr on background threads while writing stdin, avoiding the classic `ProcessBuilder`
  deadlock. A Python `SyntaxError` is classified as `COMPILATION_ERROR` (matching a judge's usual convention),
  matching the original's own string-matching heuristic. Per-execution memory limiting is out of scope here —
  the container this service runs in gets an overall memory cap at the deployment level instead (Stage 9/10);
  the original declared a `pidusage` dependency for this but never actually used it.
- **Compiler service tests exercise the real toolchain**: `CodeExecutionServiceTest` runs actual `gcc`/`g++`/
  `python3`/`javac` rather than mocking them, since GitHub Actions' `ubuntu-latest` runners already have all
  four installed — no Docker/Testcontainers needed for this module's tests.
- **Frontend adaptation kept the app structurally identical**: same components, same routing, same UI — only
  the data layer (API base URLs, endpoint paths, response field names, auth mechanism) changed. A shared
  `src/api/client.js` axios instance now attaches JWT bearer tokens from `localStorage`, replacing
  `withCredentials: true` session cookies everywhere. The editor's "Run" button now calls the backend
  (`POST /api/execute`) instead of the compiler service directly, so the frontend only needs one backend
  origin — `VITE_COMPILER_URL` is gone.
- **Two more real gaps found while wiring the frontend up, fixed rather than reproduced**: `EditProblem.jsx`
  would have fetched via the public `GET /api/problems/{id}` (public test cases only) and silently deleted a
  problem's hidden test cases on the next save — fixed by adding `GET /api/admin/problems/{id}` (full detail,
  ownership-checked). Profile.jsx's delete-contest button called `DELETE /contests/:id`, which never existed
  on the original backend at all (only `/admin/contests/:id` did) — that button never actually worked there;
  pointed it at the real endpoint instead of reproducing the dead one.
- **Leaderboard UI shows solved/not-solved, not solved/wrong/never-attempted**: Stage 3's leaderboard model
  only tracks per-problem points and solved-at, not full submission history, so the frontend can no longer
  distinguish "attempted but still wrong" from "never attempted" the way the original's embedded 2D submission
  array could. A minor, deliberate loss of granularity in exchange for not duplicating submission history.
- **Frontend Dockerfile is dev-mode only**: it runs Vite's dev server (`npm run dev -- --host 0.0.0.0`), not a
  production build behind a static server — production hosting is Vercel (Stage 10), which builds directly
  from the repo and never uses this image. It exists purely so `docker compose up` gives a working full-stack
  environment without installing Node locally.
- **Compiler service runtime image needs a full JDK, not a JRE**: unlike the backend (which only ever runs
  itself, so a JRE-only runtime stage is enough), the compiler service invokes `javac` on submitted Java code
  at request time, so its runtime image keeps the JDK. `gcc`/`g++`/`python3` are installed the same way, since
  the same reasoning applies to those languages.
- **`JWT_SECRET`/`GEMINI_API_KEY` passed through, not hardcoded or defaulted, in docker-compose.yml**: written
  as bare `- JWT_SECRET` entries (Compose's "pass through from host env if set" form) rather than
  `JWT_SECRET=${JWT_SECRET:-}`, which would set an *empty* value in the container whenever the host doesn't
  have it set and silently defeat the backend's own built-in dev-fallback secret. A root `.env.example`
  documents these as optional.
- **Mongo healthcheck added in Stage 9**: `backend` declares `depends_on: mongo: condition: service_healthy`,
  which needs Mongo to expose a healthcheck (`mongosh --eval "db.adminCommand('ping')"`) — without it Compose
  only waits for the container to *start*, not for `mongod` to actually be accepting connections yet.
- **Real Spring Boot 4 bug found via the Docker smoke test, not the unit/integration test suite**: Spring Boot 4
  split the old monolithic `spring-boot-autoconfigure` jar into per-technology modules, and moved MongoDB's own
  connection properties (`uri`/`host`/`port`/`username`/`password`/`ssl`) from `spring.data.mongodb.*` to a new
  `spring.mongodb.*` prefix — `spring.data.mongodb.*` is now Spring-Data-only (GridFS, auto-index, field
  naming), with no `uri` field at all. `application.properties` still used the old prefix, so `MONGODB_URI` was
  silently inert everywhere except the test suite, and the app always fell back to the Mongo Java driver's own
  `localhost:27017` default — invisible in every environment tried before Stage 9, since `@SpringBootTest`
  integration tests use `@ServiceConnection` with Testcontainers (`TestcontainersConfiguration.java`), which
  wires a `MongoConnectionDetails` bean directly and never resolves the property by name at all. Confirmed by
  downloading and reading the actual `spring-boot-mongodb` 4.1.1 sources from Maven Central, the same technique
  used earlier in this project for other Spring Boot 4 breaking changes. Fixed by renaming the property to
  `spring.mongodb.uri`.
