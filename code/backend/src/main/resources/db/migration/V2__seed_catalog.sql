-- =============================================================================
-- V2__seed_catalog.sql
-- ข้อมูลตัวอย่างของตารางชุด catalog สำหรับทดสอบระบบและสาธิตตอนนำเสนอ
--
-- เจตนาของชุดข้อมูลนี้
--   - มีหนังสือที่ตัวเล่มถูกยืมหมด (availableCopies = 0) เพื่อให้สมาชิกคนที่ 3
--     ทดสอบการจองคิวได้ (UC04 / BR-09)
--   - มีตัวเล่มครบทุกสถานะ เพื่อทดสอบ CopyAvailabilityRule (BR-04)
--     และกฎห้ามลบหนังสือที่ยังถูกยืม (BR-11)
-- =============================================================================

INSERT INTO categories (name, description) VALUES
    ('Software Engineering',    'วิศวกรรมซอฟต์แวร์ การออกแบบและพัฒนาระบบ'),
    ('Programming Languages',   'ภาษาโปรแกรมและการเขียนโปรแกรม'),
    ('Database',                'ฐานข้อมูลและการจัดการข้อมูล'),
    ('Computer Networks',       'ระบบเครือข่ายคอมพิวเตอร์'),
    ('Artificial Intelligence', 'ปัญญาประดิษฐ์และการเรียนรู้ของเครื่อง'),
    ('Mathematics',             'คณิตศาสตร์สำหรับวิทยาการคอมพิวเตอร์'),
    ('Literature',              'วรรณกรรมและเรื่องแต่ง'),
    ('Business',                'ธุรกิจและการจัดการ');

INSERT INTO publishers (name, country) VALUES
    ('Prentice Hall',        'United States'),
    ('Addison-Wesley',       'United States'),
    ('O''Reilly Media',      'United States'),
    ('Manning Publications', 'United States'),
    ('Packt Publishing',     'United Kingdom'),
    ('No Starch Press',      'United States'),
    ('MIT Press',            'United States'),
    ('ซีเอ็ดยูเคชั่น',           'Thailand');

INSERT INTO authors (full_name, nationality, biography) VALUES
    ('Robert C. Martin', 'American',   'ผู้เขียน Clean Code และผู้ร่วมร่าง Agile Manifesto'),
    ('Martin Fowler',    'British',    'นักเขียนและที่ปรึกษาด้านสถาปัตยกรรมซอฟต์แวร์'),
    ('Erich Gamma',      'Swiss',      'หนึ่งใน Gang of Four ผู้เขียน Design Patterns'),
    ('Richard Helm',     'Australian', 'หนึ่งใน Gang of Four'),
    ('Ralph Johnson',    'American',   'หนึ่งใน Gang of Four'),
    ('John Vlissides',   'American',   'หนึ่งใน Gang of Four'),
    ('Joshua Bloch',     'American',   'ผู้เขียน Effective Java และผู้ออกแบบ Java Collections Framework'),
    ('Kent Beck',        'American',   'ผู้ริเริ่ม Extreme Programming และ Test-Driven Development'),
    ('Andrew Hunt',      'American',   'ผู้ร่วมเขียน The Pragmatic Programmer'),
    ('David Thomas',     'British',    'ผู้ร่วมเขียน The Pragmatic Programmer'),
    ('Craig Walls',      'American',   'ผู้เขียน Spring in Action'),
    ('Vlad Mihalcea',    'Romanian',   'ผู้เชี่ยวชาญด้าน Hibernate และการปรับจูนฐานข้อมูล');

