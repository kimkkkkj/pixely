package com.pixely.seed;

import com.pixely.global.image.ImageUtils;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * _legacy/service-images 가 없을 때 쓰는 코드 생성 샘플.
 * - 상품: 투명 배경 PNG 의 제품 일러스트 (mock 합성 시 자연스럽게 얹히도록)
 * - 모델: 실존 인물이 아닌 가상 모델 일러스트 (저작권/초상권 문제 없음)
 */
public final class PlaceholderImages {

    public record Sample(String title, String tags, BufferedImage image) {}

    private PlaceholderImages() {}

    // ───────────────────────── 상품 ─────────────────────────

    public static List<Sample> products() {
        return List.of(
                new Sample("히알루론 수분 크림", "스킨케어,크림,수분", jar(new Color(0x3CF0C5), new Color(0xF4FFFB), "HYDRA")),
                new Sample("데일리 선크림 SPF50+", "선케어,선크림,자외선", tube(new Color(0xFFC93D), new Color(0xFFF8E6), "SUN 50+")),
                new Sample("벨벳 립스틱 로즈", "메이크업,립,립스틱", lipstick(new Color(0xD6265B))),
                new Sample("플로럴 오 드 퍼퓸", "향수,퍼퓸,플로럴", perfume(new Color(0xFF8FB1))),
                new Sample("비타민C 앰플 세럼", "스킨케어,세럼,앰플", dropper(new Color(0xFFA63D))),
                new Sample("콜드브루 블랙 커피", "식품,커피,음료", can(new Color(0x2B2F3A), new Color(0xFFA63D), "COLD BREW")),
                new Sample("무선 노이즈캔슬링 이어버드", "전자기기,이어폰,음향", earbuds(new Color(0xF2F4F8))),
                new Sample("데일리 미니 토트백", "패션,가방,토트백", bag(new Color(0x338AFF))));
    }

    private static Graphics2D canvas(BufferedImage img) {
        Graphics2D g = img.createGraphics();
        ImageUtils.quality(g);
        return g;
    }

    private static BufferedImage blank() {
        return new BufferedImage(800, 800, BufferedImage.TYPE_INT_ARGB);
    }

