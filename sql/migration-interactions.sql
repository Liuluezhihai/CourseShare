USE courseshare;

CREATE TABLE IF NOT EXISTS cms_resource_favorite (
    user_id BIGINT UNSIGNED NOT NULL,
    resource_id BIGINT UNSIGNED NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (user_id, resource_id),
    KEY idx_favorite_resource (resource_id),
    KEY idx_favorite_user_created (user_id, created_at),
    CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_favorite_resource FOREIGN KEY (resource_id) REFERENCES cms_article (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS cms_resource_review (
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

CREATE TABLE IF NOT EXISTS cms_review_like (
    review_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (review_id, user_id),
    KEY idx_review_like_user (user_id, created_at),
    CONSTRAINT fk_review_like_review FOREIGN KEY (review_id) REFERENCES cms_resource_review (id) ON DELETE CASCADE,
    CONSTRAINT fk_review_like_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

INSERT INTO sys_permission (id, code, name, module, http_method, path, description, status)
VALUES (16, 'review:moderate', '管理资源评价', 'review', 'PATCH', '/api/admin/reviews/{id}/visibility', '隐藏或恢复资源评价', 1)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    module = VALUES(module),
    http_method = VALUES(http_method),
    path = VALUES(path),
    description = VALUES(description),
    status = VALUES(status);

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
INNER JOIN sys_permission p ON p.code = 'review:moderate'
WHERE r.code = 'ADMIN';
