import { useEffect, useState } from 'react';
import { libraryApi } from '../api';
import { compositePreview, loadImage } from '../lib/filters';

/** 메인 페이지 비포/애프터용 샘플 (공용 상품 + 가상 모델 → 브라우저 합성 미리보기) */
export function useSamplePairs(count = 4) {
  const [pairs, setPairs] = useState(null);
  const [error, setError] = useState(false);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const [products, models] = await Promise.all([
          libraryApi.list({ type: 'PRODUCT', size: count }),
          libraryApi.list({ type: 'MODEL', size: count }),
        ]);
        const n = Math.min(products.content.length, models.content.length, count);
        const result = [];
        for (let i = 0; i < n; i++) {
          const p = products.content[i];
          const m = models.content[(i * 3 + 1) % models.content.length];
          const [pi, mi] = await Promise.all([loadImage(p.imageUrl), loadImage(m.imageUrl)]);
          result.push({ product: p, model: m, after: compositePreview(mi, pi).toDataURL('image/jpeg', 0.86) });
        }
        if (!cancelled) setPairs(result);
      } catch {
        if (!cancelled) setError(true);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [count]);

  return { pairs, error };
}