-- อ้าง id ของ category/publisher ด้วย subquery จากชื่อ เพื่อไม่ต้องผูกกับลำดับของ BIGSERIAL
INSERT INTO books (isbn, title, publish_year, price, category_id, publisher_id)
SELECT v.isbn, v.title, v.publish_year, v.price, c.id, p.id
FROM (VALUES
    ('9780132350884', 'Clean Code: A Handbook of Agile Software Craftsmanship',   2008, 1650.00, 'Software Engineering',    'Prentice Hall'),
    ('9780134494166', 'Clean Architecture: A Craftsman''s Guide to Software Structure and Design', 2017, 1750.00, 'Software Engineering', 'Prentice Hall'),
    ('9780201633610', 'Design Patterns: Elements of Reusable Object-Oriented Software', 1994, 2100.00, 'Software Engineering', 'Addison-Wesley'),
    ('9780134757599', 'Refactoring: Improving the Design of Existing Code',        2018, 1980.00, 'Software Engineering',    'Addison-Wesley'),
    ('9780134685991', 'Effective Java',                                           2018, 1850.00, 'Programming Languages',   'Addison-Wesley'),
    ('9780135957059', 'The Pragmatic Programmer: Your Journey to Mastery',         2019, 1890.00, 'Software Engineering',    'Addison-Wesley'),
    ('9781617297571', 'Spring in Action, Sixth Edition',                          2022, 2250.00, 'Programming Languages',   'Manning Publications'),
    ('9781617294945', 'Spring Boot in Action',                                    2016, 1700.00, 'Programming Languages',   'Manning Publications'),
    ('9789730228236', 'High-Performance Java Persistence',                        2016, 2400.00, 'Database',                'Packt Publishing'),
    ('9780596009205', 'Head First Design Patterns',                               2004, 1620.00, 'Software Engineering',    'O''Reilly Media'),
    ('9781449373320', 'Designing Data-Intensive Applications',                    2017, 2350.00, 'Database',                'O''Reilly Media'),
    ('9780596517748', 'JavaScript: The Good Parts',                               2008, 1180.00, 'Programming Languages',   'O''Reilly Media'),
    ('9781593279509', 'Eloquent JavaScript, 3rd Edition',                         2018, 1350.00, 'Programming Languages',   'No Starch Press'),
    ('9780262033848', 'Introduction to Algorithms, 3rd Edition',                  2009, 3200.00, 'Mathematics',             'MIT Press'),
    ('9780262035613', 'Deep Learning',                                            2016, 2900.00, 'Artificial Intelligence', 'MIT Press'),
    ('9781492032649', 'Kubernetes: Up and Running',                               2019, 1950.00, 'Computer Networks',       'O''Reilly Media'),
    ('9780134610993', 'Computer Networking: A Top-Down Approach',                 2016, 2600.00, 'Computer Networks',       'Prentice Hall'),
    ('9780321751041', 'Test-Driven Development by Example',                       2002, 1560.00, 'Software Engineering',    'Addison-Wesley'),
    ('9786160840175', 'อ่านโค้ดให้เป็น เขียนโค้ดให้สวย',                              2021,  395.00, 'Programming Languages',   'ซีเอ็ดยูเคชั่น'),
    ('9786160841257', 'พื้นฐานการจัดการฐานข้อมูล',                                    2022,  420.00, 'Database',                'ซีเอ็ดยูเคชั่น')
) AS v(isbn, title, publish_year, price, category_name, publisher_name)
JOIN categories c ON c.name = v.category_name
JOIN publishers p ON p.name = v.publisher_name;

-- Design Patterns มีผู้แต่ง 4 คน (Gang of Four) เป็นเคสที่ใช้สาธิต Many-to-Many ได้ชัดที่สุด
INSERT INTO book_authors (book_id, author_id)
SELECT b.id, a.id
FROM (VALUES
    ('9780132350884', 'Robert C. Martin'),
    ('9780134494166', 'Robert C. Martin'),
    ('9780201633610', 'Erich Gamma'),
    ('9780201633610', 'Richard Helm'),
    ('9780201633610', 'Ralph Johnson'),
    ('9780201633610', 'John Vlissides'),
    ('9780134757599', 'Martin Fowler'),
    ('9780134685991', 'Joshua Bloch'),
    ('9780135957059', 'Andrew Hunt'),
    ('9780135957059', 'David Thomas'),
    ('9781617297571', 'Craig Walls'),
    ('9781617294945', 'Craig Walls'),
    ('9789730228236', 'Vlad Mihalcea'),
    ('9780321751041', 'Kent Beck')
) AS v(isbn, full_name)
JOIN books b   ON b.isbn = v.isbn
JOIN authors a ON a.full_name = v.full_name;

