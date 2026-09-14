# REST API Specification — LibraFlow

Base URL (local): `http://localhost:8080`
Base URL (prod): _(รอ deploy)_
Swagger UI: `/swagger-ui.html` · OpenAPI JSON: `/v3/api-docs`
Authentication: `Authorization: Bearer <JWT>`

---

## 1. Endpoint ทั้งหมด

### Auth
| Method | Endpoint | Success | สิทธิ์ | คำอธิบาย |
|---|---|---|---|---|
| POST | `/api/v1/auth/register` | 201 | public | สมัครสมาชิก |
| POST | `/api/v1/auth/login` | 200 | public | เข้าสู่ระบบ รับ JWT |

### Books (Resource หลักที่ 1 — CRUD ครบ)
| Method | Endpoint | Success | Error | สิทธิ์ | คำอธิบาย |
|---|---|---|---|---|---|
| GET | `/api/v1/books?keyword=&categoryId=&page=0&size=10&sort=title,asc` | 200 | 400 | public | **Pagination + Sorting** |
| GET | `/api/v1/books/{id}` | 200 | 404 | public | ดูรายละเอียด |
| POST | `/api/v1/books` | 201 | 400, 409 | LIBRARIAN | เพิ่มหนังสือ (409 = ISBN ซ้ำ) |
| PUT | `/api/v1/books/{id}` | 200 | 400, 404 | LIBRARIAN | แก้ไข |
| DELETE | `/api/v1/books/{id}` | 204 | 404, 409 | LIBRARIAN | ลบ (409 = ยังมีตัวเล่มถูกยืม BR-11) |
| GET | `/api/v1/books/{id}/copies` | 200 | 404 | public | ตัวเล่มทั้งหมดของหนังสือ |
| POST | `/api/v1/books/{id}/copies` | 201 | 404, 409 | LIBRARIAN | เพิ่มตัวเล่ม |

### Loans (Resource หลักที่ 2 — CRUD ครบ)
| Method | Endpoint | Success | Error | สิทธิ์ | คำอธิบาย |
|---|---|---|---|---|---|
| GET | `/api/v1/loans?status=&page=&size=` | 200 | | LIBRARIAN | รายการใบยืมทั้งหมด |
| GET | `/api/v1/loans/{id}` | 200 | 404 | LIBRARIAN, เจ้าของ | ดูใบยืม |
| POST | `/api/v1/loans` | 201 | 400, 404, 409 | LIBRARIAN | บันทึกการยืม (ผ่าน BorrowRule chain) |
| PATCH | `/api/v1/loans/{id}/return` | 200 | 404, 409 | LIBRARIAN | บันทึกการคืน |
| PATCH | `/api/v1/loans/{id}/renew` | 200 | 404, 409 | LIBRARIAN | ต่ออายุ (BR-06) |
| DELETE | `/api/v1/loans/{id}` | 204 | 404, 409 | ADMIN | ยกเลิกใบยืมที่บันทึกผิด |
| GET | `/api/v1/members/{id}/loans` | 200 | 404 | LIBRARIAN, เจ้าของ | ประวัติการยืมของสมาชิก |

### Reservations
| Method | Endpoint | Success | Error | สิทธิ์ |
|---|---|---|---|---|
| GET | `/api/v1/reservations?status=&page=&size=` | 200 | | LIBRARIAN |
| POST | `/api/v1/reservations` | 201 | 400, 409 | MEMBER |
| DELETE | `/api/v1/reservations/{id}` | 204 | 404 | MEMBER (เจ้าของ) |

### Fines
| Method | Endpoint | Success | Error | สิทธิ์ |
|---|---|---|---|---|
| GET | `/api/v1/members/{id}/fines?status=UNPAID` | 200 | 404 | LIBRARIAN, เจ้าของ |
| POST | `/api/v1/fines/{id}/pay` | 200 | 404, 409 | LIBRARIAN |
| POST | `/api/v1/fines/{id}/waive` | 200 | 404, 409 | ADMIN |

### Reports
| Method | Endpoint | Success | สิทธิ์ | คำอธิบาย |
|---|---|---|---|---|
| GET | `/api/v1/reports/loans?from=&to=&format=CSV` | 200 | LIBRARIAN | Template Method Pattern |
| GET | `/api/v1/reports/overdue?format=PDF` | 200 | LIBRARIAN | รายงานหนังสือค้างส่ง |

---

## 2. ตัวอย่าง Request / Response

### POST /api/v1/loans — บันทึกการยืม

**Request**
```json
{
  "memberId": 12,
  "barcodes": ["LIB-00231", "LIB-00842"]
}
```

