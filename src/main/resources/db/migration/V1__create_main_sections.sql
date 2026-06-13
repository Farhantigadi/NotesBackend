CREATE TABLE main_sections
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  DATETIME(6),
    updated_at  DATETIME(6)
);
