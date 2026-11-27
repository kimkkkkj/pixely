import { useEffect } from 'react';

/**
 * 마우스로 끌어서 스크롤 (기존 시안은 첫 번째 그리드에만 붙던 버그 → ref 마다 개별 적용)
 * 5px 이상 끌었을 때만 드래그로 판단하고, 이어지는 click 은 한 번 무시한다.
 */
export function useDragScroll(ref, { axis = 'y' } = {}) {
  useEffect(() => {
    const el = ref.current;
    if (!el) return undefined;
    let down = false;
    let dragged = false;
    let start = 0;
    let startScroll = 0;

    const onDown = (e) => {
      if (e.pointerType !== 'mouse' || e.button !== 0) return;
      down = true;
      dragged = false;
      start = axis === 'y' ? e.clientY : e.clientX;
      startScroll = axis === 'y' ? el.scrollTop : el.scrollLeft;
    };
    const onMove = (e) => {
      if (!down) return;
      const delta = (axis === 'y' ? e.clientY : e.clientX) - start;
      if (!dragged && Math.abs(delta) > 5) {
        dragged = true;
        el.classList.add('is-dragging');
      }
      if (dragged) {
        if (axis === 'y') el.scrollTop = startScroll - delta;
        else el.scrollLeft = startScroll - delta;
      }
    };
    const onUp = () => {
      if (!down) return;
      down = false;
      el.classList.remove('is-dragging');
      if (dragged) {
        const suppress = (ev) => {
          ev.stopPropagation();
          ev.preventDefault();
        };
        el.addEventListener('click', suppress, { capture: true, once: true });
        setTimeout(() => el.removeEventListener('click', suppress, { capture: true }), 0);
      }
    };

    el.addEventListener('pointerdown', onDown);
    window.addEventListener('pointermove', onMove);
    window.addEventListener('pointerup', onUp);
    return () => {
      el.removeEventListener('pointerdown', onDown);
      window.removeEventListener('pointermove', onMove);
      window.removeEventListener('pointerup', onUp);
    };
  }, [ref, axis]);
}
