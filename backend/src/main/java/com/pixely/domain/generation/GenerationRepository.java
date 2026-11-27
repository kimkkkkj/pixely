package com.pixely.domain.generation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;

public interface GenerationRepository extends JpaRepository<Generation, Long> {

    Page<Generation> findByUserId(Long userId, Pageable pageable);

    Page<Generation> findByStatus(GenerationStatus status, Pageable pageable);

    /** 서버 재시작 등으로 중단된 작업을 FAILED 로 정리 */
    @Transactional
    @Modifying
    @Query("""
            update Generation g set g.status = com.pixely.domain.generation.GenerationStatus.FAILED,
                   g.errorMessage = :message, g.finishedAt = :now
            where g.status in :statuses
            """)
    int failStale(@Param("statuses") Collection<GenerationStatus> statuses, @Param("message") String message,
                  @Param("now") LocalDateTime now);
}
