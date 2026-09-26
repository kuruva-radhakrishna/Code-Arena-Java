# Frontend

React 19 + Vite + MUI single-page app, kept structurally the same as the original app — same components,
routing, and UI — with its data layer adapted to the new Java backend (see [docs/STAGES.md](../docs/STAGES.md)
for the full list of API contract changes this required).

## Configuration

| Env var | Purpose | Local default |
|---|---|---|
| `VITE_BACKEND_URL` | Base URL of the backend API | *(none — must be set, e.g. `http://localhost:8080`)* |

There's no `VITE_COMPILER_URL` anymore. The original had the frontend call the compiler service directly for
the editor's "Run" button (`POST {COMPILER_URL}/run`); that now goes through the backend instead
(`POST /api/execute`), so the frontend only ever needs to know about one backend origin.

## Auth

The backend uses stateless JWT bearer tokens instead of session cookies (see `docs/STAGES.md` for why). The
token is stored in `localStorage` and attached as an `Authorization: Bearer <token>` header by
`src/api/client.js` — every component makes API calls through that shared client (`api.get(...)`,
`api.post(...)`, etc.) rather than raw `axios` calls with `withCredentials: true`.

There's no logout endpoint to call — logging out is just discarding the token client-side
(`AuthContext`'s `logout()`).

## Running locally

```bash
npm install
npm run dev
```

Requires a running backend (`VITE_BACKEND_URL`) — see the root `docker-compose.yml` (Stage 9) for running the
full stack locally.
