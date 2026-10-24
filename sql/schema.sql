CREATE DATABASE IF NOT EXISTS courseshare
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE courseshare;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS cms_review_like;
DROP TABLE IF EXISTS cms_resource_review;
DROP TABLE IF EXISTS cms_resource_favorite;
DROP TABLE IF EXISTS sys_operation_log;
DROP TABLE IF EXISTS cms_article;
DROP TABLE IF EXISTS cms_category;
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE sys_user (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    email VARCHAR(100) NULL,
    status TINYINT NOT NULL DEFAULT 1,
    last_login_at DATETIME(3) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_username (username),
    UNIQUE KEY uk_user_email (email),
    KEY idx_user_status (status, deleted),
    CONSTRAINT chk_user_status CHECK (status IN (0, 1)),
    CONSTRAINT chk_user_deleted CHECK (deleted IN (0, 1))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE sys_role (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255) NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (code),
    UNIQUE KEY uk_role_name (name),
    KEY idx_role_status (status),
    CONSTRAINT chk_role_status CHECK (status IN (0, 1))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE sys_permission (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    module VARCHAR(50) NOT NULL,
    http_method VARCHAR(10) NULL,
    path VARCHAR(255) NULL,
    description VARCHAR(255) NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_permission_code (code),
    KEY idx_permission_module (module, status),
    CONSTRAINT chk_permission_status CHECK (status IN (0, 1))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE sys_user_role (
    user_id BIGINT UNSIGNED NOT NULL,
    role_id BIGINT UNSIGNED NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (user_id, role_id),
    KEY idx_user_role_role (role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE RESTRICT,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE sys_role_permission (
    role_id BIGINT UNSIGNED NOT NULL,
    permission_id BIGINT UNSIGNED NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (role_id, permission_id),
    KEY idx_role_permission_permission (permission_id),
    CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE RESTRICT,
    CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES sys_permission (id) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE cms_category (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    parent_id BIGINT UNSIGNED NULL,
    name VARCHAR(50) NOT NULL,
    level TINYINT NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    description VARCHAR(255) NULL,
    version INT UNSIGNED NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_category_tree (parent_id, status, sort_order, deleted),
    KEY idx_category_name (parent_id, name),
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES cms_category (id) ON DELETE RESTRICT,
    CONSTRAINT chk_category_level CHECK (level BETWEEN 1 AND 3),
    CONSTRAINT chk_category_sort CHECK (sort_order >= 0),
    CONSTRAINT chk_category_status CHECK (status IN (0, 1)),
    CONSTRAINT chk_category_deleted CHECK (deleted IN (0, 1))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE cms_article (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    content MEDIUMTEXT NOT NULL,
    category_id BIGINT UNSIGNED NOT NULL,
    author_id BIGINT UNSIGNED NOT NULL,
    attachment_name VARCHAR(255) NULL,
    attachment_path VARCHAR(500) NULL,
    attachment_ext VARCHAR(20) NULL,
    attachment_mime VARCHAR(100) NULL,
    attachment_size BIGINT UNSIGNED NULL,
    status TINYINT NOT NULL DEFAULT 0,
    audit_by BIGINT UNSIGNED NULL,
    audit_at DATETIME(3) NULL,
    audit_remark VARCHAR(500) NULL,
    published_at DATETIME(3) NULL,
    view_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
    download_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
    version INT UNSIGNED NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_article_public (status, deleted, category_id, published_at),
    KEY idx_article_author (author_id, deleted, status, updated_at),
    KEY idx_article_audit (status, deleted, created_at),
    KEY idx_article_attachment (attachment_path),
    CONSTRAINT fk_article_category FOREIGN KEY (category_id) REFERENCES cms_category (id) ON DELETE RESTRICT,
    CONSTRAINT fk_article_author FOREIGN KEY (author_id) REFERENCES sys_user (id) ON DELETE RESTRICT,
    CONSTRAINT fk_article_auditor FOREIGN KEY (audit_by) REFERENCES sys_user (id) ON DELETE SET NULL,
    CONSTRAINT chk_article_status CHECK (status BETWEEN 0 AND 4),
    CONSTRAINT chk_article_deleted CHECK (deleted IN (0, 1)),
    CONSTRAINT chk_article_attachment CHECK (
        (attachment_path IS NULL AND attachment_name IS NULL)
        OR (attachment_path IS NOT NULL AND attachment_name IS NOT NULL)
    )
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE cms_resource_favorite (
    user_id BIGINT UNSIGNED NOT NULL,
    resource_id BIGINT UNSIGNED NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (user_id, resource_id),
    KEY idx_favorite_resource (resource_id),
    KEY idx_favorite_user_created (user_id, created_at),
    CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_favorite_resource FOREIGN KEY (resource_id) REFERENCES cms_article (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE cms_resource_review (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    resource_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    score TINYINT UNSIGNED NOT NULL,
    content VARCHAR(500) NULL,
    status TINYINT NOT NULL DEFAULT 1,
    like_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_review_user_resource (user_id, resource_id),
    KEY idx_review_resource_status (resource_id, status, like_count, created_at),
    KEY idx_review_user_created (user_id, created_at),
    CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_review_resource FOREIGN KEY (resource_id) REFERENCES cms_article (id) ON DELETE CASCADE,
    CONSTRAINT chk_review_score CHECK (score BETWEEN 1 AND 5),
    CONSTRAINT chk_review_status CHECK (status IN (0, 1))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE cms_review_like (
    review_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (review_id, user_id),
    KEY idx_review_like_user (user_id, created_at),
    CONSTRAINT fk_review_like_review FOREIGN KEY (review_id) REFERENCES cms_resource_review (id) ON DELETE CASCADE,
    CONSTRAINT fk_review_like_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE sys_operation_log (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    request_id VARCHAR(64) NOT NULL,
    user_id BIGINT UNSIGNED NULL,
    username_snapshot VARCHAR(50) NULL,
    module VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    http_method VARCHAR(10) NOT NULL,
    uri VARCHAR(255) NOT NULL,
    ip VARCHAR(45) NULL,
    duration_ms INT UNSIGNED NOT NULL DEFAULT 0,
    success TINYINT NOT NULL DEFAULT 0,
    error_code VARCHAR(50) NULL,
    error_message VARCHAR(500) NULL,
    params_json MEDIUMTEXT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_log_created (created_at),
    KEY idx_log_user (user_id, created_at),
    KEY idx_log_module (module, created_at),
    KEY idx_log_request (request_id),
    CONSTRAINT fk_log_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE SET NULL,
    CONSTRAINT chk_log_success CHECK (success IN (0, 1))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

