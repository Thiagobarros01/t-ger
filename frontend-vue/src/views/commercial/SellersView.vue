<template>
  <div>
    <PageHeader eyebrow="Gestao do Comercial" title="Cadastro de Vendedor" subtitle="Vendedor com codigo ERP para vinculo posterior com clientes." />

    <div class="panel">
      <div class="section-head">
        <div>
          <h3>Novo vendedor</h3>
          <p>O vinculo com clientes sera feito pelo Codigo ERP do vendedor.</p>
        </div>
      </div>
      <form class="form-grid" @submit.prevent="saveSeller">
        <label>
          Codigo (sistema)
          <input value="Gerado automaticamente ao salvar" disabled />
        </label>
        <label>
          Codigo ERP (string)
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
          <p>Base para vincular clientes pelo codigo ERP do vendedor.</p>
        </div>
        <span class="tag">{{ state.sellers.length }} item(ns)</span>
      </div>
      <div v-if="state.sellers.length === 0" class="empty-state">Nenhum vendedor cadastrado.</div>
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
            <tr v-for="seller in paginatedSellers" :key="seller.id">
              <td>{{ seller.code }}</td>
              <td>{{ seller.erpCode }}</td>
              <td>{{ seller.name }}</td>
              <td>{{ seller.email || "-" }}</td>
              <td>{{ seller.phone || "-" }}</td>
              <td>
                <div class="actions-row">
                  <button type="button" @click="editSeller(seller)">Editar</button>
                  <button type="button" @click="deleteSellerRow(seller)">Remover</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <PaginationBar
        :page="sellersPagination.page"
        :page-size="sellersPagination.pageSize"
        :total-pages="sellersPagination.totalPages"
        :total-items="sellersPagination.totalItems"
        @update:page="sellersPagination.setPage"
        @update:pageSize="sellersPagination.setPageSize"
      />
    </div>

    <div class="panel" v-if="editingSeller || sellerToRemove">
      <div class="section-head">
        <div>
          <h3>{{ editingSeller ? "Editar vendedor" : "Confirmar remocao de vendedor" }}</h3>
          <p v-if="editingSeller">Atualize os dados vinculados por ERP.</p>
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
</template>

<script setup>
import { computed, reactive, ref, onMounted } from "vue";
import PageHeader from "../../components/PageHeader.vue";
import PaginationBar from "../../components/PaginationBar.vue";
import { useCommercialData } from "../../composables/useCommercialData";
import { usePagination } from "../../composables/usePagination";

const { state, ensureLoaded, addSeller, updateSeller, removeSeller } = useCommercialData();
const sellersList = computed(() => state.sellers);
const sellersPagination = usePagination(sellersList, 10);
const paginatedSellers = sellersPagination.paginatedItems;
const duplicateErp = ref(false);
const editingSeller = ref(null);
const sellerToRemove = ref(null);
const editSellerForm = reactive({ erpCode: "", name: "", email: "", phone: "" });

const form = reactive({
  erpCode: "",
  name: "",
  email: "",
  phone: ""
});

onMounted(() => {
  ensureLoaded();
});

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
  closeSellerActions();
}

async function confirmSellerRemoval() {
  if (!sellerToRemove.value) return;
  await removeSeller(sellerToRemove.value.id);
  closeSellerActions();
}

function closeSellerActions() {
  editingSeller.value = null;
  sellerToRemove.value = null;
}
</script>
