# LibraFlow — ภาพรวมระบบ (Project Overview)

## 1. ที่มาและขอบเขต

ห้องสมุดขนาดกลางประสบปัญหาการจัดการการยืม-คืนด้วยสมุดบันทึกกระดาษ
ทำให้ตรวจสอบไม่ได้ว่าหนังสือเล่มใดอยู่ที่ใคร คำนวณค่าปรับผิดพลาดบ่อย
และไม่มีระบบจองคิวสำหรับหนังสือยอดนิยม

**LibraFlow** เป็นระบบสารสนเทศบนเว็บที่แก้ปัญหาข้างต้น โดยครอบคลุม
การจัดการข้อมูลหนังสือระดับ Title และระดับตัวเล่ม (Copy) การยืม-คืน
การจองคิว การคิดค่าปรับอัตโนมัติตามประเภทสมาชิก และรายงานสำหรับผู้บริหาร

### อยู่ในขอบเขต (In Scope)
- จัดการหนังสือ ผู้แต่ง สำนักพิมพ์ หมวดหมู่ และตัวเล่ม (barcode ระดับเล่ม)
- สมัคร/จัดการสมาชิก พร้อมโปรไฟล์และประเภทสมาชิก (Tier)
- ยืม-คืน-ต่ออายุ พร้อมตรวจสอบสิทธิ์การยืมหลายเงื่อนไข
- จองคิวหนังสือ และตั้งสถานะ READY เมื่อหนังสือพร้อมให้รับ
- คิดค่าปรับอัตโนมัติและบันทึกการชำระ
- รายงานสถิติการยืม / หนังสือค้างส่ง (CSV, PDF)
- Authentication & Authorization แบ่งตาม Role

### นอกขอบเขต (Out of Scope)
- ระบบชำระเงินออนไลน์จริง (ใช้การบันทึกรับชำระที่เคาน์เตอร์)
- E-book / การอ่านออนไลน์
- ระบบจัดซื้อจัดจ้างหนังสือ

---

## 2. Actor และสิทธิ์การใช้งาน

| Actor | สิทธิ์ |
|---|---|
| **MEMBER** | ค้นหาหนังสือ, ดูรายละเอียด, ยืมตัวเล่มที่ว่างด้วยตนเอง, จองคิว, ยกเลิกการจอง, ดูประวัติการยืมและค่าปรับของตนเอง |
| **LIBRARIAN** | สิทธิ์ทั้งหมดของ MEMBER + จัดการหนังสือ/ตัวเล่ม, บันทึกการยืม, บันทึกการคืน, ต่ออายุ, รับชำระค่าปรับ, ออกรายงาน |
| **ADMIN** | สิทธิ์ทั้งหมดของ LIBRARIAN + จัดการผู้ใช้และสิทธิ์, ระงับ/คืนสถานะบัญชี, ตั้งค่านโยบายการยืม, ดู Dashboard |

---

## 3. Business Rules (กฎทางธุรกิจ)

รหัสกฎเหล่านี้จะถูกอ้างอิงในโค้ดและในเอกสาร Design Patterns

| รหัส | กฎ |
|---|---|
| BR-01 | สมาชิกที่มีสถานะ `SUSPENDED` ยืมหนังสือไม่ได้ |
| BR-02 | สมาชิกที่มีค่าปรับค้างชำระรวมเกิน 100 บาท ยืมหนังสือไม่ได้ |
| BR-03 | โควต้าการยืมพร้อมกันขึ้นกับ Tier — STUDENT 5 เล่ม, STAFF 10 เล่ม, EXTERNAL 2 เล่ม |
| BR-04 | ยืมได้เฉพาะตัวเล่ม `AVAILABLE` หรือ `RESERVED` ที่ผูกกับคิว `READY` ของสมาชิกผู้ยืมและยังไม่หมดเวลา |
| BR-05 | ระยะเวลายืมขึ้นกับ Tier — STUDENT 7 วัน, STAFF 14 วัน, EXTERNAL 3 วัน |
| BR-06 | ต่ออายุได้ไม่เกิน 2 ครั้งต่อรายการ และต่ออายุไม่ได้หากมีคนจองคิวรออยู่ |
| BR-07 | ค่าปรับคิดต่อวันตาม Tier — STUDENT 3 บาท/วัน, STAFF 5 บาท/วัน (เพดาน 300 บาท), EXTERNAL 10 บาท/วัน |
| BR-08 | รายการยืมที่เกินกำหนดคืนเกิน 60 วัน จะเปลี่ยนสถานะเป็น `LOST` และเรียกเก็บค่าหนังสือเต็มราคา |
| BR-09 | หนังสือหนึ่งเล่ม สมาชิกหนึ่งคนจองซ้ำซ้อนไม่ได้ (ตอบ 409 Conflict) |
| BR-10 | เมื่อมีการคืนหนังสือ ระบบต้องแจ้งสมาชิกที่จองคิวลำดับแรกโดยอัตโนมัติ และกันตัวเล่มไว้ 48 ชั่วโมง |
| BR-11 | ลบหนังสือไม่ได้หากยังมีตัวเล่มที่อยู่ในสถานะ `ON_LOAN` หรือ `RESERVED` |
| BR-12 | สมาชิกต้องยอมรับกฎการใช้บริการก่อนยืมหรือจองด้วยตนเอง ระบบปฏิเสธคำขอที่ไม่มีการยอมรับ |

### สถานะการทำงานของกฎที่พึ่งพาระบบภายนอก

