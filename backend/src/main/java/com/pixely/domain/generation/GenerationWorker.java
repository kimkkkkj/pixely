package com.pixely.domain.generation;

import com.pixely.ai.ImageGenerationProvider;
import com.pixely.domain.library.LibraryImage;
import com.pixely.domain.library.LibraryImageRepository;
import com.pixely.global.config.PixelyProperties;
import com.pixely.global.exception.BusinessException;
import com.pixely.global.image.ImageUtils;
import com.pixely.global.storage.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** 비동기 생성 작업: PENDING → PROCESSING → DONE / FAILED */
@Slf4j
@Component
public class GenerationWorker {

    private final GenerationRepository generationRepository;
    private final GenerationVariantRepository variantRepository;
    private final LibraryImageRepository libraryRepository;
    private final StorageService storage;
    private final ImageGenerationProvider provider;
    private final ThreadPoolTaskExecutor aiExecutor;
    private final int timeoutSeconds;

    public GenerationWorker(GenerationRepository generationRepository,
                            GenerationVariantRepository variantRepository,
                            LibraryImageRepository libraryRepository,
                            StorageService storage,
                            ImageGenerationProvider provider,
                            @Qualifier("aiCallExecutor") ThreadPoolTaskExecutor aiExecutor,
                            PixelyProperties properties) {
        this.generationRepository = generationRepository;
        this.variantRepository = variantRepository;
        this.libraryRepository = libraryRepository;
        this.storage = storage;
        this.provider = provider;
        this.aiExecutor = aiExecutor;
        this.timeoutSeconds = Math.max(1, properties.ai().timeoutSeconds());
    }

    @Async("generationExecutor")
    public void process(Long generationId) {
        Generation generation = generationRepository.findById(generationId).orElse(null);
        if (generation == null) return;
        generation.startProcessing(provider.name());
        generationRepository.save(generation);
        long started = System.currentTimeMillis();
        try {
            byte[] product = loadImage(generation.getProductImageId());
            byte[] model = loadImage(generation.getModelImageId());
            byte[] raw = callWithTimeout(product, model, generation.getPrompt());

            // provider 출력이 무엇이든 PNG 로 정규화해서 저장 (손상 응답 검증 겸)
            byte[] png = ImageUtils.toPng(ImageUtils.decode(raw));
            String url = storage.toUrl(storage.store("results", png, "png"));
            generation.complete(url);
            generationRepository.save(generation);
            variantRepository.save(GenerationVariant.builder()
                    .generationId(generationId).filterType(FilterType.ORIGINAL).imageUrl(url).build());
            log.info("Generation {} done by {} in {}ms", generationId, provider.name(),
                    System.currentTimeMillis() - started);
        } catch (Exception e) {
            log.warn("Generation {} failed: {}", generationId, e.getMessage());
            generation.fail(userMessage(e));
            generationRepository.save(generation);
        }
    }

    private byte[] loadImage(Long libraryImageId) {
        LibraryImage image = libraryRepository.findById(libraryImageId)
                .orElseThrow(() -> new IllegalStateException("선택한 이미지가 삭제되었습니다. 다른 이미지를 선택해 주세요."));
        return storage.load(storage.toKey(image.getImageUrl()));
    }

    private byte[] callWithTimeout(byte[] product, byte[] model, String prompt) throws Exception {
        Future<byte[]> future = aiExecutor.submit(() -> provider.generate(product, model, prompt));
        try {
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new IllegalStateException("이미지 생성 시간이 초과되었습니다 (%d초). 다시 시도해 주세요.".formatted(timeoutSeconds));
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception ex) throw ex;
            throw new IllegalStateException("이미지 생성 중 오류가 발생했습니다.", cause);
        }
    }

    private static String userMessage(Exception e) {
        if (e instanceof BusinessException be) return be.getMessage();
        String msg = e.getMessage();
        return msg == null || msg.isBlank() ? "이미지 생성 중 오류가 발생했습니다." : msg;
    }
}
