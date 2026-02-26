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
          <p>Lista local em memoria para validar cadastro e importacao.</p>
        </div>
        <span class="tag">{{ state.customers.length }} item(ns)</span>
      </div>
      <p class="muted" v-if="state.sellers.length === 0" style="margin-top: -4px; margin-bottom: 10px">
        Cadastre vendedores em Comercial > Cadastro de Vendedor para vincular clientes por Codigo ERP.
      </p>
      <div v-if="state.customers.length === 0" class="empty-state">Nenhum cliente cadastrado.</div>
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
            <tr v-for="customer in paginatedCustomers" :key="customer.id">
              <td>{{ customer.code }}</td>
              <td>{{ customer.erpCode || "-" }}</td>
              <td>{{ customer.corporateName }}</td>
              <td>{{ customer.type }}</td>
              <td>{{ customer.tradeName || "-" }}</td>
              <td>{{ customer.email || "-" }}</td>
              <td>{{ customer.phone || "-" }}</td>
              <td>{{ customer.erpSellerCode || "-" }}</td>
              <td>
                <div class="actions-row">
                  <button type="button" @click="editCustomer(customer)">Editar</button>
                  <button type="button" @click="deleteCustomerRow(customer)">Remover</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <PaginationBar
        :page="customersPagination.page"
        :page-size="customersPagination.pageSize"
        :total-pages="customersPagination.totalPages"
        :total-items="customersPagination.totalItems"
        @update:page="customersPagination.setPage"
        @update:pageSize="customersPagination.setPageSize"
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
import { computed, reactive, onMounted, ref } from "vue";
import PageHeader from "../../components/PageHeader.vue";
import PaginationBar from "../../components/PaginationBar.vue";
import { useCommercialData } from "../../composables/useCommercialData";
import { usePagination } from "../../composables/usePagination";

const { state, ensureLoaded, addCustomer, updateCustomer, removeCustomer } = useCommercialData();
const customersList = computed(() => state.customers);
const customersPagination = usePagination(customersList, 10);
const paginatedCustomers = customersPagination.paginatedItems;
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

onMounted(() => {
  ensureLoaded();
});

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
  closeCustomerActions();
}

async function confirmCustomerRemoval() {
  if (!customerToRemove.value) return;
  await removeCustomer(customerToRemove.value.id);
  closeCustomerActions();
}

function closeCustomerActions() {
  editingCustomer.value = null;
  customerToRemove.value = null;
}
</script>
