import { Link } from 'react-router-dom';
import { Logo } from '../common/Display';

export default function Footer() {
  return (
    <footer className="px-footer">
      <div className="container-px">
        <div className="px-footer__grid">
          <div>
            <Logo />
            <p className="mt-3" style={{ maxWidth: 360 }}>
              상품 이미지 1장 + 모델 이미지 1장으로 몇 초 만에 광고 컷을 만드는 AI 이미지 합성 서비스.
              오픈마켓 판매자의 모델 캐스팅 부담을 덜어드립니다.
            </p>
          </div>
          <div>
            <p className="px-footer__title">서비스</p>
            <ul>
              <li><Link to="/studio">스튜디오</Link></li>
              <li><Link to="/history">내 생성 기록</Link></li>
              <li><a href="/#how">사용법</a></li>
            </ul>
          </div>
          <div>
            <p className="px-footer__title">안내</p>
            <ul>
              <li><a href="/#faq">자주 묻는 질문</a></li>
              <li><span>공용 모델은 모두 가상 인물입니다</span></li>
              <li><span>사용 권한이 있는 이미지만 업로드하세요</span></li>
            </ul>
          </div>
        </div>
        <div className="px-footer__bottom">
          <span>© {new Date().getFullYear()} Pixely. All rights reserved.</span>
          <span>Made for marketplace sellers</span>
        </div>
      </div>
    </footer>
  );
}
