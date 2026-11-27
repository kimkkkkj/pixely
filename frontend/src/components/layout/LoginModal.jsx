import { Link } from 'react-router-dom';
import { useUiStore } from '../../store/uiStore';
import Modal from '../common/Modal';
import AuthForm from './AuthForm';

/** 비로그인 상태에서 생성 버튼 등을 눌렀을 때 뜨는 로그인 모달 */
export default function LoginModal() {
  const { open, reason } = useUiStore((s) => s.loginModal);
  const close = useUiStore((s) => s.closeLogin);
  return (
    <Modal open={open} onClose={close} title="로그인이 필요해요" description={reason || '로그인하면 이미지를 생성하고 기록을 저장할 수 있어요.'}>
      <AuthForm mode="login" onSuccess={close} />
      <p className="text-center mt-4 text-muted-token" style={{ fontSize: 'var(--fs-sm)' }}>
        아직 계정이 없나요?{' '}
        <Link to="/signup" onClick={close}>회원가입</Link>
      </p>
    </Modal>
  );
}
