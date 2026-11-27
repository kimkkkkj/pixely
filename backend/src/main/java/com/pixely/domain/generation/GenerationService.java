package com.pixely.domain.generation;

import com.pixely.domain.generation.dto.GenerationDtos.*;
import com.pixely.domain.library.ImageType;
import com.pixely.domain.library.LibraryImage;
import com.pixely.domain.library.LibraryImageRepository;
import com.pixely.domain.user.User;
import com.pixely.domain.user.UserRepository;
import com.pixely.global.common.PageResponse;
import com.pixely.global.exception.BusinessException;
import com.pixely.global.exception.ErrorCode;
import com.pixely.global.image.ImageUtils;
import com.pixely.global.security.UserPrincipal;
import com.pixely.global.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenerationService {

    private static final int MAX_VARIANT_BASE64_LENGTH = 20 * 1024 * 1024;

    private final GenerationRepository generationRepository;
    private final GenerationVariantRepository variantRepository;
    private final LibraryImageRepository libraryRepository;
    private final UserRepository userRepository;
    private final StorageService storage;
    private final GenerationWorker worker;
    private final GenerationRateLimiter rateLimiter;

    /** 트랜잭션 밖에서 저장(즉시 커밋) 후 비동기 워커 시작 → 워커가 항상 커밋된 행을 읽음 */
    public CreatedResponse create(CreateRequest req, UserPrincipal user) {
        LibraryImage product = requireVisible(req.productImageId(), ImageType.PRODUCT, user);
        LibraryImage model = requireVisible(req.modelImageId(), ImageType.MODEL, user);
        rateLimiter.acquire(user.id());
        Generation generation = generationRepository.save(Generation.builder()
                .userId(user.id())
                .productImageId(product.getId())
                .modelImageId(model.getId())
                .prompt(req.prompt() == null ? null : req.prompt().trim())
                .status(GenerationStatus.PENDING)
                .build());
        worker.process(generation.getId());
        return new CreatedResponse(generation.getId(), generation.getStatus().name());
    }

    @Transactional(readOnly = true)
    public GenerationResponse get(Long id, UserPrincipal user) {
        return toResponse(requireAccessible(id, user), false);
    }

    @Transactional(readOnly = true)
    public PageResponse<GenerationResponse> listMine(UserPrincipal user, int page, int size) {
        Page<Generation> result = generationRepository.findByUserId(user.id(), pageable(page, size));
        return toPage(result, false);
    }

    @Transactional(readOnly = true)
    public PageResponse<GenerationResponse> listAll(GenerationStatus status, int page, int size) {
        Page<Generation> result = status == null
                ? generationRepository.findAll(pageable(page, size))
                : generationRepository.findByStatus(status, pageable(page, size));
        return toPage(result, true);
    }

    @Transactional
    public VariantResponse saveVariant(Long id, VariantRequest req, UserPrincipal user) {
        Generation generation = requireOwned(id, user);
        if (generation.getStatus() != GenerationStatus.DONE) throw new BusinessException(ErrorCode.GENERATION_NOT_READY);
        if (req.filterType() == FilterType.ORIGINAL) {
            return variantRepository.findByGenerationIdAndFilterType(id, FilterType.ORIGINAL)
                    .map(VariantResponse::of)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        }
        byte[] png = ImageUtils.toPng(ImageUtils.decode(decodeBase64(req.imageBase64())));
        String url = storage.toUrl(storage.store("variants", png, "png"));
        GenerationVariant variant = variantRepository.findByGenerationIdAndFilterType(id, req.filterType())
                .map(existing -> {
                    storage.delete(storage.toKey(existing.getImageUrl()));
                    existing.replaceImage(url);
                    return existing;
                })
                .orElseGet(() -> variantRepository.save(GenerationVariant.builder()
                        .generationId(id).filterType(req.filterType()).imageUrl(url).build()));
        return VariantResponse.of(variant);
    }

    public Generation requireOwned(Long id, UserPrincipal user) {
        Generation g = generationRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (!g.getUserId().equals(user.id())) throw new BusinessException(ErrorCode.FORBIDDEN);
        return g;
    }

    private Generation requireAccessible(Long id, UserPrincipal user) {
        Generation g = generationRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (!g.getUserId().equals(user.id()) && !user.isAdmin()) throw new BusinessException(ErrorCode.FORBIDDEN);
        return g;
    }

    private LibraryImage requireVisible(Long imageId, ImageType type, UserPrincipal user) {
        LibraryImage image = libraryRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "선택한 이미지를 찾을 수 없습니다."));
        if (image.getType() != type) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, type == ImageType.PRODUCT
                    ? "상품 슬롯에는 상품 이미지를 선택해 주세요." : "모델 슬롯에는 모델 이미지를 선택해 주세요.");
        }
        if (!image.isPublicSample() && !image.getOwnerId().equals(user.id())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "다른 사용자의 이미지는 사용할 수 없습니다.");
        }
        return image;
    }

    GenerationResponse toResponse(Generation g, boolean withEmail) {
        Map<Long, LibraryImage> images = libraryRepository.findAllById(List.of(g.getProductImageId(), g.getModelImageId()))
                .stream().collect(Collectors.toMap(LibraryImage::getId, Function.identity()));
        String email = withEmail ? userRepository.findById(g.getUserId()).map(User::getEmail).orElse(null) : null;
        return GenerationResponse.of(g, images.get(g.getProductImageId()), images.get(g.getModelImageId()),
                variantRepository.findByGenerationIdOrderByIdAsc(g.getId()), email);
    }

    private PageResponse<GenerationResponse> toPage(Page<Generation> page, boolean withEmail) {
        List<Generation> items = page.getContent();
        Set<Long> imageIds = new HashSet<>();
        items.forEach(g -> { imageIds.add(g.getProductImageId()); imageIds.add(g.getModelImageId()); });
        Map<Long, LibraryImage> images = libraryRepository.findAllById(imageIds).stream()
                .collect(Collectors.toMap(LibraryImage::getId, Function.identity()));
        Map<Long, List<GenerationVariant>> variants = items.isEmpty() ? Map.of()
                : variantRepository.findByGenerationIdIn(items.stream().map(Generation::getId).toList()).stream()
                .sorted(Comparator.comparing(GenerationVariant::getId))
                .collect(Collectors.groupingBy(GenerationVariant::getGenerationId));
        Map<Long, String> emails = withEmail
                ? userRepository.findAllById(items.stream().map(Generation::getUserId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(User::getId, User::getEmail))
                : Map.of();
        return PageResponse.of(page, g -> GenerationResponse.of(g, images.get(g.getProductImageId()),
                images.get(g.getModelImageId()), variants.get(g.getId()), emails.get(g.getUserId())));
    }

    private static PageRequest pageable(int page, int size) {
        return PageRequest.of(Math.max(0, page), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "id"));
    }

    private static byte[] decodeBase64(String data) {
        if (data.length() > MAX_VARIANT_BASE64_LENGTH) throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        String payload = data.contains(",") && data.startsWith("data:") ? data.substring(data.indexOf(',') + 1) : data;
        try {
            return Base64.getDecoder().decode(payload.trim());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미지 데이터 형식이 올바르지 않습니다.");
        }
    }
}
