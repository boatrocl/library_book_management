# Data Dictionary — LibraFlow

อ้างอิง schema จาก Flyway migrations ใน `code/backend/src/main/resources/db/migration/`
ณ เวอร์ชัน V10. เอกสารนี้อธิบาย schema ที่โค้ดจะสร้างเมื่อรัน migration ครบ
การ deploy จริงต้องตรวจสอบผล Flyway บนฐานข้อมูลเป้าหมายอีกครั้ง

สัญลักษณ์: `PK` primary key, `FK` foreign key, `UK` unique, `IDX` index,
`NN` not null. ถ้าไม่ได้ระบุ `NN` คอลัมน์นั้นอนุญาต `NULL` ตาม migration

## Migration history

| Version | ไฟล์ | ผลต่อ schema |
|---|---|---|
| V1 | `V1__init_catalog.sql` | ตาราง catalog 6 ตาราง |
| V2 | `V2__seed_catalog.sql` | seed ข้อมูล catalog ไม่มีการเปลี่ยน schema |
| V3 | `V3__init_users.sql` | `users`, `user_profiles` และ seed admin เดิม |
| V3.1 | `V3_1__add_role_and_auth_seed.sql` | เพิ่ม `users.role`, role check และบัญชี seed ที่ใช้ BCrypt |
| V4 | `V4__init_loan.sql` | `loans`, `loan_items` |
| V5 | `V5__init_fine_reservation.sql` | `fines`, `reservations`; ยังไม่มี FK ของสองตารางนี้ |
| V6 | ไม่มีไฟล์ | เวอร์ชันนี้ขาดอยู่ใน repository ปัจจุบัน ไม่ควรสร้างย้อนหลังหลัง V7–V10 |
| V7 | `V7__add_tier_to_users.sql` | เพิ่ม `users.member_tier` โดย default `STUDENT` |
| V8 | `V8__add_fine_reservation_foreign_keys.sql` | เพิ่ม FK จาก fines และ reservations พร้อมตรวจข้อมูลกำพร้าก่อน |
| V9 | `V9__reserve_book_copy_for_ready_reservations.sql` | เพิ่ม `reservations.reserved_copy_id`; คิว READY เก่าที่ไม่มีตัวเล่มจะกลับเป็น WAITING |
| V10 | `V10__tighten_user_indexes_and_fine_status.sql` | ลบ index username/email ที่ซ้ำกับ unique constraints และเพิ่ม CHECK ของ `fines.status` |

## Tables

### 1. `users`

| Column | Type | Nullability | Key / default | Meaning |
|---|---|---|---|---|
| `id` | BIGSERIAL | NN | PK | รหัสผู้ใช้ |
| `username` | VARCHAR(50) | NN | UK | ชื่อเข้าใช้งาน |
| `password_hash` | VARCHAR(255) | NN | | รหัสผ่านที่ผ่าน BCrypt |
| `email` | VARCHAR(100) | NN | UK | อีเมล |
| `is_active` | BOOLEAN | nullable | DEFAULT TRUE | สถานะเปิดใช้งาน |
| `created_at` | TIMESTAMP | nullable | DEFAULT CURRENT_TIMESTAMP | วันเวลาสร้างบัญชี |
| `role` | VARCHAR(20) | NN | DEFAULT `MEMBER`; CHECK `ADMIN`, `LIBRARIAN`, `MEMBER` | สิทธิ์ในระบบ |
| `member_tier` | VARCHAR(20) | nullable | DEFAULT `STUDENT` | ประเภทสมาชิก: `STUDENT`, `STAFF`, `EXTERNAL` |

V3 เคยสร้าง index `idx_users_username` และ `idx_users_email` เพิ่มจาก unique indexes ที่ฐานข้อมูลสร้างให้อัตโนมัติ; V10 ลบ index ที่ซ้ำ โดย unique constraints ยังคงบังคับความไม่ซ้ำและมี index รองรับ

### 2. `user_profiles`

| Column | Type | Nullability | Key / default | Meaning |
|---|---|---|---|---|
| `user_id` | BIGINT | NN | PK, FK → `users.id` ON DELETE CASCADE | ใช้ร่วมเป็น PK เพื่อบังคับ One-to-One |
| `first_name` | VARCHAR(100) | NN | | ชื่อ |
| `last_name` | VARCHAR(100) | NN | | นามสกุล |
| `phone_number` | VARCHAR(20) | nullable | | เบอร์โทรศัพท์ |
| `address` | TEXT | nullable | | ที่อยู่ |

### 3. `categories`

| Column | Type | Nullability | Key / default | Meaning |
|---|---|---|---|---|
| `id` | BIGSERIAL | NN | PK | รหัสหมวดหมู่ |
| `name` | VARCHAR(80) | NN | UK | ชื่อหมวดหมู่ |
| `description` | VARCHAR(255) | nullable | | คำอธิบาย |

