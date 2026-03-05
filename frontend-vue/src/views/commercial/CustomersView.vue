<template>
  <div>
    <PageHeader eyebrow="Gestao do Comercial" title="Cadastro de Cliente" subtitle="Codigo interno gerado pelo sistema + campos ERP e dados cadastrais." />

    <div class="panel">
      <div class="section-head">
        <div>
          <h3>Novo cliente</h3>
          <p>Codigo interno automatico + campos ERP e dados cadastrais.</p>
        </div>
      </div>
      <form class="form-grid" @submit.prevent="saveCustomer">
        <label>
          Codigo (sistema)
          <input value="Gerado automaticamente ao salvar" disabled />
        </label>
        <label>
          Codigo ERP
          <input v-model="form.erpCode" />
        </label>
        <label>
          Razao social
          <input v-model="form.corporateName" required />
        </label>
        <label>
          E-mail
          <input v-model="form.email" type="email" />
        </label>
        <label>
          Tipo
          <select v-model="form.type">
            <option value="PJ">PJ</option>
            <option value="PF">PF</option>
          </select>
        </label>
        <label>
          Nome fantasia
          <input v-model="form.tradeName" />
        </label>
        <label>
          Telefone
          <input v-model="form.phone" />
        </label>
        <label>
          Codigo ERP Vendedor
          <select v-model="form.erpSellerCode">
            <option value="">Selecione</option>
            <option v-for="seller in state.sellers" :key="seller.id" :value="seller.erpCode">
              {{ seller.erpCode }} - {{ seller.name }}
            </option>
          </select>
        </label>
        <div class="full actions-row">
          <button class="btn-primary" type="submit">Salvar cliente</button>
        </div>
      </form>
    </div>

    <div class="table-panel">
      <div class="section-head">
        <div>
          <h3>Clientes</h3>
          <p>Busca paginada no servidor com filtros por dados comerciais.</p>
        </div>
        <span class="tag">{{ rows.length }} exibidos de {{ totalItems }} item(ns)</span>
      </div>

      <div class="filters-toolbar filters-toolbar--enhanced">
        <div class="filters-toolbar__head">
          <strong>Filtros</strong>
          <span class="muted-inline">Busque por razao social, ERP, tipo e vendedor.</span>
        </div>
        <div class="filters-grid">
          <label>
            Razao social
            <input v-model="filters.corporateName" placeholder="Ex.: mercado bom preco" />
          </label>
          <label>
            Codigo ERP
            <input v-model="filters.erpCode" placeholder="Ex.: CLI-ERP-001" />
          </label>
          <label>
            Tipo
            <select v-model="filters.type">
              <option value="">Todos</option>
              <option value="PJ">PJ</option>
              <option value="PF">PF</option>
            </select>
          </label>
          <label>
            ERP vendedor
            <input v-model="filters.erpSellerCode" placeholder="Ex.: VND-ERP-001" />
          </label>
        </div>
        <div class="filters-actions">
          <button type="button" class="btn-soft" @click="clearFilters">Limpar filtros</button>
        </div>
      </div>

      <p class="muted" v-if="state.sellers.length === 0" style="margin-top: -4px; margin-bottom: 10px">
        Cadastre vendedores em Comercial > Cadastro de Vendedor para vincular clientes por Codigo ERP.
      </p>
      <div v-if="loading" class="empty-state">Carregando clientes...</div>
      <div v-else-if="loadError" class="empty-state">{{ loadError }}</div>
      <div v-else-if="rows.length === 0" class="empty-state">Nenhum cliente encontrado.</div>
      <div v-else class="table-scroll">
        <table>
          <thead>
            <tr>
              <th>Codigo</th>
              <th>Codigo ERP</th>
              <th>Razao social</th>
              <th>Tipo</th>
              <th>Nome fantasia</th>
              <th>E-mail</th>
              <th>Telefone</th>
              <th>ERP Vendedor</th>
              <th>Acoes</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="customer in rows" :key="customer.id">
              <td>{{ customer.code }}</td>
              <td>{{ customer.erpCode || "-" }}</td>
              <td>{{ customer.corporateName }}</td>
              <td>{{ customer.type }}</td>
              <td>{{ customer.tradeName || "-" }}</td>
              <td>{{ customer.email || "-" }}</td>
              <td>{{ customer.phone || "-" }}</td>
              <td>{{ customer.erpSellerCode || "-" }}</td>
              <td>
                <div class="actions-row actions-row--compact">
                  <button type="button" class="btn-action btn-action--edit" @click="editCustomer(customer)">
                    <span class="btn-action__icon">E</span>
                    <span>Editar</span>
                  </button>
                  <button type="button" class="btn-action btn-action--remove" @click="deleteCustomerRow(customer)">
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

    <div class="panel" v-if="editingCustomer || customerToRemove">
      <div class="section-head">
        <div>
          <h3>{{ editingCustomer ? "Editar cliente" : "Confirmar remocao de cliente" }}</h3>
          <p v-if="editingCustomer">Atualize os dados cadastrais e ERP.</p>
          <p v-else>Remocao fisica do cadastro do cliente.</p>
        </div>
        <button type="button" class="btn-soft" @click="closeCustomerActions">Fechar</button>
      </div>

      <form v-if="editingCustomer" class="form-grid" @submit.prevent="submitCustomerEdit">
        <label>
          Codigo ERP
          <input v-model="editCustomerForm.erpCode" />
        </label>
        <label>
          Razao social
          <input v-model="editCustomerForm.corporateName" required />
        </label>
        <label>
          E-mail
          <input v-model="editCustomerForm.email" type="email" />
        </label>
        <label>
          Tipo
          <select v-model="editCustomerForm.type">
            <option value="PJ">PJ</option>
            <option value="PF">PF</option>
          </select>
        </label>
        <label>
          Nome fantasia
          <input v-model="editCustomerForm.tradeName" />
        </label>
        <label>
          Telefone
          <input v-model="editCustomerForm.phone" />
        </label>
        <label>
          Codigo ERP Vendedor
          <select v-model="editCustomerForm.erpSellerCode">
            <option value="">Selecione</option>
            <option v-for="seller in state.sellers" :key="seller.id" :value="seller.erpCode">
              {{ seller.erpCode }} - {{ seller.name }}
            </option>
          </select>
        </label>
        <div class="full actions-row">
          <button class="btn-primary" type="submit">Salvar alteracoes</button>
          <button type="button" @click="closeCustomerActions">Cancelar</button>
        </div>
      </form>

      <div v-else-if="customerToRemove" class="crud-box">
        <p><strong>{{ customerToRemove.corporateName }}</strong></p>
        <p class="muted">ERP: {{ customerToRemove.erpCode || "-" }}</p>
        <div class="actions-row">
          <button type="button" class="btn-soft" @click="closeCustomerActions">Cancelar</button>
          <button type="button" @click="confirmCustomerRemoval">Remover</button>
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

