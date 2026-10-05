-- V5__init_fine_reservation.sql
-- Create fines table
CREATE TABLE fines (
    id BIGSERIAL PRIMARY KEY,
    loan_item_id BIGINT NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    overdue_days INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP,
    CONSTRAINT uk_fines_loan_item UNIQUE (loan_item_id)
);

-- Create reservations table
CREATE TABLE reservations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    reserved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    status VARCHAR(20) NOT NULL
);

-- Indexes for reservations
CREATE INDEX idx_reservations_user_id ON reservations(user_id);
CREATE INDEX idx_reservations_book_id ON reservations(book_id);

-- Partial Unique Index for active reservations (BR-09)
CREATE UNIQUE INDEX uk_reservation_active ON reservations(user_id, book_id)
WHERE status IN ('WAITING', 'READY');