- **BR-10:** หลัง transaction คืนหนังสือสำเร็จ listener จะผูกตัวเล่มกับคิวแรก เปลี่ยน reservation เป็น `READY` และ copy เป็น `RESERVED` นาน 48 ชั่วโมง เจ้าของคิวจึงยืมตัวเล่มนั้นได้; การยกเลิก/หมดเวลาจะคืน copy และส่งต่อคิว ระบบยังเขียน log แทนการเชื่อมผู้ให้บริการอีเมลหรือ SMS
- **BR-08:** งานตามกำหนดเวลาจะเปลี่ยนรายการที่เกินกำหนดมากกว่า 60 วันเป็น `LOST` และสร้างค่าปรับตามราคาหนังสือ หากข้อมูลราคาไม่ถูกต้อง transaction จะยกเลิกแทนการบันทึกค่าปรับที่ผิด

---

## 4. Domain Glossary (คำศัพท์เฉพาะ)

| คำ | ความหมาย |
|---|---|
| **Book** | หนังสือระดับ Title (มี ISBN เดียว) เช่น "Clean Code" |
| **BookCopy** | ตัวเล่มจริงบนชั้น มีบาร์โค้ดของตัวเอง — หนึ่ง Book มีได้หลาย Copy |
| **Loan** | ใบยืมหนึ่งใบ (หนึ่งครั้งที่มายืม) ของสมาชิกหนึ่งคน |
| **LoanItem** | รายการย่อยในใบยืม = ตัวเล่มหนึ่งเล่ม พร้อมกำหนดคืนของตัวเอง |
| **Fine** | ค่าปรับของ LoanItem ที่คืนช้า หรือค่าทดแทนหนังสือที่ถูกจัดเป็น `LOST` (One-to-One กับ LoanItem) |
| **Reservation** | การจองคิวหนังสือระดับ Title (ไม่เจาะจงตัวเล่ม) |
| **Member Tier** | ประเภทสมาชิก STUDENT / STAFF / EXTERNAL มีผลต่อโควต้า ระยะเวลายืม และอัตราค่าปรับ |

---

## 5. Enum ทั้งหมดในระบบ

| Enum | ค่า |
|---|---|
| `Role` | ADMIN, LIBRARIAN, MEMBER |
| `UserStatus` | ACTIVE, SUSPENDED |
| `MemberTier` | STUDENT, STAFF, EXTERNAL |
| `BookCopyStatus` | AVAILABLE, ON_LOAN, RESERVED, DAMAGED, LOST |
| `LoanStatus` | ACTIVE, OVERDUE, RETURNED, LOST |
| `ReservationStatus` | WAITING, READY, FULFILLED, CANCELLED, EXPIRED |
| `FineStatus` | UNPAID, PAID, WAIVED |

---

## 6. Package Structure

```
com.libraflow.library
├── common/              API response, date/barcode helpers, loan policy
├── config/              OpenAPI, JPA auditing, CORS
├── controller/api/      REST controllers: Auth, Book, Loan, Fine, Member,
│                        PublicCatalog, Report, Reservation, UserManagement
├── domain/
│   ├── entity/          User, UserProfile, Book, BookCopy, Author, Category,
│   │                    Publisher, Loan, LoanItem, Fine, Reservation
│   └── enums/           Role, UserStatus, MemberTier, BookCopyStatus,
│                        LoanStatus, ReservationStatus, FineStatus
├── dto/
│   ├── request/         CreateBookRequest, UpdateBookRequest, BorrowRequest,
│   │                    MemberBorrowRequest, MemberReservationRequest,
│   │                    ReturnRequest, CreateReservationRequest, PayFineRequest
│   └── response/        BookResponse, BookCopyResponse, LoanResponse,
│                        FineResponse, PageResponse<T>, ErrorResponse
├── mapper/              BookMapper, LoanMapper, MemberMapper, FineMapper
├── pattern/
│   ├── state/           LoanState, ActiveState, OverdueState, ReturnedState,
│   │                    LostState, LoanStateFactory
│   ├── chain/           BorrowRule, MemberStatusRule, UnpaidFineRule,
│   │                    LoanQuotaRule, CopyAvailabilityRule, BorrowContext
│   ├── observer/        BookCopyAvailableEvent, BookReturnedEvent
│   └── template/        AbstractReportGenerator, CsvReportGenerator,
│                        PdfReportGenerator
├── repository/          Spring Data repositories and projections
├── security/            SecurityConfig, JWT authentication and authorization
└── service/              Service interfaces, LoanScheduler, ReservationScheduler
    ├── impl/             Service implementations
    ├── strategy/         FineCalculationStrategy and 3 MemberTier strategies
    ├── report/           ReportGenerator and fine/reservation reports
    └── event/listener/   ReservationNotificationListener
```

---

## 7. รายการเอกสารประกอบ

| ไฟล์ | เนื้อหา |
|---|---|
| `README.md` | ภาพรวม การติดตั้ง การรัน การ deploy |
| `doc/project-overview.md` | เอกสารนี้ — ขอบเขต, Actor, Business Rules |
| `doc/solid-analysis.md` | การวิเคราะห์ SOLID อ้างอิงคลาสและแพ็กเกจในโค้ดปัจจุบัน |
| `doc/design-patterns.md` | ตาราง Design Pattern พร้อมเหตุผลการเลือกใช้ |
| `doc/data-dictionary.md` | พจนานุกรมข้อมูลครบทุกตาราง |
| `doc/api-spec.md` | รายละเอียด REST API ทุก Endpoint |
| `doc/diagrams/*.puml` | Diagram ทั้งหมดในรูปแบบ PlantUML |
