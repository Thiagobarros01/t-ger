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
      </div>
    </div>

    <div class="panel">
      <h3 style="margin-top: 0">Nova oportunidade</h3>
      <form class="form-grid" @submit.prevent="saveDeal">
        <label>
          Cliente
          <select v-model.number="dealForm.clienteId" required>
            <option :value="null">Selecione</option>
            <option v-for="customer in state.customers" :key="customer.id" :value="customer.id">
              {{ customer.corporateName }} ({{ customer.code }})
            </option>
          </select>
        </label>
        <label>
          Empresa (B2B)
          <select v-model.number="dealForm.empresaId">
            <option :value="null">Sem empresa</option>
            <option v-for="company in state.companies" :key="company.id" :value="company.id">
              {{ company.name }}
            </option>
          </select>
        </label>
        <label>
          Vendedor (owner)
          <select v-model.number="dealForm.vendedorId" required>
            <option :value="null">Selecione</option>
            <option v-for="seller in state.sellers" :key="seller.id" :value="seller.id">
              {{ seller.name }}
            </option>
          </select>
        </label>
        <label>
          Etapa inicial
          <select v-model.number="dealForm.stageId" required>
            <option :value="null">Selecione</option>
            <option v-for="stage in selectedStages" :key="stage.id" :value="stage.id">
              {{ stage.nome }}
            </option>
          </select>
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
              :class="{ 'kanban-card--dragging': draggingDealId === deal.id }"
              v-for="deal in stageDeals(stage.id)"
              :key="deal.id"
              draggable="true"
              @dragstart="onDragStart(deal)"
              @dragend="onDragEnd"
            >
            <div class="kanban-card__head">
              <strong>#{{ deal.id }}</strong>
              <span class="tag">{{ deal.status }}</span>
            </div>
            <p>Cliente: {{ customerName(deal.clienteId) }}</p>
            <p>Vendedor: {{ sellerName(deal.vendedorId) }}</p>
            <p>Valor: {{ formatCurrency(deal.valorEstimado) }}</p>
            <div class="actions-row">
              <button type="button" @click="selectDeal(deal)">Gerenciar</button>
            </div>
            </article>
          </TransitionGroup>
          <div class="empty-state" v-if="stageDeals(stage.id).length === 0">Sem oportunidades nesta etapa.</div>
        </div>
      </section>
    </div>

    <div class="panel" v-if="selectedDeal">
      <div class="section-head">
        <div>
          <h3>Gerenciar Deal #{{ selectedDeal.id }}</h3>
          <p>Cliente: {{ customerName(selectedDeal.clienteId) }} | Status: {{ selectedDeal.status }}</p>
        </div>
        <button type="button" class="btn-soft" @click="clearSelectedDeal">Fechar</button>
      </div>
      <div class="form-grid">
        <label>
          Mover para etapa
          <select v-model.number="dealAction.stageId">
            <option v-for="stage in selectedStages" :key="stage.id" :value="stage.id">
              {{ stage.nome }}
            </option>
          </select>
        </label>
        <label v-if="selectedActionStage?.isLost">
          Motivo da perda
          <select v-model.number="dealAction.motivoPerdaId">
            <option :value="null">Selecione</option>
            <option v-for="reason in activeLossReasons" :key="reason.id" :value="reason.id">
              {{ reason.descricao }}
            </option>
          </select>
        </label>
        <div class="full actions-row">
          <button type="button" class="btn-primary" @click="moveStage">Mover etapa</button>
          <button type="button" @click="closeWon">Fechar como ganha</button>
          <button type="button" @click="closeLost">Fechar como perdida</button>
        </div>
      </div>
      <p class="danger-text" v-if="errorMessage">{{ errorMessage }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import PageHeader from "../../components/PageHeader.vue";
import { useCrmData } from "../../composables/useCrmData";

const { state, ensureLoaded, stagesForPipeline, dealsByStage, createDeal, moveDealStage, closeDealWon, closeDealLost } = useCrmData();

const selectedPipelineId = ref(null);
const selectedDealId = ref(null);
const errorMessage = ref("");
const draggingDealId = ref(null);
const dragOverStageId = ref(null);

const dealForm = reactive({
  clienteId: null,
  empresaId: null,
  vendedorId: null,
  stageId: null,
  valorEstimado: "",
  probabilidade: 0,
  dataPrevistaFechamento: ""
});

const dealAction = reactive({
  stageId: null,
  motivoPerdaId: null
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
});

const selectedPipeline = computed(() => state.pipelines.find((item) => item.id === selectedPipelineId.value) ?? null);
const selectedStages = computed(() => stagesForPipeline(selectedPipelineId.value).slice().sort((a, b) => a.ordem - b.ordem));
const activeLossReasons = computed(() => state.lossReasons.filter((item) => item.ativo));
const selectedDeal = computed(() => state.deals.find((deal) => deal.id === selectedDealId.value) ?? null);
const selectedActionStage = computed(() => selectedStages.value.find((stage) => stage.id === dealAction.stageId) ?? null);

function stageDeals(stageId) {
  const list = dealsByStage.value[stageId] ?? [];
  return list.filter((deal) => deal.pipelineId === selectedPipelineId.value);
}

function customerName(id) {
  return state.customers.find((item) => item.id === id)?.corporateName ?? `#${id}`;
}

function sellerName(id) {
  return state.sellers.find((item) => item.id === id)?.name ?? `#${id}`;
}

function formatCurrency(value) {
  if (value === null || value === undefined || value === "") return "-";
  return Number(value).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

async function saveDeal() {
  errorMessage.value = "";
  try {
    if (!selectedPipeline.value) return;
    await createDeal({
      clienteId: dealForm.clienteId,
      empresaId: selectedPipeline.value.tipoNegocio === "B2B" ? dealForm.empresaId : null,
      vendedorId: dealForm.vendedorId,
      tipoNegocio: selectedPipeline.value.tipoNegocio,
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
    dealForm.valorEstimado = "";
    dealForm.probabilidade = 0;
    dealForm.dataPrevistaFechamento = "";
  } catch (err) {
    errorMessage.value = parseError(err);
  }
}

function selectDeal(deal) {
  selectedDealId.value = deal.id;
  dealAction.stageId = deal.stageId;
  dealAction.motivoPerdaId = deal.motivoPerdaId;
  errorMessage.value = "";
}

function clearSelectedDeal() {
  selectedDealId.value = null;
  dealAction.stageId = null;
  dealAction.motivoPerdaId = null;
  errorMessage.value = "";
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
</script>