const { state, ensureLoaded, addCustomer, updateCustomer, removeCustomer } = useCommercialData();
const rows = ref([]);
const totalItems = ref(0);
const totalPages = ref(1);
const page = ref(1);
const pageSize = ref(10);
const loading = ref(false);
const loadError = ref("");
let filtersDebounce = null;

const filters = reactive({
  corporateName: "",
  erpCode: "",
  type: "",
  erpSellerCode: ""
});

const editingCustomer = ref(null);
const customerToRemove = ref(null);
const editCustomerForm = reactive({
  erpCode: "",
  corporateName: "",
  email: "",
  type: "PJ",
  tradeName: "",
  phone: "",
  erpSellerCode: ""
});

const form = reactive({
  erpCode: "",
  corporateName: "",
  email: "",
  type: "PJ",
  tradeName: "",
  phone: "",
  erpSellerCode: ""
});

onMounted(async () => {
  await ensureLoaded();
  await loadCustomers();
});

onBeforeUnmount(() => {
  if (filtersDebounce) clearTimeout(filtersDebounce);
});

watch([page, pageSize], () => {
  loadCustomers();
});

watch(
  () => [filters.corporateName, filters.erpCode, filters.type, filters.erpSellerCode],
  () => {
    if (page.value !== 1) {
      page.value = 1;
      return;
    }
    if (filtersDebounce) clearTimeout(filtersDebounce);
    filtersDebounce = setTimeout(() => loadCustomers(), 300);
  }
);

