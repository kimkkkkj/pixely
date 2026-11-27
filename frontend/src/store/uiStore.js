import { create } from 'zustand';

let toastSeq = 0;

/** 토스트 · 확인 모달 · 로그인 모달 (alert/confirm 대체) */
export const useUiStore = create((set, get) => ({
  toasts: [],
  confirmState: null,
  loginModal: { open: false, reason: null },

  toast: (message, type = 'info', duration = 3200) => {
    const id = ++toastSeq;
    set((s) => ({ toasts: [...s.toasts.slice(-3), { id, message, type }] }));
    if (duration > 0) setTimeout(() => get().dismissToast(id), duration);
    return id;
  },
  dismissToast: (id) => set((s) => ({ toasts: s.toasts.filter((t) => t.id !== id) })),

  confirm: (options) =>
    new Promise((resolve) => {
      set({ confirmState: { ...options, resolve } });
    }),
  resolveConfirm: (value) => {
    const state = get().confirmState;
    if (state) state.resolve(value);
    set({ confirmState: null });
  },

  openLogin: (reason = null) => set({ loginModal: { open: true, reason } }),
  closeLogin: () => set({ loginModal: { open: false, reason: null } }),
}));

export const toast = {
  success: (m) => useUiStore.getState().toast(m, 'success'),
  error: (m) => useUiStore.getState().toast(m, 'error', 4500),
  info: (m) => useUiStore.getState().toast(m, 'info'),
};

export const confirmDialog = (options) => useUiStore.getState().confirm(options);
