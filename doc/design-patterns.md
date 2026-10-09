# Design Patterns — LibraFlow

เอกสารนี้อธิบาย Pattern ที่ปรากฏใน code ปัจจุบันและตำแหน่ง implementation
Class Diagram อยู่ที่ [`diagrams/04-class-diagram.puml`](diagrams/04-class-diagram.puml)

## Architectural patterns

| Pattern | การใช้งาน |
|---|---|
| Layered Architecture | `controller/api` เรียก `service`; service ใช้ `repository`; repository จัดการ persistence ผ่าน JPA |
| MVC | REST controllers เป็น presentation; entities และ DTOs แทน model/API contract; React SPA เป็น client UI |
| Repository | `repository/*Repository.java` สืบทอด Spring Data JPA |
| Service Layer | `service/*` และ `service/impl/*` รวม business logic |
| DTO + Mapper | `dto/request`, `dto/response`, `mapper/BookMapper.java`, `mapper/LoanMapper.java`, `mapper/MemberMapper.java` |
| Dependency Injection | Spring constructor injection ใน controllers และ services |

## Behavioral patterns

| Pattern | ปัญหาที่แก้ | Implementation |
|---|---|---|
| Chain of Responsibility | ตรวจเงื่อนไขยืมทีละข้อและหยุดเมื่อไม่ผ่าน ทั้งการยืมด้วยตนเองและที่เคาน์เตอร์ | `pattern/chain/BorrowRule.java`; `MemberStatusRule`, `UnpaidFineRule`, `LoanQuotaRule`, `CopyAvailabilityRule`; `LoanServiceImpl` เรียงตาม `order()` |
| Strategy | คำนวณค่าปรับตามประเภทสมาชิก โดยแยกอัตราออกจาก service | `service/strategy/FineCalculationStrategy.java`; `StudentFineStrategy`, `StaffFineStrategy`, `ExternalFineStrategy`; `FineServiceImpl` เลือกตาม `MemberTier` |
| State | จำกัดการคืนและต่ออายุตามสถานะใบยืม | `pattern/state/LoanState.java`; `ActiveState`, `OverdueState`, `ReturnedState`, `LostState`; `LoanStateFactory` |
| Observer | กันตัวเล่มให้ผู้จองคิวแรกเมื่อมีตัวเล่มพร้อม | `LoanServiceImpl` ส่ง `BookReturnedEvent` (เป็น `BookCopyAvailableEvent`); `ReservationNotificationListener` รับหลัง commit แล้วผูก copy กับคิวแรก เปลี่ยนสถานะเป็น READY/RESERVED |
| Template Method | ใช้ขั้นตอนรายงานร่วมกัน แต่ render เป็น CSV หรือ PDF | `pattern/template/AbstractReportGenerator`; `CsvReportGenerator`; `PdfReportGenerator`; `ReportController` เลือก generator ตาม format |

### อัตราค่าปรับที่ใช้งาน

- STUDENT: 3 บาทต่อวัน
- STAFF: 5 บาทต่อวัน สูงสุด 300 บาท
- EXTERNAL: 10 บาทต่อวัน

`LoanServiceImpl.returnBook` เรียก FineService เมื่อคืนช้ากว่าวันครบกำหนด
และส่งประเภทสมาชิกจาก `users.member_tier`; ADMIN/LIBRARIAN ใช้นโยบาย STAFF
ผ่าน `LoanPolicyUtil.resolveMemberTier`.

### Observer transaction

`ReservationNotificationListener` ฟัง event หลัง commit และเริ่ม transaction ใหม่
เพื่อผูก copy กับคิว READY และเปลี่ยนสถานะ copy เป็น RESERVED ผู้ยืมคนอื่นใช้ตัวเล่มนั้นไม่ได้
เมื่อเจ้าของคิวยืม สถานะจะเปลี่ยนเป็น FULFILLED; เมื่อยกเลิกหรือหมดเวลา
`ReservationScheduler` ปล่อย copy แล้วส่งต่อให้คิว WAITING ถัดไป
การส่งอีเมล/SMS ยังเป็น TODO และระบบปัจจุบันเขียน log แทน

### Template Method ที่ไม่ได้ใช้โดย API

ใน `service/report/ReportGenerator.java` ยังมี implementation ตัวอย่างอีกชุด
(`FineReportGenerator`, `ReservationReportGenerator`) ซึ่ง `ReportController` ไม่ได้เรียก
เส้นทางที่ API ใช้จริงคือ `pattern/template/AbstractReportGenerator` กับ CSV/PDF
จึงไม่ควรอ้างคลาสตัวอย่างชุดแรกว่าเป็นตัวสร้างรายงาน production

## ข้อจำกัดที่ยังต้องตรวจ

- BR-08 เรียกเก็บราคาหนังสือเต็มจำนวนเมื่อเปลี่ยนเป็น LOST ผ่าน
  `LoanScheduler` → `FineService.generateLostBookFine`; หากไม่พบราคา หรือราคาเป็นค่าติดลบ
  งานจะ rollback และต้องแก้ข้อมูลหนังสือก่อนให้ scheduler ประมวลผลสำเร็จ
- V8 เติม foreign keys ที่ V5 ขาด แต่ต้องตรวจข้อมูลจริงใน Neon ก่อนรัน migration
- อัตราและ business rules ในเอกสารต้องตรงกับ `doc/project-overview.md` และใบงาน
