-- =============================================================================
-- V4__init_loan.sql
-- ตารางชุด Loan (การยืม-คืน) ของ LibraFlow — รับผิดชอบโดยสมาชิกคนที่ 2
-- อ้างอิง doc/data-dictionary.md ข้อ 9-10 และ doc/diagrams/11-er-diagram.puml
-- =============================================================================

-- ---------- 9. loans ----------
CREATE TABLE loans (
    id           BIGSERIAL    PRIMARY KEY,
    loan_code    VARCHAR(20)  NOT NULL,
    user_id      BIGINT       NOT NULL,
    librarian_id BIGINT,
    loan_date    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status       VARCHAR(20)  NOT NULL,
    CONSTRAINT uk_loans_loan_code UNIQUE (loan_code),
    CONSTRAINT fk_loans_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_loans_librarian FOREIGN KEY (librarian_id) REFERENCES users (id),
    CONSTRAINT ck_loans_status CHECK (status IN ('ACTIVE', 'OVERDUE', 'RETURNED', 'LOST'))
);

CREATE INDEX idx_loans_user_id ON loans (user_id);
CREATE INDEX idx_loans_status  ON loans (status);

-- ---------- 10. loan_items ----------
CREATE TABLE loan_items (
    id           BIGSERIAL PRIMARY KEY,
    loan_id      BIGINT    NOT NULL,
    book_copy_id BIGINT    NOT NULL,
    due_date     DATE      NOT NULL,
    returned_at  DATE,
    renew_count  SMALLINT  NOT NULL DEFAULT 0,
    -- LoanItem เป็น composition ของ Loan เมื่อลบใบยืม รายการตัวเล่มในใบยืมจะถูกลบตาม (orphanRemoval / cascade = ALL)
    CONSTRAINT fk_loan_items_loan      FOREIGN KEY (loan_id)      REFERENCES loans (id) ON DELETE CASCADE,
    CONSTRAINT fk_loan_items_book_copy FOREIGN KEY (book_copy_id) REFERENCES book_copies (id),
    -- ต่ออายุได้ไม่เกิน 2 ครั้งตาม BR-06
    CONSTRAINT ck_loan_items_renew_count CHECK (renew_count >= 0 AND renew_count <= 2)
);

CREATE INDEX idx_loan_items_loan_id      ON loan_items (loan_id);
CREATE INDEX idx_loan_items_book_copy_id ON loan_items (book_copy_id);
CREATE INDEX idx_loan_items_due_date     ON loan_items (due_date);
