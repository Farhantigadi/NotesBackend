CREATE TABLE questions
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    title          VARCHAR(255) NOT NULL,
    answer         LONGTEXT,
    code_snippet   LONGTEXT,
    code_language  VARCHAR(100),
    explanation    LONGTEXT,
    display_order  INT,
    sub_section_id BIGINT       NOT NULL,
    created_at     DATETIME(6),
    updated_at     DATETIME(6),
    CONSTRAINT fk_questions_sub_section FOREIGN KEY (sub_section_id) REFERENCES sub_sections (id) ON DELETE CASCADE
);
