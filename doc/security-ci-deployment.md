# LibraFlow — Security, CI/CD and Deployment Documentation

## 1. Overview

เอกสารนี้อธิบายส่วนงาน Security, Docker, Automated Testing, CI/CD และ Deployment
ของระบบ LibraFlow ซึ่งเป็นระบบจัดการหนังสือในห้องสมุดที่พัฒนาด้วย Spring Boot,
React และ PostgreSQL

ส่วนงานนี้รับผิดชอบโดยสมาชิกคนที่ 5 และครอบคลุมหัวข้อหลักดังนี้

- Authentication และ Authorization ด้วย JWT
- Role-based access control
- Password hashing ด้วย BCrypt
- Docker และ Docker Compose
- Unit Test
- Integration Test ด้วย Testcontainers
- GitHub Actions CI
- PostgreSQL Production Database บน Neon
- Backend Deployment บน Render

---

## 2. Authentication and Authorization

ระบบใช้ Spring Security ร่วมกับ JSON Web Token (JWT)

เมื่อผู้ใช้เข้าสู่ระบบผ่าน

```text
POST /api/v1/auth/login