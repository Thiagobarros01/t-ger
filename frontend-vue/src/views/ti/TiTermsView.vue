<template>
  <div>
    <PageHeader eyebrow="Gestao da TI" title="Termos e Contratos" subtitle="" />

    <div class="stats-row">
      <div class="stat-card">
        <span>Total</span>
        <strong>{{ totalItems }}</strong>
      </div>
      <div class="stat-card">
        <span>Na pagina (CLT)</span>
        <strong>{{ rows.filter((t) => t.type === "CLT").length }}</strong>
      </div>
      <div class="stat-card">
        <span>Na pagina (Comodato)</span>
        <strong>{{ rows.filter((t) => t.type === "COMODATO").length }}</strong>
      </div>
    </div>

    <div class="panel" v-if="currentUser.profile !== 'OPERADOR'">
      <h3 style="margin-top: 0; display:flex; align-items:center; gap:8px;">
        Cadastrar termo / contrato
        <button
          type="button"
          class="hint-icon"
          title="Termo com nome padrao 'Termo de Responsabilidade'. O cadastro define tipo (CLT/Comodato), vinculacao, status e caminho do documento."
          aria-label="Ajuda sobre cadastro de termos"
        >
          ?
        </button>
      </h3>
      <form class="form-grid" @submit.prevent="saveTerm">
        <label>
          <span class="label-inline">
            Termo (padrao)
            <button
              type="button"
              class="hint-icon"
              title="Nome padrao do documento. O tipo e a vinculacao sao os dados que mudam no cadastro."
              aria-label="Ajuda sobre termo padrao"
            >
              ?
            </button>
          </span>
          <input value="Termo de Responsabilidade" disabled />
        </label>
        <label>
          Tipo
          <select v-model="form.type">
            <option value="CLT">CLT</option>
            <option value="COMODATO">Comodato</option>
          </select>
        </label>
        <label>
          Vinculado a (usuario)
          <select v-model="form.linkedUserName">
            <option value="">Selecione</option>
            <option v-for="user in sessionState.allUsers" :key="user.id" :value="user.name">{{ user.name }}</option>
          </select>
        </label>
        <label>
          Item vinculado
          <select v-model.number="form.linkedAssetId">
            <option :value="null">Selecione</option>
            <option v-for="asset in availableAssetsForCreate" :key="asset.id" :value="asset.id">
              {{ buildAssetLabel(asset) }}
            </option>
          </select>
        </label>
        <label>
          Data de inicio
          <input v-model="form.startDate" type="date" />
        </label>
        <label>
          Status
          <select v-model="form.status">
            <option value="Ativo">Ativo</option>
            <option value="Devolvido">Devolvido</option>
            <option value="Concluido">Concluido</option>
            <option value="Revogado">Revogado</option>
          </select>
        </label>
        <label>
          Caminho do documento
          <input v-model="form.documentPath" placeholder="C:/documentos/ti/comodato/arquivo.pdf" />
        </label>
        <div class="full actions-row">
          <button class="btn-primary" type="submit">Salvar termo</button>
        </div>
      </form>
    </div>

    <div class="table-panel">
      <div class="section-head">
        <h3 style="margin-top: 0">Lista paginada</h3>
        <span class="tag">{{ rows.length }} exibidos de {{ totalItems }}</span>
      </div>

      <div class="filters-toolbar filters-toolbar--enhanced">
        <div class="filters-toolbar__head">
          <strong>Filtros</strong>
          <span class="muted-inline">Tipo, vinculado, item, status e documento.</span>
        </div>
        <div class="filters-grid filters-grid--4">
          <label>
            Tipo
            <select v-model="filters.type">
              <option value="">Todos</option>
              <option value="CLT">CLT</option>
              <option value="COMODATO">COMODATO</option>
            </select>
          </label>
          <label>
            Vinculado a
            <input v-model="filters.linkedUserName" placeholder="Ex.: joao suporte" />
          </label>
          <label>
            Item vinculado
            <select v-model.number="filters.linkedAssetId">
              <option :value="null">Todos</option>
              <option v-for="asset in assetOptions" :key="asset.id" :value="asset.id">{{ buildAssetLabel(asset) }}</option>
            </select>
          </label>
          <label>
            Status
            <input v-model="filters.status" placeholder="Ex.: ativo" />
          </label>
          <label>
            Caminho documento
            <input v-model="filters.documentPath" placeholder="Ex.: comodato" />
          </label>
          <label style="align-self: end">
            <span class="label-inline">
              <input type="checkbox" v-model="filters.showInactives" />
              Mostrar inativados
            </span>
          </label>
        </div>
        <div class="filters-actions">
          <button type="button" class="btn-soft" @click="clearFilters">Limpar filtros</button>
        </div>
      </div>

      <div v-if="loading" class="empty-state" style="margin-bottom: 10px">Carregando termos...</div>
      <div v-else-if="loadError" class="empty-state" style="margin-bottom: 10px">{{ loadError }}</div>
      <div v-else-if="rows.length === 0" class="empty-state" style="margin-bottom: 10px">Nenhum termo encontrado.</div>
      <div class="table-scroll" v-else>
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Termo</th>
              <th>Descricao</th>
              <th>Tipo</th>
              <th>Vinculado a</th>
              <th>Item vinculado</th>
              <th>Inicio</th>
              <th>Status</th>
              <th>Caminho do documento</th>
              <th>Acoes</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="term in rows" :key="term.id">
              <td>#{{ term.id }}</td>
              <td>{{ term.defaultTermName }}</td>
              <td>{{ term.description }}</td>
              <td><span class="tag">{{ term.type }}</span></td>
              <td>{{ term.linkedUserName }}</td>
              <td>{{ term.linkedItemDescription || "-" }}</td>
              <td>{{ formatDate(term.startDate) }}</td>
              <td><span class="tag" :class="{ 'danger-tag': term.status !== 'Ativo' }">{{ term.status }}</span></td>
              <td><code>{{ term.documentPath || "-" }}</code></td>
              <td>
                <div class="actions-row actions-row--compact">
                  <button type="button" class="btn-action btn-action--edit" @click="editTerm(term)">
                    <span class="btn-action__icon">E</span>
                    <span>Editar</span>
                  </button>
                  <button type="button" class="btn-soft" @click="inactivateTermRow(term)" :disabled="term.status === 'Revogado'">Revogar</button>
                  <button type="button" class="btn-action btn-action--remove" @click="deleteTermRow(term)">
                    <span class="btn-action__icon">X</span>
                    <span>Remover</span>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <PaginationBar
        :page="page"
        :page-size="pageSize"
        :total-pages="Math.max(totalPages, 1)"
        :total-items="totalItems"
        @update:page="setPage"
        @update:pageSize="setPageSize"
      />
    </div>

    <div class="modal-overlay" v-if="editingTerm || termToRemove" @click.self="closeTermActions">
      <div class="modal-card modal-card--small">
        <div class="section-head">
          <div>
            <h3>{{ editingTerm ? "Editar termo" : "Confirmar remocao de termo" }}</h3>
            <p v-if="editingTerm">Atualize os dados do termo.</p>
            <p v-else>Remocao fisica do termo selecionado.</p>
          </div>
          <button type="button" class="btn-soft" @click="closeTermActions">Fechar</button>
        </div>

        <form v-if="editingTerm" class="form-grid" @submit.prevent="submitTermEdit">
          <label>
            Tipo
            <select v-model="editTermForm.type">
              <option value="CLT">CLT</option>
              <option value="COMODATO">Comodato</option>
            </select>
          </label>
          <label>
            Vinculado a
            <input v-model="editTermForm.linkedUserName" />
          </label>
          <label>
            Item vinculado
            <select v-model.number="editTermForm.linkedAssetId">
              <option :value="null">Sem item</option>
              <option v-for="asset in availableAssetsForEdit" :key="asset.id" :value="asset.id">{{ buildAssetLabel(asset) }}</option>
            </select>
          </label>
          <label>
            Data de inicio
            <input v-model="editTermForm.startDate" type="date" />
          </label>
          <label>
            Status
            <select v-model="editTermForm.status">
              <option value="Ativo">Ativo</option>
              <option value="Devolvido">Devolvido</option>
              <option value="Concluido">Concluido</option>
              <option value="Inativo">Inativo</option>
              <option value="Revogado">Revogado</option>
            </select>
          </label>
          <label class="full">
            Caminho do documento
            <input v-model="editTermForm.documentPath" />
          </label>
          <div class="full actions-row">
            <button class="btn-primary" type="submit">Salvar alteracoes</button>
            <button type="button" @click="closeTermActions">Cancelar</button>
          </div>
        </form>

        <div v-else-if="termToRemove" class="crud-box">
          <p><strong>{{ termToRemove.defaultTermName }}</strong></p>
          <p class="muted">{{ termToRemove.type }} | {{ termToRemove.linkedUserName }}</p>
          <div class="actions-row">
            <button type="button" class="btn-soft" @click="closeTermActions">Cancelar</button>
            <button type="button" @click="confirmTermRemoval">Remover</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from "vue";
