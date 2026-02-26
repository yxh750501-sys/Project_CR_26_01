-- ============================================================
-- CareRoute: 게시글(post) / 첨부파일(post_file) 테이블 생성
-- MySQL 8.0+  |  CREATE TABLE IF NOT EXISTS — 반복 실행 안전
-- ============================================================

-- ① 게시글 테이블
CREATE TABLE IF NOT EXISTS post (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    board_type  VARCHAR(10)  NOT NULL                          COMMENT 'PROGRAM / FREE',
    category    VARCHAR(20)  NULL                              COMMENT 'CAMP / SPECIAL (PROGRAM 전용)',
    title       VARCHAR(200) NOT NULL,
    body        TEXT         NOT NULL,
    member_id   BIGINT       NOT NULL,
    -- PROGRAM 전용 선택 컬럼
    start_date  DATE         NULL                              COMMENT '프로그램 시작일',
    end_date    DATE         NULL                              COMMENT '프로그램 종료일',
    location    VARCHAR(200) NULL                              COMMENT '장소',
    fee         INT          NULL                              COMMENT '참가비(원). 0 = 무료',
    max_people  INT          NULL                              COMMENT '최대 인원',
    apply_url   VARCHAR(500) NULL                              COMMENT '신청 링크',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_post_board_type (board_type),
    INDEX idx_post_member_id  (member_id),
    INDEX idx_post_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ② 첨부파일 테이블
CREATE TABLE IF NOT EXISTS post_file (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    post_id      BIGINT       NOT NULL,
    orig_name    VARCHAR(255) NOT NULL                         COMMENT '원본 파일명',
    stored_name  VARCHAR(255) NOT NULL                         COMMENT 'UUID 기반 저장 파일명',
    file_size    BIGINT       NOT NULL DEFAULT 0               COMMENT '파일 크기(바이트)',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_post_file_post_id (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
