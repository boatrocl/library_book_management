# LibraFlow — ระบบจัดการหนังสือในห้องสมุด

ระบบบริหารจัดการห้องสมุดแบบครบวงจร รองรับการจัดการหนังสือและตัวเล่ม การยืม-คืน
การจองคิว และการคิดค่าปรับอัตโนมัติตามประเภทสมาชิก
พัฒนาด้วย Spring Boot 4 ตามสถาปัตยกรรม Layered Architecture ส่วนหน้าเว็บใช้ React
เชื่อมต่อผ่าน REST API พร้อมเอกสาร Swagger/OpenAPI
จัดทำเป็นส่วนหนึ่งของรายวิชา CP353002 Principles of Software Design and Development

---

## สมาชิกกลุ่ม

| ลำดับ | ชื่อ-นามสกุล | รหัสนักศึกษา | Section | Branch | หน้าที่รับผิดชอบ |
|---|---|---|---|---|---|
| 1 |  |  |  |  | Backend: Book / BookCopy / Category / Publisher / Author CRUD, Repository Layer, Swagger Config |
| 2 |  |  |  |  | Backend: Loan / Return flow, State Pattern, Chain of Responsibility |
| 3 |  |  |  |  | Backend: Fine (Strategy), Reservation (Observer), Report (Template Method) |
| 4 |  |  |  |  | Frontend React ทั้งหมด + API Integration |
| 5 |  |  |  |  | Security (JWT), Docker, CI/CD, Deployment, Unit & Integration Test |

> ⚠️ ชื่อ Branch ต้องเป็นรูปแบบ `ชื่อ_รหัสนักศึกษา_section` เท่านั้น (ผิดรูปแบบ = −5 คะแนนรายบุคคล)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend Framework | Spring Boot 4.1.1, Java 17 |
| Build Tool | Maven (Maven Wrapper) |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA (Hibernate) |
| Migration | Flyway |
| API Documentation | springdoc-openapi 3.1.1 (Swagger UI) |
| Frontend | React 18 + Vite + Axios + TailwindCSS |
| Security | Spring Security + JWT |
| Validation | Jakarta Bean Validation |
| Testing | JUnit 5, Mockito, Spring Boot Test, Testcontainers |
| Container | Docker, Docker Compose |
| CI/CD | GitHub Actions |
| Deployment | Render (Backend) + Neon (PostgreSQL) + Vercel (Frontend) |

---

## System Architecture

```text
[ React SPA ]
      | HTTPS / JSON
      v
[ Security Filter (JWT) ]
      v
[ Presentation Layer ]  Controller / RestController + DTO + Mapper
      v
[ Service Layer ]       Business Logic + @Transactional + Design Patterns
      v
[ Repository Layer ]    Spring Data JPA
      v
[ Domain / Entity ]     Entity, Enum, Value Object
      v
[ PostgreSQL ]
```

**กฎเหล็ก:** ห้ามข้าม Layer โดยเด็ดขาด — Controller ห้ามเรียก Repository ตรง ๆ
ทุกการเข้าถึงข้อมูลต้องผ่าน Service Layer เสมอ

รายละเอียดเพิ่มเติม: [`doc/diagrams/12-component-diagram.puml`](doc/diagrams/12-component-diagram.puml)
และ [`doc/diagrams/13-deployment-diagram.puml`](doc/diagrams/13-deployment-diagram.puml)

---

## Database Design (ER Diagram)

ฐานข้อมูลมี **12 ตาราง** ครอบคลุมความสัมพันธ์ครบทุกประเภท

| ประเภทความสัมพันธ์ | ตัวอย่าง |
|---|---|
| One-to-One | `users` ↔ `user_profiles`, `loan_items` ↔ `fines` |
| One-to-Many | `books` → `book_copies`, `loans` → `loan_items`, `users` → `loans` |
| Many-to-Many (โบนัส) | `books` ↔ `authors` ผ่านตาราง `book_authors` |