-- Clean Code    : 5 เล่ม (3 AVAILABLE, 1 ON_LOAN, 1 RESERVED) -> ใช้ทดสอบ BR-11 ตอนลบหนังสือ
-- Design Patterns: ถูกยืมหมดทั้ง 2 เล่ม   -> availableCopies = 0 ใช้ทดสอบการจองคิว UC04
INSERT INTO book_copies (barcode, book_id, status, shelf_location, acquired_at)
SELECT v.barcode, b.id, v.status, v.shelf_location, v.acquired_at
FROM (VALUES
    ('LIB-00001', '9780132350884', 'AVAILABLE', 'A1-01', DATE '2024-01-15'),
    ('LIB-00002', '9780132350884', 'AVAILABLE', 'A1-01', DATE '2024-01-15'),
    ('LIB-00003', '9780132350884', 'AVAILABLE', 'A1-02', DATE '2024-03-20'),
    ('LIB-00004', '9780132350884', 'ON_LOAN',   'A1-02', DATE '2024-03-20'),
    ('LIB-00005', '9780132350884', 'RESERVED',  'A1-02', DATE '2024-03-20'),
    ('LIB-00011', '9780134494166', 'AVAILABLE', 'A1-03', DATE '2024-02-10'),
    ('LIB-00012', '9780134494166', 'AVAILABLE', 'A1-03', DATE '2024-02-10'),
    ('LIB-00021', '9780201633610', 'ON_LOAN',   'A2-01', DATE '2023-11-05'),
    ('LIB-00022', '9780201633610', 'ON_LOAN',   'A2-01', DATE '2023-11-05'),
    ('LIB-00031', '9780134757599', 'AVAILABLE', 'A2-02', DATE '2024-05-01'),
    ('LIB-00032', '9780134757599', 'AVAILABLE', 'A2-02', DATE '2024-05-01'),
    ('LIB-00033', '9780134757599', 'DAMAGED',   'A2-02', DATE '2024-05-01'),
    ('LIB-00041', '9780134685991', 'AVAILABLE', 'B1-01', DATE '2024-04-12'),
    ('LIB-00042', '9780134685991', 'AVAILABLE', 'B1-01', DATE '2024-04-12'),
    ('LIB-00043', '9780134685991', 'ON_LOAN',   'B1-01', DATE '2024-04-12'),
    ('LIB-00051', '9780135957059', 'AVAILABLE', 'A2-03', DATE '2024-06-18'),
    ('LIB-00052', '9780135957059', 'AVAILABLE', 'A2-03', DATE '2024-06-18'),
    ('LIB-00061', '9781617297571', 'AVAILABLE', 'B1-02', DATE '2024-07-22'),
    ('LIB-00062', '9781617297571', 'AVAILABLE', 'B1-02', DATE '2024-07-22'),
    ('LIB-00063', '9781617297571', 'AVAILABLE', 'B1-02', DATE '2024-07-22'),
    ('LIB-00071', '9781617294945', 'AVAILABLE', 'B1-03', DATE '2023-09-30'),
    ('LIB-00081', '9789730228236', 'AVAILABLE', 'C1-01', DATE '2024-02-28'),
    ('LIB-00082', '9789730228236', 'ON_LOAN',   'C1-01', DATE '2024-02-28'),
    ('LIB-00091', '9780596009205', 'AVAILABLE', 'A3-01', DATE '2023-08-14'),
    ('LIB-00092', '9780596009205', 'AVAILABLE', 'A3-01', DATE '2023-08-14'),
    ('LIB-00101', '9781449373320', 'AVAILABLE', 'C1-02', DATE '2024-03-05'),
    ('LIB-00102', '9781449373320', 'AVAILABLE', 'C1-02', DATE '2024-03-05'),
    ('LIB-00111', '9780596517748', 'AVAILABLE', 'B2-01', DATE '2023-07-19'),
    ('LIB-00121', '9781593279509', 'AVAILABLE', 'B2-02', DATE '2024-01-08'),
    ('LIB-00122', '9781593279509', 'AVAILABLE', 'B2-02', DATE '2024-01-08'),
    ('LIB-00131', '9780262033848', 'AVAILABLE', 'D1-01', DATE '2023-06-25'),
    ('LIB-00132', '9780262033848', 'LOST',      'D1-01', DATE '2023-06-25'),
    ('LIB-00141', '9780262035613', 'AVAILABLE', 'E1-01', DATE '2024-08-11'),
    ('LIB-00151', '9781492032649', 'AVAILABLE', 'F1-01', DATE '2024-09-02'),
    ('LIB-00161', '9780134610993', 'AVAILABLE', 'F1-02', DATE '2023-12-17'),
    ('LIB-00171', '9780321751041', 'AVAILABLE', 'A3-02', DATE '2024-04-30'),
    ('LIB-00181', '9786160840175', 'AVAILABLE', 'G1-01', DATE '2024-10-05'),
    ('LIB-00182', '9786160840175', 'AVAILABLE', 'G1-01', DATE '2024-10-05'),
    ('LIB-00191', '9786160841257', 'AVAILABLE', 'G1-02', DATE '2024-10-05')
) AS v(barcode, isbn, status, shelf_location, acquired_at)
JOIN books b ON b.isbn = v.isbn;
