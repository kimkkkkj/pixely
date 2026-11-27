import logoWhite from '../../assets/brand/logo-white.png';
import logoDark from '../../assets/brand/logo-dark.png';
import Button from './Button';
import Icon from './Icon';

export function Card({ as: Tag = 'div', pad = true, className = '', children, ...rest }) {
  return (
    <Tag className={`px-card ${pad ? 'px-card--pad' : ''} ${className}`} {...rest}>
      {children}
    </Tag>
  );
}

/** 접근성 탭 (role=tablist) — openTab 의 전역 event 의존 버그 대체 */
export function Tabs({ tabs, value, onChange, label, idPrefix = 'tab' }) {
  const onKeyDown = (e) => {
    const idx = tabs.findIndex((t) => t.value === value);
    if (e.key === 'ArrowRight' || e.key === 'ArrowLeft') {
      e.preventDefault();
      const next = (idx + (e.key === 'ArrowRight' ? 1 : -1) + tabs.length) % tabs.length;
      onChange(tabs[next].value);
      document.getElementById(`${idPrefix}-${tabs[next].value}`)?.focus();
    }
  };
  return (
    <div className="px-tabs" role="tablist" aria-label={label} onKeyDown={onKeyDown}>
      {tabs.map((t) => (
        <button
          key={t.value}
          id={`${idPrefix}-${t.value}`}
          type="button"
          role="tab"
          className="px-tab"
          aria-selected={t.value === value}
          aria-controls={`${idPrefix}-panel`}
          tabIndex={t.value === value ? 0 : -1}
          onClick={() => onChange(t.value)}
        >
          {t.icon && <Icon name={t.icon} size={16} />}
          {t.label}
          {t.count != null && <span className="px-tab__count">{t.count}</span>}
        </button>
      ))}
    </div>
  );
}

export function Skeleton({ width = '100%', height = 16, radius, className = '', style }) {
  return (
    <div
      className={`px-skeleton ${className}`}
      style={{ width, height, borderRadius: radius, ...style }}
      aria-hidden="true"
    />
  );
}

export function EmptyState({ icon = 'image', title, description, action }) {
  return (
    <div className="px-state">
      <div className="px-state__icon"><Icon name={icon} size={24} /></div>
      <p className="px-state__title">{title}</p>
      {description && <p className="px-state__desc">{description}</p>}
      {action}
    </div>
  );
}

export function ErrorState({ title = '불러오지 못했어요', description, onRetry }) {
  return (
    <div className="px-state px-state--error" role="alert">
      <div className="px-state__icon"><Icon name="alert" size={24} /></div>
      <p className="px-state__title">{title}</p>
      {description && <p className="px-state__desc">{description}</p>}
      {onRetry && (
        <Button size="sm" variant="outline" icon="refresh" onClick={onRetry}>
          다시 시도
        </Button>
      )}
    </div>
  );
}

export function StatusBadge({ status, children }) {
  return <span className={`px-badge px-badge--${String(status).toLowerCase()}`}>{children}</span>;
}

/** 5색 웨이브 볼 + 그라데이션 "이미지 생성중..." */
export function WaveLoader({ text = '이미지 생성중...', sub }) {
  return (
    <div className="px-wave" role="status" aria-live="polite">
      <div className="px-wave__balls" aria-hidden="true">
        {Array.from({ length: 5 }, (_, i) => <span key={i} className="px-wave__ball" />)}
      </div>
      <p className="px-wave__text">{text}</p>
      {sub && <p className="px-wave__sub">{sub}</p>}
    </div>
  );
}

/** 원본 시안 로고 이미지 (어두운 배경: white, 밝은 배경: dark) */
export function Logo({ className = '', variant = 'white', height = 26 }) {
  return (
    <span className={`px-logo ${className}`}>
      <img src={variant === 'dark' ? logoDark : logoWhite} alt="Pixely" height={height} style={{ height, width: 'auto' }} />
    </span>
  );
}
