-- ============================================================
-- CareRoute: users 테이블 email/name 컬럼 & UNIQUE 인덱스 설정
-- MySQL 8.0+  |  멱등(idempotent) — 반복 실행 안전
-- ============================================================

-- ① 현재 상태 확인 (실행 전 참고용)
-- SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT
-- FROM   INFORMATION_SCHEMA.COLUMNS
-- WHERE  TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users';

-- ① name 컬럼 없으면 추가 (이미 있으면 no-op)
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS name  VARCHAR(50)  NOT NULL DEFAULT '' AFTER login_id;

-- ② email 컬럼 없으면 추가
--    NULL 허용: 기존 레코드가 있어도 오류 없이 추가됨
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS email VARCHAR(100) NULL AFTER name;

-- ③ email UNIQUE 인덱스 — 없을 때만 추가 (멱등)
--    MySQL 은 ADD UNIQUE KEY IF NOT EXISTS 미지원이므로 INFORMATION_SCHEMA 로 분기
SET @idx_exists = (
    SELECT COUNT(*)
    FROM   INFORMATION_SCHEMA.STATISTICS
    WHERE  TABLE_SCHEMA = DATABASE()
      AND  TABLE_NAME   = 'users'
      AND  INDEX_NAME   = 'uq_users_email'
);

SET @add_idx = IF(
    @idx_exists = 0,
    'ALTER TABLE users ADD UNIQUE KEY uq_users_email (email)',
    'SELECT ''[OK] uq_users_email 인덱스가 이미 존재합니다'' AS status'
);

PREPARE stmt FROM @add_idx;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ④ 기존 레코드에 email NULL 값이 있는 경우 — 아래 주석 해제 후 실행
--    (NULL 은 UNIQUE 제약에서 서로 중복으로 보지 않으므로 가입에 문제 없음)
--    단, NOT NULL 로 변경하려면 먼저 더미값을 채운 뒤 MODIFY 실행
-- UPDATE users SET email = CONCAT('user_', id, '@placeholder.invalid') WHERE email IS NULL;
-- ALTER TABLE users MODIFY COLUMN email VARCHAR(100) NOT NULL;

-- ⑤ 실행 후 스키마 확인
SHOW COLUMNS FROM users;
SHOW INDEX  FROM users WHERE Key_name = 'uq_users_email';
