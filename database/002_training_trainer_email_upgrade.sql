USE it_training_center_db;

DROP PROCEDURE IF EXISTS add_trainings_trainer_email_column;

DELIMITER //
CREATE PROCEDURE add_trainings_trainer_email_column()
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'trainings'
          AND COLUMN_NAME = 'trainer_email'
    ) THEN
        ALTER TABLE trainings ADD COLUMN trainer_email VARCHAR(120) NULL AFTER instructor;
    END IF;
END//
DELIMITER ;

CALL add_trainings_trainer_email_column();
DROP PROCEDURE add_trainings_trainer_email_column;

SET @OLD_SQL_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

UPDATE trainings t
LEFT JOIN users u ON u.id = t.trainer_user_id
SET t.trainer_email = COALESCE(NULLIF(t.trainer_email, ''), u.email, CONCAT(LOWER(REPLACE(t.instructor, ' ', '.')), '@itcenter.local'))
WHERE t.trainer_email IS NULL OR t.trainer_email = '';

SET SQL_SAFE_UPDATES = @OLD_SQL_SAFE_UPDATES;
