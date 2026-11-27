package com.pixely.seed;

import com.pixely.domain.generation.GenerationRepository;
import com.pixely.domain.generation.GenerationStatus;
import com.pixely.domain.library.ImageType;
import com.pixely.domain.library.LibraryImageRepository;
import com.pixely.domain.library.LibraryService;
import com.pixely.domain.user.Role;
import com.pixely.domain.user.User;
import com.pixely.domain.user.UserRepository;
import com.pixely.global.config.PixelyProperties;
import com.pixely.global.image.ImageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * 시작 시 1회 실행:
 * 1) 관리자 계정 생성 (ADMIN_EMAIL / ADMIN_PASSWORD)
 * 2) 공용 샘플이 비어 있으면 _legacy/service-images/tab1(상품), tab2(모델) 또는 코드 생성 플레이스홀더로 시드
 * 3) 서버 재시작으로 중단된 PENDING/PROCESSING 생성 건을 FAILED 로 정리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final LibraryImageRepository libraryImageRepository;
    private final LibraryService libraryService;
    private final GenerationRepository generationRepository;
    private final PasswordEncoder passwordEncoder;
    private final PixelyProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        seedAdmin();
        seedLibrary();
        int stale = generationRepository.failStale(List.of(GenerationStatus.PENDING, GenerationStatus.PROCESSING),
                "서버가 재시작되어 생성이 중단되었습니다. 다시 시도해 주세요.", LocalDateTime.now());
        if (stale > 0) log.info("Marked {} interrupted generations as FAILED", stale);
    }

    private void seedAdmin() {
        String email = properties.admin().email().trim().toLowerCase(Locale.ROOT);
        String password = properties.admin().password();
        if (userRepository.existsByEmail(email)) return;
        if (password == null || password.isBlank()) {
            log.warn("ADMIN_PASSWORD 가 비어 있어 관리자 계정을 만들지 않았습니다.");
            return;
        }
        userRepository.save(User.builder().email(email).password(passwordEncoder.encode(password))
                .nickname("관리자").role(Role.ADMIN).build());
        log.info("Admin account created: {}", email);
    }

    private void seedLibrary() {
        if (libraryImageRepository.countByOwnerIdIsNull() > 0) return;
        Path legacy = Path.of(properties.seed().legacyImagesDir());
        int fromLegacy = seedFromDir(legacy.resolve("tab1"), ImageType.PRODUCT, "상품 샘플")
                + seedFromDir(legacy.resolve("tab2"), ImageType.MODEL, "모델 샘플");
        if (fromLegacy > 0) {
            log.info("Seeded {} sample images from {}", fromLegacy, legacy.toAbsolutePath());
            return;
        }
        PlaceholderImages.products().forEach(s -> libraryService.saveImage(ImageUtils.toPng(s.image()),
                ImageType.PRODUCT, s.title(), s.tags(), null));
        PlaceholderImages.models().forEach(s -> libraryService.saveImage(ImageUtils.toPng(s.image()),
                ImageType.MODEL, s.title(), s.tags(), null));
        log.info("Seeded placeholder sample images (8 products, 8 virtual models)");
    }

    private int seedFromDir(Path dir, ImageType type, String titlePrefix) {
        if (!Files.isDirectory(dir)) return 0;
        try (Stream<Path> files = Files.list(dir)) {
            List<Path> images = files.filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT)
                    .matches(".*\\.(png|jpe?g|webp|gif)$")).sorted().toList();
            int i = 0;
            for (Path p : images) {
                try {
                    libraryService.saveImage(Files.readAllBytes(p), type, "%s %d".formatted(titlePrefix, ++i),
                            type == ImageType.PRODUCT ? "샘플,상품" : "샘플,모델", null);
                } catch (Exception e) {
                    log.warn("Skip legacy image {}: {}", p, e.getMessage());
                }
            }
            return i;
        } catch (IOException e) {
            log.warn("Cannot read legacy dir {}", dir, e);
            return 0;
        }
    }
}
