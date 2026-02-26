<template>
  <div>
    <PageHeader eyebrow="Gestao da TI" title="Ativos de Informatica" subtitle="Cadastro e consulta de ativos.">
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
          Status
          <select v-model="form.status">
            <option v-for="status in statuses" :key="status" :value="status">{{ status }}</option>
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

        <label>
          Responsavel
          <div class="field-with-action">
            <select v-model.number="form.responsibleUserId">
              <option :value="null">Sem responsavel</option>
              <option v-for="user in sessionState.allUsers" :key="user.id" :value="user.id">{{ user.name }}</option>
            </select>
            <button type="button" class="field-add-btn" title="Cadastrar usuario rapido" @click="ui.showQuickUser = !ui.showQuickUser">+</button>
          </div>
        </label>

        <label>
          Vinculo do termo/contrato
          <div class="field-with-action">
            <select v-model.number="form.linkedTermId">
              <option :value="null">Sem vinculo</option>
              <option v-for="term in tiState.terms" :key="term.id" :value="term.id">
                {{ term.defaultTermName }} - {{ term.type }}
              </option>
            </select>
            <button type="button" class="field-add-btn" title="Cadastrar termo rapido" @click="ui.showQuickTerm = !ui.showQuickTerm">+</button>
          </div>
        </label>

        <div class="full quick-inline-card" v-if="ui.showQuickUser">
          <div class="quick-inline-card__head">
            <strong>Cadastro rapido de usuario (responsavel)</strong>
            <button type="button" @click="ui.showQuickUser = false">Fechar</button>
          </div>
          <div class="form-grid">
            <label>
              Nome
              <input v-model="quickUser.name" />
            </label>
            <label>
              E-mail
              <input v-model="quickUser.email" type="email" />
            </label>
            <div class="full actions-row">
              <button type="button" class="btn-primary" @click="createUserQuick">Salvar e selecionar</button>
              <span class="muted">Criado como Operador com modulo TI.</span>
            </div>
          </div>
        </div>

        <div class="full quick-inline-card" v-if="ui.showQuickTerm">
          <div class="quick-inline-card__head">
            <strong>Cadastro rapido de termo</strong>
            <button type="button" @click="ui.showQuickTerm = false">Fechar</button>
          </div>
          <div class="form-grid">
            <label>
              Tipo
              <select v-model="quickTerm.type">
                <option value="CLT">CLT</option>
                <option value="COMODATO">Comodato</option>
              </select>
            </label>
            <label>
              Vinculado a
              <input v-model="quickTerm.linkedUserName" placeholder="Nome do usuario/responsavel" />
            </label>
            <label>
              Data de inicio
              <input v-model="quickTerm.startDate" type="date" />
            </label>
            <label>
              Caminho documento
              <input v-model="quickTerm.documentPath" placeholder="C:/documentos/..." />
            </label>
            <div class="full actions-row">
              <button type="button" class="btn-primary" @click="createTermQuick">Salvar e selecionar</button>
            </div>
          </div>
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
      <h3 style="margin-top: 0">Ativos cadastrados</h3>
      <div class="panel" style="padding: 10px; margin-bottom: 10px">
        <div class="form-grid">
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
          <div class="actions-row" style="align-self: end">
            <button type="button" @click="clearFilters">Limpar filtros</button>
          </div>
        </div>
      </div>

      <p class="muted" style="margin-top: -4px; margin-bottom: 10px" v-if="currentUser.profile === 'OPERADOR'">
        Exibindo somente ativos sob sua responsabilidade.
      </p>

      <div v-if="visibleAssets.length === 0" class="empty-state" style="margin-bottom: 10px">Nenhum ativo cadastrado ainda.</div>

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
              <th>Responsavel</th>
              <th>Termo</th>
              <th>IP</th>
              <th>IMEI</th>
              <th>Historico</th>
              <th>Acoes</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="asset in paginatedAssets" :key="asset.id">
              <td>{{ asset.internalCode }}</td>
              <td>{{ asset.company || "-" }}</td>
              <td>{{ asset.companyErpCode || "-" }}</td>
              <td>{{ asset.assetType }}</td>
              <td>{{ asset.department }}</td>
              <td>{{ asset.brand }} / {{ asset.model }}</td>
              <td><span class="tag">{{ asset.status }}</span></td>
              <td>{{ asset.responsibleUserName || "-" }}</td>
              <td>{{ asset.linkedTermTitle || "-" }}</td>
              <td>{{ asset.ipMode }} - {{ asset.ipAddress || "-" }}</td>
              <td>{{ asset.imei || "-" }}</td>
              <td><div v-for="(item, idx) in asset.transferHistory" :key="idx">{{ item }}</div></td>
              <td>
                <div class="actions-row">
                  <button type="button" @click="editAsset(asset)">Editar</button>
                  <button type="button" @click="deleteAssetRow(asset)">Remover</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <PaginationBar
        :page="assetsPagination.page"
        :page-size="assetsPagination.pageSize"
        :total-pages="assetsPagination.totalPages"
        :total-items="assetsPagination.totalItems"
        @update:page="assetsPagination.setPage"
        @update:pageSize="assetsPagination.setPageSize"
      />
    </div>

    <div class="panel" v-if="editingAsset || assetToRemove">
      <div class="section-head">
        <div>
          <h3>{{ editingAsset ? "Editar ativo" : "Confirmar remocao de ativo" }}</h3>
          <p v-if="editingAsset">Ajuste os dados principais do ativo sem popup nativo.</p>
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
          Status
          <select v-model="editAssetForm.status">
            <option v-for="status in statuses" :key="status" :value="status">{{ status }}</option>
          </select>
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
</template>

