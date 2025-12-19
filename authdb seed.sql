CREATE DATABASE IF NOT EXISTS auth_db;
USE auth_db;

CREATE TABLE IF NOT EXISTS users_auth (
    user_id INT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) DEFAULT 'Active',
    last_login DATETIME DEFAULT NULL,
    failed_attempts INT DEFAULT 0,
    lockout_until DATETIME DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS password_history (
    history_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    password VARCHAR(255) NOT NULL,
    changed_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users_auth(user_id) ON DELETE CASCADE
);

INSERT INTO users_auth (user_id, username, role, password_hash, status, last_login, failed_attempts, lockout_until) VALUES
(1,    'admin1',       'Admin',      '$2a$10$.hwAvKIhd.Vz8XQYz.zI3.Q/Ore/QZfNbZnGR.D4QqQZImi/c.7q4K', 'Active', NULL, 0, NULL),
(101,  'inst1',        'Instructor', '$2a$10$R9nE.4a9oBvP2kXyZf0b2u.5lJ6c8w7m.9d3E.3f4g5H.6i7J8k9L',  'Active', NULL, 0, NULL),
(1001, 'stu1',         'Student',    '$2a$10$8/qP/68RMBkvWhOecJhz2OaNEGrc81YToAeBMw3WExid44z113J12',  'Active', NULL, 0, NULL),
(1002, 'stu2',         'Student',    '$2a$10$MooJSCBRYZmUmb7dW7/6aeww6uzbQSnq64e/3g0t6yi6.5Lpu.ix.',        'Active', NULL, 0, NULL),
(1003, 'harshul24253', 'Student',    '$2a$10$/tIJFdGv494Bpos1gGMRk.kdW0ui5IR0Ep6J8efBOO22lTcjD.ga.', 'Active', NULL, 0, NULL),
(1004, 'inst2',        'Instructor', '$2a$10$yaXSEBRygrZEYiAPSThPf.ujuZatmeI9cXWUSM6QLCQiFJct3e4Ja', 'Active', NULL, 0, NULL),
(1005, 'hansika24225', 'Student',    '$2a$10$g3VCVkCc2cwpY2gG/0Q6EeYdJbRNsBoWwnZKZ4FtzM/HO0xfXO.3i',  'Active', NULL, 0, NULL),
(1008, 'stu0',         'Student',    '$2a$10$WeL/MQUtoMKbCkjtz5/51.3HEXL04vusE6dnvNepJ/fW.lkqz83FC',  'Active', NULL, 0, NULL),
(1009, 'admin2',       'Admin',      '$2a$10$WpStbgFOzcM9cxB83rUEhepTTUm7IMExi788S/aXNwlRgOhSP2VGy',   'Active', NULL, 0, NULL);

INSERT INTO password_history (history_id, user_id, password, changed_at) VALUES
(1, 1008, 'donotopen', '2025-11-26 06:36:12');
