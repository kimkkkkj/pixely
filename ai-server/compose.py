"""Pillow mock 합성 — 백엔드 MockImageGenerationProvider 와 동일한 규칙."""
from __future__ import annotations

import io
import random
import time

from PIL import Image, ImageDraw, ImageEnhance, ImageFilter, ImageFont, ImageOps

TARGET_LONG_SIDE = 1024


def _has(text: str, *keywords: str) -> bool:
    t = text.replace(" ", "").lower()
    return any(k in t for k in keywords)


def _tone(img: Image.Image, prompt: str) -> Image.Image:
    rgb = img.convert("RGB")
    if _has(prompt, "흑백", "모노", "mono"):
        return ImageOps.grayscale(rgb).convert("RGB")
    if _has(prompt, "밝게", "밝은", "화사", "환하게", "bright"):
        rgb = ImageEnhance.Brightness(rgb).enhance(1.12)
    if _has(prompt, "어둡게", "시크", "dark"):
        rgb = ImageEnhance.Brightness(rgb).enhance(0.9)
    if _has(prompt, "선명", "또렷", "대비", "sharp"):
        rgb = ImageEnhance.Contrast(rgb).enhance(1.12)
    if _has(prompt, "화사", "생기", "비비드", "vivid"):
        rgb = ImageEnhance.Color(rgb).enhance(1.15)
    if _has(prompt, "따뜻", "웜", "햇살", "노을", "warm"):
        r, g, b = rgb.split()
        r = r.point(lambda v: min(255, int(v * 1.06 + 12)))
        b = b.point(lambda v: max(0, int(v * 0.94 - 10)))
        rgb = Image.merge("RGB", (r, g, b))
    if _has(prompt, "핑크", "로맨틱", "pink"):
        rgb = Image.blend(rgb, Image.new("RGB", rgb.size, (255, 170, 200)), 0.08)
    return rgb


def compose_mock(product_bytes: bytes, model_bytes: bytes, prompt: str, delay: tuple[float, float] = (1.0, 2.0)) -> bytes:
    time.sleep(random.uniform(*delay))
    model = Image.open(io.BytesIO(model_bytes)).convert("RGBA")
    product = Image.open(io.BytesIO(product_bytes))
    has_alpha = product.mode in ("RGBA", "LA") or "transparency" in product.info
    product = product.convert("RGBA")

    scale = TARGET_LONG_SIDE / max(model.size)
    cw, ch = round(model.width * scale), round(model.height * scale)
    canvas = model.resize((cw, ch), Image.LANCZOS)

    ps = min(cw * 0.42 / product.width, ch * 0.5 / product.height)
    pw, ph = max(1, int(product.width * ps)), max(1, int(product.height * ps))
    p = product.resize((pw, ph), Image.LANCZOS)
    margin = int(min(cw, ch) * 0.06)
    px, py = cw - pw - margin, ch - ph - margin

    if not has_alpha:
        mask = Image.new("L", (pw, ph), 0)
        ImageDraw.Draw(mask).rounded_rectangle((0, 0, pw, ph), radius=int(min(pw, ph) * 0.08), fill=255)
        p.putalpha(mask)

    # 알파 실루엣 기반 소프트 섀도
    pad = max(8, margin // 2)
    shadow = Image.new("RGBA", (pw + pad * 2, ph + pad * 2), (0, 0, 0, 0))
    silhouette = Image.new("RGBA", (pw, ph), (0, 0, 0, 107))
    shadow.paste(silhouette, (pad, pad), p.split()[3])
    shadow = shadow.filter(ImageFilter.GaussianBlur(pad / 2))
    canvas.alpha_composite(shadow, (px - pad + margin // 8, py - pad + margin // 4))
    canvas.alpha_composite(p, (px, py))

    draw = ImageDraw.Draw(canvas)
    try:
        font = ImageFont.truetype("arial.ttf", max(12, cw // 45))
    except OSError:
        font = ImageFont.load_default()
    draw.text((margin, ch - margin - max(12, cw // 45)), "PIXELY AI-SERVER · MOCK", fill=(255, 255, 255, 150), font=font)

    out = io.BytesIO()
    _tone(canvas, prompt or "").save(out, format="PNG")
    return out.getvalue()
