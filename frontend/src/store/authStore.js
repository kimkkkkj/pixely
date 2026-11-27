import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export const useAuthStore = create(
  persist(
    (set) => ({
      token: null,
      user: null,
      setSession: ({ accessToken, user }) => set({ token: accessToken, user }),
      setUser: (user) => set({ user }),
      logout: () => set({ token: null, user: null }),
    }),
    { name: 'pixely-auth' },
  ),
);

export const selectIsAdmin = (s) => s.user?.role === 'ADMIN';
