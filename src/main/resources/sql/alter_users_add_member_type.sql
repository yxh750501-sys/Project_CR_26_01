-- ============================================================
-- CareRoute: users 테이블에 member_type / display_role / org_name 추가
-- MySQL 8.0+  |  멱등(idempotent) — 반복 실행 안전
-- ============================================================

-- ① member_type: 회원 유형 (GUARDIAN: 보호자 / GENERAL: 일반회원)
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS member_type  VARCHAR(20)  NOT NULL DEFAULT 'GUARDIAN'
    AFTER email;

-- ② display_role: 표시 역할 (치료사 / 센터 / 기관 / 기타 — 선택 입력)
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS display_role VARCHAR(50)  NULL
    AFTER member_type;

-- ③ org_name: 소속 기관명 (선택 입력)
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS org_name     VARCHAR(100) NULL
    AFTER display_role;

-- ④ 기존 레코드를 member_type 으로 동기화 (ROLE 값이 있으면 복사)
--    필요 시 주석 해제 후 실행
-- UPDATE users SET member_type = CASE
--     WHEN `ROLE` IN ('GUARDIAN','GENERAL') THEN `ROLE`
--     ELSE 'GUARDIAN'
-- END WHERE 1=1;

-- ⑤ 현재 스키마 확인
SHOW COLUMNS FROM users;
