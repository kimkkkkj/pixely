package com.pixely.ai;

import com.pixely.global.config.PixelyProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

/** AI_PROVIDER 값(mock|openai|fastapi)에 따라 구현체 하나를 등록 */
@Slf4j
@Configuration
public class AiProviderConfig {

    @Bean
    public ImageGenerationProvider imageGenerationProvider(PixelyProperties properties) {
        PixelyProperties.Ai ai = properties.ai();
        String provider = ai.provider() == null ? "mock" : ai.provider().trim().toLowerCase(Locale.ROOT);
        ImageGenerationProvider bean = switch (provider) {
            case "openai" -> new OpenAiImageGenerationProvider(ai.openai().baseUrl(), ai.openai().apiKey(),
                    ai.openai().model(), ai.timeoutSeconds());
            case "fastapi" -> new FastApiImageGenerationProvider(ai.fastapi().url(), ai.timeoutSeconds());
            case "mock", "" -> new MockImageGenerationProvider(ai.mock().delayMinMs(), ai.mock().delayMaxMs());
            default -> throw new IllegalStateException("알 수 없는 AI_PROVIDER: " + provider + " (mock|openai|fastapi)");
        };
        log.info("AI provider: {}", bean.name());
        return bean;
    }
}
