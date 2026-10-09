# SOLID Analysis — LibraFlow

เอกสารนี้ยกตัวอย่างจาก implementation ที่ตรวจใน repository ณ 8 ตุลาคม 2569.
อ้างอิงชื่อไฟล์แทนเลขบรรทัด เพราะเลขบรรทัดเปลี่ยนได้เมื่อแก้โค้ด
การใช้ design pattern ไม่ได้แปลว่าทุกคลาสทำตาม SOLID โดยอัตโนมัติ

## S — Single Responsibility Principle

ให้แต่ละคลาสมีหน้าที่หลักที่ชัดเจนและเหตุผลในการเปลี่ยนที่เกี่ยวข้องกัน

| ตัวอย่าง | หน้าที่ |
|---|---|
| `mapper/BookMapper.java` | แปลง entity เป็น DTO; รับจำนวนตัวเล่มจาก caller และไม่ query repository |
| `exception/GlobalExceptionHandler.java` | แปลง exception เป็น HTTP error response |
| `service/impl/LoanServiceImpl.java` | ประสาน flow ยืม/คืน/ต่ออายุ แล้วใช้ rules, state, FineService และ event publisher ตามงาน |
| `pattern/template/AbstractReportGenerator.java` | ใช้ขั้นตอนร่วมของรายงาน; subclasses สร้างผลลัพธ์ CSV หรือ PDF |
| `service/event/listener/ReservationNotificationListener.java` | รับ event คืนหนังสือและปรับ reservation ลำดับแรก |

## O — Open/Closed Principle

ระบบมีจุดที่ขยาย behavior ผ่าน implementation ใหม่ได้ โดยบางกรณียังต้องเพิ่ม enum หรือ mapping:

| ตัวอย่าง | วิธีขยายและข้อควรทราบ |
|---|---|
| `pattern/chain/BorrowRule.java` | เพิ่มกฎตรวจยืมเป็น implementation ใหม่; `LoanServiceImpl` ใช้รายการ rules และเรียงด้วย `order()` |
| `service/strategy/FineCalculationStrategy.java` | `FineServiceImpl` จัด strategy ตาม `MemberTier`; การเพิ่ม tier ใหม่ต้องเพิ่ม enum/tier mapping ที่เกี่ยวข้องด้วย |
| `pattern/template/AbstractReportGenerator.java` | subclasses implement `render`; `ReportController` ปัจจุบันเลือก CSV หรือ PDF |
| `exception/ErrorCode.java` และ `GlobalExceptionHandler.java` | เพิ่ม error code ใน enum ได้โดย handler ใช้ status จาก code; ไม่ต้องเพิ่ม case แยกใน handler |

`LoanStateFactory` เลือก implementation จาก `LoanStatus`; การเพิ่ม state ต้องอัปเดต factory/mapping ด้วย
จึงไม่ควรกล่าวว่าทุกส่วนเปิดขยายโดยไม่แก้โค้ดเดิม

## L — Liskov Substitution Principle

- `MemberStatusRule`, `UnpaidFineRule`, `LoanQuotaRule` และ `CopyAvailabilityRule` ใช้ผ่าน `BorrowRule` ใน chain เดียวกัน.
- `ActiveState`, `OverdueState`, `ReturnedState` และ `LostState` ใช้ผ่าน `LoanState`; transition ที่ผิดกฎถูกปฏิเสธด้วย business exception ตาม contract.
- `CsvReportGenerator` และ `PdfReportGenerator` เติมขั้น render ของ `AbstractReportGenerator`; controller เรียกผ่านชนิดฐาน.
- ชุด unit tests `LoanStateTest` และ test ของ borrow rules ตรวจ behavior ของ implementations.

## I — Interface Segregation Principle

- `BookQueryService` รวมงานอ่าน; `PublicCatalogController` ใช้เฉพาะ interface นี้.
- `BookCommandService` รวม create/update/delete/add-copy; `BookController` ใช้เฉพาะ interface นี้.
- การแยก query/command ช่วยไม่ให้ public catalog ได้ dependency สำหรับเขียนข้อมูล.

## D — Dependency Inversion Principle

- Controllers ขึ้นกับ service interfaces เช่น `LoanService`, `BookQueryService` และ `BookCommandService`.
- `LoanServiceImpl` รับ repository interfaces, `List<BorrowRule>`, `FineService` และ `ApplicationEventPublisher` ผ่าน constructor.
- `LoanStateFactory` และ `LoanMapper` ที่ `LoanServiceImpl` ใช้เป็น concrete collaborators; `BookMapper` และ `BarcodeGenerator` ที่ catalog services ใช้ก็เป็น utility classes. จึงไม่อ้างว่าทุก service dependency เป็น interface.
- `FineServiceImpl` รับ `FineRepository` และ `List<FineCalculationStrategy>` ผ่าน constructor; unit tests สามารถ inject mocks โดยไม่ต้องเริ่ม Spring context.
- ใน services/controllers ที่ตรวจ ไม่พบ field injection; constructor injection ทำให้ dependencies ชัดเจนและทดสอบได้.

## Report implementation

เส้นทางที่ `ReportController` ใช้จริงคือ `pattern/template/AbstractReportGenerator`
กับ `CsvReportGenerator` และ `PdfReportGenerator`. คลาสใน `service/report/`
เช่น `FineReportGenerator` และ `ReservationReportGenerator` เป็น implementation อีกชุด
ที่ controller ปัจจุบันไม่ได้เรียก; ไม่ควรอธิบายว่าเป็น report API path.

## Fine calculation

`StudentFineStrategy`, `StaffFineStrategy` และ `ExternalFineStrategy` ใช้ `MemberTier`:
3 บาท/วัน, 5 บาท/วันโดยจำกัดสูงสุด 300 บาท, และ 10 บาท/วันตามลำดับ.
การเรียกคืนช้าสร้างค่าปรับผ่าน `LoanServiceImpl`; รายการที่ scheduler เปลี่ยนเป็น `LOST`
ถูกคิดค่าชดใช้ตามราคาหนังสือเต็มจำนวนผ่าน `FineService.generateLostBookFine`.
