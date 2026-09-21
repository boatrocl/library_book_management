-- V3__init_users.sql
-- สร้างตาราง users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- สร้าง Index เพื่อการค้นหาที่เร็วขึ้น (ตามเกณฑ์ใบงานข้อ 6)
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);

-- สร้างตาราง user_profiles (ความสัมพันธ์ One-to-One กับ users)
CREATE TABLE user_profiles (
    user_id BIGINT PRIMARY KEY, -- เป็นทั้ง Primary Key และ Foreign Key (One-to-One)
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    address TEXT,
    CONSTRAINT fk_user_profile_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE
);

-- ใส่ข้อมูลตัวอย่าง (Seed Data)
INSERT INTO users (username, password_hash, email, is_active) 
VALUES ('admin', '$2a$10$dummyhashpassword', 'admin@libraflow.com', TRUE);

INSERT INTO user_profiles (user_id, first_name, last_name, phone_number, address) 
VALUES (1, 'Admin', 'Libraflow', '0812345678', '123 Library St.');