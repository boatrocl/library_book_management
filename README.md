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

```
[ React SPA ]
      | HTTPS / JSON
      v
[ Security Filter (JWT) ]
      v
[ Presentation Layer ]  Controller / RestController  +  DTO + Mapper
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
```

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

## API Documentation

- Swagger UI (local): http://localhost:8080/swagger-ui.html
- Swagger UI (production): _(รอ deploy)_
- OpenAPI Spec (JSON): `/v3/api-docs`
- รายละเอียด Endpoint ทั้งหมด: [`doc/api-spec.md`](doc/api-spec.md)

---

## How to Run Tests

```bash
cd code/backend

./mvnw test          # Unit Test (JUnit 5 + Mockito)
./mvnw verify        # Unit + Integration Test (Testcontainers + PostgreSQL)
```

รายงานผลการทดสอบ:
- `code/backend/target/surefire-reports/`
- สำเนา Test Report: `test/report/`

---

## Deployment URL

| ส่วน | URL |
|---|---|
| Frontend | _(รอ deploy)_ |
| Backend API | _(รอ deploy)_ |
| Swagger UI | _(รอ deploy)_ |

---

## Project Structure

```
library_book_management/
├── code/
│   ├── backend/                 # Spring Boot
│   │   ├── pom.xml
│   │   ├── mvnw / mvnw.cmd
│   │   └── src/main/java/com/libraflow/library/
│   │       ├── config/
│   │       ├── controller/api/
│   │       ├── service/impl/
│   │       ├── repository/
│   │       ├── domain/entity/
│   │       ├── domain/enums/
│   │       ├── dto/request/
│   │       ├── dto/response/
│   │       ├── mapper/
│   │       ├── pattern/         # strategy / state / chain / observer / template
│   │       ├── exception/
│   │       └── common/
│   └── frontend/                # React + Vite
├── test/
│   ├── unit/
│   ├── integration/
│   └── report/
├── doc/
│   ├── project-overview.md
│   ├── solid-analysis.md
│   ├── design-patterns.md
│   ├── data-dictionary.md
│   ├── api-spec.md
│   ├── diagrams/                # ไฟล์ .puml ทั้งหมด + ภาพ export
│   └── slide/
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

```
feat: add customer registration API
fix: correct fine calculation for overdue loan
refactor: extract discount strategy interface
test: add unit test for LoanService
docs: update API specification
```

การรวมงานทุกครั้งต้องเปิด Pull Request เข้า `develop` และมี reviewer ในทีมอย่างน้อย 1 คน
