# Data Dictionary — LibraFlow

ฐานข้อมูล: **PostgreSQL 16** · Migration: **Flyway** (`V1__init_catalog.sql` … `V6__seed_data.sql`
— ดูตารางแบ่งไฟล์ตามผู้รับผิดชอบใน [`../README.md`](../README.md))
ER Diagram: [`diagrams/11-er-diagram.puml`](diagrams/11-er-diagram.puml)

---

## สรุปตารางทั้งหมด (12 ตาราง)

| # | ตาราง | คำอธิบาย |
|---|---|---|
| 1 | `users` | บัญชีผู้ใช้ระบบทุก Role |
| 2 | `user_profiles` | ข้อมูลส่วนตัวของผู้ใช้ (One-to-One กับ `users`) |
| 3 | `categories` | หมวดหมู่หนังสือ |
| 4 | `publishers` | สำนักพิมพ์ |
| 5 | `authors` | ผู้แต่ง |
| 6 | `books` | หนังสือระดับ Title |
| 7 | `book_authors` | ตารางเชื่อม Many-to-Many ระหว่าง `books` และ `authors` |
| 8 | `book_copies` | ตัวเล่มจริงบนชั้น (มีบาร์โค้ดของตัวเอง) |
| 9 | `loans` | ใบยืม |
| 10 | `loan_items` | รายการตัวเล่มในใบยืม |
| 11 | `fines` | ค่าปรับ (One-to-One กับ `loan_items`) |
| 12 | `reservations` | การจองคิวหนังสือ |

> รวม 12 ตาราง (เกินข้อกำหนดขั้นต่ำ 6 ตาราง)

---

## 1. users

| Column | Type | Null | Key | Default | คำอธิบาย |
|---|---|---|---|---|---|
| id | BIGSERIAL | N | PK | | รหัสผู้ใช้ |
| username | VARCHAR(50) | N | UK | | ชื่อสำหรับเข้าระบบ |
| email | VARCHAR(120) | N | UK, IDX | | อีเมล |
| password | VARCHAR(255) | N | | | รหัสผ่านเข้ารหัส BCrypt |
| role | VARCHAR(20) | N | IDX | 'MEMBER' | ADMIN / LIBRARIAN / MEMBER |
| status | VARCHAR(20) | N | | 'ACTIVE' | ACTIVE / SUSPENDED |
| created_at | TIMESTAMP | N | | now() | วันที่สร้าง (JPA Auditing) |
| updated_at | TIMESTAMP | Y | | | วันที่แก้ไขล่าสุด |

**Index:** `idx_users_email`, `idx_users_role`

---

## 2. user_profiles

| Column | Type | Null | Key | คำอธิบาย |
|---|---|---|---|---|
| id | BIGSERIAL | N | PK | |
| user_id | BIGINT | N | FK → users.id, **UK** | บังคับ One-to-One ด้วย UNIQUE |
| full_name | VARCHAR(120) | N | | ชื่อ-นามสกุล |
| phone | VARCHAR(20) | Y | | เบอร์โทรศัพท์ |
| address | VARCHAR(255) | Y | | ที่อยู่ |
| birth_date | DATE | Y | | วันเกิด |
| member_tier | VARCHAR(20) | N | IDX | STUDENT / STAFF / EXTERNAL |
| joined_at | DATE | N | | วันที่สมัครสมาชิก |

**JPA:** `@OneToOne(mappedBy = "user", cascade = ALL, orphanRemoval = true, fetch = LAZY)`
**เหตุผล:** โปรไฟล์เกิดและตายพร้อมบัญชีผู้ใช้ จึง cascade ได้อย่างปลอดภัย

---

## 3. categories

| Column | Type | Null | Key | คำอธิบาย |
|---|---|---|---|---|
| id | BIGSERIAL | N | PK | |
| name | VARCHAR(80) | N | UK | ชื่อหมวดหมู่ |
| description | VARCHAR(255) | Y | | คำอธิบาย |

---

## 4. publishers

