package com.hosting.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "servers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Server {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serverId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private ServerRequest serverRequest;

    private String osServerId;

    @Column(nullable = false)
    private String serverType; // web 또는 db

    private String internalIp;
    private String externalIp;
    private String image;
    private String flavor;
    private String status;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}