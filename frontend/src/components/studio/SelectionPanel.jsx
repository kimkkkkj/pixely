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

function Slot({ type, image, onEmptyClick }) {
  const clearSlot = useStudioStore((s) => s.clearSlot);
  const label = type === IMAGE_TYPES.PRODUCT ? '상품' : '모델';

  const handleClick = async () => {
    if (!image) {
      onEmptyClick(type);
      return;
    }
    const ok = await confirmDialog({
      title: `${label} 선택을 해제할까요?`,
      message: `"${image.title}" 선택이 해제됩니다.`,
      confirmText: '해제',
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
      <span className="slot__label">{label}</span>
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
          <Icon name="plus" size={22} />
          <span>{label} 선택</span>
        </span>
      )}
    </button>
  );
}

export function SlotsCard({ stepLabel, onEmptyClick }) {
  const product = useStudioStore((s) => s.product);
  const model = useStudioStore((s) => s.model);
  return (
    <section className="studio-panel studio-slots" aria-labelledby="slots-title">
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
      </header>
      <div className="px-field">
        <label className="sr-only" htmlFor={`${id}-prompt`}>요청사항</label>
        <textarea
          id={`${id}-prompt`}
          className="px-textarea"
          value={prompt}
          maxLength={MAX_PROMPT}
          onChange={(e) => setPrompt(e.target.value)}
          placeholder="예) 주근깨를 모두 지우고 화면을 밝게 해줘"
          rows={4}
          aria-describedby={`${id}-count`}
        />
        <span id={`${id}-count`} className="px-help text-end">{prompt.length}/{MAX_PROMPT}</span>
      </div>
      <div className="prompt-chips" aria-label="예시 요청사항">
        {PROMPT_CHIPS.map((chip) => (
          <button key={chip} type="button" className="px-chip" onClick={() => setPrompt(chip)}>
            {chip}
          </button>
        ))}
      </div>
      <Button variant="primary" size="lg" block icon="sparkles" onClick={onGenerate} loading={generating} disabled={generating}>
        {generating ? '이미지 생성중...' : '이미지 생성하기'}
      </Button>
      {!ready && !generating && <p className="px-help text-center">상품과 모델 이미지를 하나씩 선택해 주세요.</p>}
    </section>
  );
}
