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
  customersById: {},
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
      const [pipelines, lossReasons, deals, interactions, tasks, sellers, companies, users] = await Promise.all([
        apiRequest("/api/crm/catalog/pipelines"),
        apiRequest("/api/crm/catalog/loss-reasons"),
        apiRequest("/api/crm/deals"),
        apiRequest("/api/crm/interactions"),
        apiRequest("/api/crm/tasks"),
        apiRequest("/api/commercial/sellers"),
        apiRequest("/api/config/companies"),
        apiRequest("/api/admin/users")
      ]);

      state.pipelines = pipelines;
      state.lossReasons = lossReasons;
      state.deals = deals;
      state.interactions = interactions;
      state.tasks = tasks;
      state.sellers = sellers;
      state.companies = companies;
      state.users = users;
      state.customers = [];
      state.customersById = {};

      const customerIds = [
        ...new Set(
          [...deals, ...interactions, ...tasks]
            .map((item) => item?.clienteId)
            .filter((id) => id != null)
        )
      ];
      await hydrateCustomersByIds(customerIds);

      const stageEntries = await Promise.all(
        pipelines.map(async (pipeline) => {
          const stages = await apiRequest(`/api/crm/catalog/pipelines/${pipeline.id}/stages`);
          return [pipeline.id, stages];
        })
      );
      state.stagesByPipeline = Object.fromEntries(stageEntries);

      if (state.deals.length === 0) {
        try {
          await syncDealsFromSalesHistory();
        } catch {
          // keep CRM usable even if sync is forbidden or fails
        }
      }
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
    await hydrateCustomersByIds([created?.clienteId]);
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

  async function updateDeal(dealId, payload) {
    const updated = await apiRequest(`/api/crm/deals/${dealId}`, {
      method: "PATCH",
      body: JSON.stringify(payload)
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
    await hydrateCustomersByIds([created?.clienteId]);
    return created;
  }

  async function createTask(payload) {
    const created = await apiRequest("/api/crm/tasks", {
      method: "POST",
      body: JSON.stringify(payload)
    });
    state.tasks.unshift(created);
    await hydrateCustomersByIds([created?.clienteId]);
    return created;
  }

  async function updateTaskStatus(taskId, status) {
    const updated = await apiRequest(`/api/crm/tasks/${taskId}/status`, {
      method: "PATCH",
      body: JSON.stringify({ status })
    });
    const idx = state.tasks.findIndex((item) => item.id === updated.id);
    if (idx >= 0) state.tasks[idx] = updated;
    return updated;
  }

  async function listTaskHistory(taskId) {
    return apiRequest(`/api/crm/tasks/${taskId}/history`);
  }

  async function createStage(payload) {
    const created = await apiRequest("/api/crm/catalog/stages", {
      method: "POST",
      body: JSON.stringify(payload)
    });
    const list = state.stagesByPipeline[created.pipelineId] ?? [];
    state.stagesByPipeline = {
      ...state.stagesByPipeline,
      [created.pipelineId]: [...list, created].sort((a, b) => a.ordem - b.ordem)
    };
    return created;
  }

  async function syncDealsFromSalesHistory() {
    const result = await apiRequest("/api/crm/deals/sync-from-sales-history", {
      method: "POST"
    });
    state.deals = await apiRequest("/api/crm/deals");
    await hydrateCustomersByIds(state.deals.map((deal) => deal.clienteId));
    return result;
  }

  async function updateStage(stageId, payload) {
    const updated = await apiRequest(`/api/crm/catalog/stages/${stageId}`, {
      method: "PUT",
      body: JSON.stringify(payload)
    });
    const list = state.stagesByPipeline[updated.pipelineId] ?? [];
    const idx = list.findIndex((item) => item.id === updated.id);
    if (idx >= 0) list[idx] = updated;
    state.stagesByPipeline = {
      ...state.stagesByPipeline,
      [updated.pipelineId]: [...list].sort((a, b) => a.ordem - b.ordem)
    };
    return updated;
  }

  async function deleteStage(stageId, pipelineId) {
    await apiRequest(`/api/crm/catalog/stages/${stageId}`, { method: "DELETE" });
    const list = state.stagesByPipeline[pipelineId] ?? [];
    state.stagesByPipeline = {
      ...state.stagesByPipeline,
      [pipelineId]: list.filter((item) => item.id !== stageId)
    };
  }

  function replaceDeal(updated) {
    const idx = state.deals.findIndex((deal) => deal.id === updated.id);
    if (idx >= 0) state.deals[idx] = updated;
  }

  async function hydrateCustomersByIds(ids) {
    const uniqueIds = [...new Set((ids ?? []).filter((id) => id != null))];
    if (uniqueIds.length === 0) return;
    const missingIds = uniqueIds.filter((id) => !state.customersById[id]);
    if (missingIds.length === 0) return;
    const loaded = await apiRequest("/api/commercial/customers/by-ids", {
      method: "POST",
      body: JSON.stringify(missingIds)
    });
    for (const customer of loaded) {
      state.customersById[customer.id] = customer;
    }
    state.customers = Object.values(state.customersById);
  }

  async function searchCustomers(term = "", page = 1, pageSize = 20) {
    const params = new URLSearchParams();
    if (term?.trim()) {
      params.set("corporateName", term.trim());
      params.set("erpCode", term.trim());
    }
    params.set("page", String(page));
    params.set("pageSize", String(pageSize));
    const response = await apiRequest(`/api/commercial/customers/paged?${params.toString()}`);
    for (const customer of response.items ?? []) {
      state.customersById[customer.id] = customer;
    }
    state.customers = Object.values(state.customersById);
    return response;
  }

  function customerNameById(id) {
    return state.customersById[id]?.corporateName ?? `#${id}`;
  }

  return {
    state,
    ensureLoaded,
    stagesForPipeline,
    dealsByStage,
    createDeal,
    updateDeal,
    moveDealStage,
    closeDealWon,
    closeDealLost,
    createInteraction,
    createTask,
    updateTaskStatus,
    listTaskHistory,
    createStage,
    updateStage,
    deleteStage,
    syncDealsFromSalesHistory,
    searchCustomers,
    hydrateCustomersByIds,
    customerNameById
  };
}
