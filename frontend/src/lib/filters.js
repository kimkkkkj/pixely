/**
 * Canvas 기반 실제 픽셀 필터 (기존 시안: GIF 를 바꿔 끼우던 가짜 필터 → 결과 이미지 픽셀에 직접 적용)
 */
export const FILTERS = [
  { type: 'ORIGINAL', label: '원본' },
  { type: 'MONO', label: '흑백' },
  { type: 'ROMANTIC', label: '로맨틱' },
  { type: 'BRIGHT', label: '화사하게' },
  { type: 'PINK', label: '핑크빛' },
  { type: 'WARM', label: '따뜻하게' },
];

export const filterLabel = (type) => FILTERS.find((f) => f.type === type)?.label ?? type;

const clamp = (v) => (v < 0 ? 0 : v > 255 ? 255 : v);

function adjustPixels(ctx, w, h, fn) {
  const imageData = ctx.getImageData(0, 0, w, h);
  const d = imageData.data;
  for (let i = 0; i < d.length; i += 4) {
    const [r, g, b] = fn(d[i], d[i + 1], d[i + 2]);
    d[i] = clamp(r);
    d[i + 1] = clamp(g);
    d[i + 2] = clamp(b);
  }
  ctx.putImageData(imageData, 0, 0);
}

const lum = (r, g, b) => 0.2126 * r + 0.7152 * g + 0.0722 * b;

function saturate(r, g, b, amount) {
  const l = lum(r, g, b);
  return [l + (r - l) * amount, l + (g - l) * amount, l + (b - l) * amount];
}

/** 축소 후 확대하는 방식의 블러 글로우 (ctx.filter 미지원 브라우저 포함) */
function glow(ctx, canvas, strength) {
  const { width: w, height: h } = canvas;
  const small = document.createElement('canvas');
  small.width = Math.max(1, Math.round(w / 10));
  small.height = Math.max(1, Math.round(h / 10));
  const sctx = small.getContext('2d');
  sctx.imageSmoothingQuality = 'high';
  sctx.drawImage(canvas, 0, 0, small.width, small.height);
  ctx.save();
  ctx.globalAlpha = strength;
  ctx.globalCompositeOperation = 'screen';
  ctx.imageSmoothingQuality = 'high';
  ctx.drawImage(small, 0, 0, w, h);
  ctx.restore();
}

const OPS = {
  ORIGINAL: null,
  MONO: (ctx, w, h) =>
    adjustPixels(ctx, w, h, (r, g, b) => {
      const l = (lum(r, g, b) - 128) * 1.08 + 128;
      return [l, l, l];
    }),
  ROMANTIC: (ctx, w, h, canvas) => {
    adjustPixels(ctx, w, h, (r, g, b) => {
      const [sr, sg, sb] = saturate(r, g, b, 0.9);
      return [sr * 1.02 + 14, sg * 0.98 + 4, sb * 1.0 + 12];
    });
    glow(ctx, canvas, 0.32);
  },
  BRIGHT: (ctx, w, h) =>
    adjustPixels(ctx, w, h, (r, g, b) => {
      const [sr, sg, sb] = saturate(r, g, b, 1.22);
      return [sr * 1.08 + 10, sg * 1.08 + 10, sb * 1.08 + 10];
    }),
  PINK: (ctx, w, h) =>
    adjustPixels(ctx, w, h, (r, g, b) => [r * 0.85 + 255 * 0.15 + 6, g * 0.85 + 170 * 0.15, b * 0.85 + 200 * 0.15 + 4]),
  WARM: (ctx, w, h) =>
    adjustPixels(ctx, w, h, (r, g, b) => {
      const [sr, sg, sb] = saturate(r, g, b, 1.06);
      return [sr + 20, sg + 7, sb - 16];
    }),
};

export function loadImage(src) {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.crossOrigin = 'anonymous';
    img.decoding = 'async';
    img.onload = () => resolve(img);
    img.onerror = () => reject(new Error('이미지를 불러오지 못했습니다.'));
    img.src = src;
  });
}

/** 필터를 적용한 canvas 반환. maxSize 를 주면 긴 변 기준으로 축소 (썸네일용) */
export function renderFiltered(img, type, maxSize) {
  const scale = maxSize ? Math.min(1, maxSize / Math.max(img.naturalWidth, img.naturalHeight)) : 1;
  const w = Math.max(1, Math.round(img.naturalWidth * scale));
  const h = Math.max(1, Math.round(img.naturalHeight * scale));
  const canvas = document.createElement('canvas');
  canvas.width = w;
  canvas.height = h;
  const ctx = canvas.getContext('2d', { willReadFrequently: true });
  ctx.imageSmoothingQuality = 'high';
  ctx.drawImage(img, 0, 0, w, h);
  const op = OPS[type];
  if (op) op(ctx, w, h, canvas);
  return canvas;
}

export const canvasToBlob = (canvas, type = 'image/png') =>
  new Promise((resolve, reject) =>
    canvas.toBlob((blob) => (blob ? resolve(blob) : reject(new Error('이미지 변환에 실패했습니다.'))), type),
  );

export function downloadBlob(blob, filename) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}

/** 랜딩 페이지 비포/애프터용: 브라우저에서 간단히 모델 위에 상품을 얹은 미리보기 */
export function compositePreview(modelImg, productImg, size = 720) {
  const scale = size / Math.max(modelImg.naturalWidth, modelImg.naturalHeight);
  const w = Math.round(modelImg.naturalWidth * scale);
  const h = Math.round(modelImg.naturalHeight * scale);
  const canvas = document.createElement('canvas');
  canvas.width = w;
  canvas.height = h;
  const ctx = canvas.getContext('2d');
  ctx.drawImage(modelImg, 0, 0, w, h);
  const ps = Math.min((w * 0.42) / productImg.naturalWidth, (h * 0.5) / productImg.naturalHeight);
  const pw = productImg.naturalWidth * ps;
  const ph = productImg.naturalHeight * ps;
  const m = Math.min(w, h) * 0.06;
  ctx.save();
  ctx.shadowColor = 'rgba(0,0,0,0.35)';
  ctx.shadowBlur = m;
  ctx.shadowOffsetY = m / 4;
  ctx.drawImage(productImg, w - pw - m, h - ph - m, pw, ph);
  ctx.restore();
  return canvas;
}
