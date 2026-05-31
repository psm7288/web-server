package com.hosting.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "db_server_info")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class DbServerInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dbInfoId;

    private String dbConsoleUrl; // OpenStack noVNC URL 저장용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private ServerRequest serverRequest;

    private String dbName;
    private String dbUsername;
    private String dbPassword;
    private String dbHost;

    @Builder.Default
    private Integer dbPort = 3306;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}