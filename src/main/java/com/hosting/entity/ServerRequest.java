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
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private String serverName;
    private String requestType; // "WEB" or "DB"
    private String status = "PENDING";

    @Column(columnDefinition = "TEXT")
    private String errorMsg;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() { this.createdAt = LocalDateTime.now(); }
}