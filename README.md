# LibraFlow

LibraFlow is a library management web application built for the CP353002 software design project. It supports a public book catalog and staff workflows for circulation, reservations, fines, and reports.

## Features

- Search the catalog by title, author, category, and availability.
- Let members borrow available titles or join a reservation queue.
- Manage books and physical copies, record loans and returns, and renew loans.
- Review member loan and fine history; record fine payments.
- Manage user roles and account status, and export loan and overdue reports.

## Technology

- Backend: Java 17, Spring Boot, Spring Security, Spring Data JPA, Flyway
- Database: PostgreSQL 16
- Frontend: React, Vite, Axios, Tailwind CSS
- Tests: JUnit 5, Mockito, Spring Boot Test, Testcontainers
- CI: GitHub Actions
- Hosting: Vercel (frontend), Render (backend), Neon (PostgreSQL)

## Architecture

The application uses a layered backend: REST controllers delegate to services, services use repositories for persistence, and DTOs define the API contract. Business rules use Chain of Responsibility, loan state transitions use State, fine calculation uses Strategy, reservation queue updates use Spring application events, and report formats share a Template Method.

- [Component diagram](doc/diagrams/12-component-diagram.puml)
- [Design patterns and code locations](doc/design-patterns.md)
- [Project scope and business rules](doc/project-overview.md)

## Run locally

Requirements: Docker with Compose, Java 17, and Node.js 20.19+ or 22.12+.

1. Create a local environment file and set a development JWT secret:

   ```bash
   cp .env.example .env
   openssl rand -base64 32
   ```

   Put the generated value in `JWT_SECRET` in `.env`. Do not commit `.env` or real credentials.

2. Start PostgreSQL and the backend:

   ```bash
   docker compose up -d --build db backend
   ```

   Compose starts the database and backend only. The frontend is run separately.

3. In another terminal, start the frontend:

   ```bash
   cd code/frontend
   npm ci
   npm run dev
   ```

Local URLs:

| Service | URL |
|---|---|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |

## Verify changes

Run the same checks used by the repository's CI workflows:

```bash
cd code/backend
./mvnw --batch-mode --no-transfer-progress clean verify
```

```bash
cd code/frontend
npm ci
npm run lint
npm run build
```

Backend integration tests use PostgreSQL through Testcontainers and require Docker. GitHub Actions runs backend verification and frontend lint/build on pushes to all branches and on pull requests into `develop` or `main`:

- `.github/workflows/backend-ci.yml`
- `.github/workflows/frontend-ci.yml`

The CI workflows do not deploy the LibraFlow application. After Vercel reports a successful Production deployment, `production-deployment-smoke.yml` checks the deployed SPA routes, OpenAPI document, and public catalog. This is a post-deploy verification; Vercel's GitHub integration performs the frontend deployment.

## CD teaching demo

The separate [CD Demo](doc/cd-demo.md) builds and validates a small status page, then publishes that artifact to GitHub Pages. It does not deploy the LibraFlow app or change its Vercel, Render, or Neon services. The production smoke check observes the provider deployment after it completes; it does not deploy the app or replace provider configuration.

## API and deployment

- [REST API specification](doc/api-spec.md)
- [Swagger UI](https://library-book-management-ybt2.onrender.com/swagger-ui.html)
- Frontend: https://library-book-management-alpha.vercel.app/
- Backend: https://library-book-management-ybt2.onrender.com/

The deployment URLs do not by themselves prove which Git branch or database branch is configured. Verify those values in the provider dashboards before describing the production release path.

## Current scope limits

- Reservation notifications are written to application logs; no external email or SMS provider is connected.
- Fines are recorded when a late item is returned; the scheduler changes loan status but does not add a daily balance.
- The API currently has no return-condition workflow for marking a returned copy damaged or repairing/discarding it.
- Fine payment records an in-system payment; no online payment gateway is connected.

## Repository map

```text
.github/workflows/       Backend CI, frontend CI, isolated CD demo
code/backend/            Spring Boot API and tests
code/frontend/           React application
doc/                     Requirements, API, design analysis, diagrams
test/report/             Verification notes
docker-compose.yml        Local PostgreSQL and backend services
```

## Contribution flow

Use a task branch, open a pull request into `develop`, and request review. Merge `develop` into `main` for a release after CI and review are complete. Use descriptive commits such as `feat:`, `fix:`, `test:`, `refactor:`, and `docs:`.
