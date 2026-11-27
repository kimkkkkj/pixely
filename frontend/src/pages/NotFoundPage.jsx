import { useNavigate } from 'react-router-dom';
import Button from '../components/common/Button';
import { EmptyState } from '../components/common/Display';

export default function NotFoundPage() {
  const navigate = useNavigate();
  return (
    <div className="page container-px d-grid" style={{ placeItems: 'center' }}>
      <EmptyState
        icon="search"
        title="페이지를 찾을 수 없어요"
        description="주소가 바뀌었거나 삭제된 페이지입니다."
        action={<Button variant="primary" onClick={() => navigate('/')}>홈으로</Button>}
      />
    </div>
  );
}