import PageHeader from "../../components/PageHeader.vue";
import PaginationBar from "../../components/PaginationBar.vue";
import { useSession } from "../../composables/useSession";
import { useTiData } from "../../composables/useTiData";
import { apiRequest } from "../../services/api";

const { currentUser, state: sessionState, ensureLoaded: ensureUsersLoaded } = useSession();
const { addTerm, updateTerm, removeTerm, ensureLoaded: ensureTiLoaded } = useTiData();
const editingTerm = ref(null);
const termToRemove = ref(null);
const assetOptions = ref([]);
const rows = ref([]);
const totalItems = ref(0);
const totalPages = ref(1);
const page = ref(1);
const pageSize = ref(10);
const loading = ref(false);
const loadError = ref("");
let filtersDebounce = null;

const filters = reactive({
  type: "",
  linkedUserName: "",
  linkedAssetId: null,
  linkedItemDescription: "",
  status: "",
  documentPath: "",
  showInactives: false
});

const editTermForm = reactive({
  type: "COMODATO",
  linkedUserName: "",
  linkedAssetId: null,
  linkedItemDescription: "",
  startDate: "",
  status: "Ativo",
  documentPath: ""
});

const form = reactive({
  type: "COMODATO",
  linkedUserName: "",
  linkedAssetId: null,
  linkedItemDescription: "",
  startDate: "",
  status: "Ativo",
  documentPath: ""
});
const availableAssetsForCreate = computed(() =>
  (assetOptions.value ?? []).filter((asset) => asset.active && asset.status === "DISPONIVEL")
);
const availableAssetsForEdit = computed(() =>
  (assetOptions.value ?? []).filter(
    (asset) =>
      (asset.active && asset.status === "DISPONIVEL") ||
      (editTermForm.linkedAssetId != null && asset.id === editTermForm.linkedAssetId)
  )
);

