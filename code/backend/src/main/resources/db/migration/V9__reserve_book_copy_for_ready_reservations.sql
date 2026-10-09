-- Associate READY queue entries with the physical copy held for pickup.
ALTER TABLE reservations
    ADD COLUMN reserved_copy_id BIGINT;

-- Older versions marked reservations READY without holding a copy. Requeue those
-- rows so the current listener can assign a real copy and a fresh pickup window.
UPDATE reservations
SET status = 'WAITING', expires_at = NULL
WHERE status = 'READY';

ALTER TABLE reservations
    ADD CONSTRAINT fk_reservations_reserved_copy
        FOREIGN KEY (reserved_copy_id) REFERENCES book_copies (id);

ALTER TABLE reservations
    ADD CONSTRAINT ck_reservations_ready_has_copy_and_expiry
        CHECK (status <> 'READY' OR (reserved_copy_id IS NOT NULL AND expires_at IS NOT NULL));

CREATE INDEX idx_reservations_reserved_copy_id
    ON reservations (reserved_copy_id);

CREATE UNIQUE INDEX uk_reservations_ready_copy
    ON reservations (reserved_copy_id)
    WHERE status = 'READY' AND reserved_copy_id IS NOT NULL;

CREATE INDEX idx_reservations_ready_expiry
    ON reservations (expires_at)
    WHERE status = 'READY' AND expires_at IS NOT NULL;
