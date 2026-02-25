-- ============================================================
-- users 테이블에 name, email 컬럼 추가
-- 실행 전: 기존 데이터가 있으면 UPDATE로 name/email 채운 뒤 NOT NULL 제약 강화 가능
-- ============================================================

-- 1) name 컬럼 추가 (login_id 뒤)
ALTER TABLE users
    ADD COLUMN name VARCHAR(50) NOT NULL DEFAULT '' AFTER login_id;

-- 2) email 컬럼 추가 (name 뒤, NULL 허용: 기존 레코드 충돌 방지)
ALTER TABLE users
    ADD COLUMN email VARCHAR(100) NULL AFTER name;

-- 3) email 유니크 인덱스 (NULL 값끼리는 중복 허용 — MySQL 표준)
ALTER TABLE users
    ADD UNIQUE KEY uq_users_email (email);

-- ※ 기존 사용자 레코드가 있다면 아래 예시처럼 email 채우고 NOT NULL 로 변경 가능
-- UPDATE users SET email = CONCAT('user_', id, '@placeholder.invalid') WHERE email IS NULL;
-- ALTER TABLE users MODIFY COLUMN email VARCHAR(100) NOT NULL;
