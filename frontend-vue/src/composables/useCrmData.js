import { computed, reactive } from "vue";
import { apiRequest } from "../services/api";

const state = reactive({
  pipelines: [],
  stagesByPipeline: {},
  lossReasons: [],
  deals: [],
  interactions: [],
  tasks: [],
  customers: [],
  sellers: [],
  companies: [],
  users: [],
  loaded: false,
  loading: false
});

export function useCrmData() {
  async function ensureLoaded() {
    if (state.loaded || state.loading) return;
    state.loading = true;
    try {
      const [pipelines, lossReasons, deals, interactions, tasks, customers, sellers, companies, users] = await Promise.all([
        apiRequest("/api/crm/catalog/pipelines"),
        apiRequest("/api/crm/catalog/loss-reasons"),
        apiRequest("/api/crm/deals"),
        apiRequest("/api/crm/interactions"),
        apiRequest("/api/crm/tasks"),
        apiRequest("/api/commercial/customers"),
        apiRequest("/api/commercial/sellers"),
        apiRequest("/api/config/companies"),
        apiRequest("/api/admin/users")
      ]);

      state.pipelines = pipelines;
      state.lossReasons = lossReasons;
      state.deals = deals;
      state.interactions = interactions;
      state.tasks = tasks;
      state.customers = customers;
      state.sellers = sellers;
      state.companies = companies;
      state.users = users;

      const stageEntries = await Promise.all(
        pipelines.map(async (pipeline) => {
          const stages = await apiRequest(`/api/crm/catalog/pipelines/${pipeline.id}/stages`);
          return [pipeline.id, stages];
        })
      );
      state.stagesByPipeline = Object.fromEntries(stageEntries);
    } finally {
      state.loaded = true;
      state.loading = false;
    }
  }

  function stagesForPipeline(pipelineId) {
    return state.stagesByPipeline[pipelineId] ?? [];
  }

  const dealsByStage = computed(() => {
    const result = {};
    for (const deal of state.deals) {
      if (!result[deal.stageId]) result[deal.stageId] = [];
      result[deal.stageId].push(deal);
    }
    return result;
  });

  async function createDeal(payload) {
    const created = await apiRequest("/api/crm/deals", {
      method: "POST",
      body: JSON.stringify(payload)
    });
    state.deals.unshift(created);
    return created;
  }

  async function moveDealStage(dealId, stageId, motivoPerdaId = null) {
    const updated = await apiRequest(`/api/crm/deals/${dealId}/stage`, {
      method: "PATCH",
      body: JSON.stringify({ stageId, motivoPerdaId })
    });
    replaceDeal(updated);
    return updated;
  }

  async function closeDealWon(dealId) {
    const updated = await apiRequest(`/api/crm/deals/${dealId}/close-won`, { method: "POST" });
    replaceDeal(updated);
    return updated;
  }

  async function closeDealLost(dealId, motivoPerdaId) {
    const updated = await apiRequest(`/api/crm/deals/${dealId}/close-lost`, {
      method: "POST",
      body: JSON.stringify({ motivoPerdaId })
    });
    replaceDeal(updated);
    return updated;
  }

  async function createInteraction(payload) {
    const created = await apiRequest("/api/crm/interactions", {
      method: "POST",
      body: JSON.stringify(payload)
    });
    state.interactions.unshift(created);
    return created;
  }

  async function createTask(payload) {
    const created = await apiRequest("/api/crm/tasks", {
      method: "POST",
      body: JSON.stringify(payload)
    });
    state.tasks.unshift(created);
    return created;
  }

  function replaceDeal(updated) {
    const idx = state.deals.findIndex((deal) => deal.id === updated.id);
    if (idx >= 0) state.deals[idx] = updated;
  }

  return {
    state,
    ensureLoaded,
    stagesForPipeline,
    dealsByStage,
    createDeal,
    moveDealStage,
    closeDealWon,
    closeDealLost,
    createInteraction,
    createTask
  };
}
