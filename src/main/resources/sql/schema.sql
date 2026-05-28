CREATE DATABASE IF NOT EXISTS hosting_db DEFAULT CHARACTER SET utf8mb4;
USE hosting_db;

-- 회원 정보
CREATE TABLE members (
                         member_id  BIGINT       NOT NULL AUTO_INCREMENT,
                         username   VARCHAR(50)  NOT NULL UNIQUE,
                         password   VARCHAR(255) NOT NULL,
                         name       VARCHAR(100) NOT NULL,
                         email      VARCHAR(100) NOT NULL UNIQUE,
                         phone      VARCHAR(20)  NOT NULL,
                         role       VARCHAR(20)  NOT NULL DEFAULT 'USER',
                         created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         PRIMARY KEY (member_id)
);

-- 신청 내역
CREATE TABLE server_requests (
                                 request_id   BIGINT       NOT NULL AUTO_INCREMENT,
                                 member_id    BIGINT       NOT NULL,
                                 request_type VARCHAR(10)  NOT NULL,
                                 server_name  VARCHAR(100) NOT NULL,
                                 status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
                                 error_msg    TEXT         NULL,
                                 created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 PRIMARY KEY (request_id),
                                 FOREIGN KEY (member_id) REFERENCES members(member_id)
);

-- 인스턴스 상세 정보
CREATE TABLE servers (
                         server_id    BIGINT       NOT NULL AUTO_INCREMENT,
                         request_id   BIGINT       NOT NULL,
                         os_server_id VARCHAR(100) NULL,
                         server_type  VARCHAR(10)  NOT NULL,
                         internal_ip  VARCHAR(45)  NULL,
                         external_ip  VARCHAR(45)  NULL,
                         image        VARCHAR(100) NOT NULL,
                         flavor       VARCHAR(100) NOT NULL,
                         PRIMARY KEY (server_id),
                         FOREIGN KEY (request_id) REFERENCES server_requests(request_id)
);

-- 자동화 로그
CREATE TABLE automation_logs (
                                 log_id     BIGINT      NOT NULL AUTO_INCREMENT,
                                 request_id BIGINT      NOT NULL,
                                 step       INT         NOT NULL,
                                 message    TEXT        NOT NULL,
                                 status     VARCHAR(10) NOT NULL,
                                 logged_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 PRIMARY KEY (log_id),
                                 FOREIGN KEY (request_id) REFERENCES server_requests(request_id)
);

-- DB 접속 정보
CREATE TABLE db_server_info (
                                db_info_id  BIGINT       NOT NULL AUTO_INCREMENT,
                                server_id   BIGINT       NOT NULL,
                                request_id  BIGINT       NOT NULL,
                                db_name     VARCHAR(50)  NOT NULL,
                                db_username VARCHAR(50)  NOT NULL,
                                db_password VARCHAR(255) NOT NULL,
                                db_host     VARCHAR(45)  NOT NULL,
                                db_port     INT          NOT NULL DEFAULT 3306,
                                PRIMARY KEY (db_info_id),
                                FOREIGN KEY (server_id)  REFERENCES servers(server_id),
                                FOREIGN KEY (request_id) REFERENCES server_requests(request_id)
);

CREATE DATABASE IF NOT EXISTS hosting_service DEFAULT CHARACTER SET utf8mb4;
USE hosting_service;

-- 1. 서비스 신청 내역 테이블
CREATE TABLE server_requests (
                                 request_id      BIGINT(20)   NOT NULL AUTO_INCREMENT,
                                 member_id       BIGINT(20)   NOT NULL,
                                 request_type    VARCHAR(255) DEFAULT NULL,
                                 server_name     VARCHAR(255) DEFAULT NULL,
                                 web_server_name VARCHAR(100) DEFAULT NULL,
                                 db_server_name  VARCHAR(100) DEFAULT NULL,
                                 image           VARCHAR(100) DEFAULT 'Ubuntu-24.04',
                                 flavor          VARCHAR(100) DEFAULT 'm1.small',
                                 network_name    VARCHAR(100) DEFAULT 'selfservice',
                                 key_name        VARCHAR(100) DEFAULT 'mykey',
                                 target_host     VARCHAR(100) DEFAULT 'compute-PowerEdge-T360',
                                 db_name         VARCHAR(50)  DEFAULT NULL,
                                 db_username     VARCHAR(50)  DEFAULT NULL,
                                 need_db         TINYINT(1)   DEFAULT 0,
                                 status          VARCHAR(255) DEFAULT 'PENDING',
                                 error_msg       TEXT         DEFAULT NULL,
                                 created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 PRIMARY KEY (request_id),
                                 INDEX idx_member (member_id)
);

-- 2. 생성된 인스턴스 정보 테이블
CREATE TABLE servers (
                         server_id    BIGINT(20)   NOT NULL AUTO_INCREMENT,
                         request_id   BIGINT(20)   NOT NULL,
                         os_server_id VARCHAR(100) DEFAULT NULL,
                         server_type  ENUM('web', 'db') NOT NULL,
                         internal_ip  VARCHAR(45)  DEFAULT NULL,
                         external_ip  VARCHAR(45)  DEFAULT NULL,
                         image        VARCHAR(100) NOT NULL,
                         flavor       VARCHAR(100) NOT NULL,
                         status       VARCHAR(50)  DEFAULT 'BUILDING',
                         created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         PRIMARY KEY (server_id),
                         FOREIGN KEY (request_id) REFERENCES server_requests(request_id) ON DELETE CASCADE
);

-- 3. DB 접속 정보 상세 테이블
CREATE TABLE db_server_info (
                                db_info_id  BIGINT(20)   NOT NULL AUTO_INCREMENT,
                                server_id   BIGINT(20)   NOT NULL,
                                request_id  BIGINT(20)   NOT NULL,
                                db_name     VARCHAR(50)  NOT NULL,
                                db_username VARCHAR(50)  NOT NULL,
                                db_password VARCHAR(255) NOT NULL,
                                db_host     VARCHAR(45)  NOT NULL,
                                db_port     INT(11)      NOT NULL DEFAULT 3306,
                                created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (db_info_id),
                                FOREIGN KEY (server_id) REFERENCES servers(server_id) ON DELETE CASCADE,
                                FOREIGN KEY (request_id) REFERENCES server_requests(request_id) ON DELETE CASCADE
);

-- 4. 인프라 구축 자동화 로그 테이블
CREATE TABLE automation_logs (
                                 log_id     BIGINT(20)  NOT NULL AUTO_INCREMENT,
                                 request_id BIGINT(20)  NOT NULL,
                                 step       INT(11)     NOT NULL,
                                 message    TEXT        NOT NULL,
                                 status     VARCHAR(50) NOT NULL,
                                 logged_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 PRIMARY KEY (log_id),
                                 FOREIGN KEY (request_id) REFERENCES server_requests(request_id) ON DELETE CASCADE
);