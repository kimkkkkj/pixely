import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

// 루트(..) 의 .env 를 함께 사용: FRONTEND_PORT, SERVER_PORT
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, '..', '');
  const backendPort = env.SERVER_PORT || '8081';
  const backend = `http://localhost:${backendPort}`;
  return {
    envDir: '..',
    plugins: [react()],
    server: {
      port: Number(env.FRONTEND_PORT || 5174),
      strictPort: true,
      proxy: {
        '/api': { target: backend, changeOrigin: true },
        '/files': { target: backend, changeOrigin: true },
      },
    },
    preview: {
      port: Number(env.FRONTEND_PORT || 5174),
      proxy: {
        '/api': { target: backend, changeOrigin: true },
        '/files': { target: backend, changeOrigin: true },
      },
    },
    build: {
      chunkSizeWarningLimit: 900,
      rollupOptions: {
        output: {
          manualChunks: {
            react: ['react', 'react-dom', 'react-router-dom'],
            vendor: ['axios', 'zustand', 'aos', 'swiper'],
          },
        },
      },
    },
  };
});
