# SOLID Analysis — LibraFlow

เอกสารนี้ยกตัวอย่างจาก implementation ที่ตรวจใน repository ณ 9 ตุลาคม 2569.
หลักฐานท้ายเอกสารระบุไฟล์และช่วงบรรทัดใน revision นี้; ให้อัปเดตช่วงบรรทัดเมื่อโค้ดเปลี่ยน.
การใช้ design pattern ไม่ได้แปลว่าทุกคลาสทำตาม SOLID โดยอัตโนมัติ

## S — Single Responsibility Principle

ให้แต่ละคลาสมีหน้าที่หลักที่ชัดเจนและเหตุผลในการเปลี่ยนที่เกี่ยวข้องกัน

| ตัวอย่าง | หน้าที่ |
|---|---|
| `mapper/BookMapper.java` | แปลง entity เป็น DTO; รับจำนวนตัวเล่มจาก caller และไม่ query repository |
| `exception/GlobalExceptionHandler.java` | แปลง exception เป็น HTTP error response |
| `service/impl/LoanServiceImpl.java` | ประสาน flow ยืม/คืน/ต่ออายุ แล้วใช้ rules, state, FineService และ event publisher ตามงาน |
| `service/impl/ReportServiceImpl.java` | เลือก generator สำหรับรายงาน CSV/PDF; controller ไม่รู้จัก repository หรือ generator implementation |
| `pattern/template/AbstractReportGenerator.java` | ใช้ขั้นตอนร่วมของรายงาน; subclasses สร้างผลลัพธ์ CSV หรือ PDF |
| `service/event/listener/ReservationNotificationListener.java` | รับ event คืนหนังสือและปรับ reservation ลำดับแรก |

## O — Open/Closed Principle

ระบบมีจุดที่ขยาย behavior ผ่าน implementation ใหม่ได้ โดยบางกรณียังต้องเพิ่ม enum หรือ mapping:

| ตัวอย่าง | วิธีขยายและข้อควรทราบ |
|---|---|
| `pattern/chain/BorrowRule.java` | เพิ่มกฎตรวจยืมเป็น implementation ใหม่; `LoanServiceImpl` ใช้รายการ rules และเรียงด้วย `order()` |
| `service/strategy/FineCalculationStrategy.java` | `FineServiceImpl` จัด strategy ตาม `MemberTier`; การเพิ่ม tier ใหม่ต้องเพิ่ม enum/tier mapping ที่เกี่ยวข้องด้วย |
| `pattern/template/AbstractReportGenerator.java` | subclasses implement `render`; `ReportServiceImpl` เลือก CSV หรือ PDF ตาม use case |
| `exception/ErrorCode.java` และ `GlobalExceptionHandler.java` | เพิ่ม error code ใน enum ได้โดย handler ใช้ status จาก code; ไม่ต้องเพิ่ม case แยกใน handler |

`LoanStateFactory` เลือก implementation จาก `LoanStatus`; การเพิ่ม state ต้องอัปเดต factory/mapping ด้วย
จึงไม่ควรกล่าวว่าทุกส่วนเปิดขยายโดยไม่แก้โค้ดเดิม

## L — Liskov Substitution Principle

- `MemberStatusRule`, `UnpaidFineRule`, `LoanQuotaRule` และ `CopyAvailabilityRule` ใช้ผ่าน `BorrowRule` ใน chain เดียวกัน.
- `ActiveState`, `OverdueState`, `ReturnedState` และ `LostState` ใช้ผ่าน `LoanState`; transition ที่ผิดกฎถูกปฏิเสธด้วย business exception ตาม contract.
- `CsvReportGenerator` และ `PdfReportGenerator` เติมขั้น render ของ `AbstractReportGenerator`; service เรียกผ่าน `ReportFileGenerator`.
- ชุด unit tests `LoanStateTest` และ test ของ borrow rules ตรวจ behavior ของ implementations.

## I — Interface Segregation Principle

- `BookQueryService` รวมงานอ่าน; `PublicCatalogController` ใช้เฉพาะ interface นี้.
- `BookCommandService` รวม create/update/delete/add-copy; `BookController` ใช้เฉพาะ interface นี้.
- การแยก query/command ช่วยไม่ให้ public catalog ได้ dependency สำหรับเขียนข้อมูล.

## D — Dependency Inversion Principle

