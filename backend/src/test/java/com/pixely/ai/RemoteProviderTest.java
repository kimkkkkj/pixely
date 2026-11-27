package com.pixely.ai;

import com.pixely.global.image.ImageUtils;
import com.pixely.seed.PlaceholderImages;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 로컬 스텁 서버로 openai / fastapi provider 의 요청 형식과 응답 처리를 검증 (실제 키 불필요) */
class RemoteProviderTest {

    private HttpServer server;
    private final AtomicReference<String> body = new AtomicReference<>();
    private final AtomicReference<String> auth = new AtomicReference<>();
    private final AtomicReference<String> path = new AtomicReference<>();

    private String start(int status, String contentType, byte[] response) throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", ex -> {
            path.set(ex.getRequestURI().getPath());
            auth.set(ex.getRequestHeaders().getFirst("Authorization"));
            body.set(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.ISO_8859_1));
            ex.getResponseHeaders().add("Content-Type", contentType);
            ex.sendResponseHeaders(status, response.length);
            ex.getResponseBody().write(response);
            ex.close();
        });
        server.start();
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void stop() {
        if (server != null) server.stop(0);
    }

    private static byte[] png() {
        return ImageUtils.toPng(PlaceholderImages.products().get(0).image());
    }

    @Test
    void openAiSendsTwoImagesAndDecodesB64() throws Exception {
        byte[] result = png();
        String json = "{\"data\":[{\"b64_json\":\"" + Base64.getEncoder().encodeToString(result) + "\"}]}";
        String base = start(200, "application/json", json.getBytes(StandardCharsets.UTF_8));

        byte[] out = new OpenAiImageGenerationProvider(base + "/v1", "sk-test", "gpt-image-1", 10)
                .generate(png(), png(), "주근깨를 지워줘");

        assertThat(out).isEqualTo(result);
        assertThat(path.get()).isEqualTo("/v1/images/edits");
        assertThat(auth.get()).isEqualTo("Bearer sk-test");
        assertThat(body.get()).contains("name=\"model\"").contains("gpt-image-1")
                .contains("name=\"image[]\"; filename=\"product.png\"")
                .contains("name=\"image[]\"; filename=\"model.png\"")
                .contains("advertising photo");
    }

    @Test
    void openAiErrorIsWrapped() throws Exception {
        String base = start(401, "application/json", "{\"error\":{\"message\":\"bad key\"}}".getBytes());
        assertThatThrownBy(() -> new OpenAiImageGenerationProvider(base, "sk-x", null, 10).generate(png(), png(), ""))
                .hasMessageContaining("OpenAI 호출 실패 (401)");
    }

    @Test
    void openAiRequiresKey() {
        assertThatThrownBy(() -> new OpenAiImageGenerationProvider("http://x", "", "m", 10))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fastApiPostsMultipartAndReturnsBytes() throws Exception {
        byte[] result = png();
        String base = start(200, "image/png", result);
        byte[] out = new FastApiImageGenerationProvider(base, 10).generate(png(), png(), "밝게");
        assertThat(out).isEqualTo(result);
        assertThat(path.get()).isEqualTo("/generate");
        assertThat(body.get()).contains("name=\"product_image\"").contains("name=\"model_image\"").contains("name=\"prompt\"");
    }
}
