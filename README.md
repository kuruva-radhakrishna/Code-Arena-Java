# CodeArena (Java)

A Java Spring Boot + React + MongoDB rewrite of [CodeArena](https://github.com/kuruva-radhakrishna/Code-Arena) —
an online coding platform for practicing problems, competing in contests, and getting AI-assisted code review.

This is a from-scratch reimplementation of the original MERN app's backend and code-execution service in Java,
keeping the React frontend's structure and UI intact. It's built and verified stage by stage; see
[docs/STAGES.md](docs/STAGES.md) for the plan and current status.

## Project structure

```
code-arena-java/
  backend/     Spring Boot REST API (Java 21, MongoDB, JWT auth)
  compiler/    Spring Boot code-execution service (compiles/runs submitted C/C++/Java/Python)
  frontend/    React + Vite + MUI single-page app
  infra/       Local dev support files (Mongo init scripts, etc.)
  docker-compose.yml   Full local dev stack (Mongo, backend, compiler, frontend)
  render.yaml          Render Blueprint for the backend + compiler service
```

## Tech stack

- **Backend**: Java 21, Spring Boot 4, Spring Data MongoDB, Spring Security, JWT (jjwt), Bean Validation
- **Compiler service**: Java 21, Spring Boot 4, `ProcessBuilder`-based execution of C/C++/Java/Python with
  enforced timeouts, containerized with the required toolchains (gcc, g++, python3, JDK)
- **Frontend**: React 19, Vite, MUI, React Router, Axios, Monaco Editor
- **Database**: MongoDB (local Docker container for dev, MongoDB Atlas for the deployed backend)
- **AI**: Google Gemini API for code review/debug assistance
- **Deployment**: frontend on Vercel, backend + compiler service on Render

## Local development

The whole stack (MongoDB, backend, compiler service, frontend) runs with a single command:

```
docker compose up --build
```

- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- Compiler service: http://localhost:8000 (only called internally by the backend)

Copy [`.env.example`](.env.example) to `.env` first if you want to set a real `JWT_SECRET` or a
`GEMINI_API_KEY` (AI endpoints fail without one) — anything left unset falls back to the backend's
built-in dev defaults. Compose only passes these through if they're actually set in your shell/`.env`,
so an unset `JWT_SECRET` still uses the backend's fallback rather than being overridden with an empty
value.

Each module also has its own README for running it standalone, outside Compose:

- `backend/README.md`
- `compiler/README.md`
- `frontend/README.md`

## Deployment

Frontend on Vercel, backend + compiler service on Render, database on MongoDB Atlas. See
[docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) for the full walkthrough.

## Status

See [docs/STAGES.md](docs/STAGES.md) for the staged build plan, what's implemented, and how each stage is tested.
