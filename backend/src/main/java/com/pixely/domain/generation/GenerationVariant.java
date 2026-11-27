package com.pixely.domain.generation;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/** 생성 결과에 필터를 적용해 저장한 이미지 (generation 당 필터 종류별 1개) */
@Entity
@Table(name = "generation_variants", uniqueConstraints = @UniqueConstraint(
        name = "uk_variant_generation_filter", columnNames = {"generationId", "filterType"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GenerationVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long generationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private FilterType filterType;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void replaceImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
