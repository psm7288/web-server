package com.hosting.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "automation_logs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AutomationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private ServerRequest serverRequest;

    private Integer step;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String status;

    @Builder.Default
    private LocalDateTime loggedAt = LocalDateTime.now();
}