async function saveCustomer() {
  if (!form.corporateName.trim()) return;
  await addCustomer({
    erpCode: form.erpCode.trim(),
    corporateName: form.corporateName.trim(),
    email: form.email.trim(),
    type: form.type,
    tradeName: form.tradeName.trim(),
    phone: form.phone.trim(),
    erpSellerCode: form.erpSellerCode.trim()
  });
  form.erpCode = "";
  form.corporateName = "";
  form.email = "";
  form.type = "PJ";
  form.tradeName = "";
  form.phone = "";
  form.erpSellerCode = "";
  await loadCustomers();
}

function editCustomer(customer) {
  editingCustomer.value = customer;
  customerToRemove.value = null;
  editCustomerForm.erpCode = customer.erpCode ?? "";
  editCustomerForm.corporateName = customer.corporateName ?? "";
  editCustomerForm.email = customer.email ?? "";
  editCustomerForm.type = customer.type ?? "PJ";
  editCustomerForm.tradeName = customer.tradeName ?? "";
  editCustomerForm.phone = customer.phone ?? "";
  editCustomerForm.erpSellerCode = customer.erpSellerCode ?? "";
}

function deleteCustomerRow(customer) {
  customerToRemove.value = customer;
  editingCustomer.value = null;
}

async function submitCustomerEdit() {
  if (!editingCustomer.value || !editCustomerForm.corporateName.trim()) return;
  await updateCustomer(editingCustomer.value.id, { ...editCustomerForm });
  await loadCustomers();
  closeCustomerActions();
}

async function confirmCustomerRemoval() {
  if (!customerToRemove.value) return;
  const removedId = customerToRemove.value.id;
  await removeCustomer(removedId);
  rows.value = rows.value.filter((item) => item.id !== removedId);
  totalItems.value = Math.max(0, totalItems.value - 1);
  if (rows.value.length === 0 && page.value > 1) {
    page.value = page.value - 1;
  } else {
    await loadCustomers();
  }
  closeCustomerActions();
}

function closeCustomerActions() {
  editingCustomer.value = null;
  customerToRemove.value = null;
}

function clearFilters() {
  filters.corporateName = "";
  filters.erpCode = "";
  filters.type = "";
  filters.erpSellerCode = "";
  if (page.value !== 1) {
    page.value = 1;
  } else {
    loadCustomers();
  }
}

function setPage(nextPage) {
  page.value = nextPage;
}

function setPageSize(nextSize) {
  pageSize.value = Number(nextSize) || 10;
  page.value = 1;
}

async function loadCustomers() {
  loading.value = true;
  loadError.value = "";
  try {
    const params = new URLSearchParams({
      page: String(page.value),
      pageSize: String(pageSize.value)
    });
    if (filters.corporateName.trim()) params.set("corporateName", filters.corporateName.trim());
    if (filters.erpCode.trim()) params.set("erpCode", filters.erpCode.trim());
    if (filters.type.trim()) params.set("type", filters.type.trim());
    if (filters.erpSellerCode.trim()) params.set("erpSellerCode", filters.erpSellerCode.trim());
    const response = await apiRequest(`/api/commercial/customers/paged?${params.toString()}`);
    rows.value = response.items ?? [];
    totalItems.value = response.totalItems ?? 0;
    totalPages.value = Math.max(response.totalPages ?? 1, 1);
  } catch {
    rows.value = [];
    totalItems.value = 0;
    totalPages.value = 1;
    loadError.value = "Nao foi possivel carregar os clientes.";
  } finally {
    loading.value = false;
  }
}
</script>
