<template>
  <div>
    <PageHeader eyebrow="CRM Comercial" title="Kanban de Oportunidades" subtitle="Pipeline e etapas de negociações B2C/B2B." />

    <div class="panel">
      <div class="form-grid">
        <label>
          Pipeline
          <select v-model.number="selectedPipelineId">
            <option v-for="pipeline in state.pipelines" :key="pipeline.id" :value="pipeline.id">
              {{ pipeline.nome }} ({{ pipeline.tipoNegocio }})
            </option>
          </select>
        </label>
        <div class="actions-row" style="align-self: end">
          <button type="button" class="btn-primary" @click="syncFromErpHistory" :disabled="syncingHistory">
            {{ syncingHistory ? "Sincronizando..." : "Preencher com ERP" }}
          </button>
          <button type="button" class="btn-soft" @click="openPipelineConfig">Configurar funil</button>
        </div>
      </div>
      <p class="muted" style="margin-top: 8px;" v-if="syncMessage">{{ syncMessage }}</p>
    </div>

    <div class="panel">
      <div class="section-head" style="margin-bottom: 8px">
        <h3 style="margin: 0">Filtros do kanban</h3>
        <button type="button" class="btn-soft" @click="clearKanbanFilters">Limpar filtros</button>
      </div>
      <div class="form-grid">
        <label>
          Cliente (nome ou codigo ERP)
          <input v-model.trim="kanbanFilters.customerTerm" type="text" placeholder="Ex.: Farmacia Central ou 12045" />
        </label>
        <label>
          Numero do pedido
          <input v-model.trim="kanbanFilters.orderNumber" type="text" placeholder="Ex.: 458921" />
        </label>
        <label>
          Data inicial do ultimo pedido
          <input v-model="kanbanFilters.dateFrom" type="date" />
        </label>
        <label>
          Data final do ultimo pedido
          <input v-model="kanbanFilters.dateTo" type="date" />
        </label>
        <label class="checkbox-inline" style="align-self: end;">
          <input v-model="kanbanFilters.onlyLatestLoadDate" type="checkbox" />
          Mostrar apenas a ultima data carregada
        </label>
      </div>
      <p class="muted" style="margin-top: 8px;">
        Base atual: {{ latestOrderDateLabel }}
      </p>
    </div>

    <div class="panel">
      <div class="section-head" style="margin-bottom: 0">
        <h3 style="margin-top: 0">Nova oportunidade</h3>
        <button type="button" class="btn-soft" @click="createCollapsed = !createCollapsed">
          {{ createCollapsed ? "Expandir" : "Minimizar" }}
        </button>
      </div>
      <form v-if="!createCollapsed" class="form-grid" @submit.prevent="saveDeal">
        <label>
          Cliente
          <SearchSelect
            v-model="dealForm.clienteId"
            :fetch-options="searchCustomerOptions"
            placeholder="Selecione cliente"
          />
        </label>
        <label>
          Empresa (B2B)
          <SearchSelect
            v-model="dealForm.empresaId"
            :options="companyOptions"
            placeholder="Sem empresa"
            :allow-empty="true"
            empty-label="Sem empresa"
          />
        </label>
        <label>
          Vendedor (owner)
          <SearchSelect
            v-model="dealForm.vendedorId"
            :options="sellerOptions"
            placeholder="Selecione vendedor"
          />
        </label>
        <label>
          Tipo da oportunidade
          <select v-model="dealForm.tipoOportunidade">
            <option value="NOVA">Nova</option>
            <option value="RECOMPRA">Recompra</option>
            <option value="RESGATE">Resgate</option>
          </select>
        </label>
        <label>
          Etapa inicial
          <SearchSelect
            v-model="dealForm.stageId"
            :options="stageOptions"
            placeholder="Selecione etapa"
          />
        </label>
        <label>
          Valor estimado
          <input v-model="dealForm.valorEstimado" type="number" step="0.01" />
        </label>
        <label>
          Probabilidade (%)
          <input v-model.number="dealForm.probabilidade" type="number" min="0" max="100" />
        </label>
        <label>
          Fechamento previsto
          <input v-model="dealForm.dataPrevistaFechamento" type="date" />
        </label>
        <div class="full actions-row">
          <button class="btn-primary" type="submit">Criar oportunidade</button>
          <span class="muted">{{ selectedPipeline?.tipoNegocio || "-" }}</span>
        </div>
      </form>
    </div>

    <div class="kanban-board" v-if="selectedStages.length">
      <section class="kanban-column" :class="{ 'kanban-column--drop': dragOverStageId === stage.id }" v-for="stage in selectedStages" :key="stage.id">
        <header class="kanban-column__header">
          <strong>{{ stage.nome }}</strong>
          <span class="tag">{{ stageDeals(stage.id).length }}</span>
        </header>
        <div
          class="kanban-list"
          @dragover.prevent="onDragOverStage(stage.id)"
          @dragenter.prevent="onDragOverStage(stage.id)"
          @dragleave="onDragLeaveStage(stage.id)"
          @drop.prevent="onDropStage(stage)"
        >
          <TransitionGroup name="kanban-move" tag="div" class="kanban-cards">
            <article
              class="kanban-card"
              :class="{ 'kanban-card--dragging': draggingDealId === deal.id, 'kanban-card--selected': selectedDealId === deal.id }"
              v-for="deal in paginatedStageDeals(stage.id)"
              :key="deal.id"
              draggable="true"
              @dragstart="onDragStart(deal)"
              @dragend="onDragEnd"
              @click="selectDeal(deal)"
            >
            <div class="kanban-card__head">
              <strong>#{{ deal.id }}</strong>
              <span class="tag">{{ deal.status }}</span>
            </div>
            <p>Tipo: {{ deal.tipoOportunidade || "NOVA" }}</p>
            <p>Cliente: {{ customerName(deal.clienteId) }}</p>
            <p>Vendedor: {{ sellerName(deal.vendedorId) }}</p>
            <p>Valor: {{ formatCurrency(deal.valorEstimado) }}</p>
            <p>Pedido: {{ deal.numeroPedido || "-" }}</p>
            <p>Situação pedido: {{ deal.situacaoPedido || "-" }}</p>
            <p>Ultimo pedido: {{ formatDate(deal.dataUltimoPedido) }}</p>
            <div class="actions-row">
              <button type="button" @click.stop="selectDeal(deal)">Gerenciar</button>
            </div>
            </article>
          </TransitionGroup>
          <div class="pagination-bar" v-if="stageTotalPages(stage.id) > 1" style="margin-top: 6px;">
            <div class="pagination-bar__meta">
              <span>Pagina {{ stagePageFor(stage.id) }} / {{ stageTotalPages(stage.id) }}</span>
            </div>
            <div class="pagination-bar__controls">
              <button type="button" @click.stop="setStagePage(stage.id, stagePageFor(stage.id) - 1)" :disabled="stagePageFor(stage.id) <= 1">&lt;</button>
              <button type="button" @click.stop="setStagePage(stage.id, stagePageFor(stage.id) + 1)" :disabled="stagePageFor(stage.id) >= stageTotalPages(stage.id)">&gt;</button>
            </div>
          </div>
          <div class="empty-state" v-if="stageDeals(stage.id).length === 0">Sem oportunidades nesta etapa.</div>
        </div>
      </section>
    </div>

    <div class="panel" v-if="selectedDeal">
      <div class="section-head">
        <div>
          <h3>Gerenciar Deal #{{ selectedDeal.id }}</h3>
          <p>Cliente: {{ customerName(selectedDeal.clienteId) }} | Status: {{ selectedDeal.status }} | Pedido: {{ selectedDeal.numeroPedido || "-" }} ({{ selectedDeal.situacaoPedido || "-" }})</p>
        </div>
        <button type="button" class="btn-soft" @click="clearSelectedDeal">Fechar</button>
      </div>
      <div class="form-grid">
        <label>
          Cliente
          <SearchSelect
            v-model="dealEdit.clienteId"
            :fetch-options="searchCustomerOptions"
            placeholder="Selecione cliente"
          />
        </label>
        <label>
          Empresa (B2B)
          <SearchSelect
            v-model="dealEdit.empresaId"
            :options="companyOptions"
            placeholder="Sem empresa"
            :allow-empty="true"
            empty-label="Sem empresa"
          />
        </label>
        <label>
          Vendedor (owner)
          <SearchSelect
            v-model="dealEdit.vendedorId"
            :options="sellerOptions"
            placeholder="Selecione vendedor"
          />
        </label>
        <label>
          Tipo da oportunidade
          <select v-model="dealEdit.tipoOportunidade">
            <option value="NOVA">Nova</option>
            <option value="RECOMPRA">Recompra</option>
            <option value="RESGATE">Resgate</option>
          </select>
        </label>
        <label>
          Valor estimado
          <input v-model="dealEdit.valorEstimado" type="number" step="0.01" />
        </label>
        <label>
          Probabilidade (%)
          <input v-model.number="dealEdit.probabilidade" type="number" min="0" max="100" />
        </label>
        <label>
          Fechamento previsto
          <input v-model="dealEdit.dataPrevistaFechamento" type="date" />
        </label>
        <div class="full actions-row">
          <button type="button" class="btn-primary" @click="saveDealEdit">Salvar dados do card</button>
        </div>
        <label>
          Mover para etapa
          <SearchSelect
            v-model="dealAction.stageId"
            :options="stageOptions"
            placeholder="Selecione etapa"
          />
        </label>
        <label v-if="selectedActionStage?.isLost">
          Motivo da perda
          <SearchSelect
            v-model="dealAction.motivoPerdaId"
            :options="lossReasonOptions"
            placeholder="Selecione motivo"
          />
        </label>
        <div class="full actions-row">
          <button type="button" class="btn-primary" @click="moveStage">Mover etapa</button>
          <button type="button" @click="closeWon">Fechar como ganha</button>
          <button type="button" @click="closeLost">Fechar como perdida</button>
        </div>
      </div>
      <p class="danger-text" v-if="errorMessage">{{ errorMessage }}</p>
    </div>

    <div class="modal-overlay" v-if="showPipelineConfig" @click.self="closePipelineConfig">
      <div class="modal-card modal-card--small">
        <div class="section-head">
          <div>
            <h3>Configurar funil</h3>
            <p>{{ selectedPipeline?.nome || "-" }} ({{ selectedPipeline?.tipoNegocio || "-" }})</p>
          </div>
          <button type="button" class="btn-soft" @click="closePipelineConfig">Fechar</button>
        </div>

        <div class="table-scroll">
          <table>
            <thead>
              <tr>
                <th>Ordem</th>
                <th>Nome</th>
                <th>Regra</th>
                <th>Acoes</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(stage, idx) in stageEditor" :key="stage.localId">
                <td>{{ idx + 1 }}</td>
                <td><input v-model="stage.nome" /></td>
                <td>
                  <select v-model="stage.rule">
                    <option value="OPEN">Aberta</option>
                    <option value="WON">Ganha</option>
                    <option value="LOST">Perdida</option>
                  </select>
                </td>
                <td>
                  <div class="actions-row actions-row--compact">
                    <button type="button" class="btn-soft" @click="moveStageUp(idx)" :disabled="idx === 0">Subir</button>
                    <button type="button" class="btn-soft" @click="moveStageDown(idx)" :disabled="idx === stageEditor.length - 1">Descer</button>
                    <button type="button" @click="removeStageEditorRow(idx)">Remover</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="actions-row" style="margin-top: 10px">
          <button type="button" class="btn-soft" @click="addStageEditorRow">Adicionar etapa</button>
          <button type="button" class="btn-primary" @click="savePipelineConfig">Salvar funil</button>
        </div>
        <p class="danger-text" v-if="pipelineConfigError">{{ pipelineConfigError }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import PageHeader from "../../components/PageHeader.vue";
import SearchSelect from "../../components/SearchSelect.vue";
import { useCrmData } from "../../composables/useCrmData";

const { state, ensureLoaded, stagesForPipeline, dealsByStage, createDeal, updateDeal, moveDealStage, closeDealWon, closeDealLost, createStage, updateStage, deleteStage, syncDealsFromSalesHistory, searchCustomers, customerNameById } = useCrmData();

const selectedPipelineId = ref(null);
const selectedDealId = ref(null);
const createCollapsed = ref(false);
const errorMessage = ref("");
const draggingDealId = ref(null);
const dragOverStageId = ref(null);
const showPipelineConfig = ref(false);
const stageEditor = ref([]);
const pipelineConfigError = ref("");
const syncingHistory = ref(false);
const syncMessage = ref("");
const stagePageMap = ref({});
const stagePageSize = 25;
const kanbanFilters = reactive({
  customerTerm: "",
  orderNumber: "",
  dateFrom: "",
  dateTo: "",
  onlyLatestLoadDate: true
});

const dealForm = reactive({
  clienteId: null,
  empresaId: null,
  vendedorId: null,
  tipoOportunidade: "NOVA",
  stageId: null,
  valorEstimado: "",
  probabilidade: 0,
  dataPrevistaFechamento: ""
});

const dealAction = reactive({
  stageId: null,
  motivoPerdaId: null
});

const dealEdit = reactive({
  clienteId: null,
  empresaId: null,
  vendedorId: null,
  tipoOportunidade: "NOVA",
  valorEstimado: "",
  probabilidade: 0,
  dataPrevistaFechamento: ""
});

onMounted(async () => {
  await ensureLoaded();
  if (state.pipelines.length > 0) {
    selectedPipelineId.value = state.pipelines[0].id;
  }
});

watch(selectedPipelineId, (pipelineId) => {
  const pipeline = state.pipelines.find((item) => item.id === pipelineId);
  const stages = stagesForPipeline(pipelineId);
  dealForm.stageId = stages[0]?.id ?? null;
  selectedDealId.value = null;
  errorMessage.value = "";
  if (pipeline) {
    if (pipeline.tipoNegocio === "B2C") {
      dealForm.empresaId = null;
    }
  }
  stagePageMap.value = {};
});

watch(
  () => [
    kanbanFilters.customerTerm,
    kanbanFilters.orderNumber,
    kanbanFilters.dateFrom,
    kanbanFilters.dateTo,
    kanbanFilters.onlyLatestLoadDate
  ],
  () => {
    stagePageMap.value = {};
  }
);

const selectedPipeline = computed(() => state.pipelines.find((item) => item.id === selectedPipelineId.value) ?? null);
const selectedStages = computed(() => stagesForPipeline(selectedPipelineId.value).slice().sort((a, b) => a.ordem - b.ordem));
const activeLossReasons = computed(() => state.lossReasons.filter((item) => item.ativo));
const selectedDeal = computed(() => state.deals.find((deal) => deal.id === selectedDealId.value) ?? null);
const selectedActionStage = computed(() => selectedStages.value.find((stage) => stage.id === dealAction.stageId) ?? null);
const companyOptions = computed(() =>
  state.companies.map((company) => ({
    value: company.id,
    label: company.name,
    meta: company.erpCode || null,
    searchText: `${company.name ?? ""} ${company.erpCode ?? ""}`
  }))
);
const sellerOptions = computed(() =>
  state.sellers.map((seller) => ({
    value: seller.id,
    label: seller.name,
    meta: seller.code || seller.erpCode || null,
    searchText: `${seller.name ?? ""} ${seller.code ?? ""} ${seller.erpCode ?? ""}`
  }))
);
const stageOptions = computed(() =>
  selectedStages.value.map((stage) => ({
    value: stage.id,
    label: stage.nome,
    meta: `Ordem ${stage.ordem}`,
    searchText: `${stage.nome ?? ""} ${stage.ordem ?? ""}`
  }))
);
const lossReasonOptions = computed(() =>
  activeLossReasons.value.map((reason) => ({
    value: reason.id,
    label: reason.descricao,
    meta: null,
    searchText: reason.descricao
  }))
);

const latestOrderDate = computed(() => {
  const deals = state.deals
    .filter((deal) => deal.pipelineId === selectedPipelineId.value)
    .filter((deal) => !!deal.dataUltimoPedido);
  if (deals.length === 0) return null;
  return deals
    .map((deal) => deal.dataUltimoPedido)
    .sort((a, b) => (a > b ? -1 : a < b ? 1 : 0))[0];
});

const latestOrderDateLabel = computed(() => {
  if (!latestOrderDate.value) return "Sem data de pedido";
  return formatDate(latestOrderDate.value);
});

function stageDeals(stageId) {
  const list = dealsByStage.value[stageId] ?? [];
  return list
    .filter((deal) => deal.pipelineId === selectedPipelineId.value)
    .filter(filterDealForKanban)
    .sort((a, b) => {
      const ad = a.dataUltimoPedido || "";
      const bd = b.dataUltimoPedido || "";
      if (ad !== bd) return ad < bd ? 1 : -1;
      return (b.id || 0) - (a.id || 0);
    });
}

function stagePageFor(stageId) {
  const value = Number(stagePageMap.value[stageId] ?? 1);
  return Number.isFinite(value) && value > 0 ? Math.floor(value) : 1;
}

function stageTotalPages(stageId) {
  const total = stageDeals(stageId).length;
  return Math.max(1, Math.ceil(total / stagePageSize));
}

function setStagePage(stageId, page) {
  const totalPages = stageTotalPages(stageId);
  const next = Math.min(Math.max(1, page), totalPages);
  stagePageMap.value = { ...stagePageMap.value, [stageId]: next };
}

function paginatedStageDeals(stageId) {
  const list = stageDeals(stageId);
  const totalPages = stageTotalPages(stageId);
  const page = Math.min(stagePageFor(stageId), totalPages);
  if (page !== stagePageFor(stageId)) setStagePage(stageId, page);
  const start = (page - 1) * stagePageSize;
  return list.slice(start, start + stagePageSize);
}

function customerName(id) {
  return customerNameById(id);
}

function sellerName(id) {
  return state.sellers.find((item) => item.id === id)?.name ?? `#${id}`;
}

async function searchCustomerOptions(query, page, pageSize) {
  const response = await searchCustomers(query, page, pageSize);
  return {
    items: (response.items ?? []).map((customer) => ({
      value: customer.id,
      label: customer.corporateName,
      meta: customer.code || customer.erpCode || null,
      searchText: `${customer.corporateName ?? ""} ${customer.code ?? ""} ${customer.erpCode ?? ""}`
    })),
    totalPages: response.totalPages ?? 1
  };
}

function formatCurrency(value) {
  if (value === null || value === undefined || value === "") return "-";
  return Number(value).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

function formatDate(value) {
  if (!value) return "-";
  const [y, m, d] = String(value).split("-");
  if (!y || !m || !d) return String(value);
  return `${d}/${m}/${y}`;
}

function normalizeText(value) {
  return String(value ?? "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .trim();
}

function filterDealForKanban(deal) {
  const customerTerm = normalizeText(kanbanFilters.customerTerm);
  if (customerTerm) {
    const customer = state.customersById[deal.clienteId];
    const customerText = normalizeText(
      `${customer?.corporateName ?? ""} ${customer?.code ?? ""} ${customer?.erpCode ?? ""}`
    );
    if (!customerText.includes(customerTerm)) return false;
  }

  const orderNumber = normalizeText(kanbanFilters.orderNumber);
  if (orderNumber) {
    const order = normalizeText(deal.numeroPedido);
    if (!order.includes(orderNumber)) return false;
  }

  if (kanbanFilters.onlyLatestLoadDate && latestOrderDate.value) {
    if ((deal.dataUltimoPedido || "") !== latestOrderDate.value) return false;
  }

  if (kanbanFilters.dateFrom && deal.dataUltimoPedido) {
    if (deal.dataUltimoPedido < kanbanFilters.dateFrom) return false;
  }
  if (kanbanFilters.dateTo && deal.dataUltimoPedido) {
    if (deal.dataUltimoPedido > kanbanFilters.dateTo) return false;
  }
  if ((kanbanFilters.dateFrom || kanbanFilters.dateTo) && !deal.dataUltimoPedido) {
    return false;
  }

  return true;
}

function clearKanbanFilters() {
  kanbanFilters.customerTerm = "";
  kanbanFilters.orderNumber = "";
  kanbanFilters.dateFrom = "";
  kanbanFilters.dateTo = "";
  kanbanFilters.onlyLatestLoadDate = true;
}

async function saveDeal() {
  errorMessage.value = "";
  try {
    if (!selectedPipeline.value) return;
    if (!dealForm.clienteId || !dealForm.vendedorId || !dealForm.stageId) {
      errorMessage.value = "Preencha cliente, vendedor e etapa inicial.";
      return;
    }
    await createDeal({
      clienteId: dealForm.clienteId,
      empresaId: selectedPipeline.value.tipoNegocio === "B2B" ? dealForm.empresaId : null,
      vendedorId: dealForm.vendedorId,
      tipoNegocio: selectedPipeline.value.tipoNegocio,
      tipoOportunidade: dealForm.tipoOportunidade || "NOVA",
      pipelineId: selectedPipeline.value.id,
      stageId: dealForm.stageId,
      valorEstimado: dealForm.valorEstimado === "" ? null : Number(dealForm.valorEstimado),
      probabilidade: dealForm.probabilidade,
      dataPrevistaFechamento: dealForm.dataPrevistaFechamento || null,
      motivoPerdaId: null
    });
    dealForm.clienteId = null;
    dealForm.empresaId = null;
    dealForm.vendedorId = null;
    dealForm.tipoOportunidade = "NOVA";
    dealForm.valorEstimado = "";
    dealForm.probabilidade = 0;
    dealForm.dataPrevistaFechamento = "";
  } catch (err) {
    errorMessage.value = parseError(err);
  }
}

async function syncFromErpHistory() {
  syncingHistory.value = true;
  syncMessage.value = "";
  errorMessage.value = "";
  try {
    const result = await syncDealsFromSalesHistory();
    syncMessage.value = `Sincronizacao concluida: ${result.created} criadas, ${result.updated} atualizadas, ${result.skipped} ignoradas, ${result.unmapped} sem mapeamento ERP.`;
  } catch (err) {
    errorMessage.value = parseError(err);
  } finally {
    syncingHistory.value = false;
  }
}

function selectDeal(deal) {
  selectedDealId.value = deal.id;
  dealAction.stageId = deal.stageId;
  dealAction.motivoPerdaId = deal.motivoPerdaId;
  dealEdit.clienteId = deal.clienteId;
  dealEdit.empresaId = deal.empresaId;
  dealEdit.vendedorId = deal.vendedorId;
  dealEdit.tipoOportunidade = deal.tipoOportunidade ?? "NOVA";
  dealEdit.valorEstimado = deal.valorEstimado ?? "";
  dealEdit.probabilidade = deal.probabilidade ?? 0;
  dealEdit.dataPrevistaFechamento = deal.dataPrevistaFechamento ?? "";
  errorMessage.value = "";
}

function clearSelectedDeal() {
  selectedDealId.value = null;
  dealAction.stageId = null;
  dealAction.motivoPerdaId = null;
  dealEdit.clienteId = null;
  dealEdit.empresaId = null;
  dealEdit.vendedorId = null;
  dealEdit.tipoOportunidade = "NOVA";
  dealEdit.valorEstimado = "";
  dealEdit.probabilidade = 0;
  dealEdit.dataPrevistaFechamento = "";
  errorMessage.value = "";
}

async function saveDealEdit() {
  if (!selectedDeal.value) return;
  errorMessage.value = "";
  try {
    await updateDeal(selectedDeal.value.id, {
      clienteId: dealEdit.clienteId,
      empresaId: selectedPipeline.value?.tipoNegocio === "B2B" ? dealEdit.empresaId : null,
      vendedorId: dealEdit.vendedorId,
      tipoOportunidade: dealEdit.tipoOportunidade || "NOVA",
      valorEstimado: dealEdit.valorEstimado === "" ? null : Number(dealEdit.valorEstimado),
      probabilidade: dealEdit.probabilidade,
      dataPrevistaFechamento: dealEdit.dataPrevistaFechamento || null
    });
  } catch (err) {
    errorMessage.value = parseError(err);
  }
}

async function moveStage() {
  if (!selectedDeal.value) return;
  errorMessage.value = "";
  try {
    await moveDealStage(selectedDeal.value.id, dealAction.stageId, dealAction.motivoPerdaId);
  } catch (err) {
    errorMessage.value = parseError(err);
  }
}

async function closeWon() {
  if (!selectedDeal.value) return;
  errorMessage.value = "";
  try {
    await closeDealWon(selectedDeal.value.id);
  } catch (err) {
    errorMessage.value = parseError(err);
  }
}

async function closeLost() {
  if (!selectedDeal.value) return;
  errorMessage.value = "";
  try {
    await closeDealLost(selectedDeal.value.id, dealAction.motivoPerdaId);
  } catch (err) {
    errorMessage.value = parseError(err);
  }
}

function onDragStart(deal) {
  draggingDealId.value = deal.id;
  errorMessage.value = "";
}

function onDragEnd() {
  draggingDealId.value = null;
  dragOverStageId.value = null;
}

function onDragOverStage(stageId) {
  dragOverStageId.value = stageId;
}

function onDragLeaveStage(stageId) {
  if (dragOverStageId.value === stageId) {
    dragOverStageId.value = null;
  }
}

async function onDropStage(stage) {
  const dealId = draggingDealId.value;
  onDragEnd();
  if (!dealId) return;
  const deal = state.deals.find((item) => item.id === dealId);
  if (!deal || deal.stageId === stage.id) return;
  errorMessage.value = "";
  try {
    const motivoPerdaId = stage.isLost ? activeLossReasons.value[0]?.id ?? null : null;
    if (stage.isLost && !motivoPerdaId) {
      errorMessage.value = "Nao ha motivo de perda ativo para mover para etapa de perda.";
      return;
    }
    await moveDealStage(deal.id, stage.id, motivoPerdaId);
    if (selectedDealId.value === deal.id) {
      dealAction.stageId = stage.id;
      dealAction.motivoPerdaId = motivoPerdaId;
    }
  } catch (err) {
    errorMessage.value = parseError(err);
  }
}

function parseError(error) {
  const raw = String(error?.message ?? "Falha ao executar operacao.");
  try {
    const parsed = JSON.parse(raw);
    return parsed.error ?? raw;
  } catch {
    return raw;
  }
}

function openPipelineConfig() {
  stageEditor.value = selectedStages.value.map((stage, idx) => ({
    localId: `s-${stage.id}-${idx}`,
    id: stage.id,
    nome: stage.nome,
    rule: stage.isWon ? "WON" : stage.isLost ? "LOST" : "OPEN"
  }));
  pipelineConfigError.value = "";
  showPipelineConfig.value = true;
}

function closePipelineConfig() {
  showPipelineConfig.value = false;
  stageEditor.value = [];
  pipelineConfigError.value = "";
}

function addStageEditorRow() {
  stageEditor.value.push({
    localId: `new-${Date.now()}-${Math.random().toString(16).slice(2)}`,
    id: null,
    nome: "",
    rule: "OPEN"
  });
}

function removeStageEditorRow(index) {
  stageEditor.value.splice(index, 1);
}

function moveStageUp(index) {
  if (index <= 0) return;
  const list = [...stageEditor.value];
  [list[index - 1], list[index]] = [list[index], list[index - 1]];
  stageEditor.value = list;
}

function moveStageDown(index) {
  if (index >= stageEditor.value.length - 1) return;
  const list = [...stageEditor.value];
  [list[index], list[index + 1]] = [list[index + 1], list[index]];
  stageEditor.value = list;
}

async function savePipelineConfig() {
  if (!selectedPipeline.value) return;
  pipelineConfigError.value = "";
  try {
    const cleaned = stageEditor.value
      .map((item) => ({ ...item, nome: item.nome.trim() }))
      .filter((item) => item.nome);

    if (cleaned.length === 0) {
      pipelineConfigError.value = "Adicione pelo menos uma etapa.";
      return;
    }

    const wonCount = cleaned.filter((item) => item.rule === "WON").length;
    const lostCount = cleaned.filter((item) => item.rule === "LOST").length;
    if (wonCount > 1 || lostCount > 1) {
      pipelineConfigError.value = "Use no maximo uma etapa Ganha e uma etapa Perdida.";
      return;
    }

    const existingIds = new Set(selectedStages.value.map((stage) => stage.id));
    const incomingIds = new Set(cleaned.filter((item) => item.id != null).map((item) => item.id));
    const toDelete = [...existingIds].filter((id) => !incomingIds.has(id));

    for (let i = 0; i < cleaned.length; i += 1) {
      const row = cleaned[i];
      const basePayload = {
        nome: row.nome,
        ordem: i + 1,
        isWon: row.rule === "WON",
        isLost: row.rule === "LOST"
      };
      if (row.id) {
        await updateStage(row.id, basePayload);
      } else {
        await createStage({
          pipelineId: selectedPipeline.value.id,
          ...basePayload
        });
      }
    }

    for (const stageId of toDelete) {
      await deleteStage(stageId, selectedPipeline.value.id);
    }

    closePipelineConfig();
  } catch (err) {
    pipelineConfigError.value = parseError(err);
  }
}
</script>
