# LibraFlow Test Report — Security, Database and CI Findings

วันที่ตรวจ: 9 ตุลาคม 2569. ตรวจ branch `sorawit_673380295-9_01` บน Arch Linux.

## Local verification

รัน verification เต็ม 3 รอบต่อเนื่อง โดยแต่ละรอบรัน backend, frontend และ `git diff --check`:

| Check | Command | Result per round |
|---|---|---|
| Backend verification | `cd code/backend && ./mvnw --batch-mode --no-transfer-progress clean verify` | ผ่านทั้ง 3 รอบ: 170 tests, 0 failures, 0 errors, 0 skipped |
| Database integration | Testcontainers with PostgreSQL 16; Flyway migrations V1–V10 | ผ่านทั้ง 3 รอบ; V10 applied and schema reached version 10 |
| Frontend install, lint and build | `cd code/frontend && npm ci && npm run lint && npm run build` | ผ่านทั้ง 3 รอบ; Vite production build completed |
| Dependency audit | `npm ci` audit | 0 vulnerabilities reported in all 3 rounds |
| Patch formatting | `git diff --check` | ผ่านทั้ง 3 รอบ |

เครื่องที่ใช้ทดสอบ: Java 27 (compile target Java 17), Node.js 26.11.1, npm 12.2.0,
Docker Engine 29.9.0 บน Arch Linux. Workflow บน GitHub ตั้ง Java 17 และ Node.js 22;
ผล GitHub Actions ของ pull request ให้ตรวจใน PR ก่อน merge.

Frontend ไม่มี `test` หรือ browser end-to-end script ใน `package.json`; การยืนยัน frontend
ในรายงานนี้จึงครอบคลุม lint และ production build ไม่ใช่ browser interaction test.

## Findings addressed

- Registration now persists `UserProfile` through `UserProfileRepository`.
- User management rejects an administrator changing their own role or suspending their own account at the backend service layer.
- Report controllers now depend on `ReportService`; CSV/PDF generation is delegated through the report generator interface.
- Flyway V10 removes redundant username/email indexes and validates a CHECK constraint for supported fine statuses. Earlier migrations remain unchanged.
- API, data dictionary, design-pattern, SOLID evidence, and CI/deployment documentation now match the implemented routes and workflow behavior.
- GitHub Actions workflows provide CI checks only. They do not deploy; Vercel and Render deployment remains provider-managed, and no provider deployment credentials are added to the repository.
- Reservation readiness is updated in-system and logged. Real email/SMS delivery is future work.

## Production verification limits

Production SPA routes `/login`, `/profile`, and `/admin/users` returned `200 text/html` in a URL
smoke check on 9 October 2026. This verifies the routes responded at that time; it does not identify
the deployed Git commit or database branch. The V10 migration is present and tested locally, but its
application to Render/Neon must be confirmed from the provider and Flyway history after deployment.

## Reproduction commands

```bash
cd code/backend
./mvnw --batch-mode --no-transfer-progress clean verify

cd ../frontend
npm ci
npm run lint
npm run build
```

## Submission ownership

This work was requested by project member 5 and uses the `boatrocl` Git author identity as requested.
The team should review the code and be ready to explain the implementation and test results.