<script setup>
import { computed, reactive, ref, onMounted } from "vue";
import PageHeader from "../../components/PageHeader.vue";
import PaginationBar from "../../components/PaginationBar.vue";
import { useSession } from "../../composables/useSession";
import { useTiData } from "../../composables/useTiData";
import { useMasterData } from "../../composables/useMasterData";
import { usePagination } from "../../composables/usePagination";

const { currentUser, addUser, ensureLoaded: ensureUsersLoaded, state: sessionState } = useSession();
const { state: tiState, addAsset, addTerm, updateAsset, removeAsset, ensureLoaded: ensureTiLoaded } = useTiData();
const { state: masterData, ensureLoaded: ensureMasterDataLoaded, addCompany } = useMasterData();

const assetTypes = ["NOTEBOOK", "DESKTOP", "MONITOR", "IMPRESSORA", "ROTEADOR", "CELULAR", "TABLET", "OUTRO"];
const statuses = ["DISPONIVEL", "EM_USO", "MANUTENCAO", "DESCARTE"];

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
  status: "DISPONIVEL",
  responsibleUserId: null,
  linkedTermId: null,
  ipMode: "DHCP",
  ipAddress: "",
  imei: ""
});

const filters = reactive({
  type: "",
  responsible: "",
  department: ""
});

const ui = reactive({
  showQuickCompany: false,
  showQuickUser: false,
  showQuickTerm: false
});

const quickCompany = reactive({ name: "", erpCode: "" });
const quickUser = reactive({ name: "", email: "" });
const quickTerm = reactive({ type: "COMODATO", linkedUserName: "", startDate: "", documentPath: "" });
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
  status: "DISPONIVEL",
  ipMode: "DHCP",
  ipAddress: "",
  imei: "",
  detailedDescription: "",
  transferHistoryText: ""
});

const extraFieldEntries = ref([]);
const transferHistoryText = ref("");
const showImeiField = computed(() => ["CELULAR", "TABLET"].includes(form.assetType));

const visibleAssets = computed(() => {
  const base =
    currentUser.value.profile === "OPERADOR"
      ? tiState.assets.filter((asset) => asset.responsibleUserId === currentUser.value.id)
      : tiState.assets;

  return base.filter((asset) => {
    const byType = !filters.type || asset.assetType === filters.type;
    const byResponsible = !filters.responsible || asset.responsibleUserName === filters.responsible;
    const byDepartment =
      !filters.department || (asset.department ?? "").toLowerCase().includes(filters.department.trim().toLowerCase());
    return byType && byResponsible && byDepartment;
  });
});

