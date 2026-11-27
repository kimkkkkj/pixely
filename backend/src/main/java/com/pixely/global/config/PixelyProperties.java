package com.pixely.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

@ConfigurationProperties(prefix = "pixely")
public record PixelyProperties(
        Jwt jwt,
        Admin admin,
        Cors cors,
        String publicBaseUrl,
        Storage storage,
        Seed seed,
        Ai ai
) {
    public record Jwt(String secret, long expirationMinutes) {}

    public record Admin(String email, String password) {}

    public record Cors(String allowedOrigins) {
        public List<String> originList() {
            if (allowedOrigins == null || allowedOrigins.isBlank()) return List.of();
            return Arrays.stream(allowedOrigins.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        }
    }

    public record Storage(String uploadDir) {}

    public record Seed(String legacyImagesDir) {}

    public record Ai(String provider, int timeoutSeconds, int rateLimitPerMinute,
                     Mock mock, OpenAi openai, FastApi fastapi) {
        public record Mock(long delayMinMs, long delayMaxMs) {}
        public record OpenAi(String apiKey, String model, String baseUrl) {}
        public record FastApi(String url) {}
    }
}
