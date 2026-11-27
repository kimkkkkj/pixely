import { useId, useState } from 'react';
import { authApi } from '../../api';
import { useAuthStore } from '../../store/authStore';
import { toast } from '../../store/uiStore';
import Button from '../common/Button';

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

/** 개발 모드 전용 테스트 계정 (루트 .env 의 VITE_DEV_*). 빌드(production)에서는 빈 배열 → 버튼 미표시 */
const DEV_ACCOUNTS = import.meta.env.DEV
  ? [
      { label: '테스트 셀러', email: import.meta.env.VITE_DEV_SELLER_EMAIL, password: import.meta.env.VITE_DEV_SELLER_PASSWORD },
      { label: '관리자', email: import.meta.env.VITE_DEV_ADMIN_EMAIL, password: import.meta.env.VITE_DEV_ADMIN_PASSWORD },
    ].filter((a) => a.email && a.password)
  : [];

function validate(mode, form) {
  const errors = {};
  if (!form.email.trim()) errors.email = '이메일을 입력해 주세요.';
  else if (!EMAIL_RE.test(form.email.trim())) errors.email = '이메일 형식이 올바르지 않습니다.';
  if (!form.password) errors.password = '비밀번호를 입력해 주세요.';
  else if (mode === 'signup' && form.password.length < 8) errors.password = '비밀번호는 8자 이상 입력해 주세요.';
  if (mode === 'signup') {
    const n = form.nickname.trim();
    if (n.length < 2 || n.length > 20) errors.nickname = '닉네임은 2자 이상 20자 이하로 입력해 주세요.';
  }
  return errors;
}

/** 로그인/회원가입 공용 폼 — 페이지와 로그인 모달에서 함께 사용 */
export default function AuthForm({ mode = 'login', onSuccess }) {
  const id = useId();
  const setSession = useAuthStore((s) => s.setSession);
  const [form, setForm] = useState({ email: '', password: '', nickname: '' });
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const update = (key) => (e) => {
    setForm((f) => ({ ...f, [key]: e.target.value }));
    setErrors((er) => ({ ...er, [key]: undefined }));
  };

  const submit = async (e) => {
    e.preventDefault();
    const v = validate(mode, form);
    setErrors(v);
    setServerError(null);
    if (Object.keys(v).length) return;
    setSubmitting(true);
    try {
      const body = { email: form.email.trim(), password: form.password };
      const session = mode === 'signup' ? await authApi.signup({ ...body, nickname: form.nickname.trim() }) : await authApi.login(body);
      setSession(session);
      toast.success(mode === 'signup' ? `${session.user.nickname}님, 환영합니다!` : '로그인되었습니다.');
      onSuccess?.(session);
    } catch (err) {
      const fieldErrors = Object.fromEntries((err.errors || []).map((fe) => [fe.field, fe.message]));
      setErrors(fieldErrors);
      setServerError(Object.keys(fieldErrors).length ? null : err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const fillDevAccount = (account) => {
    setForm((f) => ({ ...f, email: account.email, password: account.password }));
    setErrors({});
    setServerError(null);
  };

  const field = (key, label, type, extra = {}) => (
    <div className="px-field">
      <label className="px-label" htmlFor={`${id}-${key}`}>{label}</label>
      <input
        id={`${id}-${key}`}
        className="px-input"
        type={type}
        value={form[key]}
        onChange={update(key)}
        aria-invalid={!!errors[key]}
        aria-describedby={errors[key] ? `${id}-${key}-err` : undefined}
        {...extra}
      />
      {errors[key] && <span id={`${id}-${key}-err`} className="px-field-error">{errors[key]}</span>}
    </div>
  );

  return (
    <form onSubmit={submit} noValidate className="d-grid gap-3">
      {mode === 'login' && DEV_ACCOUNTS.length > 0 && (
        <div className="d-flex flex-wrap align-items-center gap-2" aria-label="개발용 빠른 입력">
          <span style={{ fontSize: 'var(--fs-xs)', color: 'var(--color-text-muted)' }}>DEV 빠른 입력</span>
          {DEV_ACCOUNTS.map((a) => (
            <button key={a.label} type="button" className="px-chip" onClick={() => fillDevAccount(a)}>
              {a.label}
            </button>
          ))}
        </div>
      )}
      {field('email', '이메일', 'email', { autoComplete: 'email', placeholder: 'seller@example.com', 'data-autofocus': true })}
      {mode === 'signup' && field('nickname', '닉네임', 'text', { autoComplete: 'nickname', placeholder: '스토어 이름 또는 닉네임' })}
      {field('password', '비밀번호', 'password', {
        autoComplete: mode === 'signup' ? 'new-password' : 'current-password',
        placeholder: mode === 'signup' ? '8자 이상' : '',
      })}
      {serverError && <p className="px-field-error" role="alert">{serverError}</p>}
      <Button type="submit" variant="primary" size="lg" block loading={submitting}>
        {mode === 'signup' ? '가입하고 시작하기' : '로그인'}
      </Button>
    </form>
  );
}
