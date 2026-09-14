# Design Patterns — LibraFlow

เอกสารนี้ระบุ Pattern ทุกตัวที่ใช้ในระบบ พร้อม **ปัญหาที่แก้** และ **ไฟล์/คลาสที่ใช้**
ทุก Pattern ที่ระบุไว้ถูกเลือกเพราะแก้ปัญหาจริงในระบบ ไม่ได้ใส่เพื่อให้ครบจำนวน

Class Diagram ที่แสดงตำแหน่งของ Pattern: [`diagrams/04-class-diagram.puml`](diagrams/04-class-diagram.puml)

---

## 1. Enterprise / Architectural Patterns (บังคับทุกกลุ่ม)

| Pattern | ปัญหาที่แก้ | ไฟล์ / คลาสที่ใช้ |
|---|---|---|
| **Layered Architecture** | แยกความรับผิดชอบเป็นชั้น ทำให้แก้ไขและทดสอบทีละชั้นได้ เปลี่ยน Frontend หรือฐานข้อมูลโดยไม่กระทบ Business Logic | โครงสร้าง package ทั้งโปรเจค — `controller/` → `service/` → `repository/` → `domain/entity/` |
| **MVC** | แยกการรับ request, ตรรกะ และการแสดงผลออกจากกัน | Model: `domain/entity/*` + `dto/*` · Controller: `controller/api/*` · View: React SPA (`code/frontend/`) |
| **Repository** | ไม่ต้องเขียน JDBC boilerplate เอง, ซ่อนรายละเอียดการเข้าถึงข้อมูล, mock ใน unit test ได้ง่าย | `repository/BookRepository.java`, `repository/LoanRepository.java` (extends `JpaRepository`) |
| **Service Layer** | รวม Business Logic และขอบเขต Transaction ไว้ที่เดียว ไม่กระจายไปอยู่ใน Controller | `service/impl/LoanServiceImpl.java` (`@Service` + `@Transactional`) |
| **DTO + Mapper** | ป้องกัน Entity (รวมถึง field `password`) รั่วออกทาง API และแยก API Contract ออกจาก Database Schema ทำให้เปลี่ยน schema ได้โดยไม่ break client | `dto/request/*`, `dto/response/*`, `mapper/BookMapper.java`, `mapper/LoanMapper.java` |
| **Dependency Injection** | ลด coupling ระหว่างคลาส และทำให้ inject mock ใน unit test ได้ | ทุก Service และ Controller ใช้ **Constructor Injection** กับ field `private final` เท่านั้น — ไม่มี `@Autowired` บน field หรือ setter ในโปรเจคนี้ |

---

## 2. GoF Patterns — เลือกกลุ่ม **Behavioral** (ใช้ 5 แบบ)

เหตุผลที่เลือกกลุ่ม Behavioral: ระบบห้องสมุดมีจุดเด่นอยู่ที่ **กฎทางธุรกิจที่เปลี่ยนแปลงบ่อย**
(อัตราค่าปรับ เงื่อนไขการยืม) และ **สถานะที่ไหลไปตามเวลา** (Loan, BookCopy)
ซึ่งเป็นปัญหาที่ Behavioral Patterns ออกแบบมาเพื่อแก้โดยตรง

| Pattern | ปัญหาที่แก้ | ไฟล์ / คลาสที่ใช้ |
|---|---|---|
| **Strategy** | ค่าปรับคิดต่างกันตาม `MemberTier` (BR-07): STUDENT 3฿/วัน, STAFF 5฿/วัน เพดาน 300฿, EXTERNAL 10฿/วัน หากใช้ `if-else` ตรวจ Tier ใน `FineServiceImpl` ทุกครั้งที่ฝ่ายบริหารเพิ่มประเภทสมาชิกใหม่จะต้องแก้ไขคลาสเดิม ซึ่งละเมิด Open/Closed Principle | `pattern/strategy/FineCalculationStrategy.java` (interface)<br>`StudentFineStrategy.java`<br>`StaffFineStrategy.java`<br>`ExternalFineStrategy.java`<br>`FineStrategyResolver.java` |
| **State** | `Loan` มีสถานะ ACTIVE / OVERDUE / RETURNED / LOST ซึ่งอนุญาต action ต่างกัน (เช่น ต่ออายุได้เฉพาะ ACTIVE, คืนซ้ำไม่ได้) การเช็ค `if (loan.getStatus() == ...)` กระจายอยู่หลายเมธอดทำให้เพิ่มสถานะใหม่ต้องไล่แก้ทุกจุดและพลาดง่าย | `pattern/state/LoanState.java` (interface)<br>`ActiveState.java`, `OverdueState.java`,<br>`ReturnedState.java`, `LostState.java`,<br>`LoanStateFactory.java` |
| **Observer** | เมื่อคืนหนังสือ (BR-10) ต้องทำงานหลายอย่างที่ไม่เกี่ยวกันโดยตรง: ปลดสถานะตัวเล่ม, แจ้งสมาชิกที่จองคิวลำดับแรก, เขียน audit log หากเขียนรวมใน `returnBook()` คลาสเดียว จะละเมิด SRP และผูกติดกับระบบส่งอีเมลโดยไม่จำเป็น | `pattern/observer/BookReturnedEvent.java`<br>`ReservationNotificationListener.java`<br>`AuditLogListener.java`<br>(ใช้ `ApplicationEventPublisher` + `@EventListener` ของ Spring) |
| **Chain of Responsibility** | ก่อนอนุมัติการยืมต้องตรวจหลายเงื่อนไขเรียงลำดับ (BR-01 → BR-02 → BR-03 → BR-04) การรวมเป็นเมธอดเดียวยาว ๆ ทำให้เพิ่มกฎใหม่ สลับลำดับ หรือปิดกฎชั่วคราวทำได้ยากและทดสอบแยกไม่ได้ | `pattern/chain/BorrowRule.java` (interface)<br>`MemberStatusRule.java` (order 1)<br>`UnpaidFineRule.java` (order 2)<br>`LoanQuotaRule.java` (order 3)<br>`CopyAvailabilityRule.java` (order 4)<br>`BorrowContext.java` |
| **Template Method** | รายงานสถิติการยืมต้องออกได้ทั้ง CSV และ PDF ซึ่งมีขั้นตอนเหมือนกันทุกประการ (query → transform → render → export) ต่างกันเฉพาะขั้น render ถ้าเขียนแยกสองคลาสเต็ม ๆ จะเกิดโค้ดซ้ำและแก้ตรรกะการ query สองที่ | `pattern/template/AbstractReportGenerator.java`<br>`CsvReportGenerator.java`<br>`PdfReportGenerator.java` |