onMounted(async () => {
  await ensureUsersLoaded();
  await ensureTiLoaded();
  await loadAssetOptions();
  await loadTerms();
});

onBeforeUnmount(() => {
  if (filtersDebounce) clearTimeout(filtersDebounce);
});

watch([page, pageSize], () => {
  loadTerms();
});

watch(
  () => [filters.type, filters.linkedUserName, filters.linkedAssetId, filters.linkedItemDescription, filters.status, filters.documentPath, filters.showInactives],
  () => {
    if (page.value !== 1) {
      page.value = 1;
      return;
    }
    if (filtersDebounce) clearTimeout(filtersDebounce);
    filtersDebounce = setTimeout(() => loadTerms(), 300);
  }
);

async function saveTerm() {
  if (!form.linkedUserName) return;
  if (form.status === "Ativo" && !form.linkedAssetId) return;
  await addTerm({
    type: form.type,
    linkedUserName: form.linkedUserName,
    linkedAssetId: form.linkedAssetId ?? null,
    linkedItemDescription: null,
    startDate: form.startDate || new Date().toISOString().slice(0, 10),
    status: form.status,
    documentPath: form.documentPath.trim()
  });
  form.type = "COMODATO";
  form.linkedUserName = "";
  form.linkedAssetId = null;
  form.linkedItemDescription = "";
  form.startDate = "";
  form.status = "Ativo";
  form.documentPath = "";
  await loadAssetOptions();
  await loadTerms();
}

function formatDate(date) {
  if (!date) return "-";
  return new Date(`${date}T00:00:00`).toLocaleDateString("pt-BR");
}

