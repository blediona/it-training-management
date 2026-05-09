CREATE TABLE candidates (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    personal_no VARCHAR(20) NOT NULL UNIQUE,
    birth_date DATE NULL,
    gender ENUM('M', 'F', 'OTHER') NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    address VARCHAR(150) NULL,
    city VARCHAR(50) NULL,
    registration_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    status ENUM('NEW', 'ACTIVE', 'COMPLETED', 'DROPPED', 'BLOCKED') NOT NULL DEFAULT 'NEW',
    notes TEXT NULL,
    CONSTRAINT fk_candidates_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);