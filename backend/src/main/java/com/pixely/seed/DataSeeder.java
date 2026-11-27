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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * 시작 시 1회 실행:
 * 1) 관리자 계정 생성 (ADMIN_EMAIL / ADMIN_PASSWORD)
 *    + 개발용 테스트 셀러 계정 (DEV_SELLER_EMAIL / DEV_SELLER_PASSWORD 가 설정된 경우만)
 * 2) 공용 상품 샘플: classpath:seed/products (원본 시안 service-images/tab1 상품 이미지)
 *    → 없으면 코드로 그린 상품 일러스트
 * 3) 공용 모델 샘플: 항상 코드로 그린 가상 모델 (실존 인물/연예인 사진은 쓰지 않음)
 * 4) 서버 재시작으로 중단된 PENDING/PROCESSING 생성 건을 FAILED 로 정리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    /** classpath:seed/products/product-NN.jpg 의 제목/태그 (원본 tab1 순서) */
    static final List<String[]> PRODUCT_SEEDS = List.of(
            new String[]{"세라마이드 장벽 보습 크림 세트", "스킨케어,크림,보습,세트"},
            new String[]{"히알루론산 수분 세럼 리필 기획", "스킨케어,세럼,수분"},
            new String[]{"시카 B5+ 진정 밤", "스킨케어,크림,진정"},
            new String[]{"징크 테카 트러블 세럼", "스킨케어,세럼,트러블"},
            new String[]{"시카 B5 밤 대용량 기획", "스킨케어,크림,진정,세트"},
            new String[]{"아쿠아 스쿠알란 수분 크림", "스킨케어,크림,수분"},
            new String[]{"글루타치온 비타민 세럼 1+1", "스킨케어,세럼,미백"},
            new String[]{"대나무 워터 부스팅 크림", "스킨케어,크림,수분"},
            new String[]{"워터 립 틴트 4종", "메이크업,립,틴트"},
            new String[]{"실키 립스틱", "메이크업,립"},
            new String[]{"퍼펙트 립 키트", "메이크업,립,틴트,세트"},
            new String[]{"매트 립스틱 듀오", "메이크업,립"},
            new String[]{"글로시 립 틴트 컬렉션", "메이크업,립,틴트"},
            new String[]{"멜팅 립 밤 6종", "메이크업,립,밤"});

    private final UserRepository userRepository;
    private final LibraryImageRepository libraryImageRepository;
    private final LibraryService libraryService;
    private final GenerationRepository generationRepository;
    private final PasswordEncoder passwordEncoder;
    private final PixelyProperties properties;

    @Value("${DEV_SELLER_EMAIL:}")
    private String devSellerEmail;

    @Value("${DEV_SELLER_PASSWORD:}")
    private String devSellerPassword;

    @Override
    public void run(ApplicationArguments args) {
        seedAdmin();
        seedDevSeller();
        seedProducts();
        seedModels();
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

    /** 로컬 테스트용 일반(셀러) 계정 — 두 값이 모두 있을 때만 생성. 운영 .env 에는 넣지 말 것 */
    private void seedDevSeller() {
        if (devSellerEmail == null || devSellerEmail.isBlank() || devSellerPassword == null || devSellerPassword.isBlank()) return;
        String email = devSellerEmail.trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) return;
        userRepository.save(User.builder().email(email).password(passwordEncoder.encode(devSellerPassword))
                .nickname("테스트셀러").role(Role.USER).build());
        log.info("Dev seller account created: {}", email);
    }

    private void seedProducts() {
        if (libraryImageRepository.countByOwnerIdIsNullAndType(ImageType.PRODUCT) > 0) return;
        int seeded = 0;
        for (int i = 0; i < PRODUCT_SEEDS.size(); i++) {
            var resource = new ClassPathResource("seed/products/product-%02d.jpg".formatted(i + 1));
            if (!resource.exists()) continue;
            try (InputStream in = resource.getInputStream()) {
                libraryService.saveImage(in.readAllBytes(), ImageType.PRODUCT, PRODUCT_SEEDS.get(i)[0],
                        PRODUCT_SEEDS.get(i)[1], null);
                seeded++;
            } catch (IOException | RuntimeException e) {
                log.warn("Skip product seed {}: {}", resource.getPath(), e.getMessage());
            }
        }
        if (seeded == 0) {
            PlaceholderImages.products().forEach(s -> libraryService.saveImage(ImageUtils.toPng(s.image()),
                    ImageType.PRODUCT, s.title(), s.tags(), null));
            seeded = PlaceholderImages.products().size();
        }
        log.info("Seeded {} public product samples", seeded);
    }

    private void seedModels() {
        if (libraryImageRepository.countByOwnerIdIsNullAndType(ImageType.MODEL) > 0) return;
        PlaceholderImages.models().forEach(s -> libraryService.saveImage(ImageUtils.toPng(s.image()),
                ImageType.MODEL, s.title(), s.tags(), null));
        log.info("Seeded {} virtual model samples", PlaceholderImages.models().size());
    }
}
