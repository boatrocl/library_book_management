-- =============================================================================
-- V1__init_catalog.sql
-- ตารางชุด catalog ของ LibraFlow — รับผิดชอบโดยสมาชิกคนที่ 1
-- อ้างอิง doc/data-dictionary.md ข้อ 3-8 และ doc/diagrams/11-er-diagram.puml
--
-- หมายเหตุ: ไฟล์ migration ที่ merge เข้า develop แล้วห้ามแก้ซ้ำ
-- เพราะ Flyway เก็บ checksum ไว้ ถ้าเนื้อหาเปลี่ยนจะ fail ตอน startup
-- ต้องการแก้ schema ให้เพิ่มไฟล์ V ถัดไปแทน
-- =============================================================================

-- ---------- 3. categories ----------
CREATE TABLE categories (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(80)  NOT NULL,
    description VARCHAR(255),
    CONSTRAINT uk_categories_name UNIQUE (name)
);

-- ---------- 4. publishers ----------
CREATE TABLE publishers (
    id      BIGSERIAL     PRIMARY KEY,
    name    VARCHAR(120)  NOT NULL,
    country VARCHAR(60),
    CONSTRAINT uk_publishers_name UNIQUE (name)
);

-- ---------- 5. authors ----------
CREATE TABLE authors (
    id          BIGSERIAL     PRIMARY KEY,
    full_name   VARCHAR(120)  NOT NULL,
    nationality VARCHAR(60),
    biography   TEXT
);

-- ชื่อผู้แต่งซ้ำกันได้ (คนละคนชื่อเหมือนกัน) จึงเป็น index ไม่ใช่ unique
CREATE INDEX idx_authors_full_name ON authors (full_name);

-- ---------- 6. books ----------
CREATE TABLE books (
    id           BIGSERIAL     PRIMARY KEY,
    isbn         VARCHAR(20)   NOT NULL,
    title        VARCHAR(200)  NOT NULL,
    publish_year INT,
    -- NUMERIC ไม่ใช่ DOUBLE PRECISION เพราะเป็นข้อมูลเงิน ใช้คิดค่าชดใช้ตาม BR-08
    price        NUMERIC(10,2),
    category_id  BIGINT        NOT NULL,
    publisher_id BIGINT        NOT NULL,
    created_at   TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT uk_books_isbn UNIQUE (isbn),
    -- ไม่ใช้ ON DELETE CASCADE เพราะลบหมวดหมู่ต้องไม่ลบหนังสือทิ้งไปด้วย
    -- ถ้ายังมีหนังสืออ้างอยู่ PostgreSQL จะปฏิเสธการลบให้เอง
    CONSTRAINT fk_books_category  FOREIGN KEY (category_id)  REFERENCES categories (id),
    CONSTRAINT fk_books_publisher FOREIGN KEY (publisher_id) REFERENCES publishers (id)
);

-- รองรับการค้นหาด้วย LIKE ตาม UC02
CREATE INDEX idx_books_title        ON books (title);
CREATE INDEX idx_books_category_id  ON books (category_id);
CREATE INDEX idx_books_publisher_id ON books (publisher_id);

-- ---------- 7. book_authors (Many-to-Many) ----------
CREATE TABLE book_authors (
    book_id   BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    -- composite PK กันผู้แต่งคนเดิมซ้ำในหนังสือเล่มเดียวกันที่ระดับฐานข้อมูล
    CONSTRAINT pk_book_authors PRIMARY KEY (book_id, author_id),
    CONSTRAINT fk_book_authors_book   FOREIGN KEY (book_id)   REFERENCES books (id)   ON DELETE CASCADE,
    CONSTRAINT fk_book_authors_author FOREIGN KEY (author_id) REFERENCES authors (id)
);

-- PK ครอบ (book_id, author_id) อยู่แล้ว จึงเพิ่ม index ฝั่ง author_id
-- เพื่อให้ค้นย้อนจากผู้แต่งได้เร็วด้วย
CREATE INDEX idx_book_authors_author_id ON book_authors (author_id);

-- ---------- 8. book_copies ----------
CREATE TABLE book_copies (
    id             BIGSERIAL    PRIMARY KEY,
    barcode        VARCHAR(30)  NOT NULL,
    book_id        BIGINT       NOT NULL,
    status         VARCHAR(20)  NOT NULL,
    shelf_location VARCHAR(30),
    acquired_at    DATE         NOT NULL,
    CONSTRAINT uk_book_copies_barcode UNIQUE (barcode),
    CONSTRAINT fk_book_copies_book FOREIGN KEY (book_id) REFERENCES books (id),
    CONSTRAINT ck_book_copies_status CHECK (status IN ('AVAILABLE', 'ON_LOAN', 'RESERVED', 'DAMAGED', 'LOST'))
);

-- composite index เพราะ query จริงคือ "หาตัวเล่มว่างของหนังสือเล่มนี้"
-- ซึ่งใช้ book_id กับ status พร้อมกันเสมอ
CREATE INDEX idx_copies_book_status ON book_copies (book_id, status);
