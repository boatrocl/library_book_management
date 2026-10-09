# LibraFlow — Security, CI and Deployment

เอกสารนี้อธิบาย implementation ที่พบใน repository และขั้นตอน release ที่ทีมใช้ตรวจสอบได้
ค่าลับและการตั้งค่าบริการจริงต้องตรวจใน dashboard ของผู้ให้บริการ โดยไม่เผยค่า credential

## Authentication and authorization

- Login: `POST /api/v1/auth/login`; สมัครสมาชิก: `POST /api/v1/auth/register`.
- สำเร็จแล้วได้ Bearer JWT; ส่งผ่าน `Authorization: Bearer <token>` กับ protected endpoint.
- Password ใช้ `BCryptPasswordEncoder`; JWT ใช้ HMAC key จาก Base64 `JWT_SECRET` ซึ่งต้อง decode ได้อย่างน้อย 32 bytes.
- API เป็น stateless; JWT ตรวจ issuer, signature และ expiration. Expiration มาจาก `JWT_EXPIRATION` หน่วย milliseconds.
- `GET /api/v1/books/**`, auth endpoints, Swagger และ CORS preflight เปิดให้เรียกได้โดยไม่ยืนยันตัวตน.
- `POST /api/v1/loans/self` จำกัดเฉพาะ `MEMBER`; backend ระบุ username จาก JWT, ไม่รับ member ID จาก browser และตรวจ `termsAccepted: true`.
- `GET/POST /api/v1/reservations/self` จำกัดเฉพาะ `MEMBER`; backend ใช้ username จาก JWT สำหรับดูประวัติและสร้างคิวจอง พร้อมตรวจ `termsAccepted: true` ในคำขอสร้างคิว.
- Endpoint ยืมที่เคาน์เตอร์ `POST /api/v1/loans` จำกัด `LIBRARIAN` หรือ `ADMIN`.
- การเขียน/แก้/ลบหนังสือจำกัด `ADMIN` หรือ `LIBRARIAN`; endpoint อื่นต้องยืนยันตัวตนและอาจมี method-level role เพิ่ม.
- CORS อนุญาต origin เดียวจาก `FRONTEND_ORIGIN`; production ต้องตั้งเป็น URL ของ frontend จริง.

ตำแหน่งหลัก: `security/SecurityConfig.java`, `security/JwtService.java`,
`security/JwtAuthenticationFilter.java`, `controller/api/AuthController.java`.

## Database and migrations

- PostgreSQL เป็นฐานข้อมูลหลัก; Hibernate ตั้ง `ddl-auto=validate` และ Flyway จัดการ schema.
- Migrations ปัจจุบันคือ V1, V2, V3, V3_1, V4, V5, V7, V8, V9 และ V10; repository ไม่มี V6.
- V8 เพิ่ม FK ที่ขาดให้ `fines.loan_item_id`, `reservations.user_id` และ `reservations.book_id`.
- V9 ผูก Reservation READY กับ BookCopy จริง เพิ่มข้อบังคับ READY ต้องมีตัวเล่ม/เวลาหมดอายุ และ reset READY เก่าที่ไม่เคยกัน copy.
- V10 ลบ index username/email ที่ซ้ำกับ unique constraints และเพิ่ม CHECK สำหรับ `fines.status`; migration ตรวจค่าที่มีอยู่ก่อน validate constraint.
- V8 ตรวจ orphan rows ก่อนสร้าง constraints. ทดสอบกับ staging/สำเนาฐานข้อมูลก่อน deploy; ห้ามแก้ migration ที่ใช้ไปแล้ว.
- ไฟล์ migration ที่อยู่ใน Git ไม่ยืนยันว่า production database ได้รันไฟล์นั้นแล้ว. ตรวจ Flyway history และ schema ใน Neon ก่อนสรุป.

## Tests and CI

GitHub Actions workflows: `.github/workflows/backend-ci.yml` และ `.github/workflows/frontend-ci.yml`.

- Push ไป `main`, `develop` หรือ branch สมาชิกที่ระบุจะรัน backend `clean verify` และ frontend lint/build แยก workflow.
- Pull request เข้า `develop` หรือ `main` จะรันทั้ง backend และ frontend workflows.
- Backend integration test `AuthSecurityIntegrationTest` ใช้ PostgreSQL 16 ผ่าน Testcontainers; ต้องมี Docker daemon.
- Frontend checks ใช้ `npm ci`, `npm run lint` และ `npm run build`.

