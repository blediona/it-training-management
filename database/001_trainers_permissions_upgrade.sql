USE it_training_center_db;

-- Migration per rolin TRAINER pa fshire te dhena ekzistuese.
-- Ekzekutohet mbi databazen ekzistuese pasi projekti eshte perditesuar.

INSERT IGNORE INTO roles(name, description) VALUES
    ('TRAINER', 'Can view assigned candidates, update candidate status and issue certificates.');

INSERT IGNORE INTO permissions(code, description) VALUES
    ('CANDIDATE_READ', 'View candidates'),
    ('CERTIFICATE_MANAGE', 'Issue and view certificates'),
    ('CANDIDATE_STATUS_UPDATE', 'Update candidate status only');

INSERT IGNORE INTO role_permissions(role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('CANDIDATE_READ', 'CERTIFICATE_MANAGE', 'CANDIDATE_STATUS_UPDATE')
WHERE r.name = 'TRAINER';

DROP PROCEDURE IF EXISTS add_trainer_user_id_column;

DELIMITER //
CREATE PROCEDURE add_trainer_user_id_column()
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'trainings'
          AND COLUMN_NAME = 'trainer_user_id'
    ) THEN
        ALTER TABLE trainings ADD COLUMN trainer_user_id INT NULL;
    END IF;
END//
DELIMITER ;

CALL add_trainer_user_id_column();
DROP PROCEDURE add_trainer_user_id_column;

-- Krijon user-a per trajneret ekzistues vetem nese nuk ekzistojne.
-- Password-at fillestare jane: arta123, dardan123, elira123.
INSERT IGNORE INTO users(full_name, email, username, salt, salted_hash, role_id) VALUES
    ('Arta Krasniqi', 'arta.krasniqi@itcenter.local', 'arta_krasniqi', '', 'WSp0i4M8S3plSZdOQxph1ls7JbY69+diAZ46+Rb6W/8=', (SELECT id FROM roles WHERE name = 'TRAINER')),
    ('Dardan Hoxha', 'dardan.hoxha@itcenter.local', 'dardan_hoxha', '', 'q8YT9Dw4vmZgw3iN8Zi0FhpNyINA6ZmMjOAygBl4wsU=', (SELECT id FROM roles WHERE name = 'TRAINER')),
    ('Elira Berisha', 'elira.berisha@itcenter.local', 'elira_berisha', '', 'S74MVGZlSkFMzhrcQ5e358M1uf7NJjL5adibU29AlOU=', (SELECT id FROM roles WHERE name = 'TRAINER'));

SET @OLD_SQL_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

UPDATE trainings t
JOIN users u ON LOWER(u.full_name) = LOWER(t.instructor)
JOIN roles r ON r.id = u.role_id AND r.name = 'TRAINER'
SET t.trainer_user_id = u.id
WHERE t.trainer_user_id IS NULL;

SET SQL_SAFE_UPDATES = @OLD_SQL_SAFE_UPDATES;

DROP PROCEDURE IF EXISTS add_trainings_trainer_fk;

DELIMITER //
CREATE PROCEDURE add_trainings_trainer_fk()
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.TABLE_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = DATABASE()
          AND TABLE_NAME = 'trainings'
          AND CONSTRAINT_NAME = 'fk_trainings_trainer'
    ) THEN
        ALTER TABLE trainings
            ADD CONSTRAINT fk_trainings_trainer
            FOREIGN KEY (trainer_user_id) REFERENCES users(id)
            ON DELETE SET NULL
            ON UPDATE CASCADE;
    END IF;
END//
DELIMITER ;

CALL add_trainings_trainer_fk();
DROP PROCEDURE add_trainings_trainer_fk;