| Column | Type | Null | Key | คำอธิบาย |
|---|---|---|---|---|
| id | BIGSERIAL | N | PK | |
| name | VARCHAR(120) | N | UK | ชื่อสำนักพิมพ์ |
| country | VARCHAR(60) | Y | | ประเทศ |

---

## 5. authors

| Column | Type | Null | Key | คำอธิบาย |
|---|---|---|---|---|
| id | BIGSERIAL | N | PK | |
| full_name | VARCHAR(120) | N | IDX | ชื่อผู้แต่ง |
| nationality | VARCHAR(60) | Y | | สัญชาติ |
| biography | TEXT | Y | | ประวัติโดยย่อ |

---

## 6. books

| Column | Type | Null | Key | คำอธิบาย |
|---|---|---|---|---|
| id | BIGSERIAL | N | PK | |
| isbn | VARCHAR(20) | N | UK | เลข ISBN |
| title | VARCHAR(200) | N | IDX | ชื่อหนังสือ |
| publish_year | INT | Y | | ปีที่พิมพ์ |
| price | NUMERIC(10,2) | Y | | ราคาปก (ใช้คิดค่าชดใช้กรณีหาย BR-08) |
| category_id | BIGINT | N | FK → categories.id, IDX | หมวดหมู่ |
| publisher_id | BIGINT | N | FK → publishers.id, IDX | สำนักพิมพ์ |
| created_at | TIMESTAMP | N | | |

**Index:** `idx_books_title` (รองรับการค้นหาแบบ LIKE), `idx_books_category_id`
**JPA:** `@ManyToOne(fetch = LAZY)` **ไม่ใส่ cascade** — ลบหนังสือต้องไม่ลบหมวดหมู่หรือสำนักพิมพ์

---

## 7. book_authors (Join Table — Many-to-Many)

| Column | Type | Null | Key | คำอธิบาย |
|---|---|---|---|---|
| book_id | BIGINT | N | PK, FK → books.id | |
| author_id | BIGINT | N | PK, FK → authors.id | |

**Composite Primary Key** `(book_id, author_id)` กันข้อมูลซ้ำในตัว
**JPA:** `@ManyToMany` + `@JoinTable` ฝั่ง owner คือ `Book`

---

## 8. book_copies

| Column | Type | Null | Key | คำอธิบาย |
|---|---|---|---|---|
| id | BIGSERIAL | N | PK | |
| barcode | VARCHAR(30) | N | UK | บาร์โค้ดติดสันหนังสือ |
| book_id | BIGINT | N | FK → books.id, IDX | หนังสือต้นเรื่อง |
| status | VARCHAR(20) | N | IDX | AVAILABLE / ON_LOAN / RESERVED / DAMAGED / LOST |
| shelf_location | VARCHAR(30) | Y | | ตำแหน่งชั้นวาง เช่น A3-02 |
| acquired_at | DATE | N | | วันที่รับเข้าคลัง |

**Index:** `idx_copies_book_status (book_id, status)` — ใช้ตอนหาตัวเล่มว่างของหนังสือเล่มหนึ่ง
**JPA:** `@OneToMany(mappedBy = "book", cascade = PERSIST/MERGE, fetch = LAZY)`
**เหตุผล:** ไม่ใช้ `REMOVE` เพราะห้ามลบหนังสือทิ้งตัวเล่มที่ยังถูกยืมอยู่ (BR-11)

---

## 9. loans

| Column | Type | Null | Key | คำอธิบาย |
|---|---|---|---|---|
| id | BIGSERIAL | N | PK | |
| loan_code | VARCHAR(20) | N | UK | รหัสใบยืม เช่น LN-20260912-0007 |
| user_id | BIGINT | N | FK → users.id, IDX | ผู้ยืม |
| librarian_id | BIGINT | Y | FK → users.id | บรรณารักษ์ผู้บันทึก |
| loan_date | TIMESTAMP | N | | วันเวลาที่ยืม |
| status | VARCHAR(20) | N | IDX | ACTIVE / OVERDUE / RETURNED / LOST |

