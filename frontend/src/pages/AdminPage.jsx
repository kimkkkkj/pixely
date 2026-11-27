import { useCallback, useEffect, useState } from 'react';
import { adminApi } from '../api';
import { useDebounce } from '../hooks/useDebounce';
import { formatDateTime, STATUS_LABEL } from '../lib/format';
import { confirmDialog, toast } from '../store/uiStore';
import Button from '../components/common/Button';
import { EmptyState, ErrorState, Skeleton, StatusBadge, Tabs } from '../components/common/Display';
import Icon from '../components/common/Icon';
import UploadModal from '../components/studio/UploadModal';
import '../styles/pages.css';
import '../styles/studio.css';

const SAMPLE_TYPES = [
  { value: 'PRODUCT', label: '상품 샘플', icon: 'box' },
  { value: 'MODEL', label: '모델 샘플', icon: 'user' },
];
const STATUS_FILTERS = ['', 'DONE', 'FAILED', 'PROCESSING', 'PENDING'];

function SamplesSection() {
  const [type, setType] = useState('PRODUCT');
  const [query, setQuery] = useState('');
  const q = useDebounce(query.trim(), 300);
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);
  const [uploadOpen, setUploadOpen] = useState(false);

  const load = useCallback(async () => {
    setError(null);
    setData(null);
    try {
      setData(await adminApi.library({ type, q, size: 60 }));
    } catch (e) {
      setError(e.message);
    }
  }, [type, q]);

  useEffect(() => {
    load();
  }, [load]);

  const remove = async (item) => {
    const ok = await confirmDialog({ title: '공용 샘플을 삭제할까요?', message: `"${item.title}" 은(는) 모든 사용자의 라이브러리에서 사라집니다.`, confirmText: '삭제', danger: true });
    if (!ok) return;
    try {
      await adminApi.removeSample(item.id);
      toast.success('샘플을 삭제했어요.');
      load();
    } catch (e) {
      toast.error(e.message);
    }
  };

  return (
    <section className="admin-section" aria-labelledby="samples-title">
      <div className="admin-section__head">
        <h2 id="samples-title" className="admin-section__title">공용 샘플 관리</h2>
        <Button variant="primary" size="sm" icon="upload" onClick={() => setUploadOpen(true)}>샘플 업로드</Button>
      </div>
      <div className="admin-toolbar">
        <div style={{ width: 280 }}><Tabs tabs={SAMPLE_TYPES} value={type} onChange={setType} label="샘플 종류" idPrefix="admin-tab" /></div>
        <div className="px-search" style={{ flex: 1, maxWidth: 320 }}>
          <Icon name="search" size={16} />
          <input className="px-input" type="search" value={query} onChange={(e) => setQuery(e.target.value)} placeholder="제목·태그 검색" aria-label="샘플 검색" />
        </div>
        <span className="text-muted-token" style={{ fontSize: 'var(--fs-sm)' }}>{data ? `${data.totalElements}개` : ''}</span>
      </div>
      {error ? (
        <ErrorState description={error} onRetry={load} />
      ) : !data ? (
        <div className="admin-samples">{Array.from({ length: 8 }, (_, i) => <Skeleton key={i} className="admin-sample__sk" height="auto" />)}</div>
      ) : data.content.length === 0 ? (
        <EmptyState title="샘플이 없어요" description="저작권·초상권 문제가 없는 이미지를 업로드해 주세요." />
      ) : (
        <div className="admin-samples">
          {data.content.map((item) => (
            <figure key={item.id} className="admin-sample">
              <div className={`admin-sample__img ${item.type === 'PRODUCT' ? 'admin-sample__img--product' : ''}`}>
                <img src={item.imageUrl} alt={item.title} loading="lazy" />
              </div>
              <figcaption>
                <span className="admin-sample__title">{item.title}</span>
                <Button size="sm" variant="danger" iconOnly icon="trash" aria-label={`${item.title} 삭제`} onClick={() => remove(item)} />
              </figcaption>
            </figure>
          ))}
        </div>
      )}
      <UploadModal
        open={uploadOpen}
        onClose={() => setUploadOpen(false)}
        defaultType={type}
        title="공용 샘플 업로드"
        onUpload={async (payload) => {
          await adminApi.uploadSample(payload);
          toast.success('공용 샘플을 추가했어요.');
          if (payload.type !== type) setType(payload.type);
          else load();
        }}
      />
    </section>
  );
}