- ER Diagram: [`doc/diagrams/11-er-diagram.puml`](doc/diagrams/11-er-diagram.puml)
- Data Dictionary: [`doc/data-dictionary.md`](doc/data-dictionary.md)

### Flyway Migration

| ไฟล์ | เนื้อหา | ผู้รับผิดชอบ |
|---|---|---|
| `V1__init_catalog.sql` | categories, publishers, authors, books, book_authors, book_copies | คนที่ 1 |
| `V2__seed_catalog.sql` | ข้อมูลตัวอย่างของตารางชุด catalog | คนที่ 1 |
| `V3__init_users.sql` | users, user_profiles | คนที่ 5 |
| `V3_1__add_role_and_auth_seed.sql` | เพิ่ม role, role constraint และ BCrypt authentication seed สำหรับ ADMIN / LIBRARIAN / MEMBER | คนที่ 5 |
| `V4__init_loan.sql` | loans, loan_items | คนที่ 2 |
| `V5__init_fine_reservation.sql` | fines, reservations | คนที่ 3 |
| `V6__seed_data.sql` | ข้อมูลตัวอย่างส่วนที่เหลือ | ทีม |

> ไฟล์ migration ใช้ร่วมกันทั้งทีม ห้ามแก้ไฟล์ที่ merge เข้า `develop` ไปแล้ว ให้เพิ่มไฟล์ `V` ถัดไปแทน

---

## Installation & Setup

### ความต้องการของระบบ

- JDK 17 หรือสูงกว่า
- Node.js 20+
- Docker Desktop / Docker Engine + Docker Compose
- (ไม่ต้องติดตั้ง Maven — ใช้ Maven Wrapper `./mvnw` ที่มากับโปรเจค)

### ขั้นตอนติดตั้ง

```bash
git clone https://github.com/boatrocl/library_book_management.git
cd library_book_management
cp .env.example .env
```

แก้ไขค่าในไฟล์ `.env`

```properties
DB_URL=jdbc:postgresql://localhost:5432/libraflow
DB_USERNAME=libraflow
DB_PASSWORD=changeme
JWT_SECRET=<random-256-bit-secret>
JWT_EXPIRATION=86400000
FRONTEND_ORIGIN=http://localhost:5173
```

สร้าง JWT Secret สำหรับ development ได้ด้วย:

```bash
openssl rand -base64 32
```

> ห้าม commit `.env`, database password หรือ JWT secret จริงลง Git repository

---

## How to Run

### วิธีที่ 1 — รันทั้งระบบด้วย Docker Compose (แนะนำ)

```bash
docker compose up -d --build
```

| Service | URL |
|---|---|
| Backend API | http://localhost:8080 |
| Frontend | http://localhost:5173 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| PostgreSQL | localhost:5432 |

### วิธีที่ 2 — รันแยกส่วนตอนพัฒนา

```bash
# 1) ฐานข้อมูล
docker run -d --name libraflow-db \
  -e POSTGRES_DB=libraflow \
  -e POSTGRES_USER=libraflow \
  -e POSTGRES_PASSWORD=changeme \
  -p 5432:5432 postgres:16

# 2) Backend
cd code/backend
./mvnw spring-boot:run

# 3) Frontend
cd code/frontend
npm install
npm run dev
```

### บัญชีทดสอบ (seed จาก Flyway)

| Username | Password | Role |
|---|---|---|
| admin | Admin@123 | ADMIN |
| librarian01 | Lib@123 | LIBRARIAN |
| member01 | Mem@123 | MEMBER |

---

## Security / JWT Authentication

ระบบใช้ Spring Security และ JWT สำหรับ Authentication และ Authorization

Login endpoint:

```text
POST /api/v1/auth/login
```

เมื่อ login สำเร็จ ระบบจะส่ง JWT กลับมา:

```json
{
  "token": "<JWT>",
  "tokenType": "Bearer",
  "expiresInSeconds": 86400,
  "username": "admin",
  "role": "ADMIN"
}
```