const assetsPagination = usePagination(visibleAssets, 10);
const paginatedAssets = assetsPagination.paginatedItems;

onMounted(() => {
  ensureUsersLoaded();
  ensureMasterDataLoaded();
  ensureTiLoaded();
});

function addExtraField() {
  extraFieldEntries.value.push({ key: "", value: "" });
}

function removeExtraField(index) {
  extraFieldEntries.value.splice(index, 1);
}

function clearFilters() {
  filters.type = "";
  filters.responsible = "";
  filters.department = "";
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
  form.status = "DISPONIVEL";
  form.responsibleUserId = null;
  form.linkedTermId = null;
  form.ipMode = "DHCP";
  form.ipAddress = "";
  form.imei = "";
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

async function createUserQuick() {
  if (!quickUser.name.trim() || !quickUser.email.trim()) return;
  const created = await addUser({
    name: quickUser.name.trim(),
    email: quickUser.email.trim(),
    profile: "OPERADOR",
    modules: ["TI"]
  });
  if (!created) return;
  form.responsibleUserId = created.id;
  quickUser.name = "";
  quickUser.email = "";
  ui.showQuickUser = false;
}

async function createTermQuick() {
  if (!quickTerm.linkedUserName.trim()) return;
  const created = await addTerm({
    type: quickTerm.type,
    linkedUserName: quickTerm.linkedUserName.trim(),
    startDate: quickTerm.startDate || new Date().toISOString().slice(0, 10),
    status: "Ativo",
    documentPath: quickTerm.documentPath.trim()
  });
  if (!created) return;
  form.linkedTermId = created.id;
  quickTerm.type = "COMODATO";
  quickTerm.linkedUserName = "";
  quickTerm.startDate = "";
  quickTerm.documentPath = "";
  ui.showQuickTerm = false;
}

async function saveAsset() {
  const responsible = sessionState.allUsers.find((user) => user.id === form.responsibleUserId);
  const term = tiState.terms.find((item) => item.id === form.linkedTermId);
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
    status: form.status,
    responsibleUserId: form.responsibleUserId,
    responsibleUserName: responsible?.name ?? "",
    linkedTermId: form.linkedTermId,
    linkedTermTitle: term?.defaultTermName ?? "",
    transferHistory: transferHistoryText.value.split("\n").map((line) => line.trim()).filter(Boolean),
    ipMode: form.ipMode,
    ipAddress: form.ipAddress,
    imei: showImeiField.value ? form.imei : "",
    extraFields
  });

  resetForm();
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
  editAssetForm.status = asset.status ?? "DISPONIVEL";
  editAssetForm.ipMode = asset.ipMode ?? "DHCP";
  editAssetForm.ipAddress = asset.ipAddress ?? "";
  editAssetForm.imei = asset.imei ?? "";
  editAssetForm.detailedDescription = asset.detailedDescription ?? "";
  editAssetForm.transferHistoryText = (asset.transferHistory ?? []).join("\n");
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
    status: editAssetForm.status,
    responsibleUserId: base.responsibleUserId ?? null,
    responsibleUserName: base.responsibleUserName ?? "",
    linkedTermId: base.linkedTermId ?? null,
    linkedTermTitle: base.linkedTermTitle ?? "",
    transferHistory: editAssetForm.transferHistoryText.split("\n").map((line) => line.trim()).filter(Boolean),
    ipMode: editAssetForm.ipMode,
    ipAddress: editAssetForm.ipAddress,
    imei: editAssetForm.imei,
    extraFields: base.extraFields ?? {}
  });
  closeAssetActions();
}

async function confirmAssetRemoval() {
  if (!assetToRemove.value) return;
  await removeAsset(assetToRemove.value.id);
  closeAssetActions();
}

function closeAssetActions() {
  editingAsset.value = null;
  assetToRemove.value = null;
}
</script>
