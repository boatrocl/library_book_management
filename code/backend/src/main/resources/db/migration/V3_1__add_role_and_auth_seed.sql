-- V3_1__add_role_and_auth_seed.sql
-- Security extension owned by member 5.
-- V4-V6 are reserved for other team modules, so this migration is version 3.1.

ALTER TABLE users
    ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'MEMBER';

ALTER TABLE users
    ADD CONSTRAINT chk_users_role
        CHECK (role IN ('ADMIN', 'LIBRARIAN', 'MEMBER'));

-- Password: Admin@123
UPDATE users
SET password_hash = '$2y$10$W2extLbXApePxXHwkyXcy.6.oGoZB78mmOFH9qZ3kycav9n544FGa',
    role = 'ADMIN',
    is_active = TRUE
WHERE username = 'admin';

-- Password: Lib@123
INSERT INTO users (
    username,
    password_hash,
    email,
    role,
    is_active
)
VALUES (
    'librarian01',
    '$2y$10$AuSp/s2GhpV8w4ei4iMQIexn04jFiBerLUG1Eil7xRc/LW3vkIlCa',
    'librarian01@libraflow.com',
    'LIBRARIAN',
    TRUE
)
ON CONFLICT (username) DO UPDATE
SET password_hash = EXCLUDED.password_hash,
    email = EXCLUDED.email,
    role = EXCLUDED.role,
    is_active = TRUE;

-- Password: Mem@123
INSERT INTO users (
    username,
    password_hash,
    email,
    role,
    is_active
)
VALUES (
    'member01',
    '$2y$10$ZpTkyrExLE6Po0oAufgWlOm9HIQJwy5axh.nAqskcMOniZh/cHNB.',
    'member01@libraflow.com',
    'MEMBER',
    TRUE
)
ON CONFLICT (username) DO UPDATE
SET password_hash = EXCLUDED.password_hash,
    email = EXCLUDED.email,
    role = EXCLUDED.role,
    is_active = TRUE;

INSERT INTO user_profiles (
    user_id,
    first_name,
    last_name,
    phone_number,
    address
)
SELECT
    id,
    'Library',
    'Staff',
    NULL,
    NULL
FROM users
WHERE username = 'librarian01'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO user_profiles (
    user_id,
    first_name,
    last_name,
    phone_number,
    address
)
SELECT
    id,
    'Library',
    'Member',
    NULL,
    NULL
FROM users
WHERE username = 'member01'
ON CONFLICT (user_id) DO NOTHING;
