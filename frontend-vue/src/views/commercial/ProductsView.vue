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
          <p>Lista local em memoria para validar cadastro e UX.</p>
        </div>
        <span class="tag">{{ state.products.length }} item(ns)</span>
      </div>
      <div v-if="state.products.length === 0" class="empty-state">Nenhum produto cadastrado.</div>
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
            <tr v-for="product in paginatedProducts" :key="product.id">
              <td>{{ product.code }}</td>
              <td>{{ product.erpCode || "-" }}</td>
              <td>{{ product.description }}</td>
              <td>{{ product.department || "-" }}</td>
              <td>{{ product.category || "-" }}</td>
              <td>{{ product.line || "-" }}</td>
              <td>{{ product.manufacturer || "-" }}</td>
              <td>
                <div class="actions-row">
                  <button type="button" @click="editProduct(product)">Editar</button>
                  <button type="button" @click="deleteProductRow(product)">Remover</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <PaginationBar
        :page="productsPagination.page"
        :page-size="productsPagination.pageSize"
        :total-pages="productsPagination.totalPages"
        :total-items="productsPagination.totalItems"
        @update:page="productsPagination.setPage"
        @update:pageSize="productsPagination.setPageSize"
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
import { computed, reactive, onMounted, ref } from "vue";
import PageHeader from "../../components/PageHeader.vue";
import PaginationBar from "../../components/PaginationBar.vue";
import { useCommercialData } from "../../composables/useCommercialData";
import { usePagination } from "../../composables/usePagination";

const { state, ensureLoaded, addProduct, updateProduct, removeProduct } = useCommercialData();
const productsList = computed(() => state.products);
const productsPagination = usePagination(productsList, 10);
const paginatedProducts = productsPagination.paginatedItems;
const editingProduct = ref(null);
const productToRemove = ref(null);
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
  ensureLoaded();
});

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
  closeProductActions();
}

async function confirmProductRemoval() {
  if (!productToRemove.value) return;
  await removeProduct(productToRemove.value.id);
  closeProductActions();
}

function closeProductActions() {
  editingProduct.value = null;
  productToRemove.value = null;
}
</script>
