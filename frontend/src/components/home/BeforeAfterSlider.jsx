import { Swiper, SwiperSlide } from 'swiper/react';
import { A11y, Keyboard, Navigation, Pagination } from 'swiper/modules';
import 'swiper/css';
import 'swiper/css/navigation';
import 'swiper/css/pagination';
import { Skeleton } from '../common/Display';

/** 비포/애프터 예시 슬라이더 (tiny-slider → Swiper 로 통일, 실제로 사용) */
export default function BeforeAfterSlider({ pairs, error }) {
  if (error) {
    return (
      <div className="ba-fallback">
        <p>백엔드를 실행하면 공용 샘플로 만든 실제 비포/애프터 예시가 표시됩니다.</p>
      </div>
    );
  }
  if (!pairs) {
    return (
      <div className="ba-grid">
        <Skeleton height={360} radius="var(--radius-xl)" />
        <Skeleton height={360} radius="var(--radius-xl)" />
      </div>
    );
  }
  return (
    <Swiper
      className="ba-swiper"
      modules={[Navigation, Pagination, Keyboard, A11y]}
      navigation
      pagination={{ clickable: true }}
      keyboard={{ enabled: true }}
      spaceBetween={24}
      slidesPerView={1}
      a11y={{ prevSlideMessage: '이전 예시', nextSlideMessage: '다음 예시', paginationBulletMessage: '{{index}}번째 예시로 이동' }}
    >
      {pairs.map((pair) => (
        <SwiperSlide key={pair.product.id}>
          <div className="ba-grid">
            <figure className="ba-card ba-card--before">
              <span className="ba-tag">BEFORE · 상품 컷만</span>
              <div className="ba-card__before-img">
                <img src={pair.product.imageUrl} alt={`${pair.product.title} 상품 단독 이미지`} loading="lazy" />
              </div>
              <figcaption>{pair.product.title} — 흰 배경 상품 사진만으로는 사용감이 전달되지 않아요.</figcaption>
            </figure>
            <figure className="ba-card ba-card--after">
              <span className="ba-tag ba-tag--after">AFTER · Pixely 합성</span>
              <img src={pair.after} alt={`${pair.model.title}과 ${pair.product.title}을 합성한 광고 이미지 예시`} loading="lazy" />
              <figcaption>{pair.model.title} 와 함께 연출한 광고 컷</figcaption>
            </figure>
          </div>
        </SwiperSlide>
      ))}
    </Swiper>
  );
}
