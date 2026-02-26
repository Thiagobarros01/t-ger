import { reactive } from "vue";
import { apiRequest } from "../services/api";

const state = reactive({
  companies: [],
  departments: [],
  categories: [],
  lines: [],
  manufacturers: [],
  loaded: false,
  loading: false
});

export function useMasterData() {
  async function ensureLoaded() {
    if (state.loaded || state.loading) return;
    state.loading = true;
    try {
      state.companies = await apiRequest("/api/config/companies");
      state.loaded = true;
    } catch {
      state.loaded = true;
    } finally {
      state.loading = false;
    }
  }

  async function addCompany(input) {
    const payload = typeof input === "string" ? { name: input, erpCode: "" } : input ?? {};
    if (!payload.name?.trim()) return;
    const created = await apiRequest("/api/config/companies", {
      method: "POST",
      body: JSON.stringify({ name: payload.name.trim(), erpCode: (payload.erpCode ?? "").trim() })
    });
    state.companies.unshift(created);
    return created;
  }

  async function removeCompany(id) {
    await apiRequest(`/api/config/companies/${id}`, { method: "DELETE" });
    const index = state.companies.findIndex((item) => item.id === id);
    if (index >= 0) state.companies.splice(index, 1);
  }

  async function updateCompany(id, input) {
    const updated = await apiRequest(`/api/config/companies/${id}`, {
      method: "PUT",
      body: JSON.stringify({
        name: (input.name ?? "").trim(),
        erpCode: (input.erpCode ?? "").trim()
      })
    });
    const index = state.companies.findIndex((item) => item.id === id);
    if (index >= 0) state.companies[index] = updated;
    return updated;
  }

  return {
    state,
    ensureLoaded,
    addCompany,
    updateCompany,
    removeCompany
  };
}
