import { useCallback, useEffect, useRef, useState } from 'react';
import { canvasToBlob, FILTERS, loadImage, renderFiltered } from '../lib/filters';

/**
 * 결과 이미지 URL 을 받아 필터 6종 썸네일과 현재 필터의 전체 크기 미리보기를 만든다.
 * 계산은 결과/필터별로 캐시한다.
 */
export function useFilteredResult(resultUrl, filter) {
  const [image, setImage] = useState(null);
  const [thumbs, setThumbs] = useState({});
  const [previewUrl, setPreviewUrl] = useState(null);
  const [error, setError] = useState(null);
  const cache = useRef(new Map());

  // 결과 이미지 로드 + 썸네일 생성
  useEffect(() => {
    cache.current.forEach((url) => URL.revokeObjectURL(url));
    cache.current = new Map();
    setImage(null);
    setThumbs({});
    setPreviewUrl(null);
    setError(null);
    if (!resultUrl) return undefined;
    let cancelled = false;
    loadImage(resultUrl)
      .then((img) => {
        if (cancelled) return;
        setImage(img);
        const next = {};
        FILTERS.forEach((f) => {
          next[f.type] = renderFiltered(img, f.type, 220).toDataURL('image/jpeg', 0.85);
        });
        setThumbs(next);
      })
      .catch((e) => !cancelled && setError(e.message));
    return () => {
      cancelled = true;
    };
  }, [resultUrl]);

  // 현재 필터 전체 크기 미리보기
  useEffect(() => {
    if (!image) return undefined;
    if (filter === 'ORIGINAL') {
      setPreviewUrl(resultUrl);
      return undefined;
    }
    const cached = cache.current.get(filter);
    if (cached) {
      setPreviewUrl(cached);
      return undefined;
    }
    let cancelled = false;
    canvasToBlob(renderFiltered(image, filter)).then((blob) => {
      const url = URL.createObjectURL(blob);
      cache.current.set(filter, url);
      if (!cancelled) setPreviewUrl(url);
    });
    return () => {
      cancelled = true;
    };
  }, [image, filter, resultUrl]);

  useEffect(() => {
    const map = cache.current;
    return () => map.forEach((url) => URL.revokeObjectURL(url));
  }, []);

  /** 다운로드/저장용 전체 크기 canvas */
  const renderCanvas = useCallback((type) => (image ? renderFiltered(image, type) : null), [image]);

  return { ready: !!image, thumbs, previewUrl, error, renderCanvas };
}
