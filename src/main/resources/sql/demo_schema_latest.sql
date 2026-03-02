-- ============================================================
-- CareRoute: 최신 전체 스키마 (신규 DB 세팅 전용)
-- MySQL 8.0+  |  CREATE TABLE IF NOT EXISTS — 반복 실행 안전
-- 규칙: DROP/ALTER/UPDATE 금지 | post/post_file 제외(create_post.sql)
-- ============================================================

SET NAMES utf8mb4;

-- ──────────────────────────────────────────────────────────────────
-- ① users
-- ──────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    login_id      VARCHAR(50)  NOT NULL,
    login_pw      VARCHAR(255) NOT NULL                        COMMENT 'BCrypt 해시',
    name          VARCHAR(50)  NOT NULL DEFAULT '',
    email         VARCHAR(100) NULL,
    phone         VARCHAR(20)  NULL,
    profile_image VARCHAR(255) NULL,
    `ROLE`        VARCHAR(20)  NOT NULL DEFAULT 'USER',
    member_type   VARCHAR(20)  NOT NULL DEFAULT 'GUARDIAN'     COMMENT 'GUARDIAN / GENERAL',
    display_role  VARCHAR(50)  NULL                            COMMENT '치료사 / 센터 / 기관 / 기타 (선택)',
    org_name      VARCHAR(100) NULL                            COMMENT '소속 기관명 (선택)',
    reg_date      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date   DATETIME     NULL     ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_login_id (login_id),
    UNIQUE KEY uq_users_email    (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ──────────────────────────────────────────────────────────────────
-- ② children
-- ──────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS children (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    user_id     BIGINT      NOT NULL,
    name        VARCHAR(50) NOT NULL,
    birth_date  DATE        NULL,
    gender      VARCHAR(10) NULL,
    note        TEXT        NULL,
    reg_date    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_children_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ──────────────────────────────────────────────────────────────────
-- ③ checklists
-- ──────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS checklists (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    `CODE`        VARCHAR(50)  NOT NULL,
    title         VARCHAR(200) NOT NULL,
    `DESCRIPTION` TEXT         NULL,
    reg_date      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_checklists_code (`CODE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ──────────────────────────────────────────────────────────────────
-- ④ checklist_questions
-- ──────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS checklist_questions (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    checklist_id  BIGINT       NOT NULL,
    `CODE`        VARCHAR(20)  NOT NULL,
    question_text VARCHAR(500) NOT NULL,
    help_text     VARCHAR(500) NULL,
    response_type VARCHAR(20)  NOT NULL DEFAULT 'SCALE5'   COMMENT 'SCALE5 / YN / TEXT',
    options_json  JSON         NULL,
    weight        INT          NOT NULL DEFAULT 1,
    sort_order    INT          NOT NULL DEFAULT 0,
    domain_code   VARCHAR(30)  NOT NULL DEFAULT 'COMMUNICATION'
                               COMMENT 'COMMUNICATION / SENSORY_DAILY / BEHAVIOR_EMOTION / MOTOR_FINE / PLAY_SOCIAL',
    PRIMARY KEY (id),
    UNIQUE KEY uq_cq_checklist_code (checklist_id, `CODE`),
    INDEX idx_cq_checklist_id (checklist_id),
    INDEX idx_cq_domain       (domain_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ──────────────────────────────────────────────────────────────────
-- ⑤ checklist_runs
-- ──────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS checklist_runs (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    checklist_id   BIGINT      NOT NULL,
    child_id       BIGINT      NOT NULL,
    user_id        BIGINT      NOT NULL,
    `STATUS`       VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
                               COMMENT 'DRAFT / SUBMITTED / DISCARDED',
    total_score    INT         NOT NULL DEFAULT 0,
    submitted_date DATETIME    NULL,
    reg_date       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_runs_user_status (user_id, `STATUS`),
    INDEX idx_runs_child       (child_id),
    INDEX idx_runs_update_date (update_date DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ──────────────────────────────────────────────────────────────────
-- ⑥ checklist_answers
-- ──────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS checklist_answers (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    run_id       BIGINT      NOT NULL,
    question_id  BIGINT      NOT NULL,
    answer_value VARCHAR(20) NULL,
    answer_text  TEXT        NULL,
    score        INT         NULL,
    reg_date     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_answer_run_question (run_id, question_id),
    INDEX idx_answers_run_id (run_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ──────────────────────────────────────────────────────────────────
-- ⑦ therapy_types
-- ──────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS therapy_types (
    code          VARCHAR(30)  NOT NULL,
    title         VARCHAR(100) NOT NULL,
    `DESCRIPTION` TEXT         NULL,
    reg_date      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ──────────────────────────────────────────────────────────────────
-- ⑧ centers
-- ──────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS centers (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    name              VARCHAR(100)  NOT NULL,
    center_type       VARCHAR(30)   NULL,
    phone             VARCHAR(20)   NULL,
    website           VARCHAR(255)  NULL,
    sido              VARCHAR(20)   NULL,
    sigungu           VARCHAR(30)   NULL,
    address           VARCHAR(255)  NULL,
    lat               DECIMAL(10,7) NULL,
    lng               DECIMAL(10,7) NULL,
    `DESCRIPTION`     TEXT          NULL,
    is_active         TINYINT(1)    NOT NULL DEFAULT 1,
    source            VARCHAR(50)   NULL,
    source_updated_at DATE          NULL,
    reg_date          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_centers_sido   (sido),
    INDEX idx_centers_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ──────────────────────────────────────────────────────────────────
-- ⑨ center_services
-- ──────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS center_services (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    center_id         BIGINT       NOT NULL,
    therapy_type_code VARCHAR(30)  NOT NULL,
    service_name      VARCHAR(100) NULL,
    target_age_min    INT          NULL,
    target_age_max    INT          NULL,
    price_type        VARCHAR(20)  NULL     COMMENT 'FREE / PAID / MIXED',
    waitlist          TINYINT(1)   NOT NULL DEFAULT 0,
    waitlist_note     VARCHAR(255) NULL,
    notes             TEXT         NULL,
    reg_date          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_cs_center_id         (center_id),
    INDEX idx_cs_therapy_type_code (therapy_type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ──────────────────────────────────────────────────────────────────
-- ⑩ domain_therapy_map
-- ──────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS domain_therapy_map (
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    domain_code       VARCHAR(30) NOT NULL,
    therapy_type_code VARCHAR(30) NOT NULL,
    priority          INT         NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uq_dtm (domain_code, therapy_type_code),
    INDEX idx_dtm_domain (domain_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ──────────────────────────────────────────────────────────────────
-- ⑪ recommendations
-- ──────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS recommendations (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    `CODE`        VARCHAR(50)  NOT NULL,
    title         VARCHAR(200) NOT NULL,
    `DESCRIPTION` TEXT         NULL,
    category      VARCHAR(50)  NULL,
    reg_date      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_recommendations_code (`CODE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ──────────────────────────────────────────────────────────────────
-- ⑫ run_recommendations
-- ──────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS run_recommendations (
    id                BIGINT   NOT NULL AUTO_INCREMENT,
    run_id            BIGINT   NOT NULL,
    recommendation_id BIGINT   NOT NULL,
    reason_text       TEXT     NULL,
    reg_date          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_rr_run_id (run_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ──────────────────────────────────────────────────────────────────
-- ⑬ run_recommendation_evidence
-- ──────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS run_recommendation_evidence (
    id                    BIGINT   NOT NULL AUTO_INCREMENT,
    run_recommendation_id BIGINT   NOT NULL,
    question_id           BIGINT   NULL,
    evidence_text         TEXT     NULL,
    reg_date              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_rre_run_reco_id (run_recommendation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ──────────────────────────────────────────────────────────────────
-- ⑭ favorites
-- ──────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS favorites (
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    member_id  BIGINT   NOT NULL COMMENT '사용자 PK (users.id)',
    center_id  BIGINT   NOT NULL COMMENT '기관  PK (centers.id)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reg_date   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '정렬용 — created_at와 동일 값 유지',
    PRIMARY KEY (id),
    UNIQUE KEY uq_favorites (member_id, center_id),
    CONSTRAINT fk_fav_member FOREIGN KEY (member_id) REFERENCES users(id)   ON DELETE CASCADE,
    CONSTRAINT fk_fav_center FOREIGN KEY (center_id) REFERENCES centers(id) ON DELETE CASCADE,
    INDEX idx_fav_member_reg (member_id, reg_date DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Seed Data
-- ============================================================

-- therapy_types (TherapyTypeCode enum 기준)
INSERT IGNORE INTO therapy_types (code, title) VALUES
  ('SPEECH_THERAPY', '언어치료'),
  ('AAC_COACHING',   'AAC 코칭/도구 세팅'),
  ('OT_SENSORY',     '작업치료(감각·일상)'),
  ('OT_FINE',        '작업치료(미세·협응)'),
  ('ABA_PARENT',     '행동상담/부모코칭(ABA)'),
  ('PLAY_THERAPY',   '놀이치료·사회성'),
  ('PSY_COUNSEL',    '심리·정서 상담');

-- domain_therapy_map (ChecklistDomain ↔ TherapyTypeCode 매핑)
INSERT IGNORE INTO domain_therapy_map (domain_code, therapy_type_code, priority) VALUES
  ('COMMUNICATION',    'SPEECH_THERAPY', 1),
  ('COMMUNICATION',    'AAC_COACHING',   2),
  ('SENSORY_DAILY',    'OT_SENSORY',     1),
  ('BEHAVIOR_EMOTION', 'ABA_PARENT',     1),
  ('BEHAVIOR_EMOTION', 'PSY_COUNSEL',    2),
  ('MOTOR_FINE',       'OT_FINE',        1),
  ('MOTOR_FINE',       'OT_SENSORY',     2),
  ('PLAY_SOCIAL',      'PLAY_THERAPY',   1),
  ('PLAY_SOCIAL',      'PSY_COUNSEL',    2);

-- recommendations (RunRecommendationService.domainRecoCode 코드 기준)
INSERT IGNORE INTO recommendations (`CODE`, title, `DESCRIPTION`, category) VALUES
  ('RECO_COMMUNICATION_AAC',           '의사소통·AAC 지원',        '의사소통 영역 약점이 확인되었습니다. 언어치료·AAC 코칭 전문가 상담을 권장합니다.', 'COMMUNICATION'),
  ('RECO_SENSORY_DAILY_ROUTINE',       '감각·일상 루틴 구성',      '감각·일상 영역 지원이 필요합니다. 작업치료(감각통합) 전문가 상담을 권장합니다.',   'SENSORY_DAILY'),
  ('RECO_BEHAVIOR_EMOTION_REGULATION', '행동·정서 조절 지원',      '행동·정서 영역 개입이 권장됩니다. 행동상담(ABA) 또는 심리 상담을 고려하세요.',    'BEHAVIOR_EMOTION'),
  ('RECO_MOTOR_FINE_ACCESS',           '소근육·협응 운동 지원',    '소근육·미세운동 영역 지원이 필요합니다. 작업치료 전문가 상담을 권장합니다.',        'MOTOR_FINE'),
  ('RECO_PLAY_SOCIAL_JOINT',           '놀이·사회성 향상',         '놀이·사회성 영역 지원이 권장됩니다. 놀이치료·사회성 그룹 프로그램을 참고하세요.',    'PLAY_SOCIAL'),
  ('RECO_SAFETY_PLAN',                 '안전 계획 최우선 확인',    '최근 위험 신호가 확인되었습니다. 즉시 전문가·지역 지원팀과 상담하시기 바랍니다.',   'SAFETY'),
  ('RECO_REINFORCER_LIST',             '강화물 목록 확보',         '효과적인 강화물 목록을 구체적으로 확보하는 것을 우선 권장합니다.',                  'BEHAVIOR');

-- checklists — 기본 체크리스트 1건
INSERT IGNORE INTO checklists (`CODE`, title, `DESCRIPTION`) VALUES
  ('DEV_SCREEN_v1', '아동 발달 선별 체크리스트',
   '의사소통·감각·행동·운동·사회성 5개 영역 30문항 체크리스트');

-- checklist_questions — Q1-Q30 (5개 도메인 × 6문항)
-- SCALE5: 1=불가능, 2=거의불가능, 3=거의가능, 4=가능함, 5=모름
-- YN:     4=예,     1=아니오
-- RunRecommendationService.domainCodeByQ: Q1-6 COMMUNICATION, Q7-12 SENSORY_DAILY, Q13-18 BEHAVIOR_EMOTION, Q19-24 MOTOR_FINE, Q25-30 PLAY_SOCIAL

INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q1', '이름을 부르면 반응하나요?','SCALE5',1,1,'COMMUNICATION' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q2', '간단한 지시(앉아/와봐)를 따르나요?','SCALE5',1,2,'COMMUNICATION' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q3', '원하는 것을 말이나 몸짓으로 요청하나요?','SCALE5',1,3,'COMMUNICATION' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q4', '두 단어 이상 조합하여 말하나요?','SCALE5',1,4,'COMMUNICATION' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q5', '질문에 예/아니오로 대답하나요?','SCALE5',1,5,'COMMUNICATION' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q6', '상황에 맞게 타인과 대화를 주고받을 수 있나요?','SCALE5',1,6,'COMMUNICATION' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q7', '특정 소리·냄새·촉감에 과민 반응을 보이나요?','SCALE5',1,7,'SENSORY_DAILY' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q8', '양치·세안·목욕 등 위생 관리를 허용하나요?','SCALE5',1,8,'SENSORY_DAILY' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q9', '옷 입기·신발 신기를 스스로 시도하나요?','SCALE5',1,9,'SENSORY_DAILY' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q10','식사 중 다양한 음식 질감을 받아들이나요?','SCALE5',1,10,'SENSORY_DAILY' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q11','낯선 장소나 새로운 루틴 변화에 적응하나요?','SCALE5',1,11,'SENSORY_DAILY' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q12','일상 루틴(등원·식사·취침)을 예측 가능하게 따르나요?','SCALE5',1,12,'SENSORY_DAILY' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q13','최근 7일 안에 자해·타해 위험 행동이 있었나요?','YN',2,13,'BEHAVIOR_EMOTION' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q14','활동 전환(그만하기·기다리기) 시 심한 폭발이 있나요?','SCALE5',1,14,'BEHAVIOR_EMOTION' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q15','좌절 상황에서 스스로 감정을 조절하나요?','SCALE5',1,15,'BEHAVIOR_EMOTION' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q16','효과적인 강화물을 구체 목록으로 확보했나요?','YN',1,16,'BEHAVIOR_EMOTION' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q17','반복·의례적 행동이 일상을 크게 방해하나요?','SCALE5',1,17,'BEHAVIOR_EMOTION' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q18','기분 변화가 예측 가능한 편인가요?','SCALE5',1,18,'BEHAVIOR_EMOTION' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q19','연필·크레용 쥐고 낙서·선 긋기가 되나요?','SCALE5',1,19,'MOTOR_FINE' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q20','가위로 종이를 자르는 시도를 하나요?','SCALE5',1,20,'MOTOR_FINE' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q21','단추·지퍼·끈 묶기 등 조작 활동을 시도하나요?','SCALE5',1,21,'MOTOR_FINE' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q22','젓가락·숟가락으로 식사를 스스로 하나요?','SCALE5',1,22,'MOTOR_FINE' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q23','블록·퍼즐 맞추기 등 조립 놀이를 하나요?','SCALE5',1,23,'MOTOR_FINE' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q24','두 손을 함께 쓰는 양손 협응이 가능한가요?','SCALE5',1,24,'MOTOR_FINE' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q25','타인의 시선이나 손짓을 따라 보나요(공동주의)?','SCALE5',1,25,'PLAY_SOCIAL' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q26','또래와 같은 장난감·게임으로 함께 놀 수 있나요?','SCALE5',1,26,'PLAY_SOCIAL' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q27','순서 기다리기·규칙 있는 게임에 참여하나요?','SCALE5',1,27,'PLAY_SOCIAL' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q28','상상놀이(소꿉·역할극)에 참여하나요?','SCALE5',1,28,'PLAY_SOCIAL' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q29','낯선 성인에게 먼저 다가가거나 인사하나요?','SCALE5',1,29,'PLAY_SOCIAL' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
INSERT IGNORE INTO checklist_questions (checklist_id, `CODE`, question_text, response_type, weight, sort_order, domain_code)
SELECT id,'Q30','또래와의 갈등 시 타협·양보가 되나요?','SCALE5',1,30,'PLAY_SOCIAL' FROM checklists WHERE `CODE`='DEV_SCREEN_v1';
