package com.hosting.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "server_orders")
@Getter @Setter
public class ServerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 회원 매핑용 (추후 로그인 세션 연동)
    private String username;

    // 웹서버 설정 항목
    private String webName;
    private String webDesc;
    private int webCpu;
    private int webRam;
    private int webStorage;

    // DB서버 설정 항목
    private String dbName;
    private String dbSchemaName;
    private String dbUser;
    private String dbPassword; // ⚠️ 실무에선 암호화 필요
    private int dbCpu;
    private int dbRam;
    private int dbStorage;

    // 현재 진행 상태 (PENDING, RUNNING, ERROR 등)
    private String status;
    private LocalDateTime createdAt;
}