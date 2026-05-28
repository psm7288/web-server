package com.hosting.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "server_requests")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ServerRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 결제창에 출력될 핵심 필드들
    private String webServerName;
    private String dbServerName;

    private String webDesc;
    private String dbName;
    private String dbUser;
    private String dbPassword;

    // 사양 정보
    private String webCpu;
    private String webRam;
    private String webStorage;
    private String dbCpu;
    private String dbRam;
    private String dbStorage;

    @Builder.Default
    private String image = "Ubuntu 24.04 LTS";

    @Builder.Default
    private String flavor = "m1.small";

    @Builder.Default
    private boolean needDb = true;

    @Builder.Default
    private String status = "WAITING_PAYMENT";

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}