เมื่อต้องการเรียก Protected Endpoint ให้ส่ง Header:

```text
Authorization: Bearer <JWT>
```

Role ที่รองรับ:

| Role | สิทธิ์หลัก |
|---|---|
| `ADMIN` | จัดการข้อมูลระบบและหนังสือ |
| `LIBRARIAN` | จัดการข้อมูลที่เกี่ยวข้องกับงานห้องสมุด |
| `MEMBER` | ใช้งาน Endpoint ตามสิทธิ์ของสมาชิก |

ตัวอย่าง Security Rule:

- `GET /api/v1/books/**` เป็น Public Endpoint
- `POST /api/v1/books/**` ต้องเป็น `ADMIN` หรือ `LIBRARIAN`
- `PUT /api/v1/books/**` ต้องเป็น `ADMIN` หรือ `LIBRARIAN`
- `PATCH /api/v1/books/**` ต้องเป็น `ADMIN` หรือ `LIBRARIAN`
- `DELETE /api/v1/books/**` ต้องเป็น `ADMIN` หรือ `LIBRARIAN`

รายละเอียดเพิ่มเติม:

- [`doc/security-ci-deployment.md`](doc/security-ci-deployment.md)

---

## API Documentation

- Swagger UI (local): http://localhost:8080/swagger-ui.html
- Swagger UI (production): https://library-book-management-ybt2.onrender.com/swagger-ui.html
- OpenAPI Spec (JSON): `/v3/api-docs`
- รายละเอียด Endpoint ทั้งหมด: [`doc/api-spec.md`](doc/api-spec.md)

Swagger รองรับ Bearer JWT Authentication
โดยสามารถ Login ผ่าน `/api/v1/auth/login`
แล้วนำ Token ไปใช้ผ่านปุ่ม **Authorize**

---

## How to Run Tests

```bash
cd code/backend

./mvnw test          # Unit Test
./mvnw verify        # Unit + Integration Test
```

ระบบทดสอบด้วย:

- JUnit 5
- Mockito
- Spring Boot Test
- Spring Security Test
- Testcontainers
- PostgreSQL 16

Security Unit Tests:

```text
JwtServiceTest
JwtAuthenticationFilterTest
AuthServiceImplTest
```

Integration Test:

```text
AuthSecurityIntegrationTest
```

Integration Test ใช้ PostgreSQL 16 จริงผ่าน Testcontainers
และทดสอบ Flyway migration, authentication, JWT และ role authorization

ผลการทดสอบล่าสุด:

```text
Tests run: 40
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

รายงานผลการทดสอบ:

- Maven Surefire Report: `code/backend/target/surefire-reports/`
- Security / Deployment Test Report: [`test/report/member5-security-test-report.md`](test/report/member5-security-test-report.md)

---

## CI/CD

Backend ใช้ GitHub Actions สำหรับ Continuous Integration

Workflow:

```text
.github/workflows/backend-ci.yml
```

เมื่อ Workflow ทำงาน ระบบจะ:

```text
Checkout Repository
      |
      v
Setup Java 17
      |
      v
Check Docker
      |
      v
Maven Clean Verify
      |
      v
Unit Test + Integration Test
```

คำสั่งหลักของ CI:

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
```

Integration Test สามารถสร้าง PostgreSQL ชั่วคราวผ่าน Testcontainers
บน GitHub Actions runner ได้

---

## Production Deployment

### PostgreSQL

Production Database ใช้ Neon PostgreSQL

Database:

```text
libraflow
```

การเชื่อมต่อ Production ใช้ SSL และเก็บ credential ผ่าน Environment Variables

รูปแบบ JDBC URL:

```text
jdbc:postgresql://<NEON_HOST>:5432/libraflow?sslmode=require
```

### Backend

Backend deploy ด้วย Docker บน Render จาก branch:

```text
develop
```

Render configuration:

```text
Root Directory: code/backend
Dockerfile Path: ./Dockerfile
Docker Build Context Directory: .
```

