-- ═══════════════════════════════════════
-- EduAPI — Database Schema (MySQL)
-- ═══════════════════════════════════════

CREATE DATABASE IF NOT EXISTS eduapi;
USE eduapi;

-- ─── Users ───
CREATE TABLE IF NOT EXISTS users (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)     NOT NULL UNIQUE,
    email           VARCHAR(100)    NOT NULL UNIQUE,
    password        VARCHAR(255)    NOT NULL,
    role            ENUM('ADMIN', 'STUDENT') NOT NULL DEFAULT 'STUDENT',
    first_name      VARCHAR(50)     NOT NULL,
    last_name       VARCHAR(50)     NOT NULL,
    phone           VARCHAR(20),
    profile_picture_url VARCHAR(500),
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ─── Students ───
CREATE TABLE IF NOT EXISTS students (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL UNIQUE,
    student_code    VARCHAR(20)     NOT NULL UNIQUE,
    date_of_birth   DATE,
    enrollment_date DATE            NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ─── Courses ───
CREATE TABLE IF NOT EXISTS courses (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    course_code     VARCHAR(20)     NOT NULL UNIQUE,
    title           VARCHAR(200)    NOT NULL,
    description     TEXT,
    credits         INT             NOT NULL DEFAULT 3,
    max_capacity    INT             NOT NULL DEFAULT 30,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ─── Enrollments ───
CREATE TABLE IF NOT EXISTS enrollments (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    student_id      BIGINT          NOT NULL,
    course_id       BIGINT          NOT NULL,
    enrollment_date DATE            NOT NULL,
    grade           VARCHAR(5),
    status          ENUM('ACTIVE', 'COMPLETED', 'DROPPED') NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_enrollment_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollment_course  FOREIGN KEY (course_id)  REFERENCES courses(id)  ON DELETE CASCADE,
    CONSTRAINT uk_student_course     UNIQUE (student_id, course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
