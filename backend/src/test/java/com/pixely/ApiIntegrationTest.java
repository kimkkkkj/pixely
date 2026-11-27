package com.pixely;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    // ───────── helpers ─────────

    private String signup() throws Exception {
        String email = "u" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        MvcResult r = mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("email", email, "password", "password123", "nickname", "테스터"))))
                .andExpect(status().isCreated())
                .andReturn();
        return json(r).at("/data/accessToken").asText();
    }

    private String login(String email, String password) throws Exception {
        MvcResult r = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk()).andReturn();
        return json(r).at("/data/accessToken").asText();
    }

    private JsonNode json(MvcResult r) throws Exception {
        return om.readTree(r.getResponse().getContentAsByteArray());
    }

    private long firstLibraryId(String type, String token) throws Exception {
        var req = get("/api/library").param("type", type);
        if (token != null) req.header("Authorization", "Bearer " + token);
        return json(mvc.perform(req).andExpect(status().isOk()).andReturn()).at("/data/content/0/id").asLong();
    }

    private long createGeneration(String token, String prompt) throws Exception {
        long product = firstLibraryId("PRODUCT", token);
        long model = firstLibraryId("MODEL", token);
        MvcResult r = mvc.perform(post("/api/generations").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("productImageId", product, "modelImageId", model, "prompt", prompt))))
                .andExpect(status().isAccepted()).andReturn();
        return json(r).at("/data/generationId").asLong();
    }

    private JsonNode waitFinished(String token, long id) throws Exception {
        for (int i = 0; i < 100; i++) {
            JsonNode body = json(mvc.perform(get("/api/generations/" + id).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk()).andReturn());
            String st = body.at("/data/status").asText();
            if (st.equals("DONE") || st.equals("FAILED")) return body;
            Thread.sleep(100);
        }
        throw new AssertionError("generation did not finish");
    }

    private static byte[] png(int w, int h, Color color) throws Exception {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, w, h);
        g.dispose();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "png", out);
        return out.toByteArray();
    }

    // ───────── tests ─────────

    @Test
    void signupLoginAndMe() throws Exception {
        String email = "me" + UUID.randomUUID().toString().substring(0, 6) + "@test.com";
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("email", email, "password", "password123", "nickname", "픽셀리"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.user.role").value("USER"));
        String token = login(email, "password123");
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.nickname").value("픽셀리"));
    }

    @Test
    void signupValidationMessagesAreKorean() throws Exception {
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("email", "bad", "password", "1", "nickname", "a"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errors.length()").value(3));
    }

    @Test
    void duplicateEmailAndWrongPassword() throws Exception {
        String body = om.writeValueAsString(Map.of("email", "dup@test.com", "password", "password123", "nickname", "중복"));
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated());
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 가입된 이메일입니다."));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("email", "dup@test.com", "password", "wrongpass"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointRequiresLogin() throws Exception {
        mvc.perform(get("/api/generations"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
    }

    @Test
    void anonymousCanBrowseSeededSamples() throws Exception {
        mvc.perform(get("/api/library").param("type", "PRODUCT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(14));
        mvc.perform(get("/api/library").param("type", "MODEL").param("q", "가상 모델"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(8));
        mvc.perform(get("/api/library").param("type", "PRODUCT").param("q", "립스틱"))
                .andExpect(jsonPath("$.data.totalElements").value(2));
        mvc.perform(get("/api/library").param("type", "PRODUCT").param("q", "세럼"))
                .andExpect(jsonPath("$.data.totalElements").value(3));
    }

    @Test
    void uploadListAndDeleteOwnImage() throws Exception {
        String token = signup();
        MockMultipartFile file = new MockMultipartFile("file", "my-product.png", "image/png", png(2400, 1200, Color.ORANGE));
        MvcResult up = mvc.perform(multipart("/api/library").file(file).param("type", "PRODUCT").param("tags", "내상품, 테스트")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("my-product"))
                .andExpect(jsonPath("$.data.width").value(1600))   // 서버 리사이즈
                .andExpect(jsonPath("$.data.mine").value(true))
                .andReturn();
        long id = json(up).at("/data/id").asLong();
        String url = json(up).at("/data/imageUrl").asText();
        mvc.perform(get(url)).andExpect(status().isOk());

        mvc.perform(get("/api/library").param("type", "PRODUCT").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.totalElements").value(15));
        // 다른 사용자에게는 보이지 않음
        mvc.perform(get("/api/library").param("type", "PRODUCT").header("Authorization", "Bearer " + signup()))
                .andExpect(jsonPath("$.data.totalElements").value(14));

        mvc.perform(delete("/api/library/" + id).header("Authorization", "Bearer " + signup()))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/library/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsNonImageUpload() throws Exception {
        String token = signup();
        MockMultipartFile file = new MockMultipartFile("file", "note.txt", "text/plain", "hello".getBytes());
        mvc.perform(multipart("/api/library").file(file).param("type", "PRODUCT").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_FILE"));
    }

    @Test
    void fullGenerationFlowWithVariantAndShare() throws Exception {
        String token = signup();
        long id = createGeneration(token, "화면을 밝고 따뜻하게 해줘");
        JsonNode done = waitFinished(token, id);
        assertThat(done.at("/data/status").asText()).isEqualTo("DONE");
        assertThat(done.at("/data/provider").asText()).isEqualTo("mock");
        String resultUrl = done.at("/data/resultUrl").asText();
        assertThat(resultUrl).startsWith("/files/results/");
        mvc.perform(get(resultUrl)).andExpect(status().isOk());

        // 필터 적용본 저장
        String b64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(png(64, 64, Color.PINK));
        mvc.perform(post("/api/generations/" + id + "/variants").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("filterType", "PINK", "imageBase64", b64))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.filterType").value("PINK"));

        // 히스토리
        mvc.perform(get("/api/generations").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(id))
                .andExpect(jsonPath("$.data.content[0].variants.length()").value(2));

        // 다른 사용자는 조회 불가
        mvc.perform(get("/api/generations/" + id).header("Authorization", "Bearer " + signup()))
                .andExpect(status().isForbidden());

        // 공유 링크 → 비로그인 공개 조회
        MvcResult share = mvc.perform(post("/api/generations/" + id + "/share").header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated()).andReturn();
        String shareToken = json(share).at("/data/token").asText();
        assertThat(json(share).at("/data/url").asText()).endsWith("/share/" + shareToken);
        mvc.perform(get("/api/share/" + shareToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resultUrl").value(resultUrl))
                .andExpect(jsonPath("$.data.creatorNickname").value("테스터"));
        mvc.perform(get("/api/share/" + UUID.randomUUID())).andExpect(status().isNotFound());
    }

    @Test
    void generationValidatesSlotTypes() throws Exception {
        String token = signup();
        long product = firstLibraryId("PRODUCT", token);
        mvc.perform(post("/api/generations").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("productImageId", product, "modelImageId", product))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("모델 슬롯에는 모델 이미지를 선택해 주세요."));
        mvc.perform(post("/api/generations").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rateLimitPerMinute() throws Exception {
        String token = signup();
        for (int i = 0; i < 5; i++) createGeneration(token, "테스트 " + i);
        long product = firstLibraryId("PRODUCT", token);
        long model = firstLibraryId("MODEL", token);
        mvc.perform(post("/api/generations").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("productImageId", product, "modelImageId", model))))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("RATE_LIMITED"));
    }

    @Test
    void adminEndpoints() throws Exception {
        String user = signup();
        mvc.perform(get("/api/admin/generations").header("Authorization", "Bearer " + user))
                .andExpect(status().isForbidden());

        String admin = login("admin@pixely.com", "admin-test-pass");
        mvc.perform(get("/api/admin/generations").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());

        MockMultipartFile file = new MockMultipartFile("file", "sample.jpg", "image/jpeg", png(300, 300, Color.BLUE));
        MvcResult up = mvc.perform(multipart("/api/admin/library").file(file).param("type", "MODEL").param("title", "새 가상 모델")
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sample").value(true))
                .andReturn();
        long id = json(up).at("/data/id").asLong();
        mvc.perform(get("/api/admin/library").param("q", "새 가상").header("Authorization", "Bearer " + admin))
                .andExpect(jsonPath("$.data.totalElements").value(1));
        mvc.perform(delete("/api/admin/library/" + id).header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());
    }
}
