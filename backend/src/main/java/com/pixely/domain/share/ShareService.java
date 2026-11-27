package com.pixely.domain.share;

import com.pixely.domain.generation.Generation;
import com.pixely.domain.generation.GenerationRepository;
import com.pixely.domain.generation.GenerationStatus;
import com.pixely.domain.generation.GenerationVariantRepository;
import com.pixely.domain.generation.dto.GenerationDtos.VariantResponse;
import com.pixely.domain.share.dto.ShareDtos.ShareLinkResponse;
import com.pixely.domain.share.dto.ShareDtos.SharedGenerationResponse;
import com.pixely.domain.user.User;
import com.pixely.domain.user.UserRepository;
import com.pixely.global.config.PixelyProperties;
import com.pixely.global.exception.BusinessException;
import com.pixely.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShareService {

    private static final int VALID_DAYS = 30;

    private final ShareLinkRepository shareLinkRepository;
    private final GenerationRepository generationRepository;
    private final GenerationVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final PixelyProperties properties;

    /** 유효한 링크가 있으면 재사용, 없으면 새 토큰 발급 */
    @Transactional
    public ShareLinkResponse create(Generation generation) {
        if (generation.getStatus() != GenerationStatus.DONE) throw new BusinessException(ErrorCode.GENERATION_NOT_READY);
        ShareLink link = shareLinkRepository
                .findFirstByGenerationIdAndExpiresAtAfterOrderByIdDesc(generation.getId(), LocalDateTime.now().plusDays(1))
                .orElseGet(() -> shareLinkRepository.save(ShareLink.builder()
                        .generationId(generation.getId())
                        .token(UUID.randomUUID().toString())
                        .expiresAt(LocalDateTime.now().plusDays(VALID_DAYS))
                        .build()));
        return new ShareLinkResponse(link.getToken(), buildUrl(link.getToken()), link.getExpiresAt());
    }

    @Transactional(readOnly = true)
    public SharedGenerationResponse getPublic(String token) {
        ShareLink link = shareLinkRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 공유 링크입니다."));
        if (link.isExpired()) throw new BusinessException(ErrorCode.SHARE_EXPIRED);
        Generation g = generationRepository.findById(link.getGenerationId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "공유된 이미지가 삭제되었습니다."));
        String nickname = userRepository.findById(g.getUserId()).map(User::getNickname).orElse("Pixely 사용자");
        return new SharedGenerationResponse(link.getToken(), g.getId(), g.getPrompt(), g.getResultUrl(),
                variantRepository.findByGenerationIdOrderByIdAsc(g.getId()).stream().map(VariantResponse::of).toList(),
                nickname, g.getCreatedAt(), link.getExpiresAt());
    }

    private String buildUrl(String token) {
        String base = properties.publicBaseUrl() == null ? "" : properties.publicBaseUrl().replaceAll("/+$", "");
        return base + "/share/" + token;
    }
}
