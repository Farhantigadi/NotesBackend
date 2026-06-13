CREATE TABLE sub_sections
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    main_section_id BIGINT       NOT NULL,
    created_at      DATETIME(6),
    updated_at      DATETIME(6),
    CONSTRAINT fk_sub_sections_main_section FOREIGN KEY (main_section_id) REFERENCES main_sections (id)
);
