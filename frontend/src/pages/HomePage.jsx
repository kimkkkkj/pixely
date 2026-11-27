import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Button from '../components/common/Button';
import Icon from '../components/common/Icon';
import { Skeleton } from '../components/common/Display';
import BeforeAfterSlider from '../components/home/BeforeAfterSlider';
import { useCountUp } from '../hooks/useCountUp';
import { useParallax } from '../hooks/useParallax';
import { useSamplePairs } from '../hooks/useSamplePairs';
import Modal from '../components/common/Modal';
import aiVisual from '../assets/brand/ai-visual.png';
import robot from '../assets/brand/robot.png';
import '../styles/home.css';

// 원본 템플릿의 히어로 배경 영상 자리. _legacy/video 의 mp4 를 frontend/public/videos/ 에 복사하고
// 루트 .env 에 VITE_HERO_VIDEO=/videos/hero.mp4 를 지정하면 재생된다 (영상은 커밋하지 않음).
const HERO_VIDEO = import.meta.env.VITE_HERO_VIDEO || '';

const PLATFORMS = ['쿠팡', '올리브영', '11번가', 'G마켓', '컬리', '스마트스토어', '옥션', 'SSG'];

const STEPS = [
  { icon: 'image', title: '이미지 선택', desc: '상품 이미지와 모델 이미지를 하나씩 고르세요. 내 상품 사진을 바로 업로드할 수도 있어요.' },
  { icon: 'wand', title: '요청사항 입력', desc: '"주근깨를 지우고 화면을 밝게" 처럼 원하는 연출을 한국어로 적으면 AI가 반영해요.' },
  { icon: 'download', title: '다운로드 · 공유', desc: '분위기 필터 6종을 적용해 PNG로 내려받거나 링크로 팀원에게 공유하세요.' },
];

const PROBLEMS = [
  { icon: 'box', title: '상품 이미지만으로는 부족해요', desc: '흰 배경 누끼 컷은 깔끔하지만, 실제로 쓰는 모습이 보이지 않아 구매 전환이 낮아요.' },
  { icon: 'user', title: '모델 캐스팅은 너무 비싸요', desc: '모델 섭외, 스튜디오, 촬영, 보정까지 — 상품 하나에 수십~수백만 원과 몇 주가 필요해요.' },
  { icon: 'refresh', title: '상품마다 다시 찍어야 해요', desc: '옵션이 늘고 시즌이 바뀔 때마다 촬영을 반복하는 건 소규모 판매자에게 큰 부담이에요.' },
];

const FAQS = [
  { q: '정말 AI가 이미지를 합성하나요?', a: '네. 운영 환경에서는 OpenAI 이미지 편집 모델 또는 자체 ComfyUI 워크플로우(FastAPI 서버)로 두 이미지를 합성합니다. 개발/데모 환경에서는 키 없이 동작하는 mock 합성기가 모델 위에 상품을 배치하고 요청사항에 맞춰 톤을 보정합니다.' },
  { q: '공용 모델 이미지는 실제 연예인인가요?', a: '아니요. 공용 모델은 저작권·초상권 문제가 없는 가상 모델입니다. 직접 업로드하는 이미지는 반드시 사용 권한이 있는 이미지만 올려 주세요.' },
  { q: '어떤 파일을 업로드할 수 있나요?', a: 'JPG, PNG, WEBP 형식, 10MB 이하 이미지를 올릴 수 있어요. 서버에서 긴 변 1600px 기준으로 자동 리사이즈합니다. 배경이 투명한 PNG 상품 이미지를 쓰면 더 자연스러워요.' },
  { q: '필터를 적용한 이미지도 저장되나요?', a: '다운로드할 때 현재 필터가 적용된 PNG가 저장되고, 같은 필터 버전이 생성 기록에도 함께 보관됩니다.' },
  { q: '하루에 몇 번 생성할 수 있나요?', a: '서버 안정성을 위해 계정당 1분에 5회까지 생성할 수 있어요. 제한은 환경변수로 조정할 수 있습니다.' },
];

function Stat({ end, suffix, label, decimals = 0 }) {
  const [ref, value] = useCountUp(end, { decimals });
  return (
    <div className="stat" ref={ref}>
      <p className="stat__value">
        <span className="text-gradient">{value.toLocaleString('ko-KR')}</span>
        <span className="stat__suffix">{suffix}</span>
      </p>
      <p className="stat__label">{label}</p>
    </div>
  );
}