Workflows เหล่านี้เป็น CI: ไม่มี deploy job. Vercel preview/production และ Render deployment
เป็นงานของ provider integration/dashboard แยกจาก GitHub Actions; ต้องตรวจ deployment status ที่ provider
ก่อนยืนยันว่า production อัปเดตแล้ว. การเพิ่ม GitHub Actions deploy job ต้องตั้ง provider credentials
ใน secret store ก่อน; ห้ามใส่ token ลง repository.

คำสั่งตรวจในเครื่อง (Linux/macOS; บน Windows ให้ใช้ `mvnw.cmd`):

```bash
cd code/backend
./mvnw clean verify

cd ../frontend
npm ci
npm run lint
npm run build
npm audit --audit-level=high
```

ถ้าไม่มี Docker และต้องแยกตรวจ unit/API tests จาก integration test ชั่วคราว:

```bash
cd code/backend
./mvnw -Dtest="*Test,!AuthSecurityIntegrationTest" test
```

การรันแบบยกเว้น integration test ไม่ถือว่าแทนผล `clean verify`; ให้บันทึกข้อจำกัดนี้ในรายงาน
และให้ CI ที่มี Docker รัน integration test ก่อน merge.

## Deployment and branches

การแบ่งหน้าที่ของ branch:

| Branch | ใช้งาน |
|---|---|
| `main` | release/production |
| `develop` | integration และทดสอบรวม |
| `ชื่อ_รหัสนักศึกษา_Section` | งานรายบุคคล; เปิด PR เข้า `develop` |

หลัง CI ผ่านและมีสมาชิก review ให้ทีมเปิด PR จาก `develop` เข้า `main` สำหรับ release.
Vercel ควร deploy production จาก `main` และสร้าง preview สำหรับ branch ทดสอบ.
Render production ควรชี้ `main`; ตรวจค่าจริงใน Render Dashboard ก่อนเปลี่ยน service.

`code/frontend/vercel.json` กำหนด SPA fallback ไปยังหน้า app. ตรวจ production deep link เมื่อ 9 ตุลาคม 2026:
`/login`, `/profile` และ `/admin/users` ตอบ `200 text/html` เมื่อเปิดตรงหรือ refresh.
สถานะนี้ยืนยันการตอบสนองของ URL ณ เวลาตรวจ แต่ไม่ยืนยันว่า Render เชื่อม Neon branch ใด.

ค่าที่ต้องตรวจใน Render: deployment branch, Root Directory (`code/backend`), Dockerfile path,
build context และชื่อตัวแปร `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`,
`JWT_EXPIRATION`, `FRONTEND_ORIGIN`, `SERVER_PORT`. แสดงเฉพาะชื่อ variable; ปิดบังค่า.

Neon branch ที่ชื่อ `production` เป็น branch ฝั่งฐานข้อมูล ไม่ได้ยืนยันว่า Render ใช้ branch นั้น.
ยืนยันการเชื่อมต่อโดยเทียบ host/database จาก Render `DB_URL` แบบปิดบัง credential
กับ connection details ของ Neon; อย่าส่ง password หรือ URL เต็มลงแชทหรือ Git.

## Public URLs and current verification limits

| Service | URL |
|---|---|
| Frontend | https://library-book-management-alpha.vercel.app/ |
| Backend | https://library-book-management-ybt2.onrender.com/ |
| Swagger UI | https://library-book-management-ybt2.onrender.com/swagger-ui.html |

การเปิด URL สาธารณะทดสอบเพียงการตอบสนองของ service ไม่สามารถพิสูจน์ branch ที่ deploy,
ค่า environment variable หรือฐานข้อมูลที่เชื่อมต่อได้. สำหรับข้อสรุปดังกล่าวต้องมีภาพหน้า Settings
ของ Render ที่เห็น branch และชื่อตัวแปรโดยปิดบังค่า secret.

## Secret handling

- `.env` และ secret จริงต้องไม่ commit; ใช้ `.env.example` เป็น template เท่านั้น.
- หากเคยเผย password, JWT secret หรือ connection string ให้ rotate จาก provider แล้วอัปเดต secret store.
- อย่าใช้ credential seed สำหรับ production; เปลี่ยนหรือปิดบัญชีเริ่มต้นก่อนเปิดใช้งานจริง.