---

## 10. loan_items

| Column | Type | Null | Key | คำอธิบาย |
|---|---|---|---|---|
| id | BIGSERIAL | N | PK | |
| loan_id | BIGINT | N | FK → loans.id, IDX | ใบยืมต้นสังกัด |
| book_copy_id | BIGINT | N | FK → book_copies.id, IDX | ตัวเล่มที่ยืม |
| due_date | DATE | N | IDX | กำหนดคืน (คำนวณตาม BR-05) |
| returned_at | DATE | Y | | วันที่คืนจริง (NULL = ยังไม่คืน) |
| renew_count | SMALLINT | N | | จำนวนครั้งที่ต่ออายุ (≤ 2 ตาม BR-06) |

**JPA:** `@ManyToOne(fetch = LAZY)` ฝั่ง `Loan` ใช้
`@OneToMany(mappedBy = "loan", cascade = ALL, orphanRemoval = true)`
**เหตุผล:** `LoanItem` เป็น composition ของ `Loan` ไม่มีความหมายเมื่อไม่มีใบยืม

---

## 11. fines

| Column | Type | Null | Key | คำอธิบาย |
|---|---|---|---|---|
| id | BIGSERIAL | N | PK | |
| loan_item_id | BIGINT | N | FK → loan_items.id, **UK** | บังคับ One-to-One |
| amount | NUMERIC(10,2) | N | | ยอดค่าปรับ |
| overdue_days | INT | N | | จำนวนวันที่เกินกำหนด |
| status | VARCHAR(20) | N | IDX | UNPAID / PAID / WAIVED |
| created_at | TIMESTAMP | N | | วันที่เกิดค่าปรับ |
| paid_at | TIMESTAMP | Y | | วันที่ชำระ |

**หมายเหตุ:** ใช้ `NUMERIC` ไม่ใช้ `DOUBLE` เพราะเป็นข้อมูลเงิน ต้องไม่มีความคลาดเคลื่อนทศนิยม

---

## 12. reservations

| Column | Type | Null | Key | คำอธิบาย |
|---|---|---|---|---|
| id | BIGSERIAL | N | PK | |
| user_id | BIGINT | N | FK → users.id, IDX | ผู้จอง |
| book_id | BIGINT | N | FK → books.id, IDX | หนังสือที่จอง (ระดับ Title) |
| reserved_at | TIMESTAMP | N | | เวลาที่จอง (ใช้เรียงคิว) |
| expires_at | TIMESTAMP | Y | | หมดเวลารับ (48 ชม. หลังสถานะ READY) |
| status | VARCHAR(20) | N | IDX | WAITING / READY / FULFILLED / CANCELLED / EXPIRED |

**Unique Constraint:** `uk_reservation_active (user_id, book_id)` เมื่อ status IN ('WAITING','READY')
— บังคับกฎ BR-09 ที่ระดับฐานข้อมูล ไม่พึ่งแค่โค้ด

---

## สรุปความสัมพันธ์

| ประเภท | คู่ความสัมพันธ์ | วิธีบังคับ |
|---|---|---|
| One-to-One | `users` ↔ `user_profiles` | UNIQUE บน `user_profiles.user_id` |
| One-to-One | `loan_items` ↔ `fines` | UNIQUE บน `fines.loan_item_id` |
| One-to-Many | `categories` → `books` | FK `books.category_id` |
| One-to-Many | `publishers` → `books` | FK `books.publisher_id` |
| One-to-Many | `books` → `book_copies` | FK `book_copies.book_id` |
| One-to-Many | `users` → `loans` | FK `loans.user_id` |
| One-to-Many | `loans` → `loan_items` | FK `loan_items.loan_id` |
| One-to-Many | `users` → `reservations` | FK `reservations.user_id` |
| One-to-Many | `books` → `reservations` | FK `reservations.book_id` |
| Many-to-Many | `books` ↔ `authors` | Join Table `book_authors` |
