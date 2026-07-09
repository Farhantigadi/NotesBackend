CREATE TABLE users
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(100)  NOT NULL UNIQUE,
    password   VARCHAR(255)  NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6)
);

CREATE TABLE main_sections
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(255) NOT NULL,
    description   TEXT,
    display_order INT,
    user_id       BIGINT       NOT NULL,
    created_at    DATETIME(6),
    updated_at    DATETIME(6),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE sub_sections
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    display_order   INT,
    main_section_id BIGINT       NOT NULL,
    user_id         BIGINT       NOT NULL,
    created_at      DATETIME(6),
    updated_at      DATETIME(6),
    FOREIGN KEY (main_section_id) REFERENCES main_sections (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE questions
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    title          VARCHAR(255) NOT NULL,
    answer         LONGTEXT,
    code_snippet   LONGTEXT,
    code_language  VARCHAR(100),
    code_blocks    LONGTEXT,
    explanation    LONGTEXT,
    display_order  INT,
    image_url      VARCHAR(512),
    image_width    INT,
    image_align    VARCHAR(10),
    sub_section_id BIGINT       NOT NULL,
    user_id        BIGINT       NOT NULL,
    created_at     DATETIME(6),
    updated_at     DATETIME(6),
    FOREIGN KEY (sub_section_id) REFERENCES sub_sections (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);
