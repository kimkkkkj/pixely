import axios from 'axios';
import { useAuthStore } from '../store/authStore';

/** 모든 요청은 Vite 프록시(/api, /files)를 통해 백엔드로 전달된다 */
export const http = axios.create({ baseURL: '/', timeout: 30000 });

http.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

http.interceptors.response.use(
  (res) => res,
  (error) => {
    const status = error.response?.status;
    const body = error.response?.data;
    if (status === 401 && useAuthStore.getState().token) {
      useAuthStore.getState().logout();
    }
    const message =
      body?.message ||
      (error.code === 'ECONNABORTED'
        ? '요청 시간이 초과되었습니다.'
        : !error.response
          ? '서버에 연결할 수 없습니다. 백엔드가 실행 중인지 확인해 주세요.'
          : '알 수 없는 오류가 발생했습니다.');
    return Promise.reject(new ApiError(message, status, body?.code, body?.errors));
  },
);

export class ApiError extends Error {
  constructor(message, status, code, errors) {
    super(message);
    this.status = status;
    this.code = code;
    this.errors = errors || [];
  }
}

/** { success, data } 래퍼를 벗겨서 data 만 반환 */
export const unwrap = (promise) => promise.then((res) => res.data?.data);
