import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { generationApi } from '../api';
import { filterLabel } from '../lib/filters';
import { formatDateTime, relativeTime, STATUS_LABEL } from '../lib/format';
import Button from '../components/common/Button';
import { EmptyState, ErrorState, Skeleton, StatusBadge } from '../components/common/Display';
import Modal from '../components/common/Modal';
import '../styles/pages.css';

const PAGE_SIZE = 12;

function DetailModal({ item, onClose }) {
  const navigate = useNavigate();
  const [active, setActive] = useState(null);
  useEffect(() => setActive(item?.resultUrl ?? null), [item]);
  if (!item) return null;
  const variants = item.variants || [];
  return (
    <Modal open={!!item} onClose={onClose} size="lg" title={`생성 #${item.id}`} description={formatDateTime(item.createdAt)}>
      <div className="history-detail">
        <div className="history-detail__img">
          {item.status === 'DONE' && active ? (
            <img src={active} alt={`생성 #${item.id} 결과 이미지`} />
          ) : (
            <EmptyState icon={item.status === 'FAILED' ? 'alert' : 'sparkles'} title={STATUS_LABEL[item.status]} description={item.errorMessage} />
          )}
        </div>
        <div className="history-detail__info">
          <StatusBadge status={item.status}>{STATUS_LABEL[item.status]}</StatusBadge>
          <dl>
            <dt>상품</dt>
            <dd>{item.productImage?.title ?? '삭제된 이미지'}</dd>
            <dt>모델</dt>
            <dd>{item.modelImage?.title ?? '삭제된 이미지'}</dd>
            <dt>요청사항</dt>
            <dd>{item.prompt || '—'}</dd>
            <dt>엔진</dt>
            <dd>{item.provider || '—'}</dd>
          </dl>
          {variants.length > 0 && (
            <div>
              <p className="px-label mb-2">저장된 필터 버전</p>
              <div className="variant-row">
                {variants.map((v) => (
                  <button
                    key={v.id}
                    type="button"
                    className={`variant-thumb ${active === v.imageUrl ? 'is-active' : ''}`}
                    onClick={() => setActive(v.imageUrl)}
                    aria-label={`${filterLabel(v.filterType)} 버전 보기`}
                  >
                    <img src={v.imageUrl} alt="" />
                    <span>{filterLabel(v.filterType)}</span>
                  </button>
                ))}
              </div>
            </div>
          )}
          <Button
            variant="primary"
            icon="edit"
            block
            disabled={!item.productImage || !item.modelImage}
            onClick={() => navigate(`/studio?generation=${item.id}`)}
          >
            스튜디오에서 다시 편집
          </Button>
        </div>
      </div>
    </Modal>
  );
}

export default function HistoryPage() {
  const navigate = useNavigate();
  const [items, setItems] = useState([]);
  const [page, setPage] = useState(0);
  const [hasNext, setHasNext] = useState(false);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selected, setSelected] = useState(null);

  const load = useCallback(async (p) => {
    setLoading(true);
    setError(null);
    try {
      const data = await generationApi.list({ page: p, size: PAGE_SIZE });
      setItems((prev) => (p === 0 ? data.content : [...prev, ...data.content]));
      setHasNext(data.hasNext);
      setTotal(data.totalElements);
      setPage(p);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load(0);
  }, [load]);

  return (
    <div className="page container-px">
      <div className="page-header">
        <div>
          <h1 className="page-title">내 생성 기록</h1>
          <p className="page-subtitle">지금까지 만든 광고 이미지 {total.toLocaleString('ko-KR')}개</p>
        </div>
        <Button variant="primary" icon="sparkles" onClick={() => navigate('/studio')}>새로 만들기</Button>
      </div>

      {error && items.length === 0 ? (
        <ErrorState description={error} onRetry={() => load(0)} />
      ) : !loading && items.length === 0 ? (
        <EmptyState
          icon="history"
          title="아직 생성한 이미지가 없어요"
          description="스튜디오에서 첫 광고 이미지를 만들어 보세요."
          action={<Button variant="primary" onClick={() => navigate('/studio')}>스튜디오로 가기</Button>}
        />
      ) : (
        <>
          <div className="history-grid">
            {items.map((g) => (
              <button key={g.id} type="button" className="history-card" onClick={() => setSelected(g)} aria-label={`생성 #${g.id} 상세 보기`}>
                <span className="history-card__img">
                  {g.status === 'DONE' && g.resultUrl ? (
                    <img src={g.resultUrl} alt="" loading="lazy" />
                  ) : (
                    <span className="history-card__placeholder">{STATUS_LABEL[g.status]}</span>
                  )}
                  <span className="history-card__badge"><StatusBadge status={g.status}>{STATUS_LABEL[g.status]}</StatusBadge></span>
                </span>
                <span className="history-card__body">
                  <span className="history-card__prompt">{g.prompt || '요청사항 없음'}</span>
                  <span className="history-card__meta">
                    {g.productImage?.title ?? '삭제된 상품'} · {relativeTime(g.createdAt)}
                  </span>
                </span>
              </button>
            ))}
            {loading &&
              Array.from({ length: items.length ? 4 : 8 }, (_, i) => (
                <div key={`sk-${i}`} className="history-card history-card--skeleton">
                  <Skeleton className="history-card__img" height="auto" radius="0" />
                  <div className="history-card__body"><Skeleton height={14} width="80%" /><Skeleton height={12} width="50%" /></div>
                </div>
              ))}
          </div>
          {hasNext && !loading && (
            <div className="text-center mt-8">
              <Button variant="outline" onClick={() => load(page + 1)}>더 보기</Button>
            </div>
          )}
        </>
      )}
      <DetailModal item={selected} onClose={() => setSelected(null)} />
    </div>
  );
}
