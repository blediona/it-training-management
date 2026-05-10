-- =========================================================
-- IT Training Management
-- MySQL 8+ database script for the JavaFX application
-- =========================================================

CREATE DATABASE IF NOT EXISTS it_training_center_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE it_training_center_db;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS certificates;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS candidates;
DROP TABLE IF EXISTS password_reset_tokens;
DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS trainings;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS roles;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE,
    description VARCHAR(255)
) ENGINE=InnoDB;

CREATE TABLE permissions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(60) NOT NULL UNIQUE,
    description VARCHAR(255)
) ENGINE=InnoDB;

CREATE TABLE role_permissions (
    role_id INT NOT NULL,
    permission_id INT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id) REFERENCES roles(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_role_permissions_permission
        FOREIGN KEY (permission_id) REFERENCES permissions(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    username VARCHAR(60) NOT NULL UNIQUE,
    salt VARCHAR(255) NOT NULL,
    salted_hash VARCHAR(255) NOT NULL,
    role_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_role
        FOREIGN KEY (role_id) REFERENCES roles(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE trainings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    instructor VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL DEFAULT 0,
    level VARCHAR(40) NOT NULL DEFAULT 'Beginner',
    description TEXT,
    start_date DATE,
    end_date DATE,
    capacity INT NOT NULL DEFAULT 20,
    CONSTRAINT chk_trainings_price CHECK (price >= 0),
    CONSTRAINT chk_trainings_capacity CHECK (capacity > 0),
    CONSTRAINT chk_trainings_dates CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date)
) ENGINE=InnoDB;

CREATE TABLE candidates (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    email VARCHAR(120) NOT NULL,
    phone VARCHAR(30),
    training_id INT NOT NULL,
    status VARCHAR(30) NOT NULL,
    total_fee DECIMAL(10,2) NOT NULL DEFAULT 0,
    paid_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    registration_date DATE NOT NULL,
    certificate_issued TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT uq_candidates_email_training UNIQUE (email, training_id),
    CONSTRAINT fk_candidates_training
        FOREIGN KEY (training_id) REFERENCES trainings(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT chk_candidates_total_fee CHECK (total_fee >= 0),
    CONSTRAINT chk_candidates_paid_amount CHECK (paid_amount >= 0),
    CONSTRAINT chk_candidates_paid_not_over_total CHECK (paid_amount <= total_fee)
) ENGINE=InnoDB;

CREATE TABLE payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    candidate_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_date DATE NOT NULL,
    method VARCHAR(50) NOT NULL,
    CONSTRAINT chk_payments_amount CHECK (amount > 0),
    CONSTRAINT fk_payments_candidate
        FOREIGN KEY (candidate_id) REFERENCES candidates(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE certificates (
    id INT AUTO_INCREMENT PRIMARY KEY,
    candidate_id INT NOT NULL UNIQUE,
    certificate_number VARCHAR(60) NOT NULL UNIQUE,
    issued_date DATE NOT NULL,
    notes TEXT,
    CONSTRAINT fk_certificates_candidate
        FOREIGN KEY (candidate_id) REFERENCES candidates(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE password_reset_tokens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token VARCHAR(120) NOT NULL UNIQUE,
    expires_at DATETIME NOT NULL,
    used_at DATETIME,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_password_reset_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(60) NOT NULL,
    record_id INT NOT NULL,
    action VARCHAR(20) NOT NULL,
    actor VARCHAR(120) NOT NULL DEFAULT 'database',
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    old_data JSON,
    new_data JSON
) ENGINE=InnoDB;

INSERT IGNORE INTO roles(id, name, description) VALUES
    (1, 'ADMIN', 'Full access to candidates, payments, certificates and settings.'),
    (2, 'STAFF', 'Can register and update candidates and view dashboard data.');

INSERT IGNORE INTO permissions(id, code, description) VALUES
    (1, 'CANDIDATE_READ', 'View candidates'),
    (2, 'CANDIDATE_WRITE', 'Create and update candidates'),
    (3, 'CANDIDATE_DELETE', 'Delete candidates'),
    (4, 'PAYMENT_MANAGE', 'Register and view payments'),
    (5, 'CERTIFICATE_MANAGE', 'Issue and view certificates'),
    (6, 'TRAINING_MANAGE', 'Manage training programs'),
    (7, 'USER_MANAGE', 'Manage users and roles');

INSERT IGNORE INTO role_permissions(role_id, permission_id) VALUES
    (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7),
    (2, 1), (2, 2), (2, 6);

INSERT IGNORE INTO users(id, full_name, email, username, salt, salted_hash, role_id) VALUES
    (1, 'System Administrator', 'admin@itcenter.local', 'admin', '', 'JAvlGPq9JyTdtvBO6x2llnRI1+gxwIyPqCKAn3THIKk=', 1);

DELIMITER //

CREATE TRIGGER trg_candidates_insert_audit
AFTER INSERT ON candidates
FOR EACH ROW
BEGIN
    INSERT INTO audit_log(table_name, record_id, action, actor, new_data)
    VALUES ('candidates', NEW.id, 'INSERT', CURRENT_USER(), JSON_OBJECT('email', NEW.email, 'training_id', NEW.training_id, 'status', NEW.status));
END//

CREATE TRIGGER trg_candidates_update_audit
AFTER UPDATE ON candidates
FOR EACH ROW
BEGIN
    INSERT INTO audit_log(table_name, record_id, action, actor, old_data, new_data)
    VALUES (
        'candidates',
        NEW.id,
        'UPDATE',
        CURRENT_USER(),
        JSON_OBJECT('email', OLD.email, 'training_id', OLD.training_id, 'status', OLD.status, 'paid_amount', OLD.paid_amount),
        JSON_OBJECT('email', NEW.email, 'training_id', NEW.training_id, 'status', NEW.status, 'paid_amount', NEW.paid_amount)
    );
END//

CREATE TRIGGER trg_candidates_delete_audit
AFTER DELETE ON candidates
FOR EACH ROW
BEGIN
    INSERT INTO audit_log(table_name, record_id, action, actor, old_data)
    VALUES ('candidates', OLD.id, 'DELETE', CURRENT_USER(), JSON_OBJECT('email', OLD.email, 'training_id', OLD.training_id, 'status', OLD.status));
END//

CREATE TRIGGER trg_trainings_update_audit
AFTER UPDATE ON trainings
FOR EACH ROW
BEGIN
    INSERT INTO audit_log(table_name, record_id, action, actor, old_data, new_data)
    VALUES (
        'trainings',
        NEW.id,
        'UPDATE',
        CURRENT_USER(),
        JSON_OBJECT('name', OLD.name, 'price', OLD.price, 'capacity', OLD.capacity),
        JSON_OBJECT('name', NEW.name, 'price', NEW.price, 'capacity', NEW.capacity)
    );
END//

DELIMITER ;

-- Demo login:
-- username: admin
-- password: admin123
