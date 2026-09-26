# Backend

Spring Boot 4 / Java 21 REST API, backed by MongoDB, using stateless JWT authentication.

## Configuration

All configuration is env-var driven (see `src/main/resources/application.properties` for defaults):

| Env var | Purpose | Local default |
|---|---|---|
| `PORT` | HTTP port | `8080` |
| `MONGODB_URI` | MongoDB connection string | `mongodb://localhost:27017/codearena` |
| `JWT_SECRET` | HMAC signing key for JWTs (32+ bytes) — **must** be set in every deployed environment | insecure dev-only fallback |
| `JWT_EXPIRATION_MS` | Token lifetime in ms | `604800000` (7 days) |
| `CORS_ALLOWED_ORIGINS` | Comma-separated list of allowed frontend origins | `http://localhost:5173` |
| `COMPILER_URL` | Base URL of the compiler service (Stage 7) | `http://localhost:8000` |
| `COMPILER_CONNECT_TIMEOUT_MS` / `COMPILER_READ_TIMEOUT_MS` | HTTP client timeouts for calls to the compiler service | `5000` / `15000` |
| `GEMINI_API_KEY` | Google Gemini API key — **required** for any `/api/ai/**` endpoint to work | *(blank — app still starts, AI calls fail)* |
| `GEMINI_MODEL` | Gemini model name | `gemini-2.5-flash` |
| `GEMINI_BASE_URL` | Generative Language API base URL | `https://generativelanguage.googleapis.com` |
| `GEMINI_CONNECT_TIMEOUT_MS` / `GEMINI_READ_TIMEOUT_MS` | HTTP client timeouts for Gemini calls | `5000` / `30000` |

## Running locally

Requires a MongoDB instance (e.g. `docker compose up mongo` from the repo root):

```bash
./mvnw spring-boot:run
```

## Testing

```bash
./mvnw test
```

Integration tests use Testcontainers to spin up a real MongoDB instance, so they require Docker and will only
run where Docker is available (e.g. GitHub Actions CI) — see [[docs/STAGES.md]] for why this project doesn't
rely on a locally installed JDK/Maven/Docker toolchain.

## Auth

Authentication is stateless JWT bearer tokens (`Authorization: Bearer <token>`), not cookies — see
`docs/STAGES.md` for why this differs from the original app.

- `POST /api/auth/register` — `{firstname, lastname, email, password}` → `201 {token, user}`. New accounts are
  always created with role `USER`; there is no self-service way to register as `ADMIN`.
- `POST /api/auth/login` — `{email, password}` → `200 {token, user}` / `401` on bad credentials.
- `GET /api/auth/me` — requires a valid token → `200 {user}` / `401` if missing/invalid/expired.

## Problems

- `GET /api/problems` — requires auth → summaries of every problem (name, difficulty, topics, likes/dislikes).
- `GET /api/problems/{id}` — public → problem detail. Only test cases marked public are ever included; hidden
  test cases are never exposed over the API.
- `GET /api/problems/{id}/discussions` — public → list of discussions, each with the commenter's public profile.
- `POST /api/problems/{id}/discussions` — requires auth → `{comment}` → `201` with the new discussion.

Creating/editing/deleting problems is an admin-only action, added in Stage 6.

## Contests

- `GET /api/contests` — requires auth → summaries of every contest (title, description, window, problem count).
- `GET /api/contests/{id}` — requires auth → full detail (problems sorted by points ascending, ranked
  leaderboard, discussions). Non-creators get `403` until the contest has started; the creator can always view it.
- `GET /api/contests/{id}/leaderboard` — requires auth → the same ranked leaderboard on its own, sorted by
  total points descending, ties broken by earliest last-submission time.

There's no separate "join" endpoint — a leaderboard entry is created the first time a user submits to one of
the contest's problems. Creating/editing/deleting contests is admin-only, added in Stage 6.

## Submissions

