<template>
  <div>
    <PageHeader eyebrow="Gestao do Comercial" title="Vendedores" subtitle="" />

    <div class="panel">
      <div class="section-head">
        <div>
          <h3>Novo vendedor</h3>
        </div>
      </div>
      <form class="form-grid" @submit.prevent="saveSeller">
        <label>
          Codigo (sistema)
          <input value="Gerado automaticamente ao salvar" disabled />
        </label>
        <label>
          Codigo ERP
          <input v-model="form.erpCode" required placeholder="Ex.: VEND-001" />
        </label>
        <label>
          Nome
          <input v-model="form.name" required />
        </label>
        <label>
          E-mail
          <input v-model="form.email" type="email" />
        </label>
        <label>
          Telefone
          <input v-model="form.phone" />
        </label>
        <div class="full actions-row">
          <button class="btn-primary" type="submit">Salvar vendedor</button>
          <span class="danger-text" v-if="duplicateErp">Codigo ERP ja cadastrado.</span>
        </div>
      </form>
    </div>

    <div class="table-panel">
      <div class="section-head">
        <div>
          <h3>Vendedores</h3>
        </div>
        <span class="tag">{{ rows.length }} exibidos de {{ totalItems }} item(ns)</span>
      </div>

      <div class="filters-toolbar filters-toolbar--enhanced">
        <div class="filters-toolbar__head">
          <strong>Filtros</strong>
        </div>
        <div class="filters-grid">
          <label>
            Nome
            <input v-model="filters.name" placeholder="Ex.: lucas" />
          </label>
          <label>
            Codigo ERP
            <input v-model="filters.erpCode" placeholder="Ex.: VND-ERP-001" />
          </label>
          <label>
            E-mail
            <input v-model="filters.email" placeholder="Ex.: vendedor@empresa.com" />
          </label>
        </div>
        <div class="filters-actions">
          <button type="button" class="btn-soft" @click="clearFilters">Limpar filtros</button>
        </div>
      </div>

      <div v-if="loading" class="empty-state">Carregando vendedores...</div>
      <div v-else-if="loadError" class="empty-state">{{ loadError }}</div>
      <div v-else-if="rows.length === 0" class="empty-state">Nenhum vendedor encontrado.</div>
      <div v-else class="table-scroll">
        <table>
          <thead>
            <tr>
              <th>Codigo</th>
              <th>Codigo ERP</th>
              <th>Nome</th>
              <th>E-mail</th>
              <th>Telefone</th>
              <th>Acoes</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="seller in rows" :key="seller.id">
              <td>{{ seller.code }}</td>
              <td>{{ seller.erpCode }}</td>
              <td>{{ seller.name }}</td>
              <td>{{ seller.email || "-" }}</td>
              <td>{{ seller.phone || "-" }}</td>
              <td>
                <div class="actions-row actions-row--compact">
                  <button type="button" class="btn-action btn-action--edit" @click="editSeller(seller)">
                    <span class="btn-action__icon">E</span>
                    <span>Editar</span>
                  </button>
                  <button type="button" class="btn-action btn-action--remove" @click="deleteSellerRow(seller)">
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

    <div class="modal-overlay" v-if="editingSeller || sellerToRemove" @click.self="closeSellerActions">
      <div class="modal-card modal-card--small">
        <div class="section-head">
          <div>
            <h3>{{ editingSeller ? "Editar vendedor" : "Confirmar remocao de vendedor" }}</h3>
            <p v-if="editingSeller">Atualize os dados do vendedor.</p>
            <p v-else>Remocao fisica do vendedor selecionado.</p>
          </div>
          <button type="button" class="btn-soft" @click="closeSellerActions">Fechar</button>
        </div>

        <form v-if="editingSeller" class="form-grid" @submit.prevent="submitSellerEdit">
          <label>
            Codigo ERP
            <input v-model="editSellerForm.erpCode" required />
          </label>
          <label>
            Nome
            <input v-model="editSellerForm.name" required />
          </label>
          <label>
            E-mail
            <input v-model="editSellerForm.email" type="email" />
          </label>
          <label>
            Telefone
            <input v-model="editSellerForm.phone" />
          </label>
          <div class="full actions-row">
            <button class="btn-primary" type="submit">Salvar alteracoes</button>
            <button type="button" @click="closeSellerActions">Cancelar</button>
          </div>
        </form>

        <div v-else-if="sellerToRemove" class="crud-box">
          <p><strong>{{ sellerToRemove.name }}</strong></p>
          <p class="muted">ERP: {{ sellerToRemove.erpCode }}</p>
          <div class="actions-row">
            <button type="button" class="btn-soft" @click="closeSellerActions">Cancelar</button>
            <button type="button" @click="confirmSellerRemoval">Remover</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, reactive, ref, watch } from "vue";
