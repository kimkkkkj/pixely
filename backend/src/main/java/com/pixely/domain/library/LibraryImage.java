package com.pixely.domain.library;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/** 상품/모델 이미지. ownerId 가 null 이면 관리자가 관리하는 공용 샘플 */
@Entity
@Table(name = "library_images", indexes = {
        @Index(name = "idx_library_type_owner", columnList = "type, ownerId")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LibraryImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ImageType type;

    @Column(nullable = false, length = 100)
    private String title;

    /** 쉼표로 구분한 태그 (예: "스킨케어,크림") */
    @Column(length = 300)
    private String tags;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    private Integer width;

    private Integer height;

    private Long ownerId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public boolean isPublicSample() {
        return ownerId == null;
    }
}
