import { reactive } from "vue";
import { apiRequest } from "../services/api";

const state = reactive({
  clientes: [],
  produtos: [],
  loaded: { clientes: false, produtos: false }
});

export function useImportConfig() {
  async function ensureLoaded(entity) {
    if (state.loaded[entity]) return;
    try {
      state[entity] = await apiRequest(`/api/config/import-layouts/${entity}`);
      state.loaded[entity] = true;
    } catch {
      state.loaded[entity] = true;
    }
  }

  function getFields(entity) {
    return state[entity] ?? [];
  }

  async function updateField(entity, key, patch) {
    const row = (state[entity] ?? []).find((item) => item.key === key);
    if (!row) return;
    if (Object.prototype.hasOwnProperty.call(patch, "alias")) {
      row.alias = String(patch.alias ?? "");
    }
    if (Object.prototype.hasOwnProperty.call(patch, "required")) {
      row.required = Boolean(patch.required);
    }
    await apiRequest(`/api/config/import-layouts/${entity}`, {
      method: "PUT",
      body: JSON.stringify(state[entity])
    });
  }

  return {
    state,
    ensureLoaded,
    getFields,
    updateField
  };
}
