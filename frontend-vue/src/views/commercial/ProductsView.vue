<template>
  <div>
    <PageHeader eyebrow="Gestao do Comercial" title="Cadastro de Produto" subtitle="Codigo interno gerado pelo sistema + Codigo ERP." />

    <div class="panel">
      <div class="section-head">
        <div>
          <h3>Novo produto</h3>
          <p>Codigo interno automatico + integracao com ERP.</p>
        </div>
      </div>
      <form class="form-grid" @submit.prevent="saveProduct">
        <label>
          Codigo (sistema)
          <input value="Gerado automaticamente ao salvar" disabled />
        </label>
        <label>
          Codigo ERP
          <input v-model="form.erpCode" />
        </label>
        <label class="full">
          Descricao
          <input v-model="form.description" required />
        </label>
        <label>
          Departamento
          <input v-model="form.department" />
        </label>
        <label>
          Categoria
          <input v-model="form.category" />
        </label>
        <label>
          Linha
          <input v-model="form.line" />
        </label>
        <label>
          Fabricante
          <input v-model="form.manufacturer" />
        </label>
        <div class="full actions-row">
          <button class="btn-primary" type="submit">Salvar produto</button>
        </div>
      </form>
    </div>

    <div class="table-panel">
      <div class="section-head">
        <div>
          <h3>Produtos</h3>
          <p>Filtro e paginacao no servidor para manter performance em base grande.</p>
        </div>
        <span class="tag">{{ rows.length }} exibidos de {{ totalItems }} item(ns)</span>
      </div>

      <div class="filters-toolbar filters-toolbar--enhanced">
        <div class="filters-toolbar__head">
          <strong>Filtros</strong>
          <span class="muted-inline">Busque por descricao, linha e codigo ERP.</span>
        </div>
        <div class="filters-grid">
          <label>
            Buscar por descricao
            <input v-model="filters.description" placeholder="Ex.: notebook dell" />
          </label>
          <label>
            Filtrar por linha
            <input v-model="filters.line" placeholder="Ex.: notebooks" />
          </label>
          <label>
            Filtrar por codigo ERP
            <input v-model="filters.erpCode" placeholder="Ex.: PRD-ERP-001" />
          </label>
        </div>
        <div class="filters-actions">
          <button type="button" class="btn-soft" @click="clearFilters">Limpar filtros</button>
        </div>
      </div>
      <div v-if="loading" class="empty-state">Carregando produtos...</div>
      <div v-else-if="loadError" class="empty-state">{{ loadError }}</div>
      <div v-else-if="rows.length === 0" class="empty-state">Nenhum produto encontrado com os filtros atuais.</div>
      <div v-else class="table-scroll">
        <table>
          <thead>
            <tr>
              <th>Codigo</th>
              <th>Codigo ERP</th>
              <th>Descricao</th>
              <th>Departamento</th>
              <th>Categoria</th>
              <th>Linha</th>
              <th>Fabricante</th>
              <th>Acoes</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="product in rows" :key="product.id">
              <td>{{ product.code }}</td>
              <td>{{ product.erpCode || "-" }}</td>
              <td>{{ product.description }}</td>
              <td>{{ product.department || "-" }}</td>
              <td>{{ product.category || "-" }}</td>
              <td>{{ product.line || "-" }}</td>
              <td>{{ product.manufacturer || "-" }}</td>
              <td>
                <div class="actions-row actions-row--compact">
                  <button type="button" class="btn-action btn-action--edit" @click="editProduct(product)">
                    <span class="btn-action__icon">E</span>
                    <span>Editar</span>
                  </button>
                  <button type="button" class="btn-action btn-action--remove" @click="deleteProductRow(product)">
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

    <div class="panel" v-if="editingProduct || productToRemove">
      <div class="section-head">
        <div>
          <h3>{{ editingProduct ? "Editar produto" : "Confirmar remocao de produto" }}</h3>
          <p v-if="editingProduct">Altere os dados e salve.</p>
          <p v-else>Remocao fisica do cadastro do produto.</p>
        </div>
        <button type="button" class="btn-soft" @click="closeProductActions">Fechar</button>
      </div>

      <form v-if="editingProduct" class="form-grid" @submit.prevent="submitProductEdit">
        <label>
          Codigo ERP
          <input v-model="editProductForm.erpCode" />
        </label>
        <label class="full">
          Descricao
          <input v-model="editProductForm.description" required />
        </label>
        <label>
          Departamento
          <input v-model="editProductForm.department" />
        </label>
        <label>
          Categoria
          <input v-model="editProductForm.category" />
        </label>
        <label>
          Linha
          <input v-model="editProductForm.line" />
        </label>
        <label>
          Fabricante
          <input v-model="editProductForm.manufacturer" />
        </label>
        <div class="full actions-row">
          <button class="btn-primary" type="submit">Salvar alteracoes</button>
          <button type="button" @click="closeProductActions">Cancelar</button>
        </div>
      </form>

      <div v-else-if="productToRemove" class="crud-box">
        <p><strong>{{ productToRemove.description }}</strong></p>
        <p class="muted">Codigo ERP: {{ productToRemove.erpCode || "-" }}</p>
        <div class="actions-row">
          <button type="button" class="btn-soft" @click="closeProductActions">Cancelar</button>
          <button type="button" @click="confirmProductRemoval">Remover</button>
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

