import { useRef } from 'react';
import Button from '../common/Button';
import Modal from '../common/Modal';

/** 클립보드 접근이 막혔을 때 링크를 직접 복사할 수 있게 보여주는 모달 */
export default function ShareFallbackModal({ url, onClose }) {
  const inputRef = useRef(null);
  return (
    <Modal open={!!url} onClose={onClose} title="공유 링크" description="아래 링크를 복사해서 공유하세요. 30일 동안 유효합니다.">
      <input
        ref={inputRef}
        className="px-input"
        readOnly
        value={url || ''}
        onFocus={(e) => e.target.select()}
        aria-label="공유 링크"
        data-autofocus
      />
      <div className="d-flex justify-content-end gap-2 mt-3">
        <Button variant="ghost" icon="external" onClick={() => window.open(url, '_blank', 'noopener')}>열어보기</Button>
        <Button variant="primary" onClick={onClose}>닫기</Button>
      </div>
    </Modal>
  );
}
