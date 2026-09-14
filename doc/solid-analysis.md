# SOLID Analysis — LibraFlow

> **หมายเหตุสำหรับผู้จัดทำ:** หมายเลขบรรทัดในเอกสารนี้เป็นตัวอย่าง
> ต้องอัปเดตให้ตรงกับโค้ดจริงก่อนส่ง (ใบงานกำหนดให้ระบุ "ไฟล์ไหน บรรทัดไหน")
> ตรวจเลขบรรทัดได้ด้วย: `grep -n "ชื่อคลาสหรือเมธอด" -r code/backend/src/main/java`

---

## S — Single Responsibility Principle

**แต่ละคลาสมีเหตุผลที่จะถูกแก้ไขเพียงเหตุผลเดียว**

| ไฟล์ | บรรทัด | คำอธิบาย |
|---|---|---|
| `service/impl/LoanServiceImpl.java` | 38–96 | ทำหน้าที่ orchestration ของการยืม-คืนเท่านั้น ไม่คำนวณค่าปรับเอง (โยนให้ `FineCalculationStrategy`) ไม่ตรวจสอบเงื่อนไขการยืมเอง (โยนให้ `BorrowRule` chain) ไม่แปลง Entity เป็น DTO เอง (โยนให้ `LoanMapper`) |
| `mapper/BookMapper.java` | 1–54 | มีหน้าที่เดียวคือแปลงข้อมูลระหว่าง Entity กับ DTO ไม่มี business logic และไม่แตะ Repository |
| `pattern/chain/LoanQuotaRule.java` | 18–34 | ตรวจสอบกฎ BR-03 เพียงข้อเดียว ไม่ยุ่งกับกฎอื่น |
| `exception/GlobalExceptionHandler.java` | 22–88 | รับผิดชอบการแปลง Exception เป็น HTTP Response เท่านั้น |

**ตัวอย่างสิ่งที่หลีกเลี่ยง:** ไม่มีคลาสใดในระบบที่รวม validation + business logic + persistence
ไว้ด้วยกัน — `BookController` ไม่มีการเรียก `BookRepository` โดยตรง (ละเมิด Layered Architecture)

---

## O — Open/Closed Principle

**เปิดให้ขยาย ปิดไม่ให้แก้ไข — เพิ่มฟีเจอร์ด้วยการเพิ่มคลาส ไม่ใช่แก้ if-else เดิม**

| ไฟล์ | บรรทัด | คำอธิบาย |
|---|---|---|
| `pattern/strategy/FineCalculationStrategy.java` | 10–16 | interface กลางของการคำนวณค่าปรับ |
| `pattern/strategy/FineStrategyResolver.java` | 14–29 | รับ `List<FineCalculationStrategy>` ผ่าน constructor แล้วเลือกตัวที่ `supports(tier)` — ไม่มี `if-else` หรือ `switch` ตรวจ Tier แม้แต่จุดเดียว |
| `pattern/chain/BorrowRule.java` | 8–14 | เพิ่มกฎการยืมใหม่ทำได้โดยสร้างคลาสใหม่ + `@Component` Spring จะ inject เข้า `List<BorrowRule>` อัตโนมัติ |

**พิสูจน์:** การเพิ่มประเภทสมาชิก `ALUMNI` ต้องแตะไฟล์เพียง 2 ไฟล์
— เพิ่มค่าใน enum `MemberTier` และสร้างคลาสใหม่ `AlumniFineStrategy`
โดยไม่แก้ไขโค้ดเดิมที่ทดสอบผ่านแล้วเลย

---

## L — Liskov Substitution Principle

**Subclass ใช้แทน Superclass ได้โดยไม่พังตรรกะ และไม่ throw `UnsupportedOperationException`**

