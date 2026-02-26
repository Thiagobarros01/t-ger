import { computed, reactive } from "vue";

const STORAGE_KEY = "tger.auth";

const state = reactive({
  token: "",
  user: null,
  loading: false,
  error: ""
});

function loadStoredSession() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return;
    const parsed = JSON.parse(raw);
    state.token = parsed.token ?? "";
    state.user = parsed.user ?? null;
  } catch {
    localStorage.removeItem(STORAGE_KEY);
  }
}

function persistSession() {
  if (!state.token || !state.user) {
    localStorage.removeItem(STORAGE_KEY);
    return;
  }
  localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify({
      token: state.token,
      user: state.user
    })
  );
}

async function apiRequest(path, options = {}) {
  const response = await fetch(path, {
    headers: {
      "Content-Type": "application/json",
      ...(state.token ? { Authorization: `Bearer ${state.token}` } : {}),
      ...(options.headers ?? {})
    },
    ...options
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || "Falha na requisicao");
  }

  return response.json();
}

loadStoredSession();

export function useAuth() {
  const isAuthenticated = computed(() => Boolean(state.token && state.user));

  async function login(email, password) {
    state.loading = true;
    state.error = "";
    try {
      const payload = await apiRequest("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({ email, password })
      });
      state.token = payload.token;
      state.user = payload.user;
      persistSession();
      return payload.user;
    } catch (error) {
      state.error = "Login invalido. Verifique email e senha.";
      throw error;
    } finally {
      state.loading = false;
    }
  }

  async function refreshMe() {
    if (!state.token) return null;
    try {
      const user = await apiRequest("/api/auth/me");
      state.user = user;
      persistSession();
      return user;
    } catch {
      logout();
      return null;
    }
  }

  function logout() {
    state.token = "";
    state.user = null;
    state.error = "";
    persistSession();
  }

  return {
    state,
    isAuthenticated,
    login,
    refreshMe,
    logout
  };
}
