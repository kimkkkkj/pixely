import { useRef } from 'react';
import { FILTERS } from '../../lib/filters';
import { STATUS_LABEL } from '../../lib/format';
import { useDragScroll } from '../../hooks/useDragScroll';
import Icon from '../common/Icon';
import { ErrorState, Skeleton, StatusBadge, WaveLoader } from '../common/Display';
import { ResultActions } from './SelectionPanel';

/** 원본 시안 필터 바: "필터적용" + 정사각 타일, 선택되지 않은 타일은 어둡게, 이름은 타일 위에 */
function FilterStrip({ enabled, thumbs, value, onChange }) {
  const ref = useRef(null);
  useDragScroll(ref, { axis: 'x' });
  return (
    <div className="filter-strip-wrap">
      <div className="filter-strip__head">
        <span className="filter-strip__title">필터적용</span>
        {!enabled && <span className="filter-strip__hint"><Icon name="lock" size={13} /> 이미지 생성 후 사용할 수 있어요</span>}
      </div>
      <div ref={ref} className="filter-strip" role="radiogroup" aria-label="분위기 필터">
        {FILTERS.map((f) => {
          const active = enabled && value === f.type;
          return (
            <button
              key={f.type}
              type="button"
              role="radio"
              aria-checked={active}
              aria-label={`${f.label} 필터`}
              className={`filter-tile ${active ? 'is-active' : ''}`}
              disabled={!enabled}
              onClick={() => onChange(f.type)}
            >
              {enabled && thumbs[f.type] ? (
                <img src={thumbs[f.type]} alt="" draggable="false" />
              ) : enabled ? (
                <Skeleton height="100%" radius="inherit" className="filter-tile__sk" />
              ) : null}
              <span className="filter-tile__label">{enabled ? f.label : '필터 없음'}</span>
              {active && <span className="filter-tile__check" aria-hidden="true"><Icon name="check" size={12} strokeWidth={3} /></span>}
            </button>
          );
        })}
      </div>
    </div>
  );
}

export default function PreviewPanel({ stepLabel, generation, filter, setFilter, filtered, onRetry, actions, showActions }) {
  const status = generation?.status;
  const isGenerating = status === 'PENDING' || status === 'PROCESSING';
  const isDone = status === 'DONE';
  const isFailed = status === 'FAILED';
  const canUseResult = isDone && filtered.ready;

  let body;
  if (isGenerating) {
    body = <WaveLoader sub={status === 'PENDING' ? '요청을 접수했어요' : 'AI가 두 이미지를 합성하고 있어요'} />;
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
      <p className="preview__placeholder">
        이미지 생성하기 <br />
        버튼을 눌러주세요.
      </p>
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
        {showActions && <ResultActions actions={actions} enabled={canUseResult} compact />}
      </header>
      <div className={`preview ${isDone ? 'is-done' : ''}`} aria-live="polite" aria-busy={isGenerating}>
        {body}
      </div>
      <FilterStrip enabled={canUseResult} thumbs={filtered.thumbs} value={filter} onChange={setFilter} />
    </section>
  );
}
