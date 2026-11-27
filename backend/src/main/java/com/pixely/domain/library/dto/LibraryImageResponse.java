package com.pixely.domain.library.dto;

import com.pixely.domain.library.LibraryImage;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public record LibraryImageResponse(Long id, String type, String title, List<String> tags, String imageUrl,
                                   Integer width, Integer height, boolean mine, boolean sample,
                                   LocalDateTime createdAt) {

    public static LibraryImageResponse of(LibraryImage i, Long viewerId) {
        List<String> tags = i.getTags() == null || i.getTags().isBlank() ? List.of()
                : Arrays.stream(i.getTags().split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        boolean mine = viewerId != null && viewerId.equals(i.getOwnerId());
        return new LibraryImageResponse(i.getId(), i.getType().name(), i.getTitle(), tags, i.getImageUrl(),
                i.getWidth(), i.getHeight(), mine, i.isPublicSample(), i.getCreatedAt());
    }
}