function editTerm(term) {
  editingTerm.value = term;
  termToRemove.value = null;
  editTermForm.type = term.type ?? "COMODATO";
  editTermForm.linkedUserName = term.linkedUserName ?? "";
  editTermForm.linkedAssetId = term.linkedAssetId ?? null;
  editTermForm.linkedItemDescription = term.linkedItemDescription ?? "";
  editTermForm.startDate = term.startDate ?? "";
  editTermForm.status = term.status ?? "Ativo";
  editTermForm.documentPath = term.documentPath ?? "";
}

function deleteTermRow(term) {
  termToRemove.value = term;
  editingTerm.value = null;
}

async function submitTermEdit() {
  if (!editingTerm.value || !editTermForm.linkedUserName.trim()) return;
  if (editTermForm.status === "Ativo" && !editTermForm.linkedAssetId) return;
  await updateTerm(editingTerm.value.id, {
    type: editTermForm.type,
    linkedUserName: editTermForm.linkedUserName,
    linkedAssetId: editTermForm.linkedAssetId ?? null,
    linkedItemDescription: null,
    startDate: editTermForm.startDate,
    status: editTermForm.status,
    documentPath: editTermForm.documentPath
  });
  await loadAssetOptions();
  await loadTerms();
  closeTermActions();
}

async function inactivateTermRow(term) {
  await apiRequest(`/api/ti/terms-contracts/${term.id}/inactivate`, { method: "PATCH" });
  await loadAssetOptions();
  await loadTerms();
  if (editingTerm.value?.id === term.id) {
    closeTermActions();
  }
}

async function confirmTermRemoval() {
  if (!termToRemove.value) return;
  const removedId = termToRemove.value.id;
  await removeTerm(removedId);
  rows.value = rows.value.filter((item) => item.id !== removedId);
  totalItems.value = Math.max(0, totalItems.value - 1);
  await loadAssetOptions();
  if (rows.value.length === 0 && page.value > 1) {
    page.value = page.value - 1;
  } else {
    await loadTerms();
  }
  closeTermActions();
}

function closeTermActions() {
  editingTerm.value = null;
  termToRemove.value = null;
}

function clearFilters() {
  filters.type = "";
  filters.linkedUserName = "";
  filters.linkedAssetId = null;
  filters.linkedItemDescription = "";
  filters.status = "";
  filters.documentPath = "";
  filters.showInactives = false;
  if (page.value !== 1) page.value = 1;
  else loadTerms();
}

function setPage(nextPage) {
  page.value = nextPage;
}

function setPageSize(nextSize) {
  pageSize.value = Number(nextSize) || 10;
  page.value = 1;
}

async function loadTerms() {
  loading.value = true;
  loadError.value = "";
  try {
    const params = new URLSearchParams({
      page: String(page.value),
      pageSize: String(pageSize.value)
    });
    if (filters.type) params.set("type", filters.type);
    if (filters.linkedUserName.trim()) params.set("linkedUserName", filters.linkedUserName.trim());
    if (filters.linkedAssetId) params.set("linkedAssetId", String(filters.linkedAssetId));
    if (filters.linkedItemDescription.trim()) params.set("linkedItemDescription", filters.linkedItemDescription.trim());
    if (filters.status.trim()) params.set("status", filters.status.trim());
    if (filters.documentPath.trim()) params.set("documentPath", filters.documentPath.trim());
    if (filters.showInactives) params.set("showInactives", "true");
    const response = await apiRequest(`/api/ti/terms-contracts/paged?${params.toString()}`);
    rows.value = response.items ?? [];
    totalItems.value = response.totalItems ?? 0;
    totalPages.value = Math.max(response.totalPages ?? 1, 1);
  } catch {
    rows.value = [];
    totalItems.value = 0;
    totalPages.value = 1;
    loadError.value = "Nao foi possivel carregar os termos.";
  } finally {
    loading.value = false;
  }
}

async function loadAssetOptions() {
  assetOptions.value = await apiRequest("/api/ti/assets");
}

function buildAssetLabel(asset) {
  const code = asset.internalCode || `ID ${asset.id}`;
  return `${code} - ${asset.assetType}`;
}
</script>
