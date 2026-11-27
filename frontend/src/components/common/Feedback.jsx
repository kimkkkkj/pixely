import { useUiStore } from '../../store/uiStore';
import Button from './Button';
import Icon from './Icon';
import Modal from './Modal';

const TOAST_ICON = { success: 'check', error: 'alert', info: 'info' };

/** 토스트 영역 (alert 대체) */
export function ToastViewport() {
  const toasts = useUiStore((s) => s.toasts);
  const dismiss = useUiStore((s) => s.dismissToast);
  return (
    <div className="px-toast-viewport" role="region" aria-label="알림">
      {toasts.map((t) => (
        <div key={t.id} className={`px-toast px-toast--${t.type}`} role={t.type === 'error' ? 'alert' : 'status'}>
          <span className="px-toast__icon"><Icon name={TOAST_ICON[t.type]} size={18} /></span>
          <span className="px-toast__msg">{t.message}</span>
          <button type="button" className="px-toast__close" onClick={() => dismiss(t.id)} aria-label="알림 닫기">
            <Icon name="x" size={14} />
          </button>
        </div>
      ))}
    </div>
  );
}

/** 확인 모달 (confirm 대체) — confirmDialog({ title, message, confirmText, danger }) */
export function ConfirmHost() {
  const state = useUiStore((s) => s.confirmState);
  const resolve = useUiStore((s) => s.resolveConfirm);
  return (
    <Modal
      open={!!state}
      onClose={() => resolve(false)}
      title={state?.title || '확인'}
      description={state?.message}
      footer={
        <>
          <Button variant="ghost" onClick={() => resolve(false)}>{state?.cancelText || '취소'}</Button>
          <Button variant={state?.danger ? 'danger' : 'primary'} onClick={() => resolve(true)} data-autofocus>
            {state?.confirmText || '확인'}
          </Button>
        </>
      }
    />
  );
}
