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
the contest's problems (Stage 4). Creating/editing/deleting contests is admin-only, added in Stage 6.
