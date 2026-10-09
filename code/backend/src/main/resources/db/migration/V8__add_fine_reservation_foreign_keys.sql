-- Add the foreign keys omitted from V5 without changing an applied migration.
-- Existing orphan rows must be corrected manually before this migration can run.
DO $$
DECLARE
    orphan_count BIGINT;
BEGIN
    SELECT COUNT(*) INTO orphan_count
    FROM fines f
    LEFT JOIN loan_items li ON li.id = f.loan_item_id
    WHERE li.id IS NULL;

    IF orphan_count > 0 THEN
        RAISE EXCEPTION 'V8 blocked: % fines rows reference missing loan_items rows', orphan_count;
    END IF;

    SELECT COUNT(*) INTO orphan_count
    FROM reservations r
    LEFT JOIN users u ON u.id = r.user_id
    WHERE u.id IS NULL;

    IF orphan_count > 0 THEN
        RAISE EXCEPTION 'V8 blocked: % reservations rows reference missing users rows', orphan_count;
    END IF;

    SELECT COUNT(*) INTO orphan_count
    FROM reservations r
    LEFT JOIN books b ON b.id = r.book_id
    WHERE b.id IS NULL;

    IF orphan_count > 0 THEN
        RAISE EXCEPTION 'V8 blocked: % reservations rows reference missing books rows', orphan_count;
    END IF;
END $$;

ALTER TABLE fines
    ADD CONSTRAINT fk_fines_loan_item
        FOREIGN KEY (loan_item_id) REFERENCES loan_items (id);

ALTER TABLE reservations
    ADD CONSTRAINT fk_reservations_user
        FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE reservations
    ADD CONSTRAINT fk_reservations_book
        FOREIGN KEY (book_id) REFERENCES books (id);
