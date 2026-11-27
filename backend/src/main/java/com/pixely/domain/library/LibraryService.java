package com.pixely.domain.library;

import com.pixely.domain.library.dto.LibraryImageResponse;
import com.pixely.global.common.PageResponse;
import com.pixely.global.exception.BusinessException;
import com.pixely.global.exception.ErrorCode;
import com.pixely.global.image.ImageUtils;
import com.pixely.global.security.UserPrincipal;
import com.pixely.global.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LibraryService {

    public static final long MAX_FILE_BYTES = 10L * 1024 * 1024;
    public static final int MAX_DIMENSION = 1600;
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/jpg", "image/png", "image/webp");

    private final LibraryImageRepository repository;
    private final StorageService storage;

    @Transactional(readOnly = true)
    public PageResponse<LibraryImageResponse> search(ImageType type, String q, int page, int size, Long viewerId) {
        var pageable = PageRequest.of(Math.max(0, page), clampSize(size), Sort.by(Sort.Direction.DESC, "id"));
        return PageResponse.of(repository.searchVisible(type, viewerId, likePattern(q), pageable),
                i -> LibraryImageResponse.of(i, viewerId));
    }

    @Transactional(readOnly = true)
    public PageResponse<LibraryImageResponse> searchPublic(ImageType type, String q, int page, int size) {
        var pageable = PageRequest.of(Math.max(0, page), clampSize(size), Sort.by(Sort.Direction.DESC, "id"));
        return PageResponse.of(repository.searchPublic(type, likePattern(q), pageable),
                i -> LibraryImageResponse.of(i, null));
    }

    /** ownerId 가 null 이면 공용 샘플로 저장 (관리자 전용 경로에서만 호출) */
    @Transactional
    public LibraryImageResponse upload(MultipartFile file, ImageType type, String title, String tags, Long ownerId) {
        validateFile(file);
        byte[] raw;
        try {
            raw = file.getBytes();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INVALID_FILE);
        }
        String resolvedTitle = (title == null || title.isBlank()) ? baseName(file.getOriginalFilename()) : title.trim();
        return LibraryImageResponse.of(saveImage(raw, type, resolvedTitle, tags, ownerId), ownerId);
    }

    /** 시드/업로드 공통: 디코딩 → 리사이즈 → 저장 */
    @Transactional
    public LibraryImage saveImage(byte[] raw, ImageType type, String title, String tags, Long ownerId) {
        BufferedImage decoded = ImageUtils.decode(raw);
        BufferedImage resized = ImageUtils.resizeToMax(decoded, MAX_DIMENSION);
        ImageUtils.Encoded encoded = ImageUtils.encodeAuto(resized, ImageUtils.hasAlpha(decoded));
        String key = storage.store("library", encoded.bytes(), encoded.extension());
        return repository.save(LibraryImage.builder()
                .type(type)
                .title(truncate(title, 100))
                .tags(normalizeTags(tags))
                .imageUrl(storage.toUrl(key))
                .width(resized.getWidth())
                .height(resized.getHeight())
                .ownerId(ownerId)
                .build());
    }

    @Transactional
    public void deleteOwn(Long id, UserPrincipal user) {
        LibraryImage image = repository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        boolean isOwner = image.getOwnerId() != null && image.getOwnerId().equals(user.id());
        if (!isOwner && !(user.isAdmin() && image.isPublicSample())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "내가 올린 이미지만 삭제할 수 있습니다.");
        }
        delete(image);
    }

    @Transactional
    public void deletePublic(Long id) {
        LibraryImage image = repository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (!image.isPublicSample()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "공용 샘플만 관리자 메뉴에서 삭제할 수 있습니다.");
        }
        delete(image);
    }

    private void delete(LibraryImage image) {
        repository.delete(image);
        storage.delete(storage.toKey(image.getImageUrl()));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "업로드할 이미지를 선택해 주세요.");
        }
        if (file.getSize() > MAX_FILE_BYTES) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }
        String ext = extension(file.getOriginalFilename());
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXT.contains(ext) || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.INVALID_FILE);
        }
    }

    private static String likePattern(String q) {
        if (q == null || q.isBlank()) return null;
        String escaped = q.trim().toLowerCase(Locale.ROOT).replace("%", "").replace("_", "");
        return escaped.isEmpty() ? null : "%" + escaped + "%";
    }

    private static int clampSize(int size) {
        return Math.min(Math.max(size, 1), 60);
    }

    private static String extension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private static String baseName(String filename) {
        if (filename == null || filename.isBlank()) return "내 이미지";
        String name = filename.replaceAll("^.*[\\\\/]", "");
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    private static String normalizeTags(String tags) {
        if (tags == null || tags.isBlank()) return null;
        return truncate(String.join(",", java.util.Arrays.stream(tags.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).distinct().toList()), 300);
    }

    private static String truncate(String s, int max) {
        return s == null || s.length() <= max ? s : s.substring(0, max);
    }
}
