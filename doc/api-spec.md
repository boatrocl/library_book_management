# LibraFlow REST API

Base path: `/api/v1`
Local API: `http://localhost:8080`
Swagger UI: `/swagger-ui.html` · OpenAPI: `/v3/api-docs`
Protected endpoints use `Authorization: Bearer <JWT>`.

This list is synchronized with the controller mappings in the repository. The success status describes the controller response; domain errors use the shared error response format below.

## Endpoint catalog

| Method | Endpoint | Access | Success | Purpose |
|---|---|---|---:|---|
| POST | `/auth/login` | Public | 200 | Authenticate and return a JWT |
| POST | `/auth/register` | Public | 200 | Register an account and profile |
| GET | `/books` | Public | 200 | Search, filter, sort, and paginate the catalog |
| GET | `/books/{id}` | Public | 200 | Read book details |
| GET | `/books/{id}/copies` | Public | 200 | List physical copies for a title |
| POST | `/books` | ADMIN, LIBRARIAN | 201 | Create a book |
| PUT | `/books/{id}` | ADMIN, LIBRARIAN | 200 | Replace book metadata |
| DELETE | `/books/{id}` | ADMIN, LIBRARIAN | 204 | Delete a book when it is not in use |
| POST | `/books/{id}/copies` | ADMIN, LIBRARIAN | 201 | Add a physical copy |
| GET | `/categories` | Public | 200 | List categories |
| GET | `/book-references` | Authenticated | 200 | Load current categories, publishers, and authors for book forms |
| POST | `/loans/self` | MEMBER | 201 | Borrow one available title; member identity comes from JWT |
| POST | `/loans` | ADMIN, LIBRARIAN | 201 | Record a counter loan using member ID and copy barcodes |
| GET | `/loans` | ADMIN, LIBRARIAN | 200 | List loans with optional status and pagination |
| GET | `/loans/{id}` | Authenticated | 200 | Read a loan by ID |
| PATCH | `/loans/{id}/return` | ADMIN, LIBRARIAN | 200 | Record return, calculate any fine, and publish the return event |
| PATCH | `/loans/{id}/renew` | ADMIN, LIBRARIAN | 200 | Renew an eligible loan |
| DELETE | `/loans/{id}` | ADMIN | 204 | Delete an incorrectly recorded loan |
| GET | `/members/{id}` | Owner, ADMIN, LIBRARIAN | 200 | Read a member profile |
| PUT | `/members/{id}` | Owner, ADMIN, LIBRARIAN | 200 | Update a member profile |
| GET | `/members/{id}/loans` | Owner, ADMIN, LIBRARIAN | 200 | Read member loan history |
| GET | `/members/{id}/fines` | Owner, ADMIN, LIBRARIAN | 200 | Read member fines; optional `status` filter |
| GET | `/reservations` | ADMIN, LIBRARIAN | 200 | List reservations with optional status and pagination |
| GET | `/reservations/self` | MEMBER | 200 | Read the signed-in member's reservation history |
| POST | `/reservations/self` | MEMBER | 201 | Join a queue for an unavailable title |
| POST | `/reservations` | MEMBER, ADMIN, LIBRARIAN | 201 | Create a reservation on behalf of a user |
| DELETE | `/reservations/{id}` | Owner, ADMIN, LIBRARIAN | 204 | Cancel an eligible reservation |
| POST | `/fines/{id}/pay` | ADMIN, LIBRARIAN | 200 | Record an in-system fine payment |
| GET | `/reports/loans?from=YYYY-MM-DD&to=YYYY-MM-DD` | ADMIN, LIBRARIAN | 200 | Download loan statistics as CSV |
| GET | `/reports/overdue` | ADMIN, LIBRARIAN | 200 | Download overdue items as PDF |
| GET | `/users` | ADMIN | 200 | List user accounts |
| PATCH | `/users/{id}/status` | ADMIN | 200 | Activate or suspend a user |
| PATCH | `/users/{id}/role` | ADMIN | 200 | Change a user's role |

The profile and history endpoints accept the member ID in the path. The backend allows access to the owner or staff roles; another member receives `403 ACCESS_DENIED`. User role and account-status changes have one canonical route under `/users`, handled by `UserManagementService`.

## Query parameters

- `GET /books`: `keyword`, `categoryId`, `availability=ALL|AVAILABLE|UNAVAILABLE`, `page`, `size`, and `sort`. Defaults are page 0, size 10, sorted by title ascending.
- `GET /loans`: optional `status`, `page`, `size`, and `sort`.
- `GET /members/{id}/fines`: optional `status=UNPAID|PAID|WAIVED`.
- Reservation list endpoints: optional `status`, `page`, `size`, and `sort`.

## Self-service request examples

### Borrow a title

`POST /api/v1/loans/self`

```json
{
  "bookId": 18,
  "termsAccepted": true
}
```

The member is identified from the JWT. The backend selects and locks an available copy. A missing or false `termsAccepted` value fails validation. A member cannot borrow a title that they already have on loan.

### Join a reservation queue

`POST /api/v1/reservations/self`

```json
{
  "bookId": 18,
  "termsAccepted": true
}
```

The title must have no available copy. The backend rejects duplicate active reservations and titles already on loan to the same member.

### Counter loan

`POST /api/v1/loans`

```json
{
  "memberId": 12,
  "barcodes": ["LIB-00231", "LIB-00842"]
}
```

## Error responses

Validation, business-rule, authorization, and not-found errors use the shared `ErrorResponse` shape. Common status codes are:

| HTTP | Meaning |
|---:|---|
| 400 | Request validation failed |
| 401 | Authentication is missing or invalid |
| 403 | Role or member ownership check denied the request |
| 404 | Requested resource does not exist |
| 409 | A business rule prevents the operation |
| 500 | Unexpected server error |

Example:

```json
{
  "timestamp": "2026-10-10T08:00:00Z",
  "status": 409,
  "error": "Conflict",
  "errorCode": "COPY_NOT_AVAILABLE",
  "message": "ตัวเล่มไม่พร้อมให้ยืม",
  "path": "/api/v1/loans/self",
  "fieldErrors": []
}
```

There is no `POST /fines/{id}/waive` controller mapping. `WAIVED` is a persisted fine status, but the current API does not provide a waive operation. Report formats are selected by their routes; they do not use a `format` query parameter.
