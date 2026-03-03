<template>
  <div>
    <PageHeader
      eyebrow="Configuracoes"
      title="Parametros Globais"
      subtitle="Dados de apoio reutilizaveis entre modulos (ex.: empresas, categorias, departamentos)."
    />

    <div class="config-layout">
      <div class="panel">
        <div class="section-head">
          <div>
            <h3>Empresas (CRUD)</h3>
            <p>Parametro global usado em Ativos e outros modulos.</p>
          </div>
          <span class="tag">{{ rows.length }} exibidas de {{ totalItems }}</span>
        </div>

        <form class="inline-form-card inline-form-card--company" @submit.prevent="saveCompany">
          <div class="inline-form-card__field">
            <span class="inline-form-card__label">Nome da empresa</span>
            <input v-model="companyName" placeholder="Ex.: Empresa Matriz" />
          </div>
          <div class="inline-form-card__field">
            <span class="inline-form-card__label">Codigo ERP</span>
            <input v-model="companyErpCode" placeholder="Ex.: EMP001" />
          </div>
          <button class="btn-primary" type="submit">Adicionar empresa</button>
        </form>

        <div class="filters-toolbar" style="margin-top: 12px; margin-bottom: 0">
          <div class="filters-grid">
            <label>
              Nome
              <input v-model="filters.name" placeholder="Ex.: matriz" />
            </label>
            <label>
              Codigo ERP
              <input v-model="filters.erpCode" placeholder="Ex.: EMP-ERP-001" />
            </label>
          </div>
          <div class="filters-actions">
            <button type="button" class="btn-soft" @click="clearFilters">Limpar filtros</button>
          </div>
        </div>

        <div v-if="loading" class="empty-state" style="margin-top: 12px">Carregando empresas...</div>
        <div v-else-if="loadError" class="empty-state" style="margin-top: 12px">{{ loadError }}</div>
        <div v-else-if="rows.length === 0" class="empty-state" style="margin-top: 12px">Nenhuma empresa encontrada.</div>

        <div v-else class="entity-list">
          <div class="entity-row" v-for="company in rows" :key="company.id">
            <div class="entity-row__main">
              <strong>{{ company.name }}</strong>
              <small>{{ company.code }} | ERP: {{ company.erpCode || "-" }}</small>
            </div>
            <div class="actions-row">
              <button type="button" @click="editCompany(company)">Editar</button>
              <button type="button" @click="askRemoveCompany(company)">Remover</button>
            </div>
          </div>
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

      <div class="panel" v-if="editingCompany || companyToRemove">
        <div class="section-head">
          <div>
            <h3>{{ editingCompany ? "Editar empresa" : "Confirmar remocao" }}</h3>
            <p v-if="editingCompany">Ajuste sem sair da tela.</p>
            <p v-else>Remocao imediata da empresa selecionada.</p>
          </div>
          <button type="button" class="btn-soft" @click="closeCompanyActions">Fechar</button>
        </div>

        <form v-if="editingCompany" class="inline-form-card inline-form-card--company" @submit.prevent="submitCompanyEdit">
          <div class="inline-form-card__field">
            <span class="inline-form-card__label">Nome da empresa</span>
            <input v-model="editCompanyForm.name" />
          </div>
          <div class="inline-form-card__field">
            <span class="inline-form-card__label">Codigo ERP</span>
            <input v-model="editCompanyForm.erpCode" />
          </div>
          <button class="btn-primary" type="submit">Salvar alteracoes</button>
        </form>

        <div v-else-if="companyToRemove" class="crud-box">
          <p><strong>{{ companyToRemove.name }}</strong></p>
          <p class="muted">Codigo ERP: {{ companyToRemove.erpCode || "-" }}</p>
          <div class="actions-row">
            <button type="button" class="btn-soft" @click="closeCompanyActions">Cancelar</button>
            <button type="button" @click="confirmCompanyRemoval">Remover</button>
          </div>
        </div>
      </div>

      <div class="panel">
        <div class="section-head">
          <div>
            <h3>Padrao de parametros globais</h3>
            <p>Centralizar dados comuns aqui evita retrabalho e divergencia entre modulos.</p>
          </div>
        </div>
        <div class="parameter-grid">
          <div class="parameter-card">
            <span>Departamentos</span>
            <strong>Proximo passo</strong>
            <small>TI, Comercial, Financeiro...</small>
          </div>
          <div class="parameter-card">
            <span>Categorias / Linhas</span>
            <strong>Proximo passo</strong>
            <small>Cadastro comercial e importacoes</small>
          </div>
          <div class="parameter-card">
            <span>Fabricantes</span>
            <strong>Proximo passo</strong>
            <small>Produtos e ativos de TI</small>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, watch, onBeforeUnmount } from "vue";
