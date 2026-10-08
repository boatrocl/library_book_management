# LibraFlow Test Report — Security, Authentication and Deployment

## 1. Test Objective

วัตถุประสงค์ของการทดสอบคือยืนยันว่า Security, Authentication,
Authorization, Database Migration และ Deployment ของระบบ LibraFlow
สามารถทำงานร่วมกันได้อย่างถูกต้อง

ส่วนที่ทดสอบประกอบด้วย:

- JWT Authentication
- BCrypt Password Verification
- Role-based Authorization
- Spring Security Filter
- Flyway Migration
- PostgreSQL Integration
- REST API Security
- Docker
- CI Pipeline
- Production Deployment

---

## 2. Test Environment

| Item | Environment |
|---|---|
| Backend | Spring Boot 4.1.1 |
| Java Target | Java 17 |
| Database | PostgreSQL 16 |
| Unit Test | JUnit 5 + Mockito |
| Integration Test | Spring Boot Test |
| Database Test | Testcontainers |
| Container | Docker |
| CI | GitHub Actions |
| Production Database | Neon PostgreSQL |
| Production Backend | Render |

---

## 3. Unit Test

### JwtServiceTest

ทดสอบการทำงานของ JWT service

Test cases:

1. สร้าง JWT และอ่าน username/role ได้
2. Reject Base64 secret ที่ไม่ถูกต้อง
3. Reject secret ที่สั้นกว่า 256 bits
4. Reject expiration ที่มีค่าน้อยกว่าหรือเท่ากับศูนย์

Result:

```text
Tests run: 4
Failures: 0
Errors: 0
Skipped: 0