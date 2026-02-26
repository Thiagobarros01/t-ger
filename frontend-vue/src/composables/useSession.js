import { computed, reactive } from "vue";
import { modules, users } from "../data/mock";
import { useAuth } from "./useAuth";
import { apiRequest } from "../services/api";

const state = reactive({
  allUsers: [...users],
  loaded: false,
  loading: false
});

function canAccessModule(user, code) {
  if (!user) return false;
  if (user.active === false) return false;
  if (user.profile === "ADMINISTRADOR") return true;
  if (code === "CONFIGURACOES") return user.profile === "ADMINISTRADOR";
  return user.modules.includes(code);
}

export function useSession() {
  const auth = useAuth();
  const currentUser = computed(() => auth.state.user);

  const visibleModules = computed(() =>
    modules.filter((module) => canAccessModule(currentUser.value, module.code))
  );

  const visibleModuleCodes = computed(() => visibleModules.value.map((item) => item.code));

  async function ensureLoaded() {
    if (state.loaded || state.loading) return;
    state.loading = true;
    try {
      state.allUsers = await apiRequest("/api/admin/users");
      state.loaded = true;
    } catch {
      state.loaded = true;
    } finally {
      state.loading = false;
    }
  }

  async function addUser(userInput) {
    const normalizedModules = userInput.profile === "ADMINISTRADOR" ? [] : [...new Set(userInput.modules)];
    const created = await apiRequest("/api/admin/users", {
      method: "POST",
      body: JSON.stringify({
        ...userInput,
        modules: normalizedModules
      })
    });
    state.allUsers.unshift(created);
    return created;
  }

  async function updateUserEmail(userId, email) {
    await apiRequest(`/api/admin/users/${userId}/email`, {
      method: "PATCH",
      body: JSON.stringify({ email: email.trim() })
    });
    const user = state.allUsers.find((item) => item.id === userId);
    if (!user) return false;
    user.email = email.trim();
    return true;
  }

  async function resetUserPassword(userId) {
    await apiRequest(`/api/admin/users/${userId}/reset-password`, { method: "POST" });
    const user = state.allUsers.find((item) => item.id === userId);
    if (!user) return false;
    user.lastPasswordResetAt = new Date().toISOString();
    return true;
  }

  async function deactivateUser(userId) {
    await apiRequest(`/api/admin/users/${userId}/deactivate`, { method: "POST" });
    const user = state.allUsers.find((item) => item.id === userId);
    if (!user) return false;
    user.active = false;
    return true;
  }

  async function reactivateUser(userId) {
    await apiRequest(`/api/admin/users/${userId}/reactivate`, { method: "POST" });
    const user = state.allUsers.find((item) => item.id === userId);
    if (!user) return false;
    user.active = true;
    return true;
  }

  return {
    state,
    currentUser,
    visibleModules,
    visibleModuleCodes,
    ensureLoaded,
    addUser,
    updateUserEmail,
    resetUserPassword,
    deactivateUser,
    reactivateUser,
    canAccessModule
  };
}