import PageHeader from "../../components/PageHeader.vue";
import PaginationBar from "../../components/PaginationBar.vue";
import { useMasterData } from "../../composables/useMasterData";
import { apiRequest } from "../../services/api";

const { ensureLoaded, addCompany, updateCompany, removeCompany } = useMasterData();
const companyName = ref("");
const companyErpCode = ref("");
const editingCompany = ref(null);
const companyToRemove = ref(null);
const editCompanyForm = reactive({ name: "", erpCode: "" });
const rows = ref([]);
const totalItems = ref(0);
const totalPages = ref(1);
const page = ref(1);
const pageSize = ref(8);
const loading = ref(false);
const loadError = ref("");
let filtersDebounce = null;

const filters = reactive({
  name: "",
  erpCode: ""
});

onMounted(async () => {
  await ensureLoaded();
  await loadCompanies();
});

onBeforeUnmount(() => {
  if (filtersDebounce) clearTimeout(filtersDebounce);
});

watch([page, pageSize], () => {
  loadCompanies();
});

watch(
  () => [filters.name, filters.erpCode],
  () => {
    if (page.value !== 1) {
      page.value = 1;
      return;
    }
    if (filtersDebounce) clearTimeout(filtersDebounce);
    filtersDebounce = setTimeout(() => loadCompanies(), 300);
  }
);

async function saveCompany() {
  if (!companyName.value.trim()) return;
  await addCompany({ name: companyName.value, erpCode: companyErpCode.value });
  companyName.value = "";
  companyErpCode.value = "";
  await loadCompanies();
}

function editCompany(company) {
  editingCompany.value = company;
  companyToRemove.value = null;
  editCompanyForm.name = company.name ?? "";
  editCompanyForm.erpCode = company.erpCode ?? "";
}

function askRemoveCompany(company) {
  companyToRemove.value = company;
  editingCompany.value = null;
}

async function submitCompanyEdit() {
  if (!editingCompany.value || !editCompanyForm.name.trim()) return;
  await updateCompany(editingCompany.value.id, editCompanyForm);
  await loadCompanies();
  closeCompanyActions();
}

async function confirmCompanyRemoval() {
  if (!companyToRemove.value) return;
  await removeCompany(companyToRemove.value.id);
  await loadCompanies();
  closeCompanyActions();
}

function closeCompanyActions() {
  editingCompany.value = null;
  companyToRemove.value = null;
}

function clearFilters() {
  filters.name = "";
  filters.erpCode = "";
  if (page.value !== 1) page.value = 1;
  else loadCompanies();
}

function setPage(nextPage) {
  page.value = nextPage;
}

function setPageSize(nextSize) {
  pageSize.value = Number(nextSize) || 8;
  page.value = 1;
}

async function loadCompanies() {
  loading.value = true;
  loadError.value = "";
  try {
    const params = new URLSearchParams({
      page: String(page.value),
      pageSize: String(pageSize.value)
    });
    if (filters.name.trim()) params.set("name", filters.name.trim());
    if (filters.erpCode.trim()) params.set("erpCode", filters.erpCode.trim());
    const response = await apiRequest(`/api/config/companies/paged?${params.toString()}`);
    rows.value = response.items ?? [];
    totalItems.value = response.totalItems ?? 0;
    totalPages.value = Math.max(response.totalPages ?? 1, 1);
  } catch {
    rows.value = [];
    totalItems.value = 0;
    totalPages.value = 1;
    loadError.value = "Nao foi possivel carregar empresas.";
  } finally {
    loading.value = false;
  }
}
</script>