| ไฟล์ | บรรทัด | คำอธิบาย |
|---|---|---|
| `pattern/state/ActiveState.java` | 12–40 | implement ทุกเมธอดของ `LoanState` อย่างมีความหมายจริง |
| `pattern/state/ReturnedState.java` | 12–34 | กรณีที่ทำ action ไม่ได้ (เช่น คืนซ้ำ) จะ **throw `BusinessException` ที่เป็นผลลัพธ์เชิงธุรกิจที่ประกาศไว้ใน contract** ไม่ใช่ `UnsupportedOperationException` ซึ่งเป็นการพังสัญญาของ interface |
| `pattern/template/CsvReportGenerator.java` | 14–48 | ใช้แทน `AbstractReportGenerator` ได้โดย caller ไม่ต้องรู้ชนิดจริง |
| `pattern/template/PdfReportGenerator.java` | 14–52 | เช่นเดียวกัน — รับ input แบบเดียวกัน คืน `byte[]` เหมือนกัน |

**หลักที่ยึด:** ทุก implementation ไม่ทำให้ precondition เข้มขึ้นและไม่ทำให้ postcondition อ่อนลง
`LoanState` ทุกตัวรับ `Loan` ที่ไม่ใช่ null เหมือนกัน และรับประกันว่าสถานะหลังเรียกเมธอดจะถูกต้องเสมอ

---

## I — Interface Segregation Principle

**แยก interface ย่อยตามการใช้งาน ไม่มี Fat Interface**

| ไฟล์ | บรรทัด | คำอธิบาย |
|---|---|---|
| `service/BookQueryService.java` | 10–20 | มีเฉพาะเมธอดอ่านข้อมูล (`search`, `findById`, `findCopies`) |
| `service/BookCommandService.java` | 10–22 | มีเฉพาะเมธอดเขียนข้อมูล (`create`, `update`, `delete`) |
| `controller/api/PublicCatalogController.java` | 18–24 | ขึ้นกับ `BookQueryService` เท่านั้น ไม่รู้จักเมธอดเขียนเลย ทำให้สิทธิ์และ dependency ชัดเจน |
| `pattern/chain/BorrowRule.java` | 8–14 | interface มีเมธอดเพียง `check()` และ `order()` ไม่บังคับให้ implementer เขียนเมธอดที่ไม่ได้ใช้ |

**ตัวอย่างสิ่งที่หลีกเลี่ยง:** ไม่มี interface ชื่อ `LibraryService` ที่รวมทุกอย่าง
(`saveBook`, `borrow`, `payFine`, `generateReport`) ไว้ด้วยกัน

---

## D — Dependency Inversion Principle

**โมดูลระดับสูงขึ้นกับ abstraction ไม่ใช่ concrete class + ใช้ Constructor Injection เท่านั้น**

| ไฟล์ | บรรทัด | คำอธิบาย |
|---|---|---|
| `service/impl/LoanServiceImpl.java` | 24–36 | field ทั้งหมดเป็น `private final` ของชนิด **interface** — `LoanRepository`, `BookCopyRepository`, `List<BorrowRule>`, `FineStrategyResolver`, `ApplicationEventPublisher` และรับผ่าน constructor ตัวเดียว |
| `service/impl/FineServiceImpl.java` | 20–30 | ขึ้นกับ `FineCalculationStrategy` (interface) ไม่ใช่ `StudentFineStrategy` (concrete) |
| `controller/api/LoanController.java` | 26–32 | ขึ้นกับ `LoanService` (interface) ไม่ใช่ `LoanServiceImpl` |

**กฎที่บังคับใช้ทั้งโปรเจค**
- ห้ามใช้ `@Autowired` บน field หรือ setter — มีเฉพาะ Constructor Injection
- ห้ามใช้ `new` สร้าง Service หรือ Repository ในโค้ด production
- ทุก dependency ของ Service ต้องเป็นชนิด interface

**ผลที่ได้:** ใน unit test สามารถ `@Mock LoanRepository` แล้ว inject ผ่าน constructor ได้ทันที
โดยไม่ต้องยก Spring Context ขึ้นมาทั้งตัว ทำให้ test เร็วและ isolate จริง
