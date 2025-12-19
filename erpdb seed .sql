CREATE DATABASE IF NOT EXISTS erp_db;
USE erp_db;
CREATE TABLE IF NOT EXISTS students (
    user_id INT PRIMARY KEY,
    roll_no VARCHAR(20) UNIQUE,
    program VARCHAR(100),
    year INT
);
CREATE TABLE IF NOT EXISTS instructors (
    user_id INT PRIMARY KEY,
    department VARCHAR(100)
);
CREATE TABLE IF NOT EXISTS courses (
    course_id INT PRIMARY KEY,
    code VARCHAR(20) UNIQUE,
    title VARCHAR(100),
    credits INT
);
CREATE TABLE IF NOT EXISTS settings (
    setting_key VARCHAR(50) PRIMARY KEY,
    setting_value VARCHAR(50)
);
CREATE TABLE IF NOT EXISTS sections (
    section_id INT PRIMARY KEY,
    course_id INT,
    instructor_id INT,
    day_time VARCHAR(100),
    room VARCHAR(50),
    capacity INT,
    semester VARCHAR(20),
    year INT,
    drop_deadline DATE DEFAULT NULL,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    FOREIGN KEY (instructor_id) REFERENCES instructors(user_id) ON DELETE SET NULL
);
CREATE TABLE IF NOT EXISTS section_components (
    section_id INT,
    component_name VARCHAR(50),
    weight INT,
    display_order INT,
    PRIMARY KEY (section_id, component_name),
    FOREIGN KEY (section_id) REFERENCES sections(section_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS enrollments (
    enrollment_id INT PRIMARY KEY,
    student_id INT,
    section_id INT,
    status VARCHAR(20),
    FOREIGN KEY (student_id) REFERENCES students(user_id) ON DELETE CASCADE,
    FOREIGN KEY (section_id) REFERENCES sections(section_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS grades (
    grade_id INT PRIMARY KEY,
    enrollment_id INT,
    component VARCHAR(50),
    score DECIMAL(5, 2),
    final_grade VARCHAR(5),
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS notifications (
    notification_id INT PRIMARY KEY,
    student_id INT,
    message TEXT,
    created_at DATETIME,
    is_read TINYINT(1) DEFAULT 0,
    FOREIGN KEY (student_id) REFERENCES students(user_id) ON DELETE CASCADE
);
-- Students
INSERT INTO students (user_id, roll_no, program, year) VALUES
(1001, 'B23001', 'Computer Science', 2),
(1002, 'B23045', 'Electrical Engineering', 2),
(1003, '2024253', 'CSD', 1),
(1005, '2024225', 'CSE', 1),
(1008, '0000', 'cse', 1);

-- Instructors
INSERT INTO instructors (user_id, department) VALUES
(101,  'Computer Science'),
(102,  'Electrical engineering'),
(1004, 'Mathamatics');

-- Courses
INSERT INTO courses (course_id, code, title, credits) VALUES
(1,  'CSE102', 'Data Structures', 4),
(2,  'MTH203', 'Calculus', 4),
(3,  'MTH101', 'Linear algebra', 4),
(4,  'CSE101', 'IP', 4),
(5,  'MTH102', 'PnS', 4),
(9,  'cse231', 'ap', 4),
(11, 'cse234', 'ap', 4);

-- Settings
INSERT INTO settings (setting_key, setting_value) VALUES
('maintenance_on', 'false');

-- Sections
INSERT INTO sections (section_id, course_id, instructor_id, day_time, room, capacity, semester, year, drop_deadline) VALUES
(1, 1, 101,  'Mon/Wed 10-11:30', 'C01', 50, 'Fall', 2025, NULL),
(2, 2, 1004, 'tue/thu 10-11:30', 'c01', 50, 'Fall', 2025, '2025-11-25'),
(3, 4, 1004, 'not fixed', 'c101', 300, 'monsoon', 2025, NULL),
(4, 9, 1004, 'not fixed', 'none', 300, 'summer', 2025, NULL);

-- Section Components
INSERT INTO section_components (section_id, component_name, weight, display_order) VALUES
(2, 'Final Exam', 30, 0),
(2, 'midsem', 30, 1),
(2, 'labs', 20, 2),
(2, 'quiz', 20, 3);

-- Enrollments
INSERT INTO enrollments (enrollment_id, student_id, section_id, status) VALUES
(3,  1008, 2, 'Enrolled'),
(5,  1008, 3, 'Enrolled'),
(10, 1008, 4, 'Enrolled'),
(20, 1003, 2, 'Enrolled'),
(21, 1005, 2, 'Enrolled'),
(22, 1001, 2, 'Enrolled'),
(24, 1003, 4, 'Enrolled');

-- Grades
INSERT INTO grades (grade_id, enrollment_id, component, score, final_grade) VALUES
(259, 21, 'Final Exam', 100.00, 'A+'),
(260, 21, 'midsem',     100.00, 'A+'),
(261, 21, 'labs',       100.00, 'A+'),
(262, 21, 'quiz',       95.00,  'A+'),
(263, 20, 'Final Exam', 100.00, 'A+'),
(264, 20, 'midsem',     100.00, 'A+'),
(265, 20, 'labs',       100.00, 'A+'),
(266, 20, 'quiz',       55.00,  'A+');

-- Notifications
INSERT INTO notifications (notification_id, student_id, message, created_at, is_read) VALUES
(9,  1005, 'Your final grade for MTH203 (Calculus) has been released', '2025-11-26 07:12:18', 0),
(10, 1003, 'Your final grade for MTH203 (Calculus) has been released', '2025-11-26 07:12:18', 1),
(15, 1005, 'Your final grade for MTH203 (Calculus) has been released', '2025-11-27 01:34:40', 0),
(16, 1003, 'Your final grade for MTH203 (Calculus) has been released', '2025-11-27 01:34:40', 0);
