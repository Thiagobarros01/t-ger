import { reactive } from "vue";
import { apiRequest } from "../services/api";

const state = reactive({
  terms: [],
  assets: [],
  tickets: [],
  loaded: false,
  loading: false
});

export function useTiData() {
  async function ensureLoaded() {
    if (state.loaded || state.loading) return;
    state.loading = true;
    try {
      const [terms, assets, tickets] = await Promise.all([
        apiRequest("/api/ti/terms-contracts"),
        apiRequest("/api/ti/assets"),
        apiRequest("/api/ti/tickets")
      ]);
      state.terms = terms;
      state.assets = assets;
      state.tickets = tickets;
      state.loaded = true;
    } catch {
      state.loaded = true;
    } finally {
      state.loading = false;
    }
  }

  async function addTerm(term) {
    const created = await apiRequest("/api/ti/terms-contracts", {
      method: "POST",
      body: JSON.stringify(term)
    });
    state.terms.unshift(created);
    return created;
  }

  async function addAsset(asset) {
    const created = await apiRequest("/api/ti/assets", {
      method: "POST",
      body: JSON.stringify(asset)
    });
    state.assets.unshift(created);
    return created;
  }

  async function addTicketMessage(ticketId, message) {
    const updated = await apiRequest(`/api/ti/tickets/${ticketId}/messages`, {
      method: "POST",
      body: JSON.stringify(message)
    });
    const index = state.tickets.findIndex((item) => item.id === ticketId);
    if (index >= 0) {
      state.tickets[index] = updated;
    } else {
      state.tickets.unshift(updated);
    }
    return updated;
  }

  async function updateTicketStatus(ticketId, status) {
    const updated = await apiRequest(`/api/ti/tickets/${ticketId}/status`, {
      method: "PATCH",
      body: JSON.stringify({ status })
    });
    const index = state.tickets.findIndex((item) => item.id === ticketId);
    if (index >= 0) state.tickets[index] = updated;
    return updated;
  }

  async function updateTerm(id, term) {
    const updated = await apiRequest(`/api/ti/terms-contracts/${id}`, {
      method: "PUT",
      body: JSON.stringify(term)
    });
    const idx = state.terms.findIndex((item) => item.id === id);
    if (idx >= 0) state.terms[idx] = updated;
    return updated;
  }

  async function removeTerm(id) {
    await apiRequest(`/api/ti/terms-contracts/${id}`, { method: "DELETE" });
    state.terms = state.terms.filter((item) => item.id !== id);
  }

  async function updateAsset(id, asset) {
    const updated = await apiRequest(`/api/ti/assets/${id}`, {
      method: "PUT",
      body: JSON.stringify(asset)
    });
    const idx = state.assets.findIndex((item) => item.id === id);
    if (idx >= 0) state.assets[idx] = updated;
    return updated;
  }

  async function removeAsset(id) {
    await apiRequest(`/api/ti/assets/${id}`, { method: "DELETE" });
    state.assets = state.assets.filter((item) => item.id !== id);
  }

  return {
    state,
    ensureLoaded,
    addTerm,
    addAsset,
    updateTerm,
    removeTerm,
    updateAsset,
    removeAsset,
    addTicketMessage,
    updateTicketStatus
  };
}
