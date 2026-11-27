import { useId } from 'react';
import { IMAGE_TYPES, useStudioStore } from '../../store/studioStore';
import { confirmDialog } from '../../store/uiStore';
import Button from '../common/Button';
import Icon from '../common/Icon';

const MAX_PROMPT = 1000;
const PROMPT_CHIPS = [
  '주근깨를 모두 지우고 화면을 밝게 해줘',
  '모델이 상품을 손에 들고 미소 짓는 느낌으로',
  '따뜻한 햇살이 들어오는 감성적인 분위기',
  '깔끔한 화이트 스튜디오 배경, 선명하게',
];

/** 원본 시안 우측 상단: 공유하기 / 다운로드 (반반) */
export function ResultActions({ actions, enabled, compact = false }) {
  return (
    <div className={`result-actions ${compact ? 'result-actions--compact' : ''}`}>
      <Button
        variant="share"
        size={compact ? 'sm' : 'md'}
        icon="share"
        disabled={!enabled}
        loading={actions.sharing}
        onClick={actions.onShare}
        aria-label="공유하기"
      >
        <span className={compact ? 'hide-xs' : ''}>공유하기</span>
      </Button>
      <Button
        variant="download"
        size={compact ? 'sm' : 'md'}
        icon="download"
        disabled={!enabled}
        loading={actions.downloading}
        onClick={actions.onDownload}
        aria-label="다운로드"
      >
        <span className={compact ? 'hide-xs' : ''}>다운로드</span>
      </Button>
    </div>
  );
}

function Slot({ type, image, onEmptyClick }) {
  const clearSlot = useStudioStore((s) => s.clearSlot);
  const label = type === IMAGE_TYPES.PRODUCT ? '상품' : '모델';

  const handleClick = async () => {
    if (!image) {
      onEmptyClick(type);
      return;
    }
    const ok = await confirmDialog({
      title: '이미지를 삭제하시겠어요?',
      message: `선택한 ${label} 이미지 "${image.title}"가 슬롯에서 빠집니다.`,
      confirmText: '삭제',
    });
    if (ok) clearSlot(type);
  };

  return (
    <button
      type="button"
      className={`slot ${image ? 'is-filled' : ''}`}
      onClick={handleClick}
      aria-label={image ? `${label} 이미지 ${image.title} 선택됨. 눌러서 해제` : `${label} 이미지 선택하러 가기`}
    >
      {image ? (
        <>
          <span className={`slot__img ${type === IMAGE_TYPES.PRODUCT ? 'slot__img--product' : ''}`}>
            <img src={image.imageUrl} alt={image.title} />
          </span>
          <span className="slot__title">{image.title}</span>
          <span className="slot__remove" aria-hidden="true"><Icon name="x" size={12} strokeWidth={2.4} /></span>
        </>
      ) : (
        <span className="slot__empty">
          <Icon name="plus" size={20} />
          <span>{label} 이미지를<br />넣어주세요</span>
        </span>
      )}
    </button>
  );
}

export function SlotsCard({ stepLabel, onEmptyClick, actions, actionsEnabled }) {
  const product = useStudioStore((s) => s.product);
  const model = useStudioStore((s) => s.model);
  return (
    <section className="studio-panel studio-slots" aria-labelledby="slots-title">
      {actions && <ResultActions actions={actions} enabled={actionsEnabled} />}
      <header className="studio-panel__head">
        <h2 id="slots-title" className="studio-panel__title">
          {stepLabel && <span className="studio-step">{stepLabel}</span>}
          선택한 이미지
        </h2>
        <span className="studio-panel__hint">{(product ? 1 : 0) + (model ? 1 : 0)}/2</span>
      </header>
      <div className="slots">
        <Slot type={IMAGE_TYPES.PRODUCT} image={product} onEmptyClick={onEmptyClick} />
        <Slot type={IMAGE_TYPES.MODEL} image={model} onEmptyClick={onEmptyClick} />
      </div>
    </section>
  );
}

export function PromptCard({ stepLabel, onGenerate, generating }) {
  const id = useId();
  const prompt = useStudioStore((s) => s.prompt);
  const setPrompt = useStudioStore((s) => s.setPrompt);
  const product = useStudioStore((s) => s.product);
  const model = useStudioStore((s) => s.model);
  const ready = product && model;

  return (
    <section className="studio-panel studio-prompt" aria-labelledby="prompt-title">
      <header className="studio-panel__head">
        <h2 id="prompt-title" className="studio-panel__title">
          {stepLabel && <span className="studio-step">{stepLabel}</span>}
          요청사항
        </h2>
        <span id={`${id}-count`} className="studio-panel__hint">{prompt.length}/{MAX_PROMPT}</span>
      </header>
      <div className="prompt-field">
        <label className="sr-only" htmlFor={`${id}-prompt`}>요청사항</label>
        <textarea
          id={`${id}-prompt`}
          className="prompt-field__input"
          value={prompt}
          maxLength={MAX_PROMPT}
          onChange={(e) => setPrompt(e.target.value)}
          placeholder="여기에 요청사항을 적어보세요!"
          aria-describedby={`${id}-count`}
        />
      </div>
      <div className="prompt-chips" aria-label="예시 요청사항">
        {PROMPT_CHIPS.map((chip) => (
          <button key={chip} type="button" className="px-chip" onClick={() => setPrompt(chip)}>
            {chip}
          </button>
        ))}
      </div>
      <Button variant="primary" size="xl" block icon="sparkles" onClick={onGenerate} loading={generating} disabled={generating}>
        {generating ? '이미지 생성중...' : '이미지 생성하기'}
      </Button>
      {!ready && !generating && <p className="px-help text-center">상품 이미지와 모델 이미지를 모두 선택해 주세요.</p>}
    </section>
  );
}
