package com.pixely.ai;

import com.pixely.global.image.ImageUtils;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

/**
 * API 키 없이 끝까지 동작하는 mock 합성기.
 * 모델 이미지를 배경으로 깔고 상품 이미지를 하단 우측에 그림자와 함께 자연스럽게 배치한 뒤,
 * 요청사항 키워드(밝게/따뜻/흑백 …)에 따라 톤을 보정한다. 2~4초 지연으로 로딩 UX 를 재현.
 */
public class MockImageGenerationProvider implements ImageGenerationProvider {

    private static final int TARGET_LONG_SIDE = 1024;
    private final long delayMinMs;
    private final long delayMaxMs;

    public MockImageGenerationProvider(long delayMinMs, long delayMaxMs) {
        this.delayMinMs = Math.max(0, delayMinMs);
        this.delayMaxMs = Math.max(this.delayMinMs, delayMaxMs);
    }

    @Override
    public String name() {
        return "mock";
    }

    @Override
    public byte[] generate(byte[] productImage, byte[] modelImage, String prompt) throws Exception {
        long delay = delayMaxMs == delayMinMs ? delayMinMs
                : ThreadLocalRandom.current().nextLong(delayMinMs, delayMaxMs + 1);
        if (delay > 0) Thread.sleep(delay);
        return ImageUtils.toPng(compose(ImageUtils.decode(productImage), ImageUtils.decode(modelImage), prompt));
    }

    public static BufferedImage compose(BufferedImage product, BufferedImage model, String prompt) {
        // 1) 캔버스 = 모델 이미지(긴 변 1024)
        double s = (double) TARGET_LONG_SIDE / Math.max(model.getWidth(), model.getHeight());
        int cw = (int) Math.round(model.getWidth() * s), ch = (int) Math.round(model.getHeight() * s);
        BufferedImage canvas = ImageUtils.cover(model, cw, ch);
        Graphics2D g = canvas.createGraphics();
        ImageUtils.quality(g);

        // 2) 상단 좌측 은은한 라이트 + 하단 비네팅 (광고 컷 느낌)
        g.setPaint(new RadialGradientPaint(new Point(cw / 5, ch / 6), Math.max(cw, ch) * 0.7f,
                new float[]{0f, 1f}, new Color[]{new Color(255, 255, 255, 46), new Color(255, 255, 255, 0)}));
        g.fillRect(0, 0, cw, ch);
        g.setPaint(new GradientPaint(0, ch * 0.55f, new Color(0, 0, 0, 0), 0, ch, new Color(0, 0, 0, 70)));
        g.fillRect(0, (int) (ch * 0.55), cw, ch - (int) (ch * 0.55));

        // 3) 상품 배치 (가로 38%, 세로 최대 50%)
        boolean alpha = ImageUtils.hasAlpha(product);
        double ps = Math.min(cw * 0.42 / product.getWidth(), ch * 0.5 / product.getHeight());
        int pw = Math.max(1, (int) (product.getWidth() * ps)), ph = Math.max(1, (int) (product.getHeight() * ps));
        BufferedImage p = ImageUtils.scale(ImageUtils.toArgb(product), pw, ph);
        int margin = (int) (Math.min(cw, ch) * 0.06);
        int px = cw - pw - margin, py = ch - ph - margin;
        float radius = alpha ? 0 : Math.min(pw, ph) * 0.08f;

        // 부드러운 그림자: 상품 실루엣(알파)으로 만든 그림자를 축소→확대해서 블러 처리
        int pad = Math.max(8, margin / 2);
        BufferedImage shadow = softShadow(p, alpha ? null : new RoundRectangle2D.Float(0, 0, pw, ph, radius * 2, radius * 2), pad);
        g.drawImage(shadow, px - pad + margin / 8, py - pad + margin / 4, null);

        if (alpha) {
            g.drawImage(p, px, py, null);
        } else {
            Shape clip = new RoundRectangle2D.Float(px, py, pw, ph, radius * 2, radius * 2);
            g.setClip(clip);
            g.drawImage(p, px, py, null);
            g.setClip(null);
            g.setStroke(new BasicStroke(Math.max(2f, pw / 90f)));
            g.setColor(new Color(255, 255, 255, 220));
            g.draw(clip);
        }

        // 4) mock 결과임을 알리는 작은 워터마크
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(12, cw / 45)));
        g.setColor(new Color(255, 255, 255, 150));
        g.drawString("PIXELY · MOCK PREVIEW", margin, ch - margin);
        g.dispose();

        // 5) 요청사항 키워드 기반 톤 보정
        ToneAdjustment.fromPrompt(prompt).apply(canvas);
        return canvas;
    }

    /** shape 가 null 이면 이미지 알파를 실루엣으로 사용 */
    private static BufferedImage softShadow(BufferedImage src, Shape shape, int pad) {
        int w = src.getWidth() + pad * 2, h = src.getHeight() + pad * 2;
        BufferedImage mask = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = mask.createGraphics();
        ImageUtils.quality(g);
        if (shape == null) {
            int[] px = src.getRGB(0, 0, src.getWidth(), src.getHeight(), null, 0, src.getWidth());
            for (int i = 0; i < px.length; i++) {
                int a = (px[i] >>> 24) & 0xff;
                px[i] = ((int) (a * 0.42) << 24);
            }
            mask.setRGB(pad, pad, src.getWidth(), src.getHeight(), px, 0, src.getWidth());
        } else {
            g.setColor(new Color(0, 0, 0, 107));
            g.translate(pad, pad);
            g.fill(shape);
        }
        g.dispose();
        // 1/10 로 줄였다가 다시 키우면 가우시안에 가까운 블러가 된다
        int sw = Math.max(1, w / 10), sh = Math.max(1, h / 10);
        return ImageUtils.scale(ImageUtils.scale(mask, sw, sh), w, h);
    }
}
