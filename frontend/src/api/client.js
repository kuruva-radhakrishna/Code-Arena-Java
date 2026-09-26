import axios from 'axios';

// Backend now uses stateless JWT bearer tokens instead of session cookies
// (see docs/STAGES.md), so every request needs this header rather than
// `withCredentials: true`.
const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;
const TOKEN_STORAGE_KEY = 'codearena_token';

export function getToken() {
  try {
    return localStorage.getItem(TOKEN_STORAGE_KEY);
  } catch {
    return null;
  }
}

export function setToken(token) {
  try {
    if (token) {
      localStorage.setItem(TOKEN_STORAGE_KEY, token);
    } else {
      localStorage.removeItem(TOKEN_STORAGE_KEY);
    }
  } catch {
    // localStorage may be unavailable (private browsing); auth just won't persist across reloads.
  }
}

const api = axios.create({ baseURL: BACKEND_URL });

api.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
