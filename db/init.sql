-- =====================================================================
-- Pixely DB / 전용 계정 생성 스크립트 (로컬 MySQL 8 용)
-- 실행 (PowerShell, root 비밀번호 입력):
--   Get-Content db\init.sql | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -uroot -p
-- 비밀번호는 .env 의 DB_PASSWORD 와 맞춰서 아래 'change-me' 를 바꾸세요.
-- 테이블은 Spring JPA(ddl-auto=update)가 첫 실행 시 자동 생성합니다.
-- =====================================================================
CREATE DATABASE IF NOT EXISTS pixely
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'pixely'@'localhost' IDENTIFIED BY 'change-me';
CREATE USER IF NOT EXISTS 'pixely'@'%' IDENTIFIED BY 'change-me';

GRANT ALL PRIVILEGES ON pixely.* TO 'pixely'@'localhost';
GRANT ALL PRIVILEGES ON pixely.* TO 'pixely'@'%';
FLUSH PRIVILEGES;
