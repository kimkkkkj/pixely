import { useCallback, useEffect, useRef, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { generationApi } from '../api';
import { useAuthStore } from '../store/authStore';
import { useStudioStore } from '../store/studioStore';
import { toast, useUiStore } from '../store/uiStore';
import { useFilteredResult } from '../hooks/useFilteredResult';
import { useMediaQuery } from '../hooks/useMediaQuery';
import { useResultActions } from '../hooks/useResultActions';
import LibraryPanel from '../components/studio/LibraryPanel';
import PreviewPanel from '../components/studio/PreviewPanel';
import { PromptCard, SlotsCard } from '../components/studio/SelectionPanel';
import ShareFallbackModal from '../components/studio/ShareFallbackModal';
import '../styles/studio.css';

const POLL_MS = 1500;

export default function StudioPage() {
  const token = useAuthStore((s) => s.token);
  const openLogin = useUiStore((s) => s.openLogin);
  const { product, model, prompt, generation, filter, setGeneration, setFilter, setTab, loadFromHistory } = useStudioStore();
  const [submitting, setSubmitting] = useState(false);
  const [searchParams, setSearchParams] = useSearchParams();
  const compact = useMediaQuery('(max-width: 1023px)');
  const previewRef = useRef(null);

  const filtered = useFilteredResult(generation?.status === 'DONE' ? generation.resultUrl : null, filter);
  const actions = useResultActions();
  const isGenerating = submitting || generation?.status === 'PENDING' || generation?.status === 'PROCESSING';

  // 히스토리에서 "다시 편집" 으로 들어온 경우
  const fromId = searchParams.get('generation');
  useEffect(() => {
    if (!fromId || !token) return;
    generationApi
      .get(fromId)
      .then((g) => {
        loadFromHistory(g);
        toast.info('이전 작업을 불러왔어요. 필터를 바꾸거나 다시 생성해 보세요.');
      })
      .catch((e) => toast.error(e.message))
      .finally(() => setSearchParams({}, { replace: true }));
  }, [fromId, token, loadFromHistory, setSearchParams]);

  // 상태 polling (1.5초)
  const generationId = generation?.id;
  const generationStatus = generation?.status;
  useEffect(() => {
    if (!generationId || (generationStatus !== 'PENDING' && generationStatus !== 'PROCESSING')) return undefined;
    let stopped = false;
    const timer = setInterval(async () => {
      try {
        const g = await generationApi.get(generationId);
        if (stopped) return;
        if (g.status !== generationStatus) {
          setGeneration({ id: g.id, status: g.status, resultUrl: g.resultUrl, errorMessage: g.errorMessage });
          if (g.status === 'DONE') toast.success('광고 이미지가 완성됐어요!');
          if (g.status === 'FAILED') toast.error(g.errorMessage || '이미지 생성에 실패했어요.');
        }
      } catch (e) {
        if (!stopped && e.status && e.status < 500) {
          setGeneration({ id: generationId, status: 'FAILED', errorMessage: e.message });
        }
      }
    }, POLL_MS);
    return () => {
      stopped = true;
      clearInterval(timer);
    };
  }, [generationId, generationStatus, setGeneration]);

  const handleGenerate = useCallback(async () => {
    if (!token) {
      openLogin('이미지를 생성하려면 로그인이 필요해요. 선택한 이미지와 요청사항은 그대로 유지돼요.');
      return;
    }
    if (!product || !model) {
      toast.info(!product ? '상품 이미지를 먼저 선택해 주세요.' : '모델 이미지를 먼저 선택해 주세요.');
      setTab(!product ? 'PRODUCT' : 'MODEL');
      return;
    }
    setSubmitting(true);
    try {
      const { generationId: id, status } = await generationApi.create({
        productImageId: product.id,
        modelImageId: model.id,
        prompt: prompt.trim() || null,
      });
      setFilter('ORIGINAL');
      setGeneration({ id, status, resultUrl: null, errorMessage: null });
      if (compact) previewRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    } catch (e) {
      toast.error(e.message);
    } finally {
      setSubmitting(false);
    }
  }, [token, product, model, prompt, openLogin, setTab, setFilter, setGeneration, compact]);

  const handleEmptySlot = useCallback(
    (type) => {
      setTab(type);
      const lib = document.getElementById('studio-library');
      if (compact) lib?.scrollIntoView({ behavior: 'smooth', block: 'start' });
      setTimeout(() => lib?.querySelector('input[type="search"]')?.focus({ preventScroll: true }), compact ? 400 : 0);
    },
    [setTab, compact],
  );

  const canUseResult = generation?.status === 'DONE' && filtered.ready;
  const resultActions = {
    sharing: actions.sharing,
    downloading: actions.downloading,
    onShare: () => actions.share(generation.id, filter, filtered.renderCanvas),
    onDownload: () => actions.download(generation.id, filter, filtered.renderCanvas),
  };

  return (
    <div className="studio">
      <div className="studio__grid">
        <div className="studio__area studio__area--lib">
          <LibraryPanel stepLabel={compact ? '1' : null} />
        </div>
        <div className="studio__area studio__area--slots">
          <SlotsCard
            stepLabel={compact ? '1' : null}
            onEmptyClick={handleEmptySlot}
            actions={compact ? null : resultActions}
            actionsEnabled={canUseResult}
          />
        </div>
        <div className="studio__area studio__area--prompt">
          <PromptCard stepLabel={compact ? '2' : null} onGenerate={handleGenerate} generating={isGenerating} />
        </div>
        <div className="studio__area studio__area--preview" ref={previewRef}>
          <PreviewPanel
            stepLabel={compact ? '3' : null}
            generation={generation}
            filter={filter}
            setFilter={setFilter}
            filtered={filtered}
            onRetry={handleGenerate}
            actions={resultActions}
            showActions={compact}
          />
        </div>
      </div>
      <ShareFallbackModal url={actions.shareFallbackUrl} onClose={actions.closeShareFallback} />
    </div>
  );
}