- `POST /api/execute` — requires auth → `{language, code, input}` → ad hoc execution against a single input,
  relayed straight from the compiler service. Not persisted, not graded — backs the editor's "Run" button.
- `POST /api/problems/{id}/submissions` — requires auth → `{language, code}` → grades against every one of the
  problem's test cases (stopping at the first failure), persists a `Submission`, and returns the verdict.
  `WRONG_ANSWER` only ever includes the failing input/expected/actual output when that test case is public.
- `GET /api/problems/{id}/submissions` — requires auth → the caller's own submissions for that problem.
- `GET /api/submissions` — requires auth → all of the caller's own submissions, newest first.
- `GET /api/submissions/{id}` — requires auth → a single submission's detail. Hidden if its problem is
  currently locked inside a *different* live contest than the one the submission belongs to (unlike the
  original, which had no authentication on this endpoint at all).
- `POST /api/contests/{contestId}/problems/{problemId}/submissions` — requires auth → same grading as above,
  but against a contest's live window (`403` if the contest isn't currently active) and updates that contest's
  leaderboard: points are only awarded the first time a user gets `ACCEPTED` on a given problem.

Grading calls out to the compiler service (`COMPILER_URL`, Stage 7) via `CompilerClient` for each test case.

## AI

All AI endpoints require authentication (the original had none at all) and call Gemini via plain REST
(`GeminiClient`), same pattern as the compiler client.

- `POST /api/ai/review` — `{code, problemId}` → `{review}` — code review in the context of a specific problem.
- `POST /api/ai/debug` — `{code, problemDescription?}` → `{debug}`.
- `POST /api/ai/chat` — `{message, chatHistory}` → `{response}` — general programming assistant chat.
- `POST /api/ai/contest-description` — **admin only** — `{contestTitle, problemNames}` → `{description}`.
- `POST /api/ai/problem-draft` — **admin only** — `{problemName, description?, difficulty?, topics?}` →
  a completed problem draft (`problemName, description, constraints, testCases, difficulty, topics, hints`)
  for the admin to review before actually creating the problem (Stage 6).

Any Gemini failure (unreachable, malformed response) surfaces as `502 Bad Gateway`.

## Admin

All `/api/admin/**` endpoints require the `ADMIN` role. Ownership is enforced per-resource: an admin can only
update/delete problems or contests they created themselves.

- `GET /api/admin/problems` — problems created by the caller.
- `POST /api/admin/problems` / `PUT /api/admin/problems/{id}` — `{problemName, description, constraints,
  testCases, difficulty, topics, hints}` → the full problem, including every test case (not just public
  ones — unlike the public-facing problem endpoints, the requester here is verified to be its owner).
- `DELETE /api/admin/problems/{id}` — deletes the problem and all of its submissions.
- `GET /api/admin/contests` — contests created by the caller.
- `POST /api/admin/contests` / `PUT /api/admin/contests/{id}` — `{contestTitle, description, startTime,
  endTime, problems: [{problemId, points?}]}` (at least 3 problems, `points` defaults to 4, every `problemId`
  must reference an existing problem). Updating a contest that has already started is rejected.
- `DELETE /api/admin/contests/{id}` — deletes the contest and clears `contestId` on its submissions (they
  remain as ordinary practice submissions).

## Profile

- `GET /api/profile/summary` — requires auth → `{user, solvedStats, problemTotals, problemsCreatedByMe,
  recentSubmissions, contestsCreatedByMe, attendedContests}`. `solvedStats`/`problemTotals` are
  `{easy, medium, hard, total}` counts of distinct problems; `problemsCreatedByMe`/`contestsCreatedByMe` are
  only populated for `ADMIN` accounts; `attendedContests` includes this user's rank/points/last-submission-time
  for every contest they appear on the leaderboard of. This mirrors an inline route the original had directly
  in `app.js` (not part of any controller), missed in Stages 1-6 and added here once the frontend adaptation
  (Stage 8) surfaced that `Profile.jsx` depends on it.