**Response 201 Created**
```json
{
  "id": 507,
  "loanCode": "LN-20260912-0007",
  "memberName": "สมชาย ใจดี",
  "memberTier": "STUDENT",
  "loanDate": "2026-09-12T10:22:31Z",
  "status": "ACTIVE",
  "items": [
    {
      "id": 901,
      "barcode": "LIB-00231",
      "bookTitle": "Clean Code",
      "dueDate": "2026-09-19",
      "returnedAt": null
    },
    {
      "id": 902,
      "barcode": "LIB-00842",
      "bookTitle": "Design Patterns",
      "dueDate": "2026-09-19",
      "returnedAt": null
    }
  ]
}
```

### GET /api/v1/books — Pagination + Sorting

**Response 200 OK**
```json
{
  "content": [
    {
      "id": 1,
      "isbn": "9780132350884",
      "title": "Clean Code",
      "publishYear": 2008,
      "categoryName": "Software Engineering",
      "publisherName": "Prentice Hall",
      "authors": ["Robert C. Martin"],
      "availableCopies": 3,
      "totalCopies": 5
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 128,
  "totalPages": 13,
  "first": true,
  "last": false
}
```

---

## 3. Error Response Format มาตรฐาน

จัดการโดย `@RestControllerAdvice` ใน `exception/GlobalExceptionHandler.java`

```json
{
  "timestamp": "2026-09-12T10:22:31Z",
  "status": 409,
  "error": "Conflict",
  "errorCode": "UNPAID_FINE_EXCEEDED",
  "message": "สมาชิกมีค่าปรับค้างชำระ 150.00 บาท เกินเกณฑ์ 100 บาท",
  "path": "/api/v1/loans",
  "fieldErrors": []
}
```

กรณี Validation ผิดพลาด (400)

```json
{
  "timestamp": "2026-09-12T10:25:02Z",
  "status": 400,
  "error": "Bad Request",
  "errorCode": "VALIDATION_FAILED",
  "message": "ข้อมูลที่ส่งมาไม่ถูกต้อง",
  "path": "/api/v1/books",
  "fieldErrors": [
    { "field": "isbn", "message": "ISBN ต้องไม่เป็นค่าว่าง" },
    { "field": "publishYear", "message": "ปีที่พิมพ์ต้องไม่เกินปีปัจจุบัน" }
  ]
}
```

---

## 4. ตาราง Error Code

| errorCode | HTTP | ความหมาย | กฎอ้างอิง |
|---|---|---|---|
| `VALIDATION_FAILED` | 400 | ข้อมูล request ไม่ผ่าน Bean Validation | |
| `RESOURCE_NOT_FOUND` | 404 | ไม่พบข้อมูลที่ระบุ | |
| `ISBN_ALREADY_EXISTS` | 409 | ISBN ซ้ำในระบบ | |
| `MEMBER_SUSPENDED` | 409 | บัญชีสมาชิกถูกระงับ | BR-01 |
| `UNPAID_FINE_EXCEEDED` | 409 | ค่าปรับค้างชำระเกินเกณฑ์ | BR-02 |
| `LOAN_QUOTA_EXCEEDED` | 409 | ยืมครบโควต้าแล้ว | BR-03 |
| `COPY_NOT_AVAILABLE` | 409 | ตัวเล่มไม่พร้อมให้ยืม | BR-04 |
| `RENEW_LIMIT_REACHED` | 409 | ต่ออายุครบจำนวนครั้งแล้ว | BR-06 |
| `RENEW_BLOCKED_BY_RESERVATION` | 409 | มีคนจองคิวรออยู่ ต่ออายุไม่ได้ | BR-06 |
| `DUPLICATE_RESERVATION` | 409 | จองหนังสือเล่มเดิมซ้ำ | BR-09 |
| `BOOK_IN_USE` | 409 | ลบหนังสือไม่ได้เพราะมีตัวเล่มถูกยืม | BR-11 |
| `ACCESS_DENIED` | 403 | ไม่มีสิทธิ์เข้าถึง | |
| `INTERNAL_ERROR` | 500 | ข้อผิดพลาดที่ไม่คาดคิด | |

---

## 5. Validation ที่ใช้ (Bean Validation)

```java
public record CreateBookRequest(
    @NotBlank(message = "ISBN ต้องไม่เป็นค่าว่าง")
    @Pattern(regexp = "\\d{10}|\\d{13}", message = "ISBN ต้องเป็นตัวเลข 10 หรือ 13 หลัก")
    String isbn,

    @NotBlank @Size(max = 200)
    String title,

    @Min(1000) @Max(2100)
    Integer publishYear,

    @NotNull Long categoryId,
    @NotNull Long publisherId,
    @NotEmpty List<Long> authorIds
) {}
```