function FaqItem({ item, index }) {
  const [open, setOpen] = useState(index === 0);
  const id = `faq-${index}`;
  return (
    <div className={`faq-item ${open ? 'is-open' : ''}`}>
      <h3>
        <button type="button" className="faq-item__q" aria-expanded={open} aria-controls={id} onClick={() => setOpen((v) => !v)}>
          <span>{item.q}</span>
          <Icon name="chevronDown" className="faq-item__chev" />
        </button>
      </h3>
      <div id={id} className="faq-item__a" role="region" hidden={!open}>
        <p>{item.a}</p>
      </div>
    </div>
  );
}

function HeroVisual({ pairs }) {
  const pair = pairs?.[0];
  return (
    <div className="hero-visual" data-aos="fade-left" data-aos-delay="150">
      <div className="hero-visual__inputs">
        <div className="hero-tile">
          <span className="hero-tile__label">상품</span>
          {pair ? <img src={pair.product.imageUrl} alt={pair.product.title} /> : <Skeleton height="100%" />}
        </div>
        <span className="hero-visual__op" aria-hidden="true">+</span>
        <div className="hero-tile">
          <span className="hero-tile__label">모델</span>
          {pair ? <img src={pair.model.imageUrl} alt={pair.model.title} /> : <Skeleton height="100%" />}
        </div>
      </div>
      <span className="hero-visual__op hero-visual__op--eq" aria-hidden="true">=</span>
      <div className="hero-result">
        <span className="hero-tile__label hero-tile__label--accent">광고 컷</span>
        {pair ? <img src={pair.after} alt="합성된 광고 이미지 예시" /> : <Skeleton height="100%" />}
      </div>
    </div>
  );
}

