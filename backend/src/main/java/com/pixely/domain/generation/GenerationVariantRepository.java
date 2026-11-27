package com.pixely.domain.generation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GenerationVariantRepository extends JpaRepository<GenerationVariant, Long> {

    List<GenerationVariant> findByGenerationIdOrderByIdAsc(Long generationId);

    List<GenerationVariant> findByGenerationIdIn(Collection<Long> generationIds);

    Optional<GenerationVariant> findByGenerationIdAndFilterType(Long generationId, FilterType filterType);
}
