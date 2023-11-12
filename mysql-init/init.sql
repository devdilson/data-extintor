CREATE TABLE IF NOT EXISTS test
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE = INNODB;

INSERT INTO test (name, created_at)
VALUES ('Sample Name 1', '2020-01-30 10:00:00');
INSERT INTO test (name, created_at)
VALUES ('Sample Name 2', '2020-01-30 15:30:00');
INSERT INTO test (name, created_at)
VALUES ('Sample Name 3', '2020-02-10 08:00:00');
INSERT INTO test (name, created_at)
VALUES ('Sample Name 4', '2020-02-20 12:45:00');

SET GLOBAL event_scheduler = ON;
CREATE EVENT IF NOT EXISTS addRecordEverySecond
    ON SCHEDULE EVERY 1 SECOND
    DO
    INSERT INTO test (name)
    VALUES (CONCAT('Sample Name ', NOW()));
