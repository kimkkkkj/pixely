package com.pixely.domain.share;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ShareLinkRepository extends JpaRepository<ShareLink, Long> {

    Optional<ShareLink> findByToken(String token);

    Optional<ShareLink> findFirstByGenerationIdAndExpiresAtAfterOrderByIdDesc(Long generationId, LocalDateTime now);
}
