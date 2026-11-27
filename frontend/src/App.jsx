import { lazy, Suspense, useEffect } from 'react';
import { Route, Routes, useLocation } from 'react-router-dom';
import AOS from 'aos';
import { authApi } from './api';
import { useAuthStore } from './store/authStore';
import Navbar from './components/layout/Navbar';
import Footer from './components/layout/Footer';
import LoginModal from './components/layout/LoginModal';
import { RequireAdmin, RequireAuth } from './components/layout/Guards';
import { ConfirmHost, ToastViewport } from './components/common/Feedback';
import { WaveLoader } from './components/common/Display';
import HomePage from './pages/HomePage';

const StudioPage = lazy(() => import('./pages/StudioPage'));
const HistoryPage = lazy(() => import('./pages/HistoryPage'));
const SharePage = lazy(() => import('./pages/SharePage'));
const AuthPage = lazy(() => import('./pages/AuthPage'));
const AdminPage = lazy(() => import('./pages/AdminPage'));
const NotFoundPage = lazy(() => import('./pages/NotFoundPage'));

function PageFallback() {
  return (
    <div className="page d-grid" style={{ placeItems: 'center' }}>
      <WaveLoader text="불러오는 중..." />
    </div>
  );
}

export default function App() {
  const location = useLocation();
  const token = useAuthStore((s) => s.token);
  const setUser = useAuthStore((s) => s.setUser);
  const isStudio = location.pathname.startsWith('/studio');

  // AOS 초기화 (기존 aos.js 템플릿 초기화 코드 대체)
  useEffect(() => {
    AOS.init({ duration: 700, easing: 'ease-out-cubic', once: true, offset: 40 });
  }, []);
  useEffect(() => {
    AOS.refreshHard();
    if (!location.hash) window.scrollTo(0, 0);
  }, [location.pathname, location.hash]);

  // 저장된 토큰이 있으면 사용자 정보 갱신 (만료 시 인터셉터가 로그아웃 처리)
  useEffect(() => {
    if (token) authApi.me().then(setUser).catch(() => {});
  }, [token, setUser]);

  return (
    <>
      <a href="#main" className="skip-link">본문으로 건너뛰기</a>
      <Navbar />
      <main id="main">
        <Suspense fallback={<PageFallback />}>
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/studio" element={<StudioPage />} />
            <Route path="/history" element={<RequireAuth><HistoryPage /></RequireAuth>} />
            <Route path="/share/:token" element={<SharePage />} />
            <Route path="/login" element={<AuthPage mode="login" />} />
            <Route path="/signup" element={<AuthPage mode="signup" />} />
            <Route path="/admin" element={<RequireAdmin><AdminPage /></RequireAdmin>} />
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </Suspense>
      </main>
      {!isStudio && <Footer />}
      <LoginModal />
      <ConfirmHost />
      <ToastViewport />
    </>
  );
}
