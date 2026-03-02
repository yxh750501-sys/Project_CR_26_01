-- ============================================================
-- CareRoute: 기존 DB 보강 패치 (idempotent)
-- MySQL 8.0+  |  여러 번 실행 가능 — 재실행 안전
-- 적용 대상: 이미 DB가 세팅된 환경에서 누락 컬럼/인덱스 보강
-- 규칙: INFORMATION_SCHEMA 체크 후 PREPARE/EXECUTE로 조건부 실행
--       결과 출력 SELECT 없음 — no-op 는 SET @dummy := 0 처리
-- ============================================================

-- ============================================================
-- [SECTION 1] users 테이블 컬럼 보강
-- ============================================================

-- 1-1. name
SET @c = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
          WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'name');
SET @s = IF(@c = 0,
    'ALTER TABLE users ADD COLUMN name VARCHAR(50) NOT NULL DEFAULT \'\' AFTER login_pw',
    'SET @dummy := 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 1-2. email
SET @c = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
          WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'email');
SET @s = IF(@c = 0,
    'ALTER TABLE users ADD COLUMN email VARCHAR(100) NULL',
    'SET @dummy := 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 1-3. phone
SET @c = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
          WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'phone');
SET @s = IF(@c = 0,
    'ALTER TABLE users ADD COLUMN phone VARCHAR(20) NULL',
    'SET @dummy := 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 1-4. profile_image
SET @c = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
          WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'profile_image');
SET @s = IF(@c = 0,
    'ALTER TABLE users ADD COLUMN profile_image VARCHAR(255) NULL',
    'SET @dummy := 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 1-5. member_type
SET @c = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
          WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'member_type');
SET @s = IF(@c = 0,
    'ALTER TABLE users ADD COLUMN member_type VARCHAR(20) NOT NULL DEFAULT ''GUARDIAN'' COMMENT ''GUARDIAN / GENERAL''',
    'SET @dummy := 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 1-6. display_role
SET @c = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
          WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'display_role');
SET @s = IF(@c = 0,
    'ALTER TABLE users ADD COLUMN display_role VARCHAR(50) NULL',
    'SET @dummy := 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 1-7. org_name
SET @c = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
          WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'org_name');
SET @s = IF(@c = 0,
    'ALTER TABLE users ADD COLUMN org_name VARCHAR(100) NULL',
    'SET @dummy := 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 1-8. update_date
SET @c = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
          WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'update_date');
SET @s = IF(@c = 0,
    'ALTER TABLE users ADD COLUMN update_date DATETIME NULL ON UPDATE CURRENT_TIMESTAMP',
    'SET @dummy := 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 1-9. email UNIQUE 인덱스 (uq_users_email)
SET @i = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
          WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND INDEX_NAME = 'uq_users_email');
SET @s = IF(@i = 0,
    'ALTER TABLE users ADD UNIQUE KEY uq_users_email (email)',
    'SET @dummy := 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================================
-- [SECTION 2] favorites 테이블 보강 및 백필
-- ============================================================

-- 2-1. created_at 컬럼 추가
SET @c = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
          WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'favorites' AND COLUMN_NAME = 'created_at');
SET @s = IF(@c = 0,
    'ALTER TABLE favorites ADD COLUMN created_at DATETIME NULL',
    'SET @dummy := 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2-2. reg_date 컬럼 추가 (favorites 가 없는 환경 대비)
SET @c = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
          WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'favorites' AND COLUMN_NAME = 'reg_date');
SET @s = IF(@c = 0,
    'ALTER TABLE favorites ADD COLUMN reg_date DATETIME NULL',
    'SET @dummy := 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2-3. 기존 데이터 백필: reg_date NULL 이면 created_at → NULL 이면 NOW()
UPDATE favorites
SET reg_date = COALESCE(created_at, NOW())
WHERE reg_date IS NULL;

-- 2-4. 기존 데이터 백필: created_at NULL 이면 reg_date 값으로 채움
UPDATE favorites
SET created_at = reg_date
WHERE created_at IS NULL;

-- 2-5. reg_date NOT NULL DEFAULT CURRENT_TIMESTAMP 제약 보강
--      (MODIFY COLUMN 은 컬럼이 존재할 때만 실행 — 항상 실행해도 안전)
SET @c = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
          WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'favorites' AND COLUMN_NAME = 'reg_date');
