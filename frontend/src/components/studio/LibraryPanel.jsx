import { useCallback, useRef, useState } from 'react';
import { libraryApi } from '../../api';
import { useAuthStore } from '../../store/authStore';
import { IMAGE_TYPES, useStudioStore } from '../../store/studioStore';
import { confirmDialog, toast, useUiStore } from '../../store/uiStore';
import { useDebounce } from '../../hooks/useDebounce';
import { useDragScroll } from '../../hooks/useDragScroll';
import { useInfiniteLibrary } from '../../hooks/useInfiniteLibrary';
import { useIntersection } from '../../hooks/useIntersection';
import Button from '../common/Button';
import Icon from '../common/Icon';
import { EmptyState, ErrorState, Skeleton, Tabs } from '../common/Display';
import UploadModal from './UploadModal';

const TABS = [
  { value: IMAGE_TYPES.PRODUCT, label: '상품 이미지', icon: 'box' },
  { value: IMAGE_TYPES.MODEL, label: '모델 이미지', icon: 'user' },
];

function GridItem({ image, selected, onSelect, onDelete }) {
  return (
    <div className={`lib-item ${selected ? 'is-selected' : ''}`}>
      <button
        type="button"
        className="lib-item__btn"
        onClick={() => onSelect(image)}
        aria-pressed={selected}
        aria-label={`${image.title} ${selected ? '선택됨, 다시 누르면 해제' : '선택'}`}
      >
        <span className={`lib-item__thumb ${image.type === 'PRODUCT' ? 'lib-item__thumb--product' : ''}`}>
          <img src={image.imageUrl} alt={image.title} loading="lazy" draggable="false" />
        </span>
        <span className="lib-item__title">{image.title}</span>
        {selected && (
          <span className="lib-item__check" aria-hidden="true"><Icon name="check" size={14} strokeWidth={3} /></span>
        )}
        {image.mine && <span className="lib-item__mine">내 이미지</span>}
      </button>
      {image.mine && (
        <button type="button" className="lib-item__delete" onClick={() => onDelete(image)} aria-label={`${image.title} 삭제`}>
          <Icon name="trash" size={14} />
        </button>
      )}
    </div>
  );
}

export default function LibraryPanel({ stepLabel }) {
  const tab = useStudioStore((s) => s.tab);
  const setTab = useStudioStore((s) => s.setTab);
  const product = useStudioStore((s) => s.product);
  const model = useStudioStore((s) => s.model);
  const select = useStudioStore((s) => s.select);
  const clearSlot = useStudioStore((s) => s.clearSlot);
  const token = useAuthStore((s) => s.token);
  const userId = useAuthStore((s) => s.user?.id);
  const openLogin = useUiStore((s) => s.openLogin);

  const [query, setQuery] = useState('');
  const q = useDebounce(query.trim(), 300);
  const [uploadOpen, setUploadOpen] = useState(false);

  const lib = useInfiniteLibrary({ type: tab, q, authKey: userId ?? 'anon' });
  const scrollRef = useRef(null);
  const sentinelRef = useRef(null);
  useDragScroll(scrollRef);
  useIntersection(sentinelRef, lib.loadMore, { rootRef: scrollRef, enabled: lib.hasNext && !lib.loading });

  const selectedId = tab === IMAGE_TYPES.PRODUCT ? product?.id : model?.id;

  const handleDelete = useCallback(
    async (image) => {
      const ok = await confirmDialog({ title: '이미지를 삭제할까요?', message: `"${image.title}" 이미지가 내 라이브러리에서 삭제됩니다.`, confirmText: '삭제', danger: true });
      if (!ok) return;
      try {
        await libraryApi.remove(image.id);
        lib.remove(image.id);
        if (product?.id === image.id) clearSlot(IMAGE_TYPES.PRODUCT);
        if (model?.id === image.id) clearSlot(IMAGE_TYPES.MODEL);
        toast.success('삭제했어요.');
      } catch (e) {
        toast.error(e.message);
      }
    },
    [lib, product, model, clearSlot],
  );

  const handleUpload = async (payload) => {
    const created = await libraryApi.upload(payload);
    toast.success('업로드했어요. 바로 선택해 두었어요.');
    if (created.type !== tab) setTab(created.type);
    else lib.prepend(created);
    select(created);
  };

  const openUpload = () => {
    if (!token) return openLogin('내 이미지를 업로드하려면 로그인이 필요해요.');
    setUploadOpen(true);
  };

  return (
    <section className="studio-panel studio-lib" aria-labelledby="lib-title" id="studio-library">
      <header className="studio-panel__head">
        <h2 id="lib-title" className="studio-panel__title">
          {stepLabel && <span className="studio-step">{stepLabel}</span>}
          이미지 선택
        </h2>
        <Button size="sm" variant="outline" icon="upload" onClick={openUpload}>업로드</Button>
      </header>

      <div className="studio-lib__controls">
        <Tabs tabs={TABS} value={tab} onChange={setTab} label="이미지 종류" idPrefix="lib-tab" />
        <div className="px-search">
          <Icon name="search" size={16} />
          <input
            className="px-input"
            type="search"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder={tab === IMAGE_TYPES.PRODUCT ? '상품명, 태그 검색' : '모델명, 스타일 검색'}
            aria-label="이미지 검색"
          />
        </div>
        <p className="studio-lib__meta" aria-live="polite">
          {lib.total != null ? `${lib.total.toLocaleString('ko-KR')}개` : ' '}
          {q && lib.total != null && ` · "${q}" 검색 결과`}
        </p>
      </div>

      <div ref={scrollRef} className="studio-lib__scroll thin-scroll" id="lib-tab-panel" role="tabpanel" aria-labelledby={`lib-tab-${tab}`}>
        {lib.error && lib.items.length === 0 ? (
          <ErrorState description={lib.error} onRetry={lib.reload} />
        ) : !lib.loading && lib.items.length === 0 ? (
          <EmptyState
            icon={q ? 'search' : 'image'}
            title={q ? '검색 결과가 없어요' : '아직 이미지가 없어요'}
            description={q ? '다른 키워드로 검색해 보세요.' : '내 이미지를 업로드해 보세요.'}
            action={!q && <Button size="sm" variant="primary" icon="upload" onClick={openUpload}>업로드</Button>}
          />
        ) : (
          <div className="lib-grid">
            {lib.items.map((image) => (
              <GridItem key={image.id} image={image} selected={selectedId === image.id} onSelect={select} onDelete={handleDelete} />
            ))}
            {lib.loading &&
              Array.from({ length: lib.items.length ? 2 : 8 }, (_, i) => (
                <div key={`sk-${i}`} className="lib-item">
                  <Skeleton className="lib-item__skeleton" height="auto" />
                  <Skeleton height={12} width="70%" style={{ marginTop: 8 }} />
                </div>
              ))}
          </div>
        )}
        <div ref={sentinelRef} className="studio-lib__sentinel" aria-hidden="true" />
      </div>

      <UploadModal open={uploadOpen} onClose={() => setUploadOpen(false)} defaultType={tab} onUpload={handleUpload} />
    </section>
  );
}
