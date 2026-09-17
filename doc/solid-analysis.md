# SOLID Analysis — LibraFlow

> **สถานะเอกสาร:** เลขบรรทัดของโมดูล **Catalog** (สมาชิกคนที่ 1) ตรวจสอบกับโค้ดจริงแล้ว
> ส่วนที่ทำเครื่องหมาย _(รอโมดูล)_ เป็นของสมาชิกคนอื่น ให้เจ้าของโมดูลมาเติมเมื่องานเสร็จ
>
> ตรวจเลขบรรทัดซ้ำได้ด้วย
> `grep -n "ชื่อคลาสหรือเมธอด" -r code/backend/src/main/java`

---

## S — Single Responsibility Principle

**แต่ละคลาสมีเหตุผลที่จะถูกแก้ไขเพียงเหตุผลเดียว**

| ไฟล์ | บรรทัด | คำอธิบาย |
|---|---|---|
| `mapper/BookMapper.java` | 22–56 | หน้าที่เดียวคือแปลง Entity เป็น DTO ไม่มี business logic และ **ไม่แตะ Repository เลย** จำนวนตัวเล่มรับเข้ามาเป็นพารามิเตอร์ ไม่ได้ไปนับเอง — ถ้า mapper ยิง query ได้เมื่อไหร่ มันจะกลายเป็นแหล่งกำเนิด N+1 ทันที |
| `exception/GlobalExceptionHandler.java` | 24–97 | รับผิดชอบการแปลง Exception เป็น HTTP Response เท่านั้น เหตุผลเดียวที่จะถูกแก้คือ "รูปแบบ error response เปลี่ยน" ผลพลอยได้คือไม่มี `try-catch` แม้แต่ตัวเดียวใน Controller ทั้งโปรเจค |
| `service/impl/BookQueryServiceImpl.java` | 51–62 | `search()` ทำหน้าที่ประสานงานอย่างเดียว — เรียก repository, เรียก query นับตัวเล่ม, แล้วส่งต่อให้ mapper ไม่คำนวณหรือแปลงข้อมูลเอง |
| `common/BarcodeGenerator.java` | 22–38 | สร้างเลขบาร์โค้ดถัดไปเท่านั้น ไม่รู้จักฐานข้อมูล จึงทดสอบได้โดยไม่ต้องยก Spring ขึ้นมา |
| `service/impl/LoanServiceImpl.java` | _(รอโมดูล Loan)_ | orchestration ของการยืม-คืน |

**ตัวอย่างสิ่งที่หลีกเลี่ยง:** ไม่มีคลาสใดในระบบที่รวม validation + business logic + persistence
ไว้ด้วยกัน — `BookController` ไม่มีการเรียก `BookRepository` โดยตรงแม้แต่บรรทัดเดียว

---

## O — Open/Closed Principle

**เปิดให้ขยาย ปิดไม่ให้แก้ไข — เพิ่มฟีเจอร์ด้วยการเพิ่มคลาส/ค่า ไม่ใช่แก้ if-else เดิม**

| ไฟล์ | บรรทัด | คำอธิบาย |
|---|---|---|
| `exception/ErrorCode.java` | 14–53 | ผูก `HttpStatus` ไว้กับ enum แต่ละค่า ทำให้เพิ่มรหัสข้อผิดพลาดใหม่แค่เติมค่าใน enum ที่เดียว |
| `exception/GlobalExceptionHandler.java` | 29–39 | `handleBusiness()` อ่าน status จาก `ex.getErrorCode().getStatus()` — **ไม่มี `switch` หรือ `if-else` ตรวจรหัสแม้แต่จุดเดียว** สมาชิกคนที่ 2 และ 3 เพิ่ม ErrorCode ของตัวเองได้โดยไม่ต้องแตะไฟล์นี้เลย |
| `dto/response/PageResponse.java` | 37–46 | `from(Page<E>, Function<E,T>)` ใช้ซ้ำกับ resource ใดก็ได้โดยไม่ต้องแก้คลาส เพียงส่งฟังก์ชันแปลงเข้ามา |
| `pattern/strategy/FineStrategyResolver.java` | _(รอโมดูล Fine)_ | เลือก strategy ตาม MemberTier |

**พิสูจน์:** ตอนเพิ่ม `BARCODE_ALREADY_EXISTS` เข้าระบบ แตะไฟล์เดียวคือ `ErrorCode.java`
โดยที่ `GlobalExceptionHandler` ซึ่งทดสอบผ่านแล้วไม่ถูกแก้เลย

---

## L — Liskov Substitution Principle

**Subclass ใช้แทน Superclass ได้โดยไม่พังตรรกะ และไม่ throw `UnsupportedOperationException`**

