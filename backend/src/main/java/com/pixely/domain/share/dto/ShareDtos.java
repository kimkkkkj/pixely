package com.pixely.domain.share.dto;

import com.pixely.domain.generation.dto.GenerationDtos.VariantResponse;

import java.time.LocalDateTime;
import java.util.List;

public final class ShareDtos {

    private ShareDtos() {}

    public record ShareLinkResponse(String token, String url, LocalDateTime expiresAt) {}

    public record SharedGenerationResponse(String token, Long generationId, String prompt, String resultUrl,
                                           List<VariantResponse> variants, String creatorNickname,
                                           LocalDateTime createdAt, LocalDateTime expiresAt) {}
}
