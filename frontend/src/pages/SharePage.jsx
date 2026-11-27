import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { shareApi } from '../api';
import { canvasToBlob, downloadBlob, filterLabel, loadImage, renderFiltered } from '../lib/filters';
import { formatDateTime } from '../lib/format';
import Button from '../components/common/Button';
import { EmptyState, ErrorState, Skeleton } from '../components/common/Display';
import { toast } from '../store/uiStore';
import '../styles/pages.css';

/** 비로그인 공개 결과 페이지 */
export default function SharePage() {
  const { token } = useParams();
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);
  const [active, setActive] = useState(null);

  useEffect(() => {
    let cancelled = false;
    setData(null);
    setError(null);
    shareApi
      .get(token)
      .then((d) => {
        if (cancelled) return;
        setData(d);
        const last = d.variants?.[d.variants.length - 1];
        setActive(last ?? { filterType: 'ORIGINAL', imageUrl: d.resultUrl });
      })
      .catch((e) => !cancelled && setError(e));
    return () => {
      cancelled = true;
    };
  }, [token]);

  const download = async () => {
    try {
      const img = await loadImage(active.imageUrl);
      const blob = await canvasToBlob(renderFiltered(img, 'ORIGINAL'));
      downloadBlob(blob, `pixely-share-${active.filterType.toLowerCase()}.png`);
    } catch (e) {
      toast.error(e.message);
    }
  };

  if (error) {
    return (
      <div className="page container-px">
        {error.status === 404 || error.status === 410 ? (
          <EmptyState
            icon="lock"
            title={error.status === 410 ? '만료된 공유 링크예요' : '공유 링크를 찾을 수 없어요'}
            description={error.message}
            action={<Button variant="primary" onClick={() => navigate('/')}>Pixely 둘러보기</Button>}
          />
        ) : (
          <ErrorState description={error.message} onRetry={() => window.location.reload()} />
        )}
      </div>
    );
  }

  return (
    <div className="page container-px share-page">
      <div className="share-layout">
        <div className="share-image">
          {data && active ? <img src={active.imageUrl} alt="공유된 광고 이미지" /> : <Skeleton height="100%" radius="inherit" />}
        </div>
        <aside className="share-info">
          <span className="eyebrow-inline">공유된 광고 이미지</span>
          {data ? (
            <>
              <h1 className="page-title">{data.creatorNickname}님이 만든 광고 컷</h1>
              <p className="page-subtitle">{formatDateTime(data.createdAt)} · {formatDateTime(data.expiresAt)}까지 공개</p>
              {data.prompt && <blockquote className="share-prompt">“{data.prompt}”</blockquote>}
              {data.variants?.length > 1 && (
                <div>
                  <p className="px-label mb-2">필터 버전</p>
                  <div className="variant-row">
                    {data.variants.map((v) => (
                      <button
                        key={v.id}
                        type="button"
                        className={`variant-thumb ${active?.id === v.id ? 'is-active' : ''}`}
                        onClick={() => setActive(v)}
                        aria-label={`${filterLabel(v.filterType)} 버전 보기`}
                      >
                        <img src={v.imageUrl} alt="" />
                        <span>{filterLabel(v.filterType)}</span>
                      </button>
                    ))}
                  </div>
                </div>
              )}
              <div className="d-grid gap-2">
                <Button variant="primary" icon="download" onClick={download}>이미지 다운로드</Button>
                <Button variant="gradient" icon="sparkles" onClick={() => navigate('/studio')}>나도 만들어 보기</Button>
              </div>
            </>
          ) : (
            <div className="d-grid gap-3">
              <Skeleton height={32} width="80%" />
              <Skeleton height={16} width="60%" />
              <Skeleton height={64} />
            </div>
          )}
        </aside>
      </div>
    </div>
  );
}