function GenerationsSection() {
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);

  const load = useCallback(async () => {
    setError(null);
    try {
      setData(await adminApi.generations({ status, page, size: 20 }));
    } catch (e) {
      setError(e.message);
    }
  }, [status, page]);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <section className="admin-section" aria-labelledby="gens-title">
      <div className="admin-section__head">
        <h2 id="gens-title" className="admin-section__title">전체 생성 내역</h2>
        <div className="d-flex gap-2 align-items-center">
          <label className="sr-only" htmlFor="status-filter">상태 필터</label>
          <select
            id="status-filter"
            className="px-select"
            style={{ width: 140, height: 36, padding: '0 var(--space-3)' }}
            value={status}
            onChange={(e) => {
              setPage(0);
              setStatus(e.target.value);
            }}
          >
            {STATUS_FILTERS.map((s) => <option key={s} value={s}>{s ? STATUS_LABEL[s] : '전체 상태'}</option>)}
          </select>
          <Button size="sm" variant="ghost" iconOnly icon="refresh" aria-label="새로고침" onClick={load} />
        </div>
      </div>
      {error ? (
        <ErrorState description={error} onRetry={load} />
      ) : (
        <div className="admin-table-wrap thin-scroll">
          <table className="admin-table">
            <thead>
              <tr>
                <th scope="col">ID</th>
                <th scope="col">결과</th>
                <th scope="col">사용자</th>
                <th scope="col">상품 / 모델</th>
                <th scope="col">요청사항</th>
                <th scope="col">상태</th>
                <th scope="col">엔진</th>
                <th scope="col">생성 시각</th>
              </tr>
            </thead>
            <tbody>
              {!data
                ? Array.from({ length: 5 }, (_, i) => (
                    <tr key={i}><td colSpan={8}><Skeleton height={20} /></td></tr>
                  ))
                : data.content.length === 0
                  ? (
                    <tr><td colSpan={8}><EmptyState icon="history" title="생성 내역이 없어요" /></td></tr>
                  )
                  : data.content.map((g) => (
                    <tr key={g.id}>
                      <td>#{g.id}</td>
                      <td>
                        {g.resultUrl ? (
                          <a href={g.resultUrl} target="_blank" rel="noreferrer" aria-label={`생성 #${g.id} 결과 열기`}>
                            <img className="admin-table__thumb" src={g.resultUrl} alt="" loading="lazy" />
                          </a>
                        ) : '—'}
                      </td>
                      <td>{g.userEmail}</td>
                      <td>{g.productImage?.title ?? '삭제됨'}<br /><span className="text-muted-token">{g.modelImage?.title ?? '삭제됨'}</span></td>
                      <td className="admin-table__prompt" title={g.prompt || ''}>{g.prompt || '—'}</td>
                      <td>
                        <StatusBadge status={g.status}>{STATUS_LABEL[g.status]}</StatusBadge>
                        {g.errorMessage && <div className="admin-table__err">{g.errorMessage}</div>}
                      </td>
                      <td>{g.provider || '—'}</td>
                      <td>{formatDateTime(g.createdAt)}</td>
                    </tr>
                  ))}
            </tbody>
          </table>
        </div>
      )}
      {data && data.totalPages > 1 && (
        <div className="admin-pager">
          <Button size="sm" variant="outline" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>이전</Button>
          <span>{page + 1} / {data.totalPages}</span>
          <Button size="sm" variant="outline" disabled={!data.hasNext} onClick={() => setPage((p) => p + 1)}>다음</Button>
        </div>
      )}
    </section>
  );
}

export default function AdminPage() {
  return (
    <div className="page container-px">
      <div className="page-header">
        <div>
          <h1 className="page-title">관리자</h1>
          <p className="page-subtitle">공용 샘플(가상 모델 · 상품)과 전체 생성 내역을 관리합니다.</p>
        </div>
        <span className="px-badge px-badge--admin"><Icon name="shield" size={14} /> ADMIN</span>
      </div>
      <div className="d-grid gap-5">
        <SamplesSection />
        <GenerationsSection />
      </div>
    </div>
  );
}
