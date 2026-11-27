package com.pixely.domain.generation.dto;

import com.pixely.domain.generation.FilterType;
import com.pixely.domain.generation.Generation;
import com.pixely.domain.generation.GenerationVariant;
import com.pixely.domain.library.LibraryImage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public final class GenerationDtos {

    private GenerationDtos() {}

    public record CreateRequest(
            @NotNull(message = "상품 이미지를 선택해 주세요.") Long productImageId,
            @NotNull(message = "모델 이미지를 선택해 주세요.") Long modelImageId,
            @Size(max = 1000, message = "요청사항은 1000자 이하로 입력해 주세요.") String prompt
    ) {}

    public record VariantRequest(
            @NotNull(message = "필터 종류를 선택해 주세요.") FilterType filterType,
            @NotBlank(message = "이미지 데이터가 비어 있습니다.") String imageBase64
    ) {}

    public record ImageSummary(Long id, String title, String imageUrl) {
        public static ImageSummary of(LibraryImage i) {
            return i == null ? null : new ImageSummary(i.getId(), i.getTitle(), i.getImageUrl());
        }
    }

    public record VariantResponse(Long id, String filterType, String imageUrl) {
        public static VariantResponse of(GenerationVariant v) {
            return new VariantResponse(v.getId(), v.getFilterType().name(), v.getImageUrl());
        }
    }

    public record GenerationResponse(Long id, String status, String prompt, String provider, String resultUrl,
                                     String errorMessage, LocalDateTime createdAt, LocalDateTime finishedAt,
                                     ImageSummary productImage, ImageSummary modelImage,
                                     List<VariantResponse> variants, String userEmail) {

        public static GenerationResponse of(Generation g, LibraryImage product, LibraryImage model,
                                            List<GenerationVariant> variants, String userEmail) {
            return new GenerationResponse(g.getId(), g.getStatus().name(), g.getPrompt(), g.getProvider(),
                    g.getResultUrl(), g.getErrorMessage(), g.getCreatedAt(), g.getFinishedAt(),
                    ImageSummary.of(product), ImageSummary.of(model),
                    variants == null ? List.of() : variants.stream().map(VariantResponse::of).toList(),
                    userEmail);
        }
    }

    public record CreatedResponse(Long generationId, String status) {}
}
