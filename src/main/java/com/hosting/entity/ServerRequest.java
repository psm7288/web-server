package com.hosting.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;

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
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private String requestType;
    private String serverName;
    private String webServerName;
    private String dbServerName;

    @Builder.Default
    private String image = "Ubuntu-24.04";

    @Builder.Default
    private String flavor = "c2.r4.d50";

    @Builder.Default
    private String webFlavor = "c2.r4.d50";

    @Builder.Default
    private String dbFlavor = "c2.r4.d50";

    @Builder.Default
    private String networkName = "selfservice";

    @Builder.Default
    private String keyName = "mykey";

    @Builder.Default
    private String targetHost = "compute-PowerEdge-T360";

    private String dbName;
    private String dbUsername; // DB 테이블의 db_username 필드 대응
    private String dbUser;     // DB 테이블의 db_user 필드 대응
    private String dbPassword;

    @Column(columnDefinition = "TINYINT(1)")
    @Builder.Default
    private boolean needDb = false;

    @Builder.Default
    private String status = "PENDING";

    @Column(columnDefinition = "TEXT")
    private String errorMsg;

    private String webDesc;

    // 사양 정보 (DB 타입이 int이므로 Integer로 변경)
    @Builder.Default
    private Integer webCpu = 2;
    @Builder.Default
    private Integer webRam = 4;
    @Builder.Default
    private Integer webStorage = 50;
    @Builder.Default
    private Integer webPrice = 0;

    @Builder.Default
    private Integer dbCpu = 2;
    @Builder.Default
    private Integer dbRam = 4;
    @Builder.Default
    private Integer dbStorage = 50;
    @Builder.Default
    private Integer dbPrice = 0;

    @Builder.Default
    private Integer totalPrice = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 추가: Server 엔티티와의 연관관계 설정
    @OneToMany(mappedBy = "serverRequest", cascade = CascadeType.ALL)
    private List<Server> servers = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = "PENDING";
    }
}