import { useCallback, useState } from 'react';
import { generationApi } from '../api';
import { canvasToBlob, downloadBlob, filterLabel } from '../lib/filters';
import { isTouchDevice } from '../lib/format';
import { toast } from '../store/uiStore';

/** 결과 다운로드(현재 필터 적용 PNG) + 공유 링크 생성/복사/네이티브 공유 */
export function useResultActions() {
  const [downloading, setDownloading] = useState(false);
  const [sharing, setSharing] = useState(false);
  const [shareFallbackUrl, setShareFallbackUrl] = useState(null);

  const saveVariant = useCallback(async (generationId, filter, canvas) => {
    if (filter === 'ORIGINAL' || !canvas) return;
    await generationApi.saveVariant(generationId, { filterType: filter, imageBase64: canvas.toDataURL('image/png') });
  }, []);

  const download = useCallback(
    async (generationId, filter, renderCanvas) => {
      const canvas = renderCanvas(filter);
      if (!canvas) return;
      setDownloading(true);
      try {
        const blob = await canvasToBlob(canvas);
        downloadBlob(blob, `pixely-${generationId}-${filter.toLowerCase()}.png`);
        toast.success(`'${filterLabel(filter)}' 필터가 적용된 PNG를 저장했어요.`);
        saveVariant(generationId, filter, canvas).catch(() => {});
      } catch (e) {
        toast.error(e.message || '다운로드에 실패했어요.');
      } finally {
        setDownloading(false);
      }
    },
    [saveVariant],
  );

  const share = useCallback(
    async (generationId, filter, renderCanvas) => {
      setSharing(true);
      try {
        await saveVariant(generationId, filter, renderCanvas(filter)).catch(() => {});
        const { url } = await generationApi.share(generationId);
        if (isTouchDevice() && navigator.share) {
          try {
            await navigator.share({ title: 'Pixely 광고 이미지', text: 'Pixely로 만든 광고 이미지를 확인해 보세요.', url });
            return;
          } catch (e) {
            if (e?.name === 'AbortError') return;
          }
        }
        try {
          await navigator.clipboard.writeText(url);
          toast.success('공유 링크를 클립보드에 복사했어요.');
        } catch {
          setShareFallbackUrl(url);
        }
      } catch (e) {
        toast.error(e.message);
      } finally {
        setSharing(false);
      }
    },
    [saveVariant],
  );

  return { download, share, downloading, sharing, shareFallbackUrl, closeShareFallback: () => setShareFallbackUrl(null) };
}
