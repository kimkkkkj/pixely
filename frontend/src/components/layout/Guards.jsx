import { Navigate, useLocation } from 'react-router-dom';
import { selectIsAdmin, useAuthStore } from '../../store/authStore';
import { EmptyState } from '../common/Display';

export function RequireAuth({ children }) {
  const token = useAuthStore((s) => s.token);
  const location = useLocation();
  if (!token) return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  return children;
}

export function RequireAdmin({ children }) {
  const token = useAuthStore((s) => s.token);
  const isAdmin = useAuthStore(selectIsAdmin);
  const location = useLocation();
  if (!token) return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  if (!isAdmin) {
    return (
      <div className="page container-px">
        <EmptyState icon="lock" title="관리자만 접근할 수 있어요" description="관리자 계정으로 로그인해 주세요." />
      </div>
    );
  }
  return children;
}
