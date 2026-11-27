import { useRef } from 'react';
import { FILTERS } from '../../lib/filters';
import { STATUS_LABEL } from '../../lib/format';
import { useDragScroll } from '../../hooks/useDragScroll';
import Button from '../common/Button';
import Icon from '../common/Icon';
import { EmptyState, ErrorState, Skeleton, StatusBadge, WaveLoader } from '../common/Display';

function FilterStrip({ enabled, thumbs, value, onChange }) {
  const ref = useRef(null);
  useDragScroll(ref, { axis: 'x' });
  return (
    <div className="filter-strip-wrap">
      <div className="filter-strip__head">
        <span className="filter-strip__title"><Icon name="filter" size={16} /> 분위기 필터</span>
        {!enabled && <span className="filter-strip__hint"><Icon name="lock" size={13} /> 이미지 생성 후 사용할 수 있어요</span>}
      </div>
      <div ref={ref} className="filter-strip thin-scroll" role="radiogroup" aria-label="분위기 필터">
        {FILTERS.map((f) => {
          const active = enabled && value === f.type;
          return (
            <button
              key={f.type}
              type="button"
              role="radio"
              aria-checked={active}
              className={`filter-chip ${active ? 'is-active' : ''}`}
              disabled={!enabled}
              onClick={() => onChange(f.type)}
            >
              <span className="filter-chip__thumb">
                {enabled && thumbs[f.type] ? (
                  <img src={thumbs[f.type]} alt="" draggable="false" />
                ) : enabled ? (
                  <Skeleton height="100%" radius="inherit" />
                ) : (
                  <span className={`filter-chip__placeholder filter-chip__placeholder--${f.type.toLowerCase()}`} />
                )}
              </span>
              <span className="filter-chip__label">{f.label}</span>
            </button>
          );
        })}
      </div>
    </div>
  );
}

export default function PreviewPanel({ stepLabel, generation, filter, setFilter, filtered, onRetry, actions }) {
  const status = generation?.status;
  const isGenerating = status === 'PENDING' || status === 'PROCESSING';
  const isDone = status === 'DONE';
  const isFailed = status === 'FAILED';
  const canUseResult = isDone && filtered.ready;

  let body;
  if (isGenerating) {
    body = <WaveLoader sub={status === 'PENDING' ? '요청을 접수했어요' : 'AI가 두 이미지를 합성하고 있어요 · 잠시만 기다려 주세요'} />;
  } else if (isFailed) {
    body = (
      <ErrorState
        title="이미지를 만들지 못했어요"
        description={generation.errorMessage || '잠시 후 다시 시도해 주세요.'}
        onRetry={onRetry}
      />
    );
  } else if (isDone) {
    body = filtered.error ? (
      <ErrorState description={filtered.error} />
    ) : filtered.previewUrl ? (
      <img className="preview__img" src={filtered.previewUrl} alt="생성된 광고 이미지 미리보기" />
    ) : (
      <Skeleton className="preview__skeleton" height="100%" />
    );
  } else {
    body = (
      <EmptyState
        icon="sparkles"
        title="여기에 광고 이미지가 표시돼요"
        description="왼쪽에서 상품과 모델을 고르고, 요청사항을 적은 뒤 '이미지 생성하기'를 눌러 주세요."
      />
    );
  }

  return (
    <section className="studio-panel studio-preview" aria-labelledby="preview-title">
      <header className="studio-panel__head">
        <h2 id="preview-title" className="studio-panel__title">
          {stepLabel && <span className="studio-step">{stepLabel}</span>}
          결과 미리보기
          {status && <StatusBadge status={status}>{STATUS_LABEL[status]}</StatusBadge>}
        </h2>
        <div className="studio-preview__actions">
          <Button
            size="sm"
            variant="outline"
            icon="share"
            disabled={!canUseResult}
            loading={actions.sharing}
            onClick={actions.onShare}
            aria-label="공유하기"
          >
            <span className="hide-xs">공유하기</span>
          </Button>
          <Button
            size="sm"
            variant="primary"
            icon="download"
            disabled={!canUseResult}
            loading={actions.downloading}
            onClick={actions.onDownload}
            aria-label="다운로드"
          >
            <span className="hide-xs">다운로드</span>
          </Button>
        </div>
      </header>
      <div className={`preview ${isDone ? 'is-done' : ''}`} aria-live="polite" aria-busy={isGenerating}>
        {body}
      </div>
      <FilterStrip enabled={canUseResult} thumbs={filtered.thumbs} value={filter} onChange={setFilter} />
    </section>
  );
}
