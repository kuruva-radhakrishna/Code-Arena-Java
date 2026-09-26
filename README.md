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
  docker-compose.yml   Local dev stack (Mongo now; backend/compiler/frontend added in Stage 9)
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

Each module has its own README with setup details, added as that stage lands:

- `backend/README.md` (Stage 1+)
- `compiler/README.md` (Stage 7+)
- `frontend/README.md` (Stage 8+)

Full-stack local dev via Docker Compose is set up in Stage 9.

## Status

See [docs/STAGES.md](docs/STAGES.md) for the staged build plan, what's implemented, and how each stage is tested.