export default function HomePage() {
  const navigate = useNavigate();
  const [videoOpen, setVideoOpen] = useState(false);
  const { pairs, error } = useSamplePairs(4);
  const orbA = useParallax(0.25);
  const orbB = useParallax(-0.12);

  return (
    <div className="home">
      {/* HERO */}
      <section className="hero section-dark">
        {HERO_VIDEO ? (
          <video className="hero__video" src={HERO_VIDEO} autoPlay muted loop playsInline aria-hidden="true" />
        ) : (
          <img className="hero__visual-bg" src={aiVisual} alt="" aria-hidden="true" />
        )}
        <div className="hero__orb hero__orb--a" ref={orbA} aria-hidden="true" />
        <div className="hero__orb hero__orb--b" ref={orbB} aria-hidden="true" />
        <div className="container-px hero__inner">
          <div className="hero__copy">
            <span className="eyebrow" data-aos="fade-up">AI 광고 이미지 합성 · 오픈마켓 판매자용</span>
            <h1 className="hero__title" data-aos="fade-up" data-aos-delay="50">
              상품 이미지 + 모델 <br />
              <span className="text-gradient">= 광고 컷, 몇 초 만에</span>
            </h1>
            <p className="hero__desc" data-aos="fade-up" data-aos-delay="100">
              상품 사진 1장과 모델 사진 1장, 그리고 원하는 연출 한 줄. 모델 캐스팅 없이도 구매를 부르는 상세페이지 이미지를 만들어 드려요.
            </p>
            <div className="hero__cta" data-aos="fade-up" data-aos-delay="150">
              <Button variant="primary" size="lg" iconRight="arrowRight" onClick={() => navigate('/studio')}>
                서비스 시작하기
              </Button>
              <Button variant="gradient" size="lg" onClick={() => document.getElementById('how')?.scrollIntoView()}>
                사용법 보기
              </Button>
              {HERO_VIDEO && (
                <Button variant="ghost" size="lg" icon="external" onClick={() => setVideoOpen(true)}>
                  소개 영상
                </Button>
              )}
            </div>
            <p className="hero__note" data-aos="fade-up" data-aos-delay="200">
              <Icon name="check" size={16} /> 로그인 없이 스튜디오 둘러보기 <Icon name="check" size={16} /> 필터 6종 · PNG 다운로드
            </p>
          </div>
          <HeroVisual pairs={pairs} />
        </div>
      </section>

      {/* PROBLEM */}
      <section className="section section-light" aria-labelledby="problem-title">
        <div className="container-px">
          <div className="section-head" data-aos="fade-up">
            <span className="eyebrow eyebrow--dark">문제 정의</span>
            <h2 id="problem-title" className="section-title">좋은 상품도, 사진이 평범하면 묻혀요</h2>
            <p className="section-desc">쿠팡·올리브영·11번가 같은 오픈마켓에서는 썸네일과 상세 이미지가 첫인상의 전부입니다.</p>
          </div>
          <div className="problem-grid">
            {PROBLEMS.map((p, i) => (
              <article key={p.title} className="problem-card" data-aos="fade-up" data-aos-delay={i * 80}>
                <div className="problem-card__icon"><Icon name={p.icon} size={22} /></div>
                <h3>{p.title}</h3>
                <p>{p.desc}</p>
              </article>
            ))}
          </div>
        </div>
      </section>

      {/* HOW */}
      <section id="how" className="section section-dark" aria-labelledby="how-title">
        <div className="container-px">
          <div className="section-head" data-aos="fade-up">
            <span className="eyebrow">3단계 사용법</span>
            <h2 id="how-title" className="section-title">고르고, 적고, 받으면 끝</h2>
          </div>
          <ol className="steps">
            {STEPS.map((s, i) => (
              <li key={s.title} className="step" data-aos="fade-up" data-aos-delay={i * 100}>
                <span className="step__num">{String(i + 1).padStart(2, '0')}</span>
                <div className="step__icon"><Icon name={s.icon} size={24} /></div>
                <h3>{s.title}</h3>
                <p>{s.desc}</p>
              </li>
            ))}
          </ol>
        </div>
      </section>

      {/* PLATFORMS */}
      <section className="section section-light section--tight" aria-labelledby="platform-title">
        <div className="container-px text-center">
          <h2 id="platform-title" className="section-title section-title--sm" data-aos="fade-up">이런 마켓의 판매자를 위해 만들었어요</h2>
          <ul className="platforms" data-aos="fade-up" data-aos-delay="80">
            {PLATFORMS.map((p) => <li key={p} className="platform-badge">{p}</li>)}
          </ul>
        </div>
      </section>

      {/* STATS */}
      <section className="section section-dark section--tight" aria-label="Pixely 지표">
        <div className="container-px stats">
          <Stat end={5} suffix="초" label="평균 생성 시간 (mock 기준 2~4초)" />
          <Stat end={90} suffix="%" label="촬영 대비 비용 절감 목표" />
          <Stat end={6} suffix="종" label="분위기 필터" />
          <Stat end={1600} suffix="px" label="업로드 자동 리사이즈" />
        </div>
      </section>

      {/* BEFORE / AFTER */}
      <section className="section section-light" aria-labelledby="ba-title">
        <div className="container-px">
          <div className="section-head" data-aos="fade-up">
            <span className="eyebrow eyebrow--dark">비포 / 애프터</span>
            <h2 id="ba-title" className="section-title">같은 상품, 다른 첫인상</h2>
          </div>
          <div data-aos="fade-up" data-aos-delay="80">
            <BeforeAfterSlider pairs={pairs} error={error} />
          </div>
        </div>
      </section>

      {/* FAQ */}
      <section id="faq" className="section section-dark" aria-labelledby="faq-title">
        <div className="container-px faq-wrap">
          <div className="section-head section-head--left" data-aos="fade-up">
            <span className="eyebrow">FAQ</span>
            <h2 id="faq-title" className="section-title">자주 묻는 질문</h2>
          </div>
          <div className="faq-list" data-aos="fade-up" data-aos-delay="80">
            {FAQS.map((f, i) => <FaqItem key={f.q} item={f} index={i} />)}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="section section-dark section--tight">
        <div className="container-px">
          <div className="cta-banner" data-aos="zoom-in">
            <img className="cta-banner__robot" src={robot} alt="" aria-hidden="true" />
            <h2>지금 첫 광고 컷을 만들어 보세요</h2>
            <p>가입은 30초, 샘플 이미지로 바로 시작할 수 있어요.</p>
            <Button variant="primary" size="lg" iconRight="arrowRight" onClick={() => navigate('/studio')}>스튜디오 열기</Button>
          </div>
        </div>
      </section>

      {HERO_VIDEO && (
        <Modal open={videoOpen} onClose={() => setVideoOpen(false)} size="lg" title="Pixely 소개 영상">
          <video className="video-modal" src={HERO_VIDEO} controls autoPlay playsInline />
        </Modal>
      )}
    </div>
  );
}
