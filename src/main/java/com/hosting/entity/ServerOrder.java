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

    // [추가] 어떤 신청서(Request)를 통해 생성된 서버인지 연결
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private ServerRequest serverRequest;

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
    private String dbPassword;
    private int dbCpu;
    private int dbRam;
    private int dbStorage;

    private String status;
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}