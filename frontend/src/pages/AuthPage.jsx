import { useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import AuthForm from '../components/layout/AuthForm';
import { Card, Logo } from '../components/common/Display';
import '../styles/pages.css';

export default function AuthPage({ mode }) {
  const navigate = useNavigate();
  const location = useLocation();
  const token = useAuthStore((s) => s.token);
  const from = location.state?.from && location.state.from !== '/login' ? location.state.from : '/studio';

  useEffect(() => {
    if (token) navigate(from, { replace: true });
  }, [token, from, navigate]);

  const isSignup = mode === 'signup';
  return (
    <div className="auth-page">
      <div className="auth-page__glow" aria-hidden="true" />
      <Card className="auth-card" data-aos="fade-up">
        <div className="text-center mb-4">
          <Logo />
          <h1 className="auth-card__title">{isSignup ? '판매자 계정 만들기' : '다시 오신 걸 환영해요'}</h1>
          <p className="text-muted-token">
            {isSignup ? '가입 후 바로 광고 이미지를 만들어 보세요.' : '로그인하고 스튜디오에서 이어서 작업하세요.'}
          </p>
        </div>
        <AuthForm mode={mode} onSuccess={() => navigate(from, { replace: true })} />
        <p className="text-center mt-4 text-muted-token auth-card__switch">
          {isSignup ? '이미 계정이 있나요? ' : '아직 계정이 없나요? '}
          <Link to={isSignup ? '/login' : '/signup'} state={location.state}>
            {isSignup ? '로그인' : '회원가입'}
          </Link>
        </p>
      </Card>
    </div>
  );
}
