-- phone, profile_image 컬럼 추가 (MySQL 8.0+: IF NOT EXISTS 지원)
ALTER TABLE users
  ADD COLUMN IF NOT EXISTS phone         VARCHAR(20)  NULL AFTER email,
  ADD COLUMN IF NOT EXISTS profile_image VARCHAR(255) NULL AFTER phone;