- Controllers ขึ้นกับ service interfaces เช่น `LoanService`, `BookQueryService`, `BookCommandService` และ `ReportService`.
- `ReportServiceImpl` รับ `ReportFileGenerator` ผ่าน constructor; generator implementation จึงเปลี่ยนได้โดยไม่แก้ HTTP controller.
- `LoanServiceImpl` รับ repository interfaces, `List<BorrowRule>`, `FineService` และ `ApplicationEventPublisher` ผ่าน constructor.
- `LoanStateFactory` และ `LoanMapper` ที่ `LoanServiceImpl` ใช้เป็น concrete collaborators; `BookMapper` และ `BarcodeGenerator` ที่ catalog services ใช้ก็เป็น utility classes. จึงไม่อ้างว่าทุก service dependency เป็น interface.
- `FineServiceImpl` รับ `FineRepository` และ `List<FineCalculationStrategy>` ผ่าน constructor; unit tests สามารถ inject mocks โดยไม่ต้องเริ่ม Spring context.
- ใน services/controllers ที่ตรวจ ไม่พบ field injection; constructor injection ทำให้ dependencies ชัดเจนและทดสอบได้.

## Report implementation

เส้นทางที่ `ReportController` ใช้จริงคือ `ReportService` → `pattern/template/AbstractReportGenerator`
กับ `CsvReportGenerator` และ `PdfReportGenerator`. `ReportController` parse request และกำหนด HTTP response;
`ReportServiceImpl` เลือก generator. คลาสใน `service/report/`
เช่น `FineReportGenerator` และ `ReservationReportGenerator` เป็น implementation อีกชุด
ที่ controller ปัจจุบันไม่ได้เรียก; ไม่ควรอธิบายว่าเป็น report API path.

## Implementation evidence

| Claim | Source evidence |
|---|---|
| สมัครสมาชิก persist profile ผ่าน repository | [`AuthServiceImpl.java`](../code/backend/src/main/java/com/libraflow/library/service/impl/AuthServiceImpl.java#L25-L39) injects repository and [saves profile in `register`](../code/backend/src/main/java/com/libraflow/library/service/impl/AuthServiceImpl.java#L63-L96); [`UserProfileRepository.java`](../code/backend/src/main/java/com/libraflow/library/repository/UserProfileRepository.java#L1-L7) |
| ป้องกัน Admin แก้ role/ระงับบัญชีของตัวเองที่ backend | [`UserManagementController.java`](../code/backend/src/main/java/com/libraflow/library/controller/api/UserManagementController.java#L47-L68) passes the authenticated username; [`UserManagementServiceImpl.java`](../code/backend/src/main/java/com/libraflow/library/service/impl/UserManagementServiceImpl.java#L40-L94) checks the actor before saving |
| Controller แยกจาก report generator/repository | [`ReportController.java`](../code/backend/src/main/java/com/libraflow/library/controller/api/ReportController.java#L19-L45) → [`ReportServiceImpl.java`](../code/backend/src/main/java/com/libraflow/library/service/impl/ReportServiceImpl.java#L11-L34) → [`AbstractReportGenerator.java`](../code/backend/src/main/java/com/libraflow/library/pattern/template/AbstractReportGenerator.java#L13-L54), through [`ReportFileGenerator.java`](../code/backend/src/main/java/com/libraflow/library/pattern/template/ReportFileGenerator.java#L5-L8) |
| V10 removes duplicate user indexes and constrains fine status | [`V10__tighten_user_indexes_and_fine_status.sql`](../code/backend/src/main/resources/db/migration/V10__tighten_user_indexes_and_fine_status.sql#L1-L25); schema notes in [`data-dictionary.md`](data-dictionary.md#L20-L24) |
| API report routes return CSV/PDF as implemented | [`ReportController.java`](../code/backend/src/main/java/com/libraflow/library/controller/api/ReportController.java#L25-L48); [`api-spec.md`](api-spec.md#L82-L86) |
| Notification เป็น in-system reservation update/log; ไม่มี email/SMS provider | [`ReservationNotificationListener.java`](../code/backend/src/main/java/com/libraflow/library/service/event/listener/ReservationNotificationListener.java#L40-L67) |
| CI ทำ verification; deployment แยกที่ provider | [`backend-ci.yml`](../.github/workflows/backend-ci.yml#L17-L52), [`frontend-ci.yml`](../.github/workflows/frontend-ci.yml#L15-L45), [`security-ci-deployment.md`](security-ci-deployment.md#L34-L44) |

## Fine calculation

`StudentFineStrategy`, `StaffFineStrategy` และ `ExternalFineStrategy` ใช้ `MemberTier`:
3 บาท/วัน, 5 บาท/วันโดยจำกัดสูงสุด 300 บาท, และ 10 บาท/วันตามลำดับ.
การเรียกคืนช้าสร้างค่าปรับผ่าน `LoanServiceImpl`; รายการที่ scheduler เปลี่ยนเป็น `LOST`
ถูกคิดค่าชดใช้ตามราคาหนังสือเต็มจำนวนผ่าน `FineService.generateLostBookFine`.