import PageHeader from "../../components/PageHeader.vue";
import PaginationBar from "../../components/PaginationBar.vue";
import { useCommercialData } from "../../composables/useCommercialData";
import { apiRequest } from "../../services/api";

const { state, ensureLoaded, addSeller, updateSeller, removeSeller } = useCommercialData();
const duplicateErp = ref(false);
const rows = ref([]);
const totalItems = ref(0);
const totalPages = ref(1);
const page = ref(1);
const pageSize = ref(10);
const loading = ref(false);
const loadError = ref("");
let filtersDebounce = null;

const filters = reactive({ name: "", erpCode: "", email: "" });
const editingSeller = ref(null);
const sellerToRemove = ref(null);
const editSellerForm = reactive({ erpCode: "", name: "", email: "", phone: "" });

const form = reactive({
  erpCode: "",
  name: "",
  email: "",
  phone: ""
});

onMounted(async () => {
  await ensureLoaded();
  await loadSellers();
});

onBeforeUnmount(() => {
  if (filtersDebounce) clearTimeout(filtersDebounce);
});

watch([page, pageSize], () => {
  loadSellers();
});

watch(
  () => [filters.name, filters.erpCode, filters.email],
  () => {
    if (page.value !== 1) {
      page.value = 1;
      return;
    }
    if (filtersDebounce) clearTimeout(filtersDebounce);
    filtersDebounce = setTimeout(() => loadSellers(), 300);
  }
);

async function saveSeller() {
  const erp = form.erpCode.trim();
  const name = form.name.trim();
  if (!erp || !name) return;
  duplicateErp.value = state.sellers.some((seller) => seller.erpCode.toLowerCase() === erp.toLowerCase());
  if (duplicateErp.value) return;

  await addSeller({
    erpCode: erp,
    name,
    email: form.email.trim(),
    phone: form.phone.trim()
  });

  form.erpCode = "";
  form.name = "";
  form.email = "";
  form.phone = "";
  await ensureLoaded();
  await loadSellers();
}

function editSeller(seller) {
  editingSeller.value = seller;
  sellerToRemove.value = null;
  editSellerForm.erpCode = seller.erpCode ?? "";
  editSellerForm.name = seller.name ?? "";
  editSellerForm.email = seller.email ?? "";
  editSellerForm.phone = seller.phone ?? "";
}

function deleteSellerRow(seller) {
  sellerToRemove.value = seller;
  editingSeller.value = null;
}

async function submitSellerEdit() {
  if (!editingSeller.value) return;
  if (!editSellerForm.erpCode.trim() || !editSellerForm.name.trim()) return;
  await updateSeller(editingSeller.value.id, { ...editSellerForm });
  await ensureLoaded();
  await loadSellers();
  closeSellerActions();
}

async function confirmSellerRemoval() {
  if (!sellerToRemove.value) return;
  const removedId = sellerToRemove.value.id;
  await removeSeller(removedId);
  rows.value = rows.value.filter((item) => item.id !== removedId);
  totalItems.value = Math.max(0, totalItems.value - 1);
  await ensureLoaded();
  if (rows.value.length === 0 && page.value > 1) {
    page.value = page.value - 1;
  } else {
    await loadSellers();
  }
  closeSellerActions();
}

function closeSellerActions() {
  editingSeller.value = null;
  sellerToRemove.value = null;
}

function clearFilters() {
  filters.name = "";
  filters.erpCode = "";
  filters.email = "";
  if (page.value !== 1) page.value = 1;
  else loadSellers();
}

function setPage(nextPage) {
  page.value = nextPage;
}

function setPageSize(nextSize) {
  pageSize.value = Number(nextSize) || 10;
  page.value = 1;
}

async function loadSellers() {
  loading.value = true;
  loadError.value = "";
  try {
    const params = new URLSearchParams({
      page: String(page.value),
      pageSize: String(pageSize.value)
    });
    if (filters.name.trim()) params.set("name", filters.name.trim());
    if (filters.erpCode.trim()) params.set("erpCode", filters.erpCode.trim());
    if (filters.email.trim()) params.set("email", filters.email.trim());
    const response = await apiRequest(`/api/commercial/sellers/paged?${params.toString()}`);
    rows.value = response.items ?? [];
    totalItems.value = response.totalItems ?? 0;
    totalPages.value = Math.max(response.totalPages ?? 1, 1);
  } catch {
    rows.value = [];
    totalItems.value = 0;
    totalPages.value = 1;
    loadError.value = "Nao foi possivel carregar vendedores.";
  } finally {
    loading.value = false;
  }
}
</script>