SET @s = IF(@c > 0,
    'ALTER TABLE favorites MODIFY COLUMN reg_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP',
    'SET @dummy := 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2-6. created_at NOT NULL DEFAULT 보강
SET @c = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
          WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'favorites' AND COLUMN_NAME = 'created_at');
SET @s = IF(@c > 0,
    'ALTER TABLE favorites MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP',
    'SET @dummy := 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2-7. 정렬 인덱스 (member_id, reg_date DESC)
SET @i = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
          WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'favorites' AND INDEX_NAME = 'idx_fav_member_reg');
SET @s = IF(@i = 0,
    'ALTER TABLE favorites ADD INDEX idx_fav_member_reg (member_id, reg_date DESC)',
    'SET @dummy := 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================================
-- [SECTION 3] checklist_questions.domain_code 보강 및 백필
-- ============================================================

-- 3-1. domain_code 컬럼 추가 (NULL 허용으로 먼저 추가)
SET @c = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
          WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'checklist_questions' AND COLUMN_NAME = 'domain_code');
SET @s = IF(@c = 0,
    'ALTER TABLE checklist_questions ADD COLUMN domain_code VARCHAR(30) NULL',
    'SET @dummy := 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3-2. 기존 데이터 백필: domain_code NULL 이면 'COMMUNICATION' 으로 채움
UPDATE checklist_questions
SET domain_code = 'COMMUNICATION'
WHERE domain_code IS NULL OR domain_code = '';

-- 3-3. domain_code NOT NULL DEFAULT 'COMMUNICATION' 로 보강
SET @c = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
          WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'checklist_questions' AND COLUMN_NAME = 'domain_code');
SET @s = IF(@c > 0,
    'ALTER TABLE checklist_questions MODIFY COLUMN domain_code VARCHAR(30) NOT NULL DEFAULT ''COMMUNICATION''',
    'SET @dummy := 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3-4. 도메인 인덱스
SET @i = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
          WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'checklist_questions' AND INDEX_NAME = 'idx_cq_domain');
SET @s = IF(@i = 0,
    'ALTER TABLE checklist_questions ADD INDEX idx_cq_domain (domain_code)',
    'SET @dummy := 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================================
-- [SECTION 4] checklist_runs 컬럼 보강
-- ============================================================

-- 4-1. submitted_date 컬럼 추가
SET @c = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
          WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'checklist_runs' AND COLUMN_NAME = 'submitted_date');
SET @s = IF(@c = 0,
    'ALTER TABLE checklist_runs ADD COLUMN submitted_date DATETIME NULL',
    'SET @dummy := 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 4-2. STATUS 컬럼 기본값 보강 (DRAFT)
--      컬럼이 이미 존재하므로 MODIFY COLUMN 으로 DEFAULT 확인/설정
SET @c = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
          WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'checklist_runs'
            AND COLUMN_NAME = 'STATUS' AND COLUMN_DEFAULT = 'DRAFT');
SET @s = IF(@c = 0,
    'ALTER TABLE checklist_runs MODIFY COLUMN `STATUS` VARCHAR(20) NOT NULL DEFAULT ''DRAFT'' COMMENT ''DRAFT / SUBMITTED / DISCARDED''',
    'SET @dummy := 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 4-3. update_date 인덱스
SET @i = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
          WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'checklist_runs' AND INDEX_NAME = 'idx_runs_update_date');
SET @s = IF(@i = 0,
    'ALTER TABLE checklist_runs ADD INDEX idx_runs_update_date (update_date DESC)',
    'SET @dummy := 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================================
-- [SECTION 5] checklist_answers UNIQUE KEY 보강
-- ============================================================

-- 5-1. UNIQUE KEY uq_answer_run_question (run_id, question_id)
--      (upsertAnswer 의 ON DUPLICATE KEY UPDATE 동작에 필수)
SET @i = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
          WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'checklist_answers' AND INDEX_NAME = 'uq_answer_run_question');
SET @s = IF(@i = 0,
    'ALTER TABLE checklist_answers ADD UNIQUE KEY uq_answer_run_question (run_id, question_id)',
    'SET @dummy := 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