---

## 3. รายละเอียดการทำงานของแต่ละ Pattern

### 3.1 Strategy — คำนวณค่าปรับ

```java
public interface FineCalculationStrategy {
    boolean supports(MemberTier tier);
    BigDecimal calculate(LoanItem item, LocalDate returnDate);
}
```

Spring จะ inject ทุก implementation เข้ามาเป็น `List<FineCalculationStrategy>` โดยอัตโนมัติ
`FineStrategyResolver` เลือกตัวที่ `supports(tier)` เป็น `true`

**การเพิ่มประเภทสมาชิกใหม่ (เช่น ALUMNI)** ทำได้โดยสร้างคลาสใหม่ + ใส่ `@Component` เท่านั้น
ไม่ต้องแก้ `FineServiceImpl` หรือ `FineStrategyResolver` แม้แต่บรรทัดเดียว

### 3.2 State — สถานะใบยืม

```java
public interface LoanState {
    LoanStatus status();
    void onReturn(Loan loan, LoanItem item);
    void onRenew(Loan loan);
}
```

การเปลี่ยนสถานะทั้งหมดถูกรวมไว้ในคลาส State แต่ละตัว
ดู State Diagram: [`diagrams/09-state-loan.puml`](diagrams/09-state-loan.puml)

### 3.3 Observer — แจ้งเตือนคิวจอง

```java
// ฝั่ง publish
publisher.publishEvent(new BookReturnedEvent(this, bookId, copyId));

// ฝั่ง listen
@TransactionalEventListener(phase = AFTER_COMMIT)
public void onBookReturned(BookReturnedEvent event) { ... }
```

ใช้ `AFTER_COMMIT` เพื่อกันไม่ให้ส่งอีเมลแจ้งเตือนไปแล้ว transaction ดัน rollback ทีหลัง

### 3.4 Chain of Responsibility — ตรวจสิทธิ์การยืม

```java
public interface BorrowRule {
    int order();
    void check(BorrowContext ctx); // throw BusinessException ถ้าไม่ผ่าน
}
```

`LoanServiceImpl` เรียง rule ตาม `order()` แล้ววนเรียก `check()` ตามลำดับ
กฎแต่ละข้อทดสอบแยกเป็น unit test ได้อิสระ

### 3.5 Template Method — ออกรายงาน

```java
public abstract class AbstractReportGenerator {
    public final byte[] generate(ReportRequest req) {  // final = ห้าม override โครง
        var raw = fetchData(req);
        var rows = transform(raw);
        return render(rows);      // hook เดียวที่ subclass ต้อง implement
    }
    protected abstract byte[] render(List<ReportRow> rows);
}
```

---

## 4. Pattern ที่พิจารณาแล้ว "ไม่ใช้" (และเหตุผล)

การระบุสิ่งที่ไม่ใช้ช่วยยืนยันว่าเลือก Pattern อย่างมีเหตุผล ไม่ได้ยัดให้ครบ

| Pattern | เหตุผลที่ไม่ใช้ |
|---|---|
| **Singleton (เขียนเอง)** | Spring Bean มี scope เป็น singleton อยู่แล้ว การเขียน `getInstance()` เองซ้ำซ้อนและทำให้ test ยาก |
| **Abstract Factory** | ระบบมีตระกูลผลิตภัณฑ์เพียงตระกูลเดียว การเพิ่ม Abstract Factory จะเป็น over-engineering |
| **Composite** | โครงสร้างข้อมูลในระบบไม่มีลำดับชั้นแบบ tree ที่ต้องปฏิบัติกับ node เดี่ยวและกลุ่มเหมือนกัน |
| **Command** | ไม่มีความต้องการ undo/redo หรือ queue คำสั่ง — REST endpoint ทำหน้าที่นี้อยู่แล้ว |
