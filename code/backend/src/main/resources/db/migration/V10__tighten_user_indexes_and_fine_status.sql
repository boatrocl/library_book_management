-- V3 added standalone indexes for username/email even though UNIQUE constraints
-- already create indexes for both columns. Remove only the redundant indexes.
DROP INDEX IF EXISTS idx_users_username;
DROP INDEX IF EXISTS idx_users_email;

-- Guard the new check so an unexpected legacy value fails with a useful message.
DO $$
DECLARE
    invalid_status_count BIGINT;
BEGIN
    SELECT COUNT(*) INTO invalid_status_count
    FROM fines
    WHERE status NOT IN ('UNPAID', 'PAID', 'WAIVED');

    IF invalid_status_count > 0 THEN
        RAISE EXCEPTION 'V10 blocked: % fines rows have an unsupported status', invalid_status_count;
    END IF;
END $$;

ALTER TABLE fines
    ADD CONSTRAINT ck_fines_status
        CHECK (status IN ('UNPAID', 'PAID', 'WAIVED')) NOT VALID;

ALTER TABLE fines
    VALIDATE CONSTRAINT ck_fines_status;
