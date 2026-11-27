package com.pixely.ai;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Base64;

/**
 * OpenAI 이미지 편집 API(POST /v1/images/edits)에 상품·모델 이미지 2장과 광고용 영어 프롬프트를 보낸다.
 * 모델: OPENAI_IMAGE_MODEL (기본 gpt-image-1), 키: OPENAI_API_KEY
 */
public class OpenAiImageGenerationProvider implements ImageGenerationProvider {

    private final RestClient client;
    private final String apiKey;
    private final String model;

    public OpenAiImageGenerationProvider(String baseUrl, String apiKey, String model, int timeoutSeconds) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("AI_PROVIDER=openai 이지만 OPENAI_API_KEY 가 비어 있습니다.");
        }
        this.apiKey = apiKey;
        this.model = model == null || model.isBlank() ? "gpt-image-1" : model;
        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofSeconds(Math.max(10, timeoutSeconds)));
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    @Override
    public String name() {
        return "openai";
    }

    @Override
    public byte[] generate(byte[] productImage, byte[] modelImage, String prompt) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("model", model);
        form.add("prompt", PromptBuilder.advertisingPrompt(prompt));
        form.add("image[]", named(productImage, "product.png"));
        form.add("image[]", named(modelImage, "model.png"));
        form.add("size", "auto");
        form.add("n", "1");
        try {
            JsonNode body = client.post()
                    .uri("/images/edits")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(form)
                    .retrieve()
                    .body(JsonNode.class);
            String b64 = body == null ? null : body.path("data").path(0).path("b64_json").asText(null);
            if (b64 == null || b64.isBlank()) {
                throw new IllegalStateException("OpenAI 응답에 이미지가 없습니다.");
            }
            return Base64.getDecoder().decode(b64);
        } catch (RestClientResponseException e) {
            throw new IllegalStateException("OpenAI 호출 실패 (" + e.getStatusCode().value() + "): "
                    + abbreviate(e.getResponseBodyAsString()), e);
        }
    }

    private static ByteArrayResource named(byte[] bytes, String filename) {
        return new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }

    private static String abbreviate(String s) {
        if (s == null) return "";
        return s.length() > 300 ? s.substring(0, 300) + "…" : s;
    }
}
