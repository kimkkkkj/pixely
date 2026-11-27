import { useEffect, useState } from 'react';
import { Link, NavLink, useLocation, useNavigate } from 'react-router-dom';
import { selectIsAdmin, useAuthStore } from '../../store/authStore';
import { toast } from '../../store/uiStore';
import Button from '../common/Button';
import { Logo } from '../common/Display';

/** 상단 내비게이션 + 모바일 메뉴 (기존 navbar.js 대체) */
export default function Navbar() {
  const user = useAuthStore((s) => s.user);
  const isAdmin = useAuthStore(selectIsAdmin);
  const logout = useAuthStore((s) => s.logout);
  const [open, setOpen] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => setOpen(false), [location.pathname]);
  useEffect(() => {
    if (!open) return undefined;
    const onKey = (e) => e.key === 'Escape' && setOpen(false);
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [open]);

  const handleLogout = () => {
    logout();
    toast.info('로그아웃되었습니다.');
    navigate('/');
  };

  const links = (
    <>
      <NavLink to="/studio" className="px-nav__link">스튜디오</NavLink>
      {user && <NavLink to="/history" className="px-nav__link">내 생성 기록</NavLink>}
      {isAdmin && <NavLink to="/admin" className="px-nav__link">관리자</NavLink>}
      <a href="/#how" className="px-nav__link">사용법</a>
      <a href="/#faq" className="px-nav__link">FAQ</a>
    </>
  );

  const actions = user ? (
    <>
      <span className="px-nav__user">{user.nickname}님</span>
      <Button variant="ghost" size="sm" icon="logout" onClick={handleLogout}>로그아웃</Button>
    </>
  ) : (
    <>
      <Button variant="ghost" size="sm" onClick={() => navigate('/login', { state: { from: location.pathname } })}>로그인</Button>
      <Button variant="primary" size="sm" onClick={() => navigate('/signup')}>무료로 시작하기</Button>
    </>
  );

  return (
    <header className="px-nav">
      <nav className="px-nav__inner container-px" aria-label="주 메뉴">
        <Link to="/" aria-label="Pixely 홈"><Logo /></Link>
        <div className="px-nav__links">{links}</div>
        <div className="px-nav__actions">{actions}</div>
        <Button
          className="px-nav__toggle"
          variant="ghost"
          iconOnly
          icon={open ? 'x' : 'menu'}
          aria-label={open ? '메뉴 닫기' : '메뉴 열기'}
          aria-expanded={open}
          aria-controls="mobile-menu"
          onClick={() => setOpen((v) => !v)}
        />
      </nav>
      {open && (
        <div id="mobile-menu" className="px-nav__mobile">
          {links}
          <div className="d-flex gap-2 mt-2 align-items-center">{actions}</div>
        </div>
      )}
    </header>
  );
}
