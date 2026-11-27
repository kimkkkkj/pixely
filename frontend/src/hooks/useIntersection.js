import { useEffect } from 'react';

/** sentinel 이 root 안에서 보이면 callback 호출 (무한 스크롤) */
export function useIntersection(targetRef, callback, { rootRef, rootMargin = '200px', enabled = true } = {}) {
  useEffect(() => {
    const target = targetRef.current;
    if (!target || !enabled) return undefined;
    const observer = new IntersectionObserver(
      (entries) => entries.forEach((e) => e.isIntersecting && callback()),
      { root: rootRef?.current ?? null, rootMargin },
    );
    observer.observe(target);
    return () => observer.disconnect();
  }, [targetRef, rootRef, callback, rootMargin, enabled]);
}
