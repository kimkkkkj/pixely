package com.pixely.ai;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * ai-server(FastAPI) 의 POST /generate 로 위임. ai-server 는 기본 Pillow mock 합성을 하고,
 * ComfyUI 워크플로우를 연결하는 확장 지점이다.
 */
public class FastApiImageGenerationProvider implements ImageGenerationProvider {

    private final RestClient client;
    private final String baseUrl;

    public FastApiImageGenerationProvider(String baseUrl, int timeoutSeconds) {
        this.baseUrl = baseUrl;
        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofSeconds(Math.max(5, timeoutSeconds)));
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    @Override
    public String name() {
        return "fastapi";
    }

    @Override
    public byte[] generate(byte[] productImage, byte[] modelImage, String prompt) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("product_image", named(productImage, "product.png"));
        form.add("model_image", named(modelImage, "model.png"));
        form.add("prompt", prompt == null ? "" : prompt);
        form.add("system_prompt", PromptBuilder.advertisingPrompt(prompt));
        try {
            byte[] result = client.post()
                    .uri("/generate")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .accept(MediaType.IMAGE_PNG, MediaType.IMAGE_JPEG)
                    .body(form)
                    .retrieve()
                    .body(byte[].class);
            if (result == null || result.length == 0) throw new IllegalStateException("ai-server 응답이 비어 있습니다.");
            return result;
        } catch (ResourceAccessException e) {
            throw new IllegalStateException("ai-server(" + baseUrl + ")에 연결할 수 없습니다. 서버가 실행 중인지 확인하세요.", e);
        } catch (RestClientResponseException e) {
            throw new IllegalStateException("ai-server 오류 (" + e.getStatusCode().value() + ")", e);
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
}
