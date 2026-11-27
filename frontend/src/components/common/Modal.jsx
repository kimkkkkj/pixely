import { useEffect, useId, useRef } from 'react';
import { createPortal } from 'react-dom';
import Button from './Button';

/** 접근성 모달: ESC 닫기, 포커스 트랩, 배경 스크롤 잠금 */
export default function Modal({ open, onClose, title, description, children, footer, size, dismissible = true }) {
  const titleId = useId();
  const descId = useId();
  const dialogRef = useRef(null);
  const onCloseRef = useRef(onClose);
  onCloseRef.current = onClose;

  useEffect(() => {
    if (!open) return undefined;
    const previouslyFocused = document.activeElement;
    const dialog = dialogRef.current;
    const focusables = () =>
      dialog.querySelectorAll('button:not([disabled]), [href], input:not([disabled]), textarea, select, [tabindex]:not([tabindex="-1"])');
    const first = focusables()[0];
    (dialog.querySelector('[data-autofocus]') || first || dialog).focus();

    const onKey = (e) => {
      if (e.key === 'Escape' && dismissible) onCloseRef.current?.();
      if (e.key === 'Tab') {
        const list = focusables();
        if (!list.length) return;
        const firstEl = list[0];
        const lastEl = list[list.length - 1];
        if (e.shiftKey && document.activeElement === firstEl) {
          e.preventDefault();
          lastEl.focus();
        } else if (!e.shiftKey && document.activeElement === lastEl) {
          e.preventDefault();
          firstEl.focus();
        }
      }
    };
    document.addEventListener('keydown', onKey);
    const prevOverflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => {
      document.removeEventListener('keydown', onKey);
      document.body.style.overflow = prevOverflow;
      if (previouslyFocused instanceof HTMLElement) previouslyFocused.focus();
    };
  }, [open, dismissible]);

  if (!open) return null;
  return createPortal(
    <div className="px-modal-backdrop" onMouseDown={(e) => dismissible && e.target === e.currentTarget && onClose?.()}>
      <div
        ref={dialogRef}
        className={`px-modal ${size === 'lg' ? 'px-modal--lg' : ''}`}
        role="dialog"
        aria-modal="true"
        aria-labelledby={title ? titleId : undefined}
        aria-describedby={description ? descId : undefined}
        tabIndex={-1}
      >
        {(title || dismissible) && (
          <div className="px-modal__header">
            <div>
              {title && <h2 id={titleId} className="px-modal__title">{title}</h2>}
              {description && <p id={descId} className="px-modal__desc">{description}</p>}
            </div>
            {dismissible && <Button variant="ghost" size="sm" iconOnly icon="x" aria-label="닫기" onClick={onClose} />}
          </div>
        )}
        {children && <div className="px-modal__body">{children}</div>}
        {footer && <div className="px-modal__footer">{footer}</div>}
      </div>
    </div>,
    document.body,
  );
}