    private static void label(Graphics2D g, String text, float cx, float cy, float size, Color color) {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.round(size)));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(color);
        g.drawString(text, cx - fm.stringWidth(text) / 2f, cy + fm.getAscent() / 2.8f);
    }

    private static void gloss(Graphics2D g, Shape shape, Rectangle2D b) {
        g.setPaint(new GradientPaint((float) b.getX(), 0, new Color(255, 255, 255, 90),
                (float) (b.getX() + b.getWidth() * 0.45), 0, new Color(255, 255, 255, 0)));
        g.fill(shape);
    }

    private static BufferedImage jar(Color accent, Color body, String text) {
        BufferedImage img = blank();
        Graphics2D g = canvas(img);
        Shape lid = new RoundRectangle2D.Float(190, 220, 420, 120, 40, 40);
        Shape jar = new RoundRectangle2D.Float(170, 320, 460, 300, 90, 90);
        g.setPaint(new GradientPaint(170, 0, body.darker(), 630, 0, body));
        g.fill(jar);
        g.setPaint(new GradientPaint(190, 0, accent.darker(), 610, 0, accent));
        g.fill(lid);
        gloss(g, jar, jar.getBounds2D());
        g.setColor(accent);
        g.fill(new RoundRectangle2D.Float(230, 420, 340, 110, 24, 24));
        label(g, text, 400, 475, 56, Color.WHITE);
        g.dispose();
        return img;
    }

    private static BufferedImage tube(Color accent, Color body, String text) {
        BufferedImage img = blank();
        Graphics2D g = canvas(img);
        Path2D tube = new Path2D.Float();
        tube.moveTo(250, 120); tube.lineTo(550, 120); tube.lineTo(500, 600); tube.lineTo(300, 600); tube.closePath();
        g.setPaint(new GradientPaint(250, 0, body.darker(), 550, 0, body));
        g.fill(tube);
        g.setColor(accent.darker());
        g.fill(new RoundRectangle2D.Float(320, 590, 160, 110, 24, 24));
        g.setColor(new Color(0, 0, 0, 30));
        g.fill(new Rectangle2D.Float(250, 120, 300, 26));
        g.setColor(accent);
        g.fill(new RoundRectangle2D.Float(300, 260, 200, 200, 30, 30));
        gloss(g, tube, tube.getBounds2D());
        label(g, text, 400, 360, 40, Color.WHITE);
        g.dispose();
        return img;
    }

    private static BufferedImage lipstick(Color color) {
        BufferedImage img = blank();
        Graphics2D g = canvas(img);
        Path2D bullet = new Path2D.Float();
        bullet.moveTo(330, 330); bullet.lineTo(330, 210); bullet.lineTo(470, 150); bullet.lineTo(470, 330); bullet.closePath();
        g.setPaint(new GradientPaint(330, 0, color.darker(), 470, 0, color.brighter()));
        g.fill(bullet);
        g.setPaint(new GradientPaint(310, 0, new Color(0xC9A45C), 490, 0, new Color(0xF7E3A8)));
        g.fill(new RoundRectangle2D.Float(310, 320, 180, 120, 12, 12));
        Shape base = new RoundRectangle2D.Float(290, 430, 220, 260, 20, 20);
        g.setPaint(new GradientPaint(290, 0, new Color(0x15161C), 510, 0, new Color(0x3A3D4A)));
        g.fill(base);
        gloss(g, base, base.getBounds2D());
        label(g, "VELVET", 400, 560, 38, new Color(0xF7E3A8));
        g.dispose();
        return img;
    }

    private static BufferedImage perfume(Color color) {
        BufferedImage img = blank();
        Graphics2D g = canvas(img);
        g.setPaint(new GradientPaint(340, 0, new Color(0xC9A45C), 460, 0, new Color(0xF7E3A8)));
        g.fill(new RoundRectangle2D.Float(340, 150, 120, 110, 20, 20));
        g.fill(new Rectangle2D.Float(375, 250, 50, 40));
        Shape bottle = new RoundRectangle2D.Float(200, 280, 400, 380, 120, 120);
        g.setPaint(new GradientPaint(200, 280, new Color(color.getRed(), color.getGreen(), color.getBlue(), 230),
                600, 660, new Color(255, 230, 240, 230)));
        g.fill(bottle);
        gloss(g, bottle, bottle.getBounds2D());
        g.setColor(new Color(255, 255, 255, 200));
        g.fill(new RoundRectangle2D.Float(290, 420, 220, 100, 16, 16));
        label(g, "FLORAL", 400, 470, 40, color.darker());
        g.dispose();
        return img;
    }

    private static BufferedImage dropper(Color color) {
        BufferedImage img = blank();
        Graphics2D g = canvas(img);
        g.setColor(new Color(0x22232B));
        g.fill(new RoundRectangle2D.Float(355, 110, 90, 150, 40, 40));
        g.setColor(new Color(0xE6E8EE));
        g.fill(new RoundRectangle2D.Float(330, 250, 140, 70, 14, 14));
        Shape bottle = new RoundRectangle2D.Float(260, 310, 280, 380, 60, 60);
        g.setPaint(new GradientPaint(260, 0, color.darker(), 540, 0, color));
        g.fill(bottle);
        gloss(g, bottle, bottle.getBounds2D());
        g.setColor(new Color(255, 255, 255, 230));
        g.fill(new RoundRectangle2D.Float(290, 430, 220, 140, 16, 16));
        label(g, "VITA C", 400, 480, 40, color.darker());
        label(g, "AMPOULE", 400, 530, 26, new Color(0x555555));
        g.dispose();
        return img;
    }

    private static BufferedImage can(Color body, Color accent, String text) {
        BufferedImage img = blank();
        Graphics2D g = canvas(img);
        Shape can = new RoundRectangle2D.Float(250, 150, 300, 520, 50, 50);
        g.setPaint(new GradientPaint(250, 0, body, 550, 0, body.brighter().brighter()));
        g.fill(can);
        g.setColor(new Color(0xB8BCC8));
        g.fill(new RoundRectangle2D.Float(262, 140, 276, 34, 20, 20));
        g.fill(new RoundRectangle2D.Float(262, 650, 276, 30, 20, 20));
        g.setColor(accent);
        g.fill(new Rectangle2D.Float(250, 330, 300, 150));
        gloss(g, can, can.getBounds2D());
        label(g, text, 400, 405, 40, Color.WHITE);
        g.dispose();
        return img;
    }

    private static BufferedImage earbuds(Color body) {
        BufferedImage img = blank();
        Graphics2D g = canvas(img);
        Shape cse = new RoundRectangle2D.Float(210, 330, 380, 300, 150, 150);
        g.setPaint(new GradientPaint(210, 330, body, 590, 630, body.darker()));
        g.fill(cse);
        g.setColor(new Color(0, 0, 0, 40));
        g.fill(new Rectangle2D.Float(214, 430, 372, 6));
        g.setColor(new Color(0x3CF0C5));
        g.fill(new Ellipse2D.Float(392, 540, 16, 16));
        for (int i = 0; i < 2; i++) {
            float x = 250 + i * 190;
            g.setPaint(new GradientPaint(x, 160, Color.WHITE, x + 110, 300, body.darker()));
            g.fill(new Ellipse2D.Float(x, 150, 110, 110));
            g.fill(new RoundRectangle2D.Float(x + 35, 230, 40, 120, 30, 30));
        }
        gloss(g, cse, cse.getBounds2D());
        g.dispose();
        return img;
    }

    private static BufferedImage bag(Color color) {
        BufferedImage img = blank();
        Graphics2D g = canvas(img);
        g.setStroke(new BasicStroke(28, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(color.darker());
        g.draw(new Arc2D.Float(290, 150, 220, 260, 0, 180, Arc2D.OPEN));
        Path2D body = new Path2D.Float();
        body.moveTo(200, 290); body.lineTo(600, 290); body.lineTo(640, 680); body.lineTo(160, 680); body.closePath();
        g.setPaint(new GradientPaint(160, 0, color.darker(), 640, 0, color.brighter()));
        g.fill(body);
        gloss(g, body, body.getBounds2D());
        g.setColor(new Color(0xF7E3A8));
        g.fill(new RoundRectangle2D.Float(360, 380, 80, 50, 10, 10));
        g.dispose();
        return img;
    }

    // ───────────────────────── 가상 모델 ─────────────────────────

    private record ModelSpec(String name, String tags, Color bgTop, Color bgBottom, Color skin, Color hair,
                             Color cloth, String hairStyle) {}

    public static List<Sample> models() {
        List<ModelSpec> specs = List.of(
                new ModelSpec("가상 모델 · 서아 (내추럴)", "여성,내추럴,뷰티", new Color(0xFFE3D3), new Color(0xFFB8A1), new Color(0xF6D3BE), new Color(0x3B2A22), new Color(0xFFFFFF), "long"),
                new ModelSpec("가상 모델 · 하린 (시크)", "여성,시크,패션", new Color(0x1C2333), new Color(0x0B0E16), new Color(0xEFCDB6), new Color(0x121212), new Color(0x2B2F3A), "bob"),
                new ModelSpec("가상 모델 · 지후 (캐주얼)", "남성,캐주얼,데일리", new Color(0xD8EEFF), new Color(0x9CC9FF), new Color(0xE8C3A6), new Color(0x2A211C), new Color(0x338AFF), "short"),
                new ModelSpec("가상 모델 · 유나 (러블리)", "여성,러블리,핑크", new Color(0xFFE0EC), new Color(0xFF9CC0), new Color(0xF8D9C8), new Color(0x6B3F2E), new Color(0xFF3D77), "bun"),
                new ModelSpec("가상 모델 · 도윤 (스포티)", "남성,스포티,활동적", new Color(0xD9FFF3), new Color(0x7BE8CB), new Color(0xD8AE8C), new Color(0x1A1410), new Color(0x0F172A), "short"),
                new ModelSpec("가상 모델 · 소율 (오피스)", "여성,오피스,깔끔", new Color(0xF1EEE9), new Color(0xCFC6BA), new Color(0xF2CFB8), new Color(0x2E2520), new Color(0x5B6475), "long"),
                new ModelSpec("가상 모델 · 민준 (댄디)", "남성,댄디,포멀", new Color(0xE9E4FF), new Color(0xB2A6FF), new Color(0xF0CBB0), new Color(0x231C18), new Color(0x1E2A44), "short"),
                new ModelSpec("가상 모델 · 채원 (선셋)", "여성,따뜻한,감성", new Color(0xFFE6C7), new Color(0xFFA63D), new Color(0xE9BC9A), new Color(0x4A2C1D), new Color(0xFFF4E0), "bob"));
        return specs.stream().map(s -> new Sample(s.name(), s.tags(), drawModel(s))).toList();
    }

    private static BufferedImage drawModel(ModelSpec s) {
        int w = 768, h = 1024;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas(img);
        g.setPaint(new GradientPaint(0, 0, s.bgTop(), 0, h, s.bgBottom()));
        g.fillRect(0, 0, w, h);
        g.setPaint(new RadialGradientPaint(new Point2D.Float(w * 0.3f, h * 0.25f), w * 0.7f,
                new float[]{0f, 1f}, new Color[]{new Color(255, 255, 255, 90), new Color(255, 255, 255, 0)}));
        g.fillRect(0, 0, w, h);

        float cx = w / 2f, headW = w * 0.31f, headH = h * 0.30f, headY = h * 0.30f;

        // 머리카락(뒤)
        g.setColor(s.hair());
        switch (s.hairStyle()) {
            case "long" -> g.fill(new RoundRectangle2D.Float(cx - headW * 0.72f, headY - headH * 0.05f,
                    headW * 1.44f, headH * 1.55f, headW * 0.9f, headW * 0.9f));
            case "bob" -> g.fill(new RoundRectangle2D.Float(cx - headW * 0.68f, headY - headH * 0.06f,
                    headW * 1.36f, headH * 1.05f, headW * 0.8f, headW * 0.8f));
            default -> { }
        }

        // 몸, 목
        Color skinShadow = shade(s.skin(), 0.88);
        g.setColor(skinShadow);
        g.fill(new RoundRectangle2D.Float(cx - w * 0.06f, headY + headH * 0.8f, w * 0.12f, h * 0.16f, 30, 30));
        g.setPaint(new GradientPaint(0, h * 0.68f, s.cloth(), 0, h, shade(s.cloth(), 0.8)));
        g.fill(new RoundRectangle2D.Float(w * 0.14f, h * 0.70f, w * 0.72f, h * 0.45f, w * 0.42f, w * 0.42f));
        g.setColor(skinShadow);
        g.fill(new Arc2D.Float(cx - w * 0.09f, h * 0.66f, w * 0.18f, h * 0.10f, 180, 180, Arc2D.CHORD));

        // 얼굴
        g.setPaint(new GradientPaint(cx - headW / 2, 0, s.skin(), cx + headW / 2, 0, skinShadow));
        g.fill(new Ellipse2D.Float(cx - headW / 2, headY, headW, headH));
        g.setColor(skinShadow);
        g.fill(new Ellipse2D.Float(cx - headW * 0.56f, headY + headH * 0.42f, headW * 0.12f, headH * 0.16f));
        g.fill(new Ellipse2D.Float(cx + headW * 0.44f, headY + headH * 0.42f, headW * 0.12f, headH * 0.16f));

        // 머리카락(앞)
        g.setColor(s.hair());
        Shape cap = new Arc2D.Float(cx - headW * 0.56f, headY - headH * 0.08f, headW * 1.12f, headH * 0.78f, 0, 180, Arc2D.CHORD);
        g.fill(cap);
        if ("bun".equals(s.hairStyle())) {
            g.fill(new Ellipse2D.Float(cx - headW * 0.22f, headY - headH * 0.30f, headW * 0.44f, headH * 0.34f));
        }

        // 이목구비 (단순화한 일러스트)
        Color feature = new Color(0x2A1E1A);
        float eyeY = headY + headH * 0.52f;
        g.setColor(feature);
        g.fill(new Ellipse2D.Float(cx - headW * 0.24f, eyeY, headW * 0.09f, headH * 0.045f));
        g.fill(new Ellipse2D.Float(cx + headW * 0.15f, eyeY, headW * 0.09f, headH * 0.045f));
        g.setStroke(new BasicStroke(Math.max(3f, headW / 60f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Float(cx - headW * 0.27f, eyeY - headH * 0.06f, cx - headW * 0.12f, eyeY - headH * 0.075f));
        g.draw(new Line2D.Float(cx + headW * 0.12f, eyeY - headH * 0.075f, cx + headW * 0.27f, eyeY - headH * 0.06f));
        g.setColor(new Color(255, 120, 150, 60));
        g.fill(new Ellipse2D.Float(cx - headW * 0.36f, eyeY + headH * 0.08f, headW * 0.18f, headH * 0.09f));
        g.fill(new Ellipse2D.Float(cx + headW * 0.18f, eyeY + headH * 0.08f, headW * 0.18f, headH * 0.09f));
        g.setColor(new Color(0xD9536F));
        g.fill(new Arc2D.Float(cx - headW * 0.1f, headY + headH * 0.70f, headW * 0.2f, headH * 0.09f, 180, 180, Arc2D.CHORD));
        g.dispose();
        return img;
    }

    private static Color shade(Color c, double factor) {
        return new Color((int) Math.min(255, c.getRed() * factor), (int) Math.min(255, c.getGreen() * factor),
                (int) Math.min(255, c.getBlue() * factor));
    }
}