### 4. `publishers`

| Column | Type | Nullability | Key / default | Meaning |
|---|---|---|---|---|
| `id` | BIGSERIAL | NN | PK | รหัสสำนักพิมพ์ |
| `name` | VARCHAR(120) | NN | UK | ชื่อสำนักพิมพ์ |
| `country` | VARCHAR(60) | nullable | | ประเทศ |

### 5. `authors`

| Column | Type | Nullability | Key / default | Meaning |
|---|---|---|---|---|
| `id` | BIGSERIAL | NN | PK | รหัสผู้แต่ง |
| `full_name` | VARCHAR(120) | NN | IDX `idx_authors_full_name` | ชื่อผู้แต่ง; อนุญาตชื่อซ้ำ |
| `nationality` | VARCHAR(60) | nullable | | สัญชาติ |
| `biography` | TEXT | nullable | | ประวัติ |

### 6. `books`

| Column | Type | Nullability | Key / default | Meaning |
|---|---|---|---|---|
| `id` | BIGSERIAL | NN | PK | รหัสชื่อหนังสือ |
| `isbn` | VARCHAR(20) | NN | UK | ISBN |
| `title` | VARCHAR(200) | NN | IDX `idx_books_title` | ชื่อหนังสือ |
| `publish_year` | INT | nullable | | ปีพิมพ์ |
| `price` | NUMERIC(10,2) | nullable | | ราคาหนังสือ |
| `category_id` | BIGINT | NN | FK → `categories.id`, IDX | หมวดหมู่ |
| `publisher_id` | BIGINT | NN | FK → `publishers.id`, IDX | สำนักพิมพ์ |
| `created_at` | TIMESTAMP | NN | DEFAULT `now()` | วันเวลาสร้างรายการ |

### 7. `book_authors`

| Column | Type | Nullability | Key / default | Meaning |
|---|---|---|---|---|
| `book_id` | BIGINT | NN | ส่วนของ composite PK, FK → `books.id` ON DELETE CASCADE | หนังสือ |
| `author_id` | BIGINT | NN | ส่วนของ composite PK, FK → `authors.id` | ผู้แต่ง |

Composite PK คือ (`book_id`, `author_id`); มี index `idx_book_authors_author_id` สำหรับค้นย้อนจากผู้แต่ง

### 8. `book_copies`

| Column | Type | Nullability | Key / default | Meaning |
|---|---|---|---|---|
| `id` | BIGSERIAL | NN | PK | รหัสตัวเล่ม |
| `barcode` | VARCHAR(30) | NN | UK | บาร์โค้ดประจำตัวเล่ม |
| `book_id` | BIGINT | NN | FK → `books.id` | ชื่อหนังสือต้นทาง |
| `status` | VARCHAR(20) | NN | CHECK `AVAILABLE`, `ON_LOAN`, `RESERVED`, `DAMAGED`, `LOST` | สถานะตัวเล่ม |
| `shelf_location` | VARCHAR(30) | nullable | | ตำแหน่งชั้น |
| `acquired_at` | DATE | NN | | วันที่รับเข้า |

Index: `idx_copies_book_status` (`book_id`, `status`)

### 9. `loans`

| Column | Type | Nullability | Key / default | Meaning |
|---|---|---|---|---|
| `id` | BIGSERIAL | NN | PK | รหัสใบยืม |
| `loan_code` | VARCHAR(20) | NN | UK | รหัสใบยืมที่แสดงผู้ใช้ |
| `user_id` | BIGINT | NN | FK → `users.id`; IDX `idx_loans_user_id` | สมาชิกผู้ยืม |
| `librarian_id` | BIGINT | nullable | FK → `users.id` | ผู้บันทึกการยืม |
| `loan_date` | TIMESTAMP | NN | DEFAULT CURRENT_TIMESTAMP | วันเวลายืม |
| `status` | VARCHAR(20) | NN | CHECK `ACTIVE`, `OVERDUE`, `RETURNED`, `LOST`; IDX `idx_loans_status` | สถานะใบยืม |

### 10. `loan_items`

| Column | Type | Nullability | Key / default | Meaning |
|---|---|---|---|---|
| `id` | BIGSERIAL | NN | PK | รหัสรายการ |
| `loan_id` | BIGINT | NN | FK → `loans.id` ON DELETE CASCADE; IDX | ใบยืม |
| `book_copy_id` | BIGINT | NN | FK → `book_copies.id`; IDX | ตัวเล่มที่ยืม |
| `due_date` | DATE | NN | IDX `idx_loan_items_due_date` | กำหนดคืน |
| `returned_at` | DATE | nullable | | วันที่คืนจริง |
| `renew_count` | SMALLINT | NN | DEFAULT 0; CHECK 0 ถึง 2 | จำนวนการต่ออายุ |

