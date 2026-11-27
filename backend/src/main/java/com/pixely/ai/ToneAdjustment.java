package com.pixely.ai;

import java.awt.image.BufferedImage;

/** 요청사항 키워드 → 밝기/톤 보정 값 (mock provider 용) */
public record ToneAdjustment(double brightness, double contrast, double saturation, double warmth,
                             double pink, boolean mono) {

    public static ToneAdjustment fromPrompt(String prompt) {
        String p = prompt == null ? "" : prompt.replace(" ", "");
        double brightness = 0, contrast = 0, saturation = 0, warmth = 0, pink = 0;
        boolean mono = false;
        if (containsAny(p, "밝게", "밝은", "화사", "환하게", "화이트닝", "bright")) brightness += 0.12;
        if (containsAny(p, "어둡게", "어두운", "시크", "무드", "dark")) brightness -= 0.10;
        if (containsAny(p, "따뜻", "웜", "노을", "햇살", "warm")) warmth += 0.10;
        if (containsAny(p, "차갑", "시원", "쿨", "청량", "cool")) warmth -= 0.10;
        if (containsAny(p, "흑백", "모노", "mono", "black&white")) mono = true;
        if (containsAny(p, "선명", "또렷", "대비", "sharp")) contrast += 0.12;
        if (containsAny(p, "부드럽", "은은", "soft")) contrast -= 0.08;
        if (containsAny(p, "화사", "생기", "비비드", "vivid")) saturation += 0.15;
        if (containsAny(p, "핑크", "로맨틱", "사랑스", "pink")) pink += 0.08;
        return new ToneAdjustment(brightness, contrast, saturation, warmth, pink, mono);
    }

    public boolean isNeutral() {
        return brightness == 0 && contrast == 0 && saturation == 0 && warmth == 0 && pink == 0 && !mono;
    }

    /** ARGB 이미지에 픽셀 단위 보정 적용 (in-place) */
    public void apply(BufferedImage img) {
        if (isNeutral()) return;
        int w = img.getWidth(), h = img.getHeight();
        int[] px = img.getRGB(0, 0, w, h, null, 0, w);
        for (int i = 0; i < px.length; i++) {
            int a = (px[i] >>> 24) & 0xff;
            double r = ((px[i] >> 16) & 0xff) / 255.0;
            double g = ((px[i] >> 8) & 0xff) / 255.0;
            double b = (px[i] & 0xff) / 255.0;

            if (brightness != 0) { r += brightness; g += brightness; b += brightness; }
            if (contrast != 0) {
                double c = 1 + contrast;
                r = (r - 0.5) * c + 0.5; g = (g - 0.5) * c + 0.5; b = (b - 0.5) * c + 0.5;
            }
            double lum = 0.2126 * r + 0.7152 * g + 0.0722 * b;
            if (mono) {
                r = g = b = lum;
            } else {
                if (saturation != 0) {
                    double s = 1 + saturation;
                    r = lum + (r - lum) * s; g = lum + (g - lum) * s; b = lum + (b - lum) * s;
                }
                if (warmth != 0) { r += warmth * 0.6; g += warmth * 0.15; b -= warmth * 0.6; }
                if (pink != 0) { r += pink * 0.7; g -= pink * 0.1; b += pink * 0.35; }
            }
            px[i] = (a << 24) | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
        }
        img.setRGB(0, 0, w, h, px, 0, w);
    }

    private static int clamp(double v) {
        return (int) Math.round(Math.max(0, Math.min(1, v)) * 255);
    }

    private static boolean containsAny(String text, String... keywords) {
        String lower = text.toLowerCase();
        for (String k : keywords) if (lower.contains(k)) return true;
        return false;
    }
}
