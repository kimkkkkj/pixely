package com.pixely.ai;

import com.pixely.global.image.ImageUtils;
import com.pixely.seed.PlaceholderImages;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.assertThat;

class MockImageGenerationProviderTest {

    @Test
    void composesModelSizedImage() throws Exception {
        var product = PlaceholderImages.products().get(0).image();
        var model = PlaceholderImages.models().get(0).image();
        byte[] out = new MockImageGenerationProvider(0, 0)
                .generate(ImageUtils.toPng(product), ImageUtils.toPng(model), "밝게 해줘");
        BufferedImage img = ImageUtils.decode(out);
        assertThat(Math.max(img.getWidth(), img.getHeight())).isEqualTo(1024);
        assertThat(img.getWidth()).isLessThan(img.getHeight()); // 모델(세로) 비율 유지
    }

    @Test
    void promptKeywordsChangeTone() {
        assertThat(ToneAdjustment.fromPrompt("흑백으로 해주세요").mono()).isTrue();
        assertThat(ToneAdjustment.fromPrompt("화면을 밝게").brightness()).isPositive();
        assertThat(ToneAdjustment.fromPrompt("따뜻한 느낌").warmth()).isPositive();
        assertThat(ToneAdjustment.fromPrompt("주근깨를 지워줘").isNeutral()).isTrue();
    }

    @Test
    void englishPromptWrapsKoreanRequest() {
        String p = PromptBuilder.advertisingPrompt("주근깨를 모두 지우고 화면을 밝게 해줘");
        assertThat(p).contains("advertising photo").contains("주근깨를 모두 지우고");
    }
}