### 11. `fines`

| Column | Type | Nullability | Key / default | Meaning |
|---|---|---|---|---|
| `id` | BIGSERIAL | NN | PK | รหัสค่าปรับ |
| `loan_item_id` | BIGINT | NN | UK; V8 FK → `loan_items.id` | รายการยืมที่เกิดค่าปรับ; จำกัดหนึ่งค่าปรับต่อรายการ |
| `amount` | NUMERIC(10,2) | NN | | จำนวนเงิน |
| `overdue_days` | INT | NN | | จำนวนวันที่เกินกำหนด |
| `status` | VARCHAR(20) | NN | V10 CHECK `UNPAID`, `PAID`, `WAIVED` | สถานะค่าปรับตาม enum ในแอป |
| `created_at` | TIMESTAMP | NN | DEFAULT CURRENT_TIMESTAMP | เวลาสร้างค่าปรับ |
| `paid_at` | TIMESTAMP | nullable | | เวลาชำระหรือยกเว้น |

### 12. `reservations`

| Column | Type | Nullability | Key / default | Meaning |
|---|---|---|---|---|
| `id` | BIGSERIAL | NN | PK | รหัสการจอง |
| `user_id` | BIGINT | NN | IDX `idx_reservations_user_id`; V8 FK → `users.id` | สมาชิกผู้จอง |
| `book_id` | BIGINT | NN | IDX `idx_reservations_book_id`; V8 FK → `books.id` | หนังสือที่จอง |
| `reserved_copy_id` | BIGINT | nullable | IDX; V9 FK → `book_copies.id` | ตัวเล่มที่กันไว้เมื่อสถานะ READY |
| `reserved_at` | TIMESTAMP | NN | DEFAULT CURRENT_TIMESTAMP | เวลาจอง |
| `expires_at` | TIMESTAMP | nullable | | เวลาหมดอายุการรับหนังสือ |
| `status` | VARCHAR(20) | NN | V9 CHECK: READY ต้องมี `reserved_copy_id` และ `expires_at` | สถานะการจอง |

Partial unique index `uk_reservation_active` จำกัดคู่ (`user_id`, `book_id`) ให้มีสถานะ `WAITING` หรือ `READY` ได้เพียงรายการเดียว
`uk_reservations_ready_copy` กันไม่ให้ตัวเล่มเดียวถูกมอบให้คิว READY มากกว่าหนึ่งรายการพร้อมกัน
และ `idx_reservations_ready_expiry` ช่วยค้นคิวที่เลยเวลารับหนังสือ

## Relationships

| Relationship | Database enforcement |
|---|---|
| `users` 1:1 `user_profiles` | `user_profiles.user_id` เป็น PK และ FK |
| `loan_items` 1:0..1 `fines` | `fines.loan_item_id` เป็น UK และ FK ใน V8 |
| `categories` 1:N `books` | `books.category_id` FK |
| `publishers` 1:N `books` | `books.publisher_id` FK |
| `books` 1:N `book_copies` | `book_copies.book_id` FK |
| `users` 1:N `loans` | `loans.user_id` FK; `librarian_id` เป็น FK เพิ่มอีกทาง |
| `loans` 1:N `loan_items` | `loan_items.loan_id` FK |
| `book_copies` 1:N `loan_items` | `loan_items.book_copy_id` FK |
| `users` 1:N `reservations` | `reservations.user_id` FK ใน V8 |
| `books` 1:N `reservations` | `reservations.book_id` FK ใน V8 |
| `book_copies` 1:N `reservations` | `reservations.reserved_copy_id` FK ใน V9; มีได้ไม่เกินหนึ่งรายการ READY ต่อตัวเล่ม |
| `books` M:N `authors` | `book_authors` เป็น join table |

## Notes for maintainers

- ห้ามแก้ migration ที่ merge หรือรันไปแล้ว; เพิ่ม migration ใหม่แทน
- V8 จะหยุดพร้อมข้อความอธิบายถ้าพบ orphan rows ก่อนเพิ่ม FK ต้องสำรองและตรวจข้อมูลจริงก่อน deploy
- V5 ไม่มี FK ของ fines/reservations; V8 เพิ่ม FK หลัก และ V9 เพิ่ม FK ของตัวเล่มที่กันให้คิว
- V9 เปลี่ยน READY เก่าที่ไม่มีตัวเล่มกลับเป็น WAITING เพื่อป้องกันการยืมตัวเล่มที่ไม่ได้กันจริง
- V10 ลบ index ที่ซ้ำกับ unique constraints ของ `users.username` และ `users.email`; เพิ่ม/validate CHECK ของ `fines.status` หลังตรวจค่าที่มีอยู่
- การมี migration ใน Git ไม่ยืนยันว่า Render/Neon ได้รัน migration นั้นแล้ว
