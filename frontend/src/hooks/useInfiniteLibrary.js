import { useCallback, useEffect, useRef, useState } from 'react';
import { libraryApi } from '../api';

const PAGE_SIZE = 20;

/** 라이브러리 이미지 무한 스크롤 (type/q/로그인 상태가 바뀌면 처음부터 다시 로드) */
export function useInfiniteLibrary({ type, q, authKey }) {
  const [state, setState] = useState({ items: [], hasNext: true, total: null, error: null });
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [reloadKey, setReloadKey] = useState(0);
  const loadingRef = useRef(false);
  const queryKey = `${type}|${q}|${authKey}|${reloadKey}`;
  const lastKey = useRef(queryKey);

  useEffect(() => {
    const isNewQuery = lastKey.current !== queryKey;
    if (isNewQuery) {
      lastKey.current = queryKey;
      if (page !== 0) {
        // 페이지를 0 으로 돌리면 이 effect 가 다시 실행된다
        setPage(0);
        return undefined;
      }
    }
    const controller = new AbortController();
    loadingRef.current = true;
    setLoading(true);
    libraryApi
      .list({ type, q, page, size: PAGE_SIZE }, controller.signal)
      .then((data) => {
        setState((prev) => {
          const base = page === 0 ? [] : prev.items;
          const seen = new Set(base.map((i) => i.id));
          return {
            items: [...base, ...data.content.filter((i) => !seen.has(i.id))],
            hasNext: data.hasNext,
            total: data.totalElements,
            error: null,
          };
        });
      })
      .catch((e) => {
        if (controller.signal.aborted) return;
        setState((prev) => ({ ...prev, items: page === 0 ? [] : prev.items, error: e.message, hasNext: false }));
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          loadingRef.current = false;
          setLoading(false);
        }
      });
    return () => {
      controller.abort();
      loadingRef.current = false;
    };
  }, [queryKey, page, type, q]);

  const loadMore = useCallback(() => {
    if (!loadingRef.current && state.hasNext && !state.error) setPage((p) => p + 1);
  }, [state.hasNext, state.error]);

  const reload = useCallback(() => setReloadKey((k) => k + 1), []);
  const prepend = useCallback((item) => {
    setState((prev) => ({
      ...prev,
      items: [item, ...prev.items.filter((i) => i.id !== item.id)],
      total: prev.total == null ? prev.total : prev.total + 1,
    }));
  }, []);
  const remove = useCallback((id) => {
    setState((prev) => ({
      ...prev,
      items: prev.items.filter((i) => i.id !== id),
      total: prev.total == null ? prev.total : prev.total - 1,
    }));
  }, []);

  return { ...state, loading, loadMore, reload, prepend, remove };
}
