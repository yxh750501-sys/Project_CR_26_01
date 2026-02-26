-- ============================================================
-- favorites: 보호자의 치료기관 즐겨찾기 테이블
-- 실행 전제: users 테이블, centers 테이블이 존재해야 함
-- ============================================================

CREATE TABLE IF NOT EXISTS favorites (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    member_id  BIGINT       NOT NULL COMMENT '사용자 PK (users.id)',
    center_id  BIGINT       NOT NULL COMMENT '기관 PK  (centers.id)',
    reg_date   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_favorites (member_id, center_id),
    CONSTRAINT fk_fav_member FOREIGN KEY (member_id) REFERENCES users(id)   ON DELETE CASCADE,
    CONSTRAINT fk_fav_center FOREIGN KEY (center_id) REFERENCES centers(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
