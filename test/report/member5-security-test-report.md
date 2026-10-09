# LibraFlow Test Report — Security and Deployment

## Test run

วันที่ตรวจ: 8 ตุลาคม 2569. ตรวจ working tree บน branch `sorawit_673380295-9_01`.
Target backend คือ Java 17; เครื่องตรวจใช้ Java 23.0.1, Node.js 24.14.0 และ npm 11.9.0.

## Results

| Check | Command | Result |
|---|---|---|
| Backend unit / API tests | `mvnw.cmd -Dtest="*Test,!AuthSecurityIntegrationTest" test` | ผ่าน 133, failures 0, errors 0 |
| Full backend verification | `mvnw.cmd clean verify` | 133 ผ่าน, 0 failures, 1 error จากทั้งหมด 134 tests: Testcontainers หา Docker Engine ไม่พบ |
| Frontend lint | `npm run lint` | ผ่าน |
| Frontend production build | `npm run build` | ผ่าน; Vite สร้าง assets สำเร็จ |
| Frontend dependency audit | `npm audit --audit-level=high` | 0 vulnerabilities |
| Vercel home | `GET /` | HTTP 200 |
| Render Swagger UI | `GET /swagger-ui.html` | HTTP 200 |
| Render OpenAPI | `GET /v3/api-docs` | HTTP 200 |
| Render public books endpoint | `GET /api/v1/books` | HTTP 200 |

คำสั่ง backend ที่ยกเว้น integration test ใช้เพื่อแยกยืนยัน unit/API tests เท่านั้น
และไม่ได้แทน `clean verify`. Full verification error เกิดเพราะเครื่องไม่มี Docker Engine
ที่ Testcontainers ต้องใช้; ไม่มี assertion failure ในรอบ `clean verify` ล่าสุด. ผล unit/API test ที่ไม่ใช้
Docker ผ่านครบ 133 รายการ.

## Security implementation checked in source

- Password hashing ด้วย BCrypt.
- JWT secret อ่านจาก Base64 `JWT_SECRET` และตรวจความยาวขั้นต่ำ 256 bits; token ตรวจ issuer, signature และ expiration.
- API แบบ stateless; role checks อยู่ใน `SecurityConfig` และ method-level annotations.
- CORS จำกัด origin ด้วย `FRONTEND_ORIGIN`.
- Production credentials ต้องอยู่ใน environment variables; ห้ามใส่ secret ใน Git หรือแชท.

## Database and migration notes

- Hibernate ใช้ `ddl-auto=validate`; Flyway เป็นเจ้าของการเปลี่ยน schema.
- V8 เพิ่ม foreign keys ที่ขาดสำหรับ `fines.loan_item_id`, `reservations.user_id` และ `reservations.book_id`.
- V8 มี preflight ตรวจ orphan records; V9 เพิ่ม `reservations.reserved_copy_id`, CHECK constraint, partial unique index และดัชนี expiry เพื่อกันตัวเล่มจริงให้คิว READY.
- V9 เปลี่ยนคิว READY เก่าที่ไม่มีตัวเล่มกลับเป็น WAITING; V8/V9 ยังไม่ได้ apply กับ Neon ในการตรวจครั้งนี้.
- คิว READY กันตัวเล่มไว้ 48 ชั่วโมง; เจ้าของคิวเท่านั้นยืมได้ และยกเลิก/หมดเวลาจะคืนตัวเล่มให้คิวถัดไป. การแจ้งอีเมล/SMS ยังเป็น log เท่านั้น.
- ก่อน deploy V8/V9 ให้ตรวจ migration history และข้อมูลบน staging/สำเนาฐานข้อมูลก่อน; ห้ามแก้ migration ที่ apply ไปแล้ว.

## Deployment evidence and limits

URL สาธารณะที่ทดสอบตอบสนองตามผลข้างต้น แต่ response status ไม่เปิดเผย Git branch ที่ Render deploy
หรือ branch ของ Neon ที่เลือกจาก `DB_URL`. ภาพ Neon ที่ผู้ใช้ส่งแสดง branch `production`
และ database `libraflow`; ภาพนี้ยังไม่ยืนยันว่า Render เชื่อมต่อ branch ดังกล่าว.

Render Dashboard ต้องตรวจ branch ของ Production service (ควรเป็น `main`), Root Directory,
และชื่อตัวแปรที่ตั้งไว้. เทียบ host และ database ใน `DB_URL` กับ Neon connection details โดยปิด
username/password ก่อนแชร์หลักฐาน. ห้ามส่ง connection string เต็ม, password หรือ `JWT_SECRET`.

## Commands for a complete local run

เปิด Docker Desktop/Docker Engine ก่อน:

```powershell
cd code/backend
.\mvnw.cmd clean verify

cd ..\frontend
npm ci
npm run lint
npm run build
npm audit --audit-level=high
```

GitHub Actions workflow คือ `.github/workflows/backend-ci.yml`; push ทุก branch และ pull request
เข้า `develop`/`main` รัน backend verification และ frontend checks.

## Submission ownership

ผู้ใช้ยืนยันว่าใบงานห้ามจ้างคนนอกกลุ่มทำงาน แต่อนุญาตให้ใช้ AI ช่วย debug, coding และเอกสารได้.
การแก้ไขและเอกสารในรอบนี้ทำตามคำขอผู้ใช้; ไม่มีการสร้าง commit หรือ push.
สมาชิกควรตรวจทานและอธิบายการเปลี่ยนแปลงได้ และทำ commit/push ด้วยบัญชีของตนตามกติกากลุ่ม.