| ไฟล์ | บรรทัด | คำอธิบาย |
|---|---|---|
| `service/impl/BookQueryServiceImpl.java` | 36–102 | implement ทุกเมธอดของ `BookQueryService` อย่างมีความหมายจริง ไม่มีเมธอดไหนโยน `UnsupportedOperationException` |
| `service/impl/BookCommandServiceImpl.java` | 43–188 | เช่นเดียวกัน — กรณีที่ทำงานไม่ได้จะโยน `BusinessException` ซึ่งเป็น **ผลลัพธ์เชิงธุรกิจที่ประกาศไว้ใน contract ของ interface** ไม่ใช่การปฏิเสธว่า "เมธอดนี้ใช้ไม่ได้" |
| `pattern/state/ReturnedState.java` | _(รอโมดูล Loan)_ | สถานะที่ทำ action ไม่ได้ต้องโยน BusinessException ไม่ใช่ UnsupportedOperationException |
| `pattern/template/CsvReportGenerator.java` | _(รอโมดูล Report)_ | ใช้แทน AbstractReportGenerator ได้โดย caller ไม่ต้องรู้ชนิดจริง |

**หลักที่ยึด:** ทุก implementation ไม่ทำให้ precondition เข้มขึ้นและไม่ทำให้ postcondition อ่อนลง

---

## I — Interface Segregation Principle

**แยก interface ย่อยตามการใช้งาน ไม่มี Fat Interface**

| ไฟล์ | บรรทัด | คำอธิบาย |
|---|---|---|
| `service/BookQueryService.java` | 21–31 | มีเฉพาะเมธอดอ่านข้อมูล — `search`, `findById`, `findCopies` |
| `service/BookCommandService.java` | 15–27 | มีเฉพาะเมธอดเขียนข้อมูล — `create`, `update`, `delete`, `addCopy` |
| `controller/api/PublicCatalogController.java` | 40–47 | ถือ `BookQueryService` ตัวเดียว **ไม่รู้จัก `BookCommandService` เลย** ต่อให้เขียนพลาดก็เรียกเมธอดเขียนข้อมูลไม่ได้ เพราะ compile ไม่ผ่าน |
| `controller/api/BookController.java` | 42–48 | กลับกัน — ถือเฉพาะ `BookCommandService` ทำให้ขอบเขตสิทธิ์ตรงกับขอบเขตของคลาสพอดี สมาชิกคนที่ 5 ใส่ `@PreAuthorize` ที่ระดับคลาสได้เลย |

**ตัวอย่างสิ่งที่หลีกเลี่ยง:** ไม่มี interface ชื่อ `LibraryService` ที่รวม
`saveBook`, `borrow`, `payFine`, `generateReport` ไว้ด้วยกัน

---

## D — Dependency Inversion Principle

**โมดูลระดับสูงขึ้นกับ abstraction ไม่ใช่ concrete class + ใช้ Constructor Injection เท่านั้น**

| ไฟล์ | บรรทัด | คำอธิบาย |
|---|---|---|
| `service/impl/BookCommandServiceImpl.java` | 47–69 | field ทั้ง 7 ตัวเป็น `private final` ของชนิด **interface** ทั้งหมด และรับผ่าน constructor ตัวเดียว |
| `service/impl/BookQueryServiceImpl.java` | 38–48 | เช่นเดียวกัน — `BookRepository`, `BookCopyRepository` เป็น interface ที่ Spring Data สร้าง implementation ให้ตอน runtime |
| `controller/api/BookController.java` | 44–48 | ขึ้นกับ `BookCommandService` (interface) ไม่ใช่ `BookCommandServiceImpl` |
| `controller/api/PublicCatalogController.java` | 42–46 | ขึ้นกับ `BookQueryService` (interface) |

**กฎที่บังคับใช้ทั้งโปรเจค**
- ห้ามใช้ `@Autowired` บน field หรือ setter — มีเฉพาะ Constructor Injection
- ห้ามใช้ `new` สร้าง Service หรือ Repository ในโค้ด production
- ทุก dependency ของ Service ต้องเป็นชนิด interface

**ผลที่ได้ (พิสูจน์ได้จริงในเทสต์):** `BookCommandServiceImplTest` ใช้ `@Mock` กับ repository
ทั้ง 5 ตัวแล้ว inject ผ่าน constructor ได้ทันที ทดสอบกฎ BR-11 จบใน 0.5 วินาที
โดยไม่ต้องยก Spring Context หรือฐานข้อมูลขึ้นมาเลย — ถ้าเคยเขียน `new BookRepositoryImpl()`
ไว้ในคลาส จะ mock ไม่ได้และต้องใช้ฐานข้อมูลจริงทดสอบ