const { addProduct, updateProduct, removeProduct } = useCommercialData();
const filters = reactive({
  description: "",
  line: "",
  erpCode: ""
});

const rows = ref([]);
const totalItems = ref(0);
const totalPages = ref(1);
const page = ref(1);
const pageSize = ref(10);
const loading = ref(false);
const loadError = ref("");
const editingProduct = ref(null);
const productToRemove = ref(null);
let filtersDebounce = null;
const editProductForm = reactive({
  erpCode: "",
  description: "",
  department: "",
  category: "",
  line: "",
  manufacturer: ""
});

const form = reactive({
  erpCode: "",
  description: "",
  department: "",
  category: "",
  line: "",
  manufacturer: ""
});

onMounted(() => {
  loadProducts();
});

onBeforeUnmount(() => {
  if (filtersDebounce) clearTimeout(filtersDebounce);
});

watch([page, pageSize], () => {
  loadProducts();
});

watch(
  () => [filters.description, filters.line, filters.erpCode],
  () => {
    if (page.value !== 1) {
      page.value = 1;
      return;
    }
    if (filtersDebounce) clearTimeout(filtersDebounce);
    filtersDebounce = setTimeout(() => {
      loadProducts();
    }, 300);
  }
);

async function saveProduct() {
  if (!form.description.trim()) return;
  await addProduct({
    erpCode: form.erpCode.trim(),
    description: form.description.trim(),
    department: form.department.trim(),
    category: form.category.trim(),
    line: form.line.trim(),
    manufacturer: form.manufacturer.trim()
  });
  form.erpCode = "";
  form.description = "";
  form.department = "";
  form.category = "";
  form.line = "";
  form.manufacturer = "";
  await loadProducts();
}

function editProduct(product) {
  editingProduct.value = product;
  productToRemove.value = null;
  editProductForm.erpCode = product.erpCode ?? "";
  editProductForm.description = product.description ?? "";
  editProductForm.department = product.department ?? "";
  editProductForm.category = product.category ?? "";
  editProductForm.line = product.line ?? "";
  editProductForm.manufacturer = product.manufacturer ?? "";
}

function deleteProductRow(product) {
  productToRemove.value = product;
  editingProduct.value = null;
}

async function submitProductEdit() {
  if (!editingProduct.value || !editProductForm.description.trim()) return;
  await updateProduct(editingProduct.value.id, { ...editProductForm });
  await loadProducts();
  closeProductActions();
}

async function confirmProductRemoval() {
  if (!productToRemove.value) return;
  const removedId = productToRemove.value.id;
  await removeProduct(removedId);
  rows.value = rows.value.filter((item) => item.id !== removedId);
  totalItems.value = Math.max(0, totalItems.value - 1);
  if (rows.value.length === 0 && page.value > 1) {
    page.value = page.value - 1;
  } else {
    await loadProducts();
  }
  closeProductActions();
}

function closeProductActions() {
  editingProduct.value = null;
  productToRemove.value = null;
}

function clearFilters() {
  filters.description = "";
  filters.line = "";
  filters.erpCode = "";
  if (page.value !== 1) {
    page.value = 1;
  } else {
    loadProducts();
  }
}

function setPage(nextPage) {
  page.value = nextPage;
}

function setPageSize(nextSize) {
  pageSize.value = Number(nextSize) || 10;
  page.value = 1;
}

async function loadProducts() {
  loading.value = true;
  loadError.value = "";
  try {
    const params = new URLSearchParams({
      page: String(page.value),
      pageSize: String(pageSize.value)
    });
    if (filters.description.trim()) params.set("description", filters.description.trim());
    if (filters.line.trim()) params.set("line", filters.line.trim());
    if (filters.erpCode.trim()) params.set("erpCode", filters.erpCode.trim());

    const response = await apiRequest(`/api/commercial/products/paged?${params.toString()}`);
    rows.value = response.items ?? [];
    totalItems.value = response.totalItems ?? 0;
    totalPages.value = Math.max(response.totalPages ?? 1, 1);
  } catch {
    rows.value = [];
    totalItems.value = 0;
    totalPages.value = 1;
    loadError.value = "Nao foi possivel carregar os produtos.";
  } finally {
    loading.value = false;
  }
}
</script>
