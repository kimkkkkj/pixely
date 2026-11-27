package com.pixely.domain.generation;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "generations", indexes = {
        @Index(name = "idx_generation_user", columnList = "userId, id"),
        @Index(name = "idx_generation_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Generation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long productImageId;

    @Column(nullable = false)
    private Long modelImageId;

    @Column(length = 1000)
    private String prompt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private GenerationStatus status;

    @Column(length = 500)
    private String resultUrl;

    @Column(length = 500)
    private String errorMessage;

    @Column(length = 20)
    private String provider;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime finishedAt;

    public void startProcessing(String providerName) {
        this.status = GenerationStatus.PROCESSING;
        this.provider = providerName;
    }

    public void complete(String resultUrl) {
        this.status = GenerationStatus.DONE;
        this.resultUrl = resultUrl;
        this.errorMessage = null;
        this.finishedAt = LocalDateTime.now();
    }

    public void fail(String message) {
        this.status = GenerationStatus.FAILED;
        this.errorMessage = message == null ? null : (message.length() > 500 ? message.substring(0, 500) : message);
        this.finishedAt = LocalDateTime.now();
    }
}
