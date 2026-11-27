import { http, unwrap } from './client';

export const authApi = {
  signup: (body) => unwrap(http.post('/api/auth/signup', body)),
  login: (body) => unwrap(http.post('/api/auth/login', body)),
  me: () => unwrap(http.get('/api/auth/me')),
};

export const libraryApi = {
  list: ({ type, q, page = 0, size = 20 }, signal) =>
    unwrap(http.get('/api/library', { params: { type, q: q || undefined, page, size }, signal })),
  upload: ({ file, type, title, tags }) => {
    const form = new FormData();
    form.append('file', file);
    return unwrap(http.post('/api/library', form, { params: { type, title: title || undefined, tags: tags || undefined } }));
  },
  remove: (id) => unwrap(http.delete(`/api/library/${id}`)),
};

export const generationApi = {
  create: (body) => unwrap(http.post('/api/generations', body)),
  get: (id) => unwrap(http.get(`/api/generations/${id}`)),
  list: ({ page = 0, size = 12 } = {}) => unwrap(http.get('/api/generations', { params: { page, size } })),
  saveVariant: (id, body) => unwrap(http.post(`/api/generations/${id}/variants`, body, { timeout: 60000 })),
  share: (id) => unwrap(http.post(`/api/generations/${id}/share`)),
};

export const shareApi = {
  get: (token) => unwrap(http.get(`/api/share/${token}`)),
};

export const adminApi = {
  library: ({ type, q, page = 0, size = 30 } = {}) =>
    unwrap(http.get('/api/admin/library', { params: { type: type || undefined, q: q || undefined, page, size } })),
  uploadSample: ({ file, type, title, tags }) => {
    const form = new FormData();
    form.append('file', file);
    return unwrap(http.post('/api/admin/library', form, { params: { type, title: title || undefined, tags: tags || undefined } }));
  },
  removeSample: (id) => unwrap(http.delete(`/api/admin/library/${id}`)),
  generations: ({ status, page = 0, size = 20 } = {}) =>
    unwrap(http.get('/api/admin/generations', { params: { status: status || undefined, page, size } })),
};
