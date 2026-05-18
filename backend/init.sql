-- Create Database
SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS courseselection CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE courseselection;

-- 1. Major Table
CREATE TABLE IF NOT EXISTS major (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL
);

-- 2. Student Table
CREATE TABLE IF NOT EXISTS student (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_number VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    gender VARCHAR(10) NOT NULL,
    birth_date DATE,
    hometown VARCHAR(100),
    class_name VARCHAR(50),
    major_id BIGINT,
    FOREIGN KEY (major_id) REFERENCES major(id)
);

-- 3. Course Table
CREATE TABLE IF NOT EXISTS course (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_number VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    credits INT NOT NULL
);

-- 4. Selection Table (Student Course Selection)
CREATE TABLE IF NOT EXISTS selection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    grade DOUBLE,
    FOREIGN KEY (student_id) REFERENCES student(id),
    FOREIGN KEY (course_id) REFERENCES course(id),
    UNIQUE KEY unique_selection (student_id, course_id)
);

-- 5. Admin Table
CREATE TABLE IF NOT EXISTS admin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL
);

INSERT INTO admin (username, password) SELECT 'admin', '123456' WHERE NOT EXISTS (SELECT * FROM admin WHERE username = 'admin');

-- 6. Insert Test Data (Only if tables are empty)
-- We use a simple trick with INSERT IGNORE or checking existence, but since this is init.sql for docker, 
-- it runs only once on volume creation. So we can just insert.

-- Insert Majors
INSERT INTO major (name, department) SELECT '计算机科学与技术', '计算机学院' WHERE NOT EXISTS (SELECT * FROM major WHERE name = '计算机科学与技术');
INSERT INTO major (name, department) SELECT '软件工程', '软件学院' WHERE NOT EXISTS (SELECT * FROM major WHERE name = '软件工程');

-- Insert Courses
INSERT INTO course (course_number, name, credits) SELECT 'CS101', '数据库原理', 3 WHERE NOT EXISTS (SELECT * FROM course WHERE course_number = 'CS101');
INSERT INTO course (course_number, name, credits) SELECT 'CS102', '操作系统', 4 WHERE NOT EXISTS (SELECT * FROM course WHERE course_number = 'CS102');

-- Insert Students (Assuming IDs 1 and 2 for majors, but better to subquery)
INSERT INTO student (student_number, name, gender, birth_date, hometown, class_name, major_id) 
SELECT '2023001', '张三', '男', '2000-01-01', '北京', '计科1班', (SELECT id FROM major WHERE name = '计算机科学与技术') 
WHERE NOT EXISTS (SELECT * FROM student WHERE student_number = '2023001');

INSERT INTO student (student_number, name, gender, birth_date, hometown, class_name, major_id) 
SELECT '2023002', '李四', '女', '2000-02-02', '上海', '软件1班', (SELECT id FROM major WHERE name = '软件工程') 
WHERE NOT EXISTS (SELECT * FROM student WHERE student_number = '2023002');

-- Insert Selections
INSERT INTO selection (student_id, course_id, grade) 
SELECT (SELECT id FROM student WHERE student_number = '2023001'), (SELECT id FROM course WHERE course_number = 'CS101'), NULL 
WHERE NOT EXISTS (SELECT * FROM selection WHERE student_id = (SELECT id FROM student WHERE student_number = '2023001') AND course_id = (SELECT id FROM course WHERE course_number = 'CS101'));

INSERT INTO selection (student_id, course_id, grade) 
SELECT (SELECT id FROM student WHERE student_number = '2023002'), (SELECT id FROM course WHERE course_number = 'CS102'), NULL 
WHERE NOT EXISTS (SELECT * FROM selection WHERE student_id = (SELECT id FROM student WHERE student_number = '2023002') AND course_id = (SELECT id FROM course WHERE course_number = 'CS102'));


-- 7. Stored Procedure: Calculate Total and Average Grade for a Class
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS CalculateClassStats(IN classNameParam VARCHAR(50))
BEGIN
    SELECT 
        s.student_number AS studentNumber,
        s.name AS name,
        SUM(sel.grade) AS totalGrade,
        AVG(sel.grade) AS averageGrade
    FROM 
        student s
    LEFT JOIN 
        selection sel ON s.id = sel.student_id
    WHERE 
        s.class_name = classNameParam
    GROUP BY 
        s.id, s.student_number, s.name;
END //
DELIMITER ;

-- 8. View: Students needing makeup exams (Grade < 60) by Major
CREATE OR REPLACE VIEW makeup_exam_view AS
SELECT 
    s.student_number,
    s.class_name,
    s.name AS student_name,
    c.name AS course_name,
    sel.grade,
    m.name AS major_name
FROM 
    selection sel
JOIN 
    student s ON sel.student_id = s.id
JOIN 
    course c ON sel.course_id = c.id
JOIN 
    major m ON s.major_id = m.id
WHERE 
    sel.grade < 60;

-- 9. Trigger: Delete course -> Delete related selection records
DELIMITER //
CREATE TRIGGER before_course_delete
BEFORE DELETE ON course
FOR EACH ROW
BEGIN
    DELETE FROM selection WHERE course_id = OLD.id;
END //
DELIMITER ;
