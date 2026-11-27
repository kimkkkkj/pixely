package com.pixely.global.image;

import com.pixely.global.exception.BusinessException;
import com.pixely.global.exception.ErrorCode;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

/** ImageIO/Graphics2D 기반 공용 이미지 유틸 */
public final class ImageUtils {

    public record Encoded(byte[] bytes, String extension) {}

    private ImageUtils() {}

    public static BufferedImage decode(byte[] bytes) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
            if (img == null) throw new BusinessException(ErrorCode.INVALID_FILE);
            return img;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INVALID_FILE);
        }
    }

    public static boolean hasAlpha(BufferedImage img) {
        return img.getColorModel().hasAlpha();
    }

    /** 긴 변이 maxSize 를 넘으면 비율을 유지하며 축소 (고품질 보간) */
    public static BufferedImage resizeToMax(BufferedImage src, int maxSize) {
        int w = src.getWidth(), h = src.getHeight();
        double scale = Math.min(1.0, (double) maxSize / Math.max(w, h));
        BufferedImage argb = toArgb(src);
        if (scale >= 1.0) return argb;
        return scale(argb, Math.max(1, (int) Math.round(w * scale)), Math.max(1, (int) Math.round(h * scale)));
    }

    public static BufferedImage scale(BufferedImage src, int width, int height) {
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        quality(g);
        g.drawImage(src, 0, 0, width, height, null);
        g.dispose();
        return out;
    }

    /** 대상 영역을 꽉 채우도록(cover) 크롭+스케일 */
    public static BufferedImage cover(BufferedImage src, int width, int height) {
        double scale = Math.max((double) width / src.getWidth(), (double) height / src.getHeight());
        int sw = (int) Math.ceil(src.getWidth() * scale), sh = (int) Math.ceil(src.getHeight() * scale);
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        quality(g);
        g.drawImage(src, (width - sw) / 2, (height - sh) / 2, sw, sh, null);
        g.dispose();
        return out;
    }

    public static BufferedImage toArgb(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_INT_ARGB) return src;
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return out;
    }

    public static BufferedImage toRgb(BufferedImage src) {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, src.getWidth(), src.getHeight());
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return out;
    }

    /** 투명도가 있으면 PNG, 없으면 JPEG(0.9) 로 인코딩 */
    public static Encoded encodeAuto(BufferedImage img, boolean keepAlpha) {
        return keepAlpha ? new Encoded(toPng(img), "png") : new Encoded(toJpeg(img, 0.9f), "jpg");
    }

    public static byte[] toPng(BufferedImage img) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.STORAGE_ERROR);
        }
    }

    public static byte[] toJpeg(BufferedImage img, float quality) {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        ImageWriter writer = writers.next();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            writer.write(null, new IIOImage(toRgb(img), null, null), param);
            ios.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.STORAGE_ERROR);
        } finally {
            writer.dispose();
        }
    }

    public static void quality(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    }
}