Environment Variables ที่ Backend ใช้:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
JWT_EXPIRATION
FRONTEND_ORIGIN
SERVER_PORT
```

Production Backend:

```text
https://library-book-management-ybt2.onrender.com
```

Production Swagger:

```text
https://library-book-management-ybt2.onrender.com/swagger-ui.html
```

> Production secret เช่น `DB_PASSWORD` และ `JWT_SECRET`
> ต้องเก็บใน Environment Variables ของ deployment platform เท่านั้น

---

## Deployment URL

| ส่วน | URL |
|---|---|
| Frontend | _(รอ deploy)_ |
| Backend API | https://library-book-management-ybt2.onrender.com |
| Swagger UI | https://library-book-management-ybt2.onrender.com/swagger-ui.html |
| PostgreSQL | Neon PostgreSQL |

---

## Project Structure

```text
library_book_management/
├── .github/
│   └── workflows/
│       └── backend-ci.yml
├── code/
│   ├── backend/                 # Spring Boot
│   │   ├── Dockerfile
│   │   ├── pom.xml
│   │   ├── mvnw / mvnw.cmd
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/libraflow/library/
│   │       │   │   ├── config/
│   │       │   │   ├── controller/api/
│   │       │   │   ├── service/impl/
│   │       │   │   ├── repository/
│   │       │   │   ├── security/        # JWT / Spring Security
│   │       │   │   ├── domain/entity/
│   │       │   │   ├── domain/enums/
│   │       │   │   ├── dto/request/
│   │       │   │   ├── dto/response/
│   │       │   │   ├── mapper/
│   │       │   │   ├── pattern/
│   │       │   │   ├── exception/
│   │       │   │   └── common/
│   │       │   └── resources/
│   │       │       └── db/migration/
│   │       │           ├── V1__init_catalog.sql
│   │       │           ├── V2__seed_catalog.sql
│   │       │           ├── V3__init_users.sql
│   │       │           └── V3_1__add_role_and_auth_seed.sql
│   │       └── test/java/com/libraflow/library/
│   │           ├── controller/api/
│   │           ├── integration/
│   │           │   └── AuthSecurityIntegrationTest.java
│   │           ├── security/
│   │           │   ├── JwtAuthenticationFilterTest.java
│   │           │   └── JwtServiceTest.java
│   │           └── service/impl/
│   │               └── AuthServiceImplTest.java
│   └── frontend/                # React + Vite
├── test/
│   ├── unit/
│   ├── integration/
│   └── report/
│       └── member5-security-test-report.md
├── doc/
│   ├── project-overview.md
│   ├── solid-analysis.md
│   ├── design-patterns.md
│   ├── data-dictionary.md
│   ├── api-spec.md
│   ├── security-ci-deployment.md
│   ├── diagrams/                # ไฟล์ .puml ทั้งหมด + ภาพ export
│   └── slide/
├── docker-compose.yml
├── .env.example
└── img/
```

---

## Git Workflow

| Branch | หน้าที่ |
|---|---|
| `main` | Production — merge ได้เฉพาะเวอร์ชันที่ส่งมอบ |
| `develop` | Integration — รวมงานจากทุกคน |
| `ชื่อ_รหัส_section` | Branch ส่วนตัวของแต่ละคน |

ตั้งค่าก่อนเริ่มงานทุกครั้ง

```bash
git config --local user.name  "ชื่อจริงของตนเอง"
git config --local user.email "อีเมลที่ผูกกับบัญชี GitHub ของตนเอง"
git checkout -b somchai_66123456_01
```

**Commit Message Convention:** `<type>: <สิ่งที่ทำ>`

```text
feat: add customer registration API
fix: correct fine calculation for overdue loan
refactor: extract discount strategy interface
test: add unit test for LoanService
docs: update API specification
```

การรวมงานทุกครั้งต้องเปิด Pull Request เข้า `develop`
และมี reviewer ในทีมอย่างน้อย 1 คน