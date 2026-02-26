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
          <span class="tag">{{ masterData.companies.length }} cadastrada(s)</span>
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

        <div v-if="masterData.companies.length === 0" class="empty-state" style="margin-top: 12px">
          Nenhuma empresa cadastrada.
        </div>

        <div v-else class="entity-list">
          <div class="entity-row" v-for="company in paginatedCompanies" :key="company.id">
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
          :page="companiesPagination.page"
          :page-size="companiesPagination.pageSize"
          :total-pages="companiesPagination.totalPages"
          :total-items="companiesPagination.totalItems"
          @update:page="companiesPagination.setPage"
          @update:pageSize="companiesPagination.setPageSize"
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
import { computed, ref, onMounted, reactive } from "vue";
import PageHeader from "../../components/PageHeader.vue";
import PaginationBar from "../../components/PaginationBar.vue";
import { useMasterData } from "../../composables/useMasterData";
import { usePagination } from "../../composables/usePagination";

const { state: masterData, ensureLoaded, addCompany, updateCompany, removeCompany } = useMasterData();
const companyName = ref("");
const companyErpCode = ref("");
const editingCompany = ref(null);
const companyToRemove = ref(null);
const editCompanyForm = reactive({ name: "", erpCode: "" });
const companiesList = computed(() => masterData.companies);
const companiesPagination = usePagination(companiesList, 8);
const paginatedCompanies = companiesPagination.paginatedItems;

onMounted(() => {
  ensureLoaded();
});

async function saveCompany() {
  if (!companyName.value.trim()) return;
  await addCompany({ name: companyName.value, erpCode: companyErpCode.value });
  companyName.value = "";
  companyErpCode.value = "";
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
  closeCompanyActions();
}

async function confirmCompanyRemoval() {
  if (!companyToRemove.value) return;
  await removeCompany(companyToRemove.value.id);
  closeCompanyActions();
}

function closeCompanyActions() {
  editingCompany.value = null;
  companyToRemove.value = null;
}
</script>
