<template>
  <div>
    <PageHeader eyebrow="Gestao da TI" title="Equipamentos" subtitle="">
      <template #actions>
        <button class="btn-soft" @click="resetForm">Novo ativo</button>
      </template>
    </PageHeader>

    <details class="panel collapsible-panel" v-if="currentUser.profile !== 'OPERADOR'" open>
      <summary class="collapsible-panel__summary">
        <span style="display:flex; align-items:center; gap:8px;">
          Cadastro rapido
          <button
            type="button"
            class="hint-icon"
            title="Cadastro de ativos com empresa, vinculo a usuario/termo, historico de transferencias e IP (DHCP ou estatico)."
            aria-label="Ajuda sobre cadastro de ativos"
            @click.stop
          >
            ?
          </button>
        </span>
        <span class="nav-caret"></span>
      </summary>

      <form class="form-grid" @submit.prevent="saveAsset" style="margin-top: 10px;">
        <label>
          Codigo interno
          <input value="Gerado automaticamente ao salvar" disabled />
        </label>

        <label>
          Empresa
          <div class="field-with-action">
            <select v-model="form.company" @change="syncSelectedCompanyErp">
              <option value="">Selecione</option>
              <option v-for="company in masterData.companies" :key="company.id" :value="company.name">
                {{ company.name }}{{ company.erpCode ? ` (ERP: ${company.erpCode})` : "" }}
              </option>
            </select>
            <button type="button" class="field-add-btn" title="Cadastrar empresa rapido" @click="ui.showQuickCompany = !ui.showQuickCompany">+</button>
          </div>
        </label>

        <div class="full quick-inline-card" v-if="ui.showQuickCompany">
          <div class="quick-inline-card__head">
            <strong>Cadastro rapido de empresa</strong>
            <button type="button" @click="ui.showQuickCompany = false">Fechar</button>
          </div>
          <div class="actions-row">
            <input v-model="quickCompany.name" placeholder="Nome da empresa" style="max-width: 280px" />
            <input v-model="quickCompany.erpCode" placeholder="Codigo ERP" style="max-width: 180px" />
            <button type="button" class="btn-primary" @click="createCompanyQuick">Salvar e selecionar</button>
          </div>
        </div>

        <label>
          Tipo de ativo
          <select v-model="form.assetType" required>
            <option v-for="type in assetTypes" :key="type" :value="type">{{ type }}</option>
          </select>
        </label>

        <label>
          Departamento / Setor
          <input v-model="form.department" />
        </label>

        <label>
          Status operacional
          <input :value="createComputedStatusLabel" disabled />
        </label>

        <label>
          Condicao do equipamento
          <select v-model="form.equipmentCondition">
            <option v-for="condition in equipmentConditions" :key="condition" :value="condition">{{ condition }}</option>
          </select>
        </label>

        <label>
          Marca
          <input v-model="form.brand" />
        </label>

        <label>
          Modelo
          <input v-model="form.model" />
        </label>

        <label>
          Numero de serie
          <input v-model="form.serialNumber" />
        </label>

        <label>
          Patrimonio
          <input v-model="form.patrimony" />
        </label>

        <div class="full empty-state">
          Vínculo de responsável e termo é controlado em <strong>Termos e Contratos</strong>.
        </div>

        <label>
          Modo de IP
          <select v-model="form.ipMode">
            <option value="DHCP">DHCP</option>
            <option value="ESTATICO">ESTATICO</option>
          </select>
        </label>

        <label>
          IP (DHCP ou estatico)
          <input v-model="form.ipAddress" placeholder="Ex.: 192.168.0.45" />
        </label>

        <label v-if="showImeiField">
          IMEI (celular/tablet)
          <input v-model="form.imei" placeholder="Somente para celular/tablet" />
        </label>

        <label class="full">
          Descricao detalhada
          <textarea v-model="form.detailedDescription" />
        </label>

        <label class="full">
          Historico de transferencias (uma linha por item)
          <textarea v-model="transferHistoryText" />
        </label>

        <div class="full">
          <div class="actions-row">
            <strong>Campos extras</strong>
            <button type="button" @click="addExtraField">+ Adicionar coluna</button>
          </div>
          <div class="field-list" v-if="extraFieldEntries.length">
            <div class="field-list-item" v-for="(entry, index) in extraFieldEntries" :key="index">
              <input v-model="entry.key" placeholder="Nome do campo" />
              <input v-model="entry.value" placeholder="Valor" />
              <button type="button" @click="removeExtraField(index)">Remover</button>
            </div>
          </div>
        </div>

        <div class="full actions-row">
          <button class="btn-primary" type="submit">Salvar ativo</button>
          <span class="muted" v-if="masterData.companies.length === 0">Cadastre empresas em Configuracoes > Parametros.</span>
        </div>
      </form>
    </details>

    <div class="table-panel">
      <div class="section-head">
        <h3 style="margin-top: 0">Ativos cadastrados</h3>
        <button type="button" class="btn-soft" @click="openSummaryModal">Resumo rapido</button>
      </div>
      <div class="filters-toolbar filters-toolbar--enhanced" style="margin-bottom: 10px">
        <div class="filters-toolbar__head">
          <strong>Filtros</strong>
          <span class="muted-inline">Tipo, responsavel e departamento.</span>
        </div>
        <div class="filters-grid filters-grid--4">
          <label>
            Busca rapida
            <input v-model="filters.search" placeholder="Digite codigo, IP ou patrimonio" />
          </label>
          <label>
            Filtrar por tipo
            <select v-model="filters.type">
              <option value="">Todos</option>
              <option v-for="type in assetTypes" :key="type" :value="type">{{ type }}</option>
            </select>
          </label>
          <label>
            Filtrar por responsavel
            <select v-model="filters.responsible">
              <option value="">Todos</option>
              <option v-for="user in sessionState.allUsers" :key="user.id" :value="user.name">{{ user.name }}</option>
            </select>
          </label>
          <label>
            Filtrar por departamento / setor
            <input v-model="filters.department" placeholder="Ex.: TI, Comercial" />
          </label>
          <label style="align-self: end">
            <span class="label-inline">
              <input type="checkbox" v-model="filters.showInactives" />
              Mostrar inativados
            </span>
          </label>
          <div class="actions-row" style="align-self: end">
            <button type="button" class="btn-soft" @click="clearFilters">Limpar filtros</button>
          </div>
        </div>
      </div>

      <p class="muted" style="margin-top: -4px; margin-bottom: 10px" v-if="currentUser.profile === 'OPERADOR'">
        Exibindo somente ativos sob sua responsabilidade.
      </p>

      <div v-if="loadingAssets" class="empty-state" style="margin-bottom: 10px">Carregando ativos...</div>
      <div v-else-if="loadAssetsError" class="empty-state" style="margin-bottom: 10px">{{ loadAssetsError }}</div>
      <div v-else-if="rows.length === 0" class="empty-state" style="margin-bottom: 10px">Nenhum ativo encontrado.</div>

      <div class="table-scroll" v-else>
        <table>
          <thead>
            <tr>
              <th>Codigo</th>
              <th>Empresa</th>
              <th>Empresa ERP</th>
              <th>Tipo</th>
              <th>Departamento</th>
              <th>Marca/Modelo</th>
              <th>Status</th>
              <th>Condicao</th>
              <th>Responsavel</th>
              <th>Termo</th>
              <th>IP</th>
              <th>IMEI</th>
              <th>Historico</th>
              <th>Acoes</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="asset in rows" :key="asset.id">
              <td>{{ asset.internalCode }}</td>
              <td>{{ asset.company || "-" }}</td>
              <td>{{ asset.companyErpCode || "-" }}</td>
              <td>{{ asset.assetType }}</td>
              <td>{{ asset.department }}</td>
              <td>{{ asset.brand }} / {{ asset.model }}</td>
              <td>
                <span class="tag" :class="{ 'danger-tag': !asset.active }">{{ asset.status }}</span>
              </td>
              <td>{{ asset.equipmentCondition || "-" }}</td>
              <td>{{ asset.responsibleUserName || "-" }}</td>
              <td>{{ asset.linkedTermTitle || "-" }}</td>
              <td>{{ asset.ipMode }} - {{ asset.ipAddress || "-" }}</td>
              <td>{{ asset.imei || "-" }}</td>
              <td>
                <button type="button" class="btn-soft btn-action btn-action--history" @click="openHistory(asset)">
                  <span class="btn-action__icon">H</span>
                  <span>Ver historico</span>
                </button>
              </td>
              <td>
                <details class="row-actions-menu">
                  <summary class="row-actions-menu__trigger" aria-label="Abrir acoes">
                    <span class="row-actions-menu__trigger-icon">
                      <i></i><i></i><i></i>
                    </span>
                  </summary>
                  <div class="row-actions-menu__panel">
                    <button type="button" class="menu-action-btn" @click="editAsset(asset)">
                      <span class="btn-action__icon">E</span>
                      <span>Editar</span>
                    </button>
                    <button v-if="asset.active" type="button" class="menu-action-btn" @click="openReturnModal(asset)">
                      Devolucao
                    </button>
                    <button v-else type="button" class="menu-action-btn" @click="toggleAssetActive(asset)">
                      Reativar
                    </button>
                  </div>
                </details>
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

    <div class="modal-overlay" v-if="editingAsset || assetToRemove" @click.self="closeAssetActions">
      <div class="modal-card">
        <div class="section-head">
          <div>
            <h3>{{ editingAsset ? "Editar ativo" : "Confirmar remocao de ativo" }}</h3>
            <p v-if="editingAsset">Ajuste os dados principais do ativo.</p>
            <p v-else>Remocao fisica do ativo selecionado.</p>
          </div>
          <button type="button" class="btn-soft" @click="closeAssetActions">Fechar</button>
        </div>

        <form v-if="editingAsset" class="form-grid" @submit.prevent="submitAssetEdit">
          <label>
            Empresa
            <input v-model="editAssetForm.company" />
          </label>
          <label>
            Empresa ERP
            <input v-model="editAssetForm.companyErpCode" />
          </label>
          <label>
            Tipo de ativo
            <select v-model="editAssetForm.assetType">
              <option v-for="type in assetTypes" :key="type" :value="type">{{ type }}</option>
            </select>
          </label>
          <label>
            Departamento / Setor
            <input v-model="editAssetForm.department" />
          </label>
          <label>
            Marca
            <input v-model="editAssetForm.brand" />
          </label>
          <label>
            Modelo
            <input v-model="editAssetForm.model" />
          </label>
          <label>
            Numero de serie
            <input v-model="editAssetForm.serialNumber" />
          </label>
          <label>
            Patrimonio
            <input v-model="editAssetForm.patrimony" />
          </label>
          <label>
            Status operacional
            <input :value="editingAsset?.status || '-'" disabled />
          </label>
          <label>
            Condicao do equipamento
            <select v-model="editAssetForm.equipmentCondition">
              <option v-for="condition in equipmentConditions" :key="condition" :value="condition">{{ condition }}</option>
            </select>
          </label>
          <label>
            Responsavel atual
            <input :value="editingAsset?.responsibleUserName || 'Sem responsavel'" disabled />
          </label>
          <label>
            Termo atual
            <input :value="editingAsset?.linkedTermTitle || 'Sem termo'" disabled />
          </label>
          <label>
            Modo IP
            <select v-model="editAssetForm.ipMode">
              <option value="DHCP">DHCP</option>
              <option value="ESTATICO">ESTATICO</option>
            </select>
          </label>
          <label>
            IP
            <input v-model="editAssetForm.ipAddress" />
          </label>
          <label>
            IMEI
            <input v-model="editAssetForm.imei" />
          </label>
          <label class="full">
            Descricao detalhada
            <textarea v-model="editAssetForm.detailedDescription" />
          </label>
          <label class="full">
            Historico (uma linha por item)
            <textarea v-model="editAssetForm.transferHistoryText" />
          </label>
          <div class="full actions-row">
            <button class="btn-primary" type="submit">Salvar alteracoes</button>
            <button type="button" @click="closeAssetActions">Cancelar</button>
          </div>
        </form>

        <div v-else-if="assetToRemove" class="crud-box">
          <p><strong>{{ assetToRemove.internalCode }}</strong> - {{ assetToRemove.assetType }}</p>
          <p class="muted">{{ assetToRemove.company || "-" }} | {{ assetToRemove.department || "-" }}</p>
          <div class="actions-row">
            <button type="button" class="btn-soft" @click="closeAssetActions">Cancelar</button>
            <button type="button" @click="confirmAssetRemoval">Remover</button>
          </div>
        </div>
      </div>
    </div>

    <div class="modal-overlay" v-if="returnModalOpen" @click.self="closeReturnModal">
      <div class="modal-card modal-card--small">
        <div class="section-head">
          <div>
            <h3>Registrar devolucao</h3>
            <p>Ativo {{ returnAssetTarget?.internalCode }} sera liberado para disponivel.</p>
          </div>
          <button type="button" class="btn-soft" @click="closeReturnModal">Fechar</button>
        </div>
        <div class="form-grid">
          <label>
            Condicao do equipamento
            <select v-model="returnEquipmentCondition">
              <option v-for="condition in equipmentConditions" :key="condition" :value="condition">{{ condition }}</option>
            </select>
          </label>
        </div>
        <div class="actions-row" style="margin-top: 10px">
          <button type="button" class="btn-soft" @click="closeReturnModal">Cancelar</button>
          <button type="button" class="btn-primary" @click="confirmAssetReturn">Confirmar devolucao</button>
        </div>
      </div>
    </div>

    <div class="modal-overlay" v-if="historyModalOpen" @click.self="closeHistoryModal">
      <div class="modal-card">
        <div class="section-head">
          <div>
            <h3>Historico do ativo {{ historyAsset?.internalCode }}</h3>
            <p>
              Atual: <strong>{{ historyAsset?.responsibleUserName || "Sem responsavel" }}</strong>
              | Status: <strong>{{ historyAsset?.status || "-" }}</strong>
            </p>
          </div>
          <button type="button" class="btn-soft" @click="closeHistoryModal">Fechar</button>
        </div>

        <div class="form-grid">
          <label>
            Responsavel
            <input v-model="historyFilters.responsible" placeholder="Filtrar por nome" />
          </label>
          <label>
            Status
            <input v-model="historyFilters.status" placeholder="Filtrar por status" />
          </label>
          <label>
            Evento
            <select v-model="historyFilters.eventType">
              <option value="">Todos</option>
              <option value="CADASTRO">Cadastro</option>
              <option value="ENTREGA">Entrega</option>
              <option value="DEVOLUCAO">Devolucao</option>
              <option value="TRANSFERENCIA">Transferencia</option>
              <option value="ATUALIZACAO">Atualizacao</option>
            </select>
          </label>
          <div class="actions-row" style="align-self: end">
            <button type="button" @click="clearHistoryFilters">Limpar</button>
          </div>
        </div>

        <div class="table-scroll" v-if="historyRows.length > 0" style="margin-top: 10px">
          <table>
            <thead>
              <tr>
                <th>Quando</th>
                <th>Evento</th>
                <th>Responsavel</th>
                <th>Status</th>
                <th>Termo</th>
                <th>Alterado por</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in historyRows" :key="item.id">
                <td>{{ formatDateTime(item.changedAt) }}</td>
                <td><span class="tag">{{ item.eventType }}</span></td>
                <td>{{ item.previousResponsibleUserName || "-" }} -> {{ item.newResponsibleUserName || "-" }}</td>
                <td>{{ item.previousStatus || "-" }} -> {{ item.newStatus || "-" }}</td>
                <td>
                  {{ item.previousTermTitle || "-" }} (id: {{ item.previousTermId ?? "-" }}) ->
                  {{ item.newTermTitle || "-" }} (id: {{ item.newTermId ?? "-" }})
                </td>
                <td>{{ item.changedByName || "-" }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="empty-state" v-else-if="loadingHistory" style="margin-top: 10px">Carregando historico...</div>
        <div class="empty-state" v-else-if="historyError" style="margin-top: 10px">{{ historyError }}</div>
        <div class="empty-state" v-else style="margin-top: 10px">Nenhum evento de historico encontrado.</div>

        <PaginationBar
          :page="historyPage"
          :page-size="historyPageSize"
          :total-pages="Math.max(historyTotalPages, 1)"
          :total-items="historyTotalItems"
          @update:page="setHistoryPage"
          @update:pageSize="setHistoryPageSize"
        />
      </div>
    </div>

    <div class="modal-overlay" v-if="summaryModalOpen" @click.self="summaryModalOpen = false">
      <div class="modal-card modal-card--small">
        <div class="section-head">
          <div>
            <h3>Resumo de ativos</h3>
            <p>Visao rapida do total cadastrado e da situacao operacional.</p>
          </div>
          <button type="button" class="btn-soft" @click="summaryModalOpen = false">Fechar</button>
        </div>

        <div class="stats-row" v-if="!loadingSummary && !summaryError">
          <div class="stat-card">
            <span>Total cadastrados</span>
            <strong>{{ summary.total }}</strong>
          </div>
          <div class="stat-card">
            <span>Disponiveis</span>
            <strong>{{ summary.available }}</strong>
          </div>
          <div class="stat-card">
            <span>Em uso</span>
            <strong>{{ summary.inUse }}</strong>
          </div>
          <div class="stat-card">
            <span>Devolvidos</span>
            <strong>{{ summary.returned }}</strong>
          </div>
        </div>
        <div class="empty-state" v-else-if="loadingSummary">Carregando resumo...</div>
        <div class="empty-state" v-else>{{ summaryError }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref, onBeforeUnmount, onMounted, watch } from "vue";
import PageHeader from "../../components/PageHeader.vue";
import PaginationBar from "../../components/PaginationBar.vue";
import { useSession } from "../../composables/useSession";
import { useTiData } from "../../composables/useTiData";
import { useMasterData } from "../../composables/useMasterData";
import { apiRequest } from "../../services/api";

const { currentUser, ensureLoaded: ensureUsersLoaded, state: sessionState } = useSession();
const { addAsset, updateAsset, removeAsset, ensureLoaded: ensureTiLoaded } = useTiData();
const { state: masterData, ensureLoaded: ensureMasterDataLoaded, addCompany } = useMasterData();

const assetTypes = ["NOTEBOOK", "DESKTOP", "MONITOR", "IMPRESSORA", "ROTEADOR", "CELULAR", "TABLET", "OUTRO"];
const equipmentConditions = ["NOVO", "USADO", "DANIFICADO", "VELHO"];

const form = reactive({
  company: "",
  companyErpCode: "",
  assetType: "NOTEBOOK",
  department: "TI",
  brand: "",
  model: "",
  serialNumber: "",
  patrimony: "",
  detailedDescription: "",
  ipMode: "DHCP",
  ipAddress: "",
  imei: "",
  equipmentCondition: "USADO"
});

const filters = reactive({
  search: "",
  type: "",
  responsible: "",
  department: "",
  showInactives: false
});
const rows = ref([]);
const totalItems = ref(0);
const totalPages = ref(1);
const page = ref(1);
const pageSize = ref(10);
const loadingAssets = ref(false);
const loadAssetsError = ref("");
let filtersDebounce = null;
let historyDebounce = null;

const ui = reactive({
  showQuickCompany: false
});

const quickCompany = reactive({ name: "", erpCode: "" });
const editingAsset = ref(null);
const assetToRemove = ref(null);
const editAssetForm = reactive({
  company: "",
  companyErpCode: "",
  assetType: "NOTEBOOK",
  department: "",
  brand: "",
  model: "",
  serialNumber: "",
  patrimony: "",
  ipMode: "DHCP",
  ipAddress: "",
  imei: "",
  detailedDescription: "",
  transferHistoryText: "",
  equipmentCondition: "USADO"
});

const extraFieldEntries = ref([]);
const transferHistoryText = ref("");
const showImeiField = computed(() => ["CELULAR", "TABLET"].includes(form.assetType));
const historyModalOpen = ref(false);
const historyAsset = ref(null);
const historyRows = ref([]);
const historyTotalItems = ref(0);
const historyTotalPages = ref(1);
const historyPage = ref(1);
const historyPageSize = ref(10);
const loadingHistory = ref(false);
const historyError = ref("");
const historyFilters = reactive({
  responsible: "",
  status: "",
  eventType: ""
});
const summaryModalOpen = ref(false);
const loadingSummary = ref(false);
const summaryError = ref("");
const summary = reactive({
  total: 0,
  available: 0,
  inUse: 0,
  returned: 0
});
const createComputedStatusLabel = computed(() => "DISPONIVEL");
const returnModalOpen = ref(false);
const returnAssetTarget = ref(null);
const returnEquipmentCondition = ref("USADO");

onMounted(() => {
  ensureUsersLoaded();
  ensureMasterDataLoaded().then(() => applyDefaultCompany());
  ensureTiLoaded().then(() => loadAssets());
});

onBeforeUnmount(() => {
  if (filtersDebounce) clearTimeout(filtersDebounce);
  if (historyDebounce) clearTimeout(historyDebounce);
});

watch([page, pageSize], () => {
  loadAssets();
});

watch(
  () => [filters.search, filters.type, filters.responsible, filters.department, filters.showInactives],
  () => {
    if (page.value !== 1) {
      page.value = 1;
      return;
    }
    if (filtersDebounce) clearTimeout(filtersDebounce);
    filtersDebounce = setTimeout(() => loadAssets(), 300);
  }
);

watch([historyPage, historyPageSize], () => {
  if (historyModalOpen.value) loadAssetHistory();
});

watch(
  () => [historyFilters.responsible, historyFilters.status, historyFilters.eventType],
  () => {
    if (!historyModalOpen.value) return;
    if (historyPage.value !== 1) {
      historyPage.value = 1;
      return;
    }
    if (historyDebounce) clearTimeout(historyDebounce);
    historyDebounce = setTimeout(() => loadAssetHistory(), 300);
  }
);

function addExtraField() {
  extraFieldEntries.value.push({ key: "", value: "" });
}

function removeExtraField(index) {
  extraFieldEntries.value.splice(index, 1);
}

function clearFilters() {
  filters.search = "";
  filters.type = "";
  filters.responsible = "";
  filters.department = "";
  filters.showInactives = false;
  if (page.value !== 1) page.value = 1;
  else loadAssets();
}

function applyDefaultCompany() {
  if (masterData.companies.length === 1) {
    form.company = masterData.companies[0].name;
    form.companyErpCode = masterData.companies[0].erpCode ?? "";
  }
}

function syncSelectedCompanyErp() {
  const company = masterData.companies.find((item) => item.name === form.company);
  form.companyErpCode = company?.erpCode ?? "";
}

function resetForm() {
  form.company = "";
  form.assetType = "NOTEBOOK";
  form.department = "TI";
  form.brand = "";
  form.model = "";
  form.serialNumber = "";
  form.patrimony = "";
  form.detailedDescription = "";
  form.ipMode = "DHCP";
  form.ipAddress = "";
  form.imei = "";
  form.equipmentCondition = "USADO";
  form.companyErpCode = "";
  transferHistoryText.value = "";
  extraFieldEntries.value = [];
}

async function createCompanyQuick() {
  const created = await addCompany({ name: quickCompany.name, erpCode: quickCompany.erpCode });
  if (!created) return;
  form.company = created.name;
  form.companyErpCode = created.erpCode || "";
  quickCompany.name = "";
  quickCompany.erpCode = "";
  ui.showQuickCompany = false;
}

async function saveAsset() {
  const company = masterData.companies.find((item) => item.name === form.company);
  const extraFields = Object.fromEntries(
    extraFieldEntries.value
      .map((entry) => [entry.key.trim(), entry.value.trim()])
      .filter(([key]) => key)
  );

  await addAsset({
    company: form.company,
    companyErpCode: company?.erpCode ?? form.companyErpCode ?? "",
    assetType: form.assetType,
    department: form.department,
    brand: form.brand,
    model: form.model,
    serialNumber: form.serialNumber,
    patrimony: form.patrimony,
    detailedDescription: form.detailedDescription,
    status: createComputedStatusLabel.value,
    responsibleUserId: null,
    responsibleUserName: "",
    linkedTermId: null,
    linkedTermTitle: "",
    transferHistory: transferHistoryText.value.split("\n").map((line) => line.trim()).filter(Boolean),
    ipMode: form.ipMode,
    ipAddress: form.ipAddress,
    imei: showImeiField.value ? form.imei : "",
    equipmentCondition: form.equipmentCondition,
    extraFields
  });

  resetForm();
  await loadAssets();
}

function editAsset(asset) {
  editingAsset.value = asset;
  assetToRemove.value = null;
  editAssetForm.company = asset.company ?? "";
  editAssetForm.companyErpCode = asset.companyErpCode ?? "";
  editAssetForm.assetType = asset.assetType ?? "NOTEBOOK";
  editAssetForm.department = asset.department ?? "";
  editAssetForm.brand = asset.brand ?? "";
  editAssetForm.model = asset.model ?? "";
  editAssetForm.serialNumber = asset.serialNumber ?? "";
  editAssetForm.patrimony = asset.patrimony ?? "";
  editAssetForm.ipMode = asset.ipMode ?? "DHCP";
  editAssetForm.ipAddress = asset.ipAddress ?? "";
  editAssetForm.imei = asset.imei ?? "";
  editAssetForm.detailedDescription = asset.detailedDescription ?? "";
  editAssetForm.transferHistoryText = (asset.transferHistory ?? []).join("\n");
  editAssetForm.equipmentCondition = asset.equipmentCondition ?? "USADO";
}

function deleteAssetRow(asset) {
  assetToRemove.value = asset;
  editingAsset.value = null;
}

async function submitAssetEdit() {
  if (!editingAsset.value) return;
  const base = editingAsset.value;
  await updateAsset(base.id, {
    company: editAssetForm.company,
    companyErpCode: editAssetForm.companyErpCode,
    assetType: editAssetForm.assetType,
    department: editAssetForm.department,
    brand: editAssetForm.brand,
    model: editAssetForm.model,
    serialNumber: editAssetForm.serialNumber,
    patrimony: editAssetForm.patrimony,
    detailedDescription: editAssetForm.detailedDescription,
    status: base.status,
    responsibleUserId: base.responsibleUserId ?? null,
    responsibleUserName: base.responsibleUserName ?? "",
    linkedTermId: base.linkedTermId ?? null,
    linkedTermTitle: base.linkedTermTitle ?? "",
    transferHistory: editAssetForm.transferHistoryText.split("\n").map((line) => line.trim()).filter(Boolean),
    ipMode: editAssetForm.ipMode,
    ipAddress: editAssetForm.ipAddress,
    imei: editAssetForm.imei,
    equipmentCondition: editAssetForm.equipmentCondition,
    extraFields: base.extraFields ?? {}
  });
  await loadAssets();
  closeAssetActions();
}

async function confirmAssetRemoval() {
  if (!assetToRemove.value) return;
  const removedId = assetToRemove.value.id;
  await removeAsset(removedId);
  rows.value = rows.value.filter((item) => item.id !== removedId);
  totalItems.value = Math.max(0, totalItems.value - 1);
  if (rows.value.length === 0 && page.value > 1) {
    page.value = page.value - 1;
  } else {
    await loadAssets();
  }
  closeAssetActions();
}

function openReturnModal(asset) {
  returnAssetTarget.value = asset;
  returnEquipmentCondition.value = asset?.equipmentCondition ?? "USADO";
  returnModalOpen.value = true;
}

function closeReturnModal() {
  returnModalOpen.value = false;
  returnAssetTarget.value = null;
  returnEquipmentCondition.value = "USADO";
}

async function confirmAssetReturn() {
  if (!returnAssetTarget.value?.id) return;
  await apiRequest(`/api/ti/assets/${returnAssetTarget.value.id}/return`, {
    method: "PATCH",
    body: JSON.stringify({ equipmentCondition: returnEquipmentCondition.value })
  });
  closeReturnModal();
  await loadAssets();
}

async function toggleAssetActive(asset) {
  await apiRequest(`/api/ti/assets/${asset.id}/reactivate`, { method: "PATCH" });
  await loadAssets();
}

function closeAssetActions() {
  editingAsset.value = null;
  assetToRemove.value = null;
}

function openHistory(asset) {
  historyAsset.value = asset;
  historyModalOpen.value = true;
  historyPage.value = 1;
  loadAssetHistory();
}

function closeHistoryModal() {
  historyModalOpen.value = false;
  historyAsset.value = null;
  historyRows.value = [];
  historyTotalItems.value = 0;
  historyTotalPages.value = 1;
  historyError.value = "";
}

async function openSummaryModal() {
  summaryModalOpen.value = true;
  loadingSummary.value = true;
  summaryError.value = "";
  try {
    const result = await apiRequest("/api/ti/assets/summary");
    summary.total = result.total ?? 0;
    summary.available = result.available ?? 0;
    summary.inUse = result.inUse ?? 0;
    summary.returned = result.returned ?? 0;
  } catch {
    summaryError.value = "Nao foi possivel carregar o resumo.";
  } finally {
    loadingSummary.value = false;
  }
}

function clearHistoryFilters() {
  historyFilters.responsible = "";
  historyFilters.status = "";
  historyFilters.eventType = "";
  if (historyPage.value !== 1) historyPage.value = 1;
  else loadAssetHistory();
}

function setHistoryPage(nextPage) {
  historyPage.value = nextPage;
}

function setHistoryPageSize(nextSize) {
  historyPageSize.value = Number(nextSize) || 10;
  historyPage.value = 1;
}

function setPage(nextPage) {
  page.value = nextPage;
}

function setPageSize(nextSize) {
  pageSize.value = Number(nextSize) || 10;
  page.value = 1;
}

async function loadAssets() {
  loadingAssets.value = true;
  loadAssetsError.value = "";
  try {
    const params = new URLSearchParams({
      page: String(page.value),
      pageSize: String(pageSize.value)
    });
    if (filters.search.trim()) params.set("internalCode", filters.search.trim());
    if (filters.type) params.set("assetType", filters.type);
    if (filters.responsible.trim()) params.set("responsible", filters.responsible.trim());
    if (filters.department.trim()) params.set("department", filters.department.trim());
    if (filters.showInactives) params.set("showInactives", "true");
    const response = await apiRequest(`/api/ti/assets/paged?${params.toString()}`);
    rows.value = response.items ?? [];
    totalItems.value = response.totalItems ?? 0;
    totalPages.value = Math.max(response.totalPages ?? 1, 1);
  } catch {
    rows.value = [];
    totalItems.value = 0;
    totalPages.value = 1;
    loadAssetsError.value = "Nao foi possivel carregar os ativos.";
  } finally {
    loadingAssets.value = false;
  }
}

async function loadAssetHistory() {
  if (!historyAsset.value?.id) return;
  loadingHistory.value = true;
  historyError.value = "";
  try {
    const params = new URLSearchParams({
      page: String(historyPage.value),
      pageSize: String(historyPageSize.value)
    });
    if (historyFilters.responsible.trim()) params.set("responsible", historyFilters.responsible.trim());
    if (historyFilters.status.trim()) params.set("status", historyFilters.status.trim());
    if (historyFilters.eventType) params.set("eventType", historyFilters.eventType);
    const response = await apiRequest(`/api/ti/assets/${historyAsset.value.id}/history?${params.toString()}`);
    historyRows.value = response.items ?? [];
    historyTotalItems.value = response.totalItems ?? 0;
    historyTotalPages.value = Math.max(response.totalPages ?? 1, 1);
  } catch {
    historyRows.value = [];
    historyTotalItems.value = 0;
    historyTotalPages.value = 1;
    historyError.value = "Nao foi possivel carregar o historico do ativo.";
  } finally {
    loadingHistory.value = false;
  }
}

function formatDateTime(value) {
  if (!value) return "-";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString("pt-BR");
}
</script>
