-- See 프로젝트 초기 데이터베이스 설정

-- 데이터베이스 사용
USE see_dev;

-- 기본 사용자 권한 부여 (이미 존재하는 사용자)
GRANT ALL PRIVILEGES ON see_dev.* TO 'see_user'@'localhost';
FLUSH PRIVILEGES;

-- 테이블은 JPA가 자동으로 생성하므로 여기서는 기본 설정만 진행
SELECT 'Database initialization completed!' AS status;
