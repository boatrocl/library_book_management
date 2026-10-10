# LibraFlow — Project Overview

## Purpose and scope

LibraFlow is a web application for managing a library catalog and circulation. It models a book title separately from each physical copy and supports member self-service, staff circulation, reservations, fines, and reports.

### In scope

- Book, copy, author, publisher, category, and member profile management
- Member borrowing and reservation queues
- Staff checkout, returns, renewal, and fine-payment recording
- Tier-based borrowing limits, loan periods, and fine rates
- Loan and overdue reports in CSV and PDF
- JWT authentication and role-based access

### Out of scope or not implemented

- Online payment processing
- External email or SMS delivery; reservation notifications currently go to application logs
- Staff workflow to mark a returned copy damaged, repair it, or dispose of it
- Runtime configuration UI for loan and fine policy; the current policy is defined in code

## Actors

| Role | Implemented responsibilities |
|---|---|
| MEMBER | Browse books, self-borrow available titles, reserve unavailable titles, cancel eligible reservations, and read their own profile, loan history, and fines |
| LIBRARIAN | Manage books and copies, record checkout/return/renewal, record fine payments, view member history, and export reports |
| ADMIN | Use staff catalog and circulation operations, manage user roles/status, and export reports |

The roles do not inherit self-service permissions: the self-borrow and self-reservation endpoints require the MEMBER role. Administrative user changes use `/api/v1/users` and `UserManagementService`.

## Business rules

| ID | Rule |
|---|---|
| BR-01 | Suspended members cannot borrow. Account status is stored as the `users.is_active` boolean. |
| BR-02 | Borrowing is blocked when unpaid fines exceed 100 baht. |
| BR-03 | Concurrent-loan limits: STUDENT 5, STAFF 10, EXTERNAL 2. |
| BR-04 | A copy must be AVAILABLE, or RESERVED for the member with an unexpired READY reservation. |
| BR-05 | Loan periods: STUDENT 7 days, STAFF 14 days, EXTERNAL 3 days. |
| BR-06 | Each loan item can be renewed at most twice; renewal is blocked while a title has a WAITING reservation. |
| BR-07 | Fine rates: STUDENT 3 baht/day, STAFF 5 baht/day (capped at 300), EXTERNAL 10 baht/day. The fine is recorded when an overdue item is returned; the scheduler does not increment a daily balance. |
| BR-08 | More than 60 overdue days moves the loan/copy to LOST and records the book-price replacement charge. |
| BR-09 | A member cannot have duplicate active reservations or borrow/reserve a title that they still have on loan; duplicate titles in one checkout are rejected. |
| BR-10 | On return, the first waiting reservation is offered the copy as READY for 48 hours. This is an in-system workflow; no external message is sent. |
| BR-11 | A book cannot be deleted while any copy is ON_LOAN or RESERVED. |
| BR-12 | Self-service borrowing and reservation requests must include `termsAccepted: true`. |

## Domain vocabulary

- **Book**: a catalog title identified by ISBN.
- **BookCopy**: one physical copy with its own barcode and circulation status.
- **Loan**: one checkout transaction for a member.
- **LoanItem**: one copy and its due/return dates within a loan.
- **Reservation**: a member's queue entry for a title.
- **Fine**: a charge associated with a loan item.

## Implemented enums

| Enum | Values / meaning |
|---|---|
| `UserRole` | ADMIN, LIBRARIAN, MEMBER |
| `MemberTier` | STUDENT, STAFF, EXTERNAL |
| `BookAvailabilityFilter` | ALL, AVAILABLE, UNAVAILABLE |
| `BookCopyStatus` | AVAILABLE, ON_LOAN, RESERVED, DAMAGED, LOST |
| `LoanStatus` | ACTIVE, OVERDUE, RETURNED, LOST |
| `ReservationStatus` | WAITING, READY, FULFILLED, CANCELLED, EXPIRED |
| `FineStatus` | UNPAID, PAID, WAIVED |

Account activity is represented by `User.isActive`, not a `UserStatus` enum. Although DAMAGED is a valid stored copy status, there is no current return-condition endpoint or repair/disposal workflow. WAIVED is a valid fine status in the schema, but there is no API endpoint to waive a fine.

## Backend package map

```text
com.libraflow.library
├── controller/api/    REST endpoints
├── service/           service interfaces, schedulers, event listeners
├── service/impl/      business logic
├── repository/        Spring Data JPA persistence
├── domain/entity/     JPA entities
├── domain/enums/      persisted and query enums
├── dto/request/       validated API requests
├── dto/response/      API responses
├── mapper/            entity-to-response mapping
├── security/          JWT and endpoint authorization
├── pattern/           chain, state, observer, report template
└── common/            shared policy and helpers
```

## Supporting documents

- [REST API](api-spec.md)
- [Design patterns](design-patterns.md)
- [Data dictionary](data-dictionary.md)
- [SOLID analysis](solid-analysis.md)
- [Diagrams and render instructions](diagrams/README.md)
- [Security, CI, and deployment notes](security-ci-deployment.md)
