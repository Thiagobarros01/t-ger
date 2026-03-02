<template>
  <div>
    <PageHeader eyebrow="CRM Comercial" title="Interações" subtitle="Histórico de contatos por cliente/deal." />

    <div class="panel">
      <h3 style="margin-top: 0">Nova interação</h3>
      <form class="form-grid" @submit.prevent="saveInteraction">
        <label>
          Cliente
          <select v-model.number="form.clienteId" required>
            <option :value="null">Selecione</option>
            <option v-for="customer in state.customers" :key="customer.id" :value="customer.id">
              {{ customer.corporateName }}
            </option>
          </select>
        </label>
        <label>
          Deal (opcional)
          <select v-model.number="form.dealId">
            <option :value="null">Sem deal</option>
            <option v-for="deal in customerDeals" :key="deal.id" :value="deal.id">
              #{{ deal.id }} - {{ deal.status }}
            </option>
          </select>
        </label>
        <label>
          Tipo
          <select v-model="form.tipo">
            <option value="LIGACAO">Ligação</option>
            <option value="WHATSAPP">WhatsApp</option>
            <option value="EMAIL">Email</option>
            <option value="REUNIAO">Reunião</option>
            <option value="OUTRO">Outro</option>
          </select>
        </label>
        <label>
          Ocorrido em
          <input v-model="form.ocorridoEm" type="datetime-local" required />
        </label>
        <label>
          Criado por
          <select v-model.number="form.criadoPor" required>
            <option :value="null">Selecione</option>
            <option v-for="user in state.users" :key="user.id" :value="user.id">
              {{ user.name }}
            </option>
          </select>
        </label>
        <label class="full">
          Descrição
          <textarea v-model="form.descricao" required />
        </label>
        <div class="full actions-row">
          <button class="btn-primary" type="submit">Salvar interação</button>
        </div>
      </form>
    </div>

    <div class="table-panel">
      <h3 style="margin-top: 0">Histórico de interações</h3>
      <div v-if="state.interactions.length === 0" class="empty-state">Nenhuma interação cadastrada.</div>
      <div v-else class="table-scroll">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Cliente</th>
              <th>Deal</th>
              <th>Tipo</th>
              <th>Descrição</th>
              <th>Ocorrido em</th>
              <th>Criado por</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="interaction in paginatedInteractions" :key="interaction.id">
              <td>#{{ interaction.id }}</td>
              <td>{{ customerName(interaction.clienteId) }}</td>
              <td>{{ interaction.dealId ? `#${interaction.dealId}` : "-" }}</td>
              <td>{{ interaction.tipo }}</td>
              <td>{{ interaction.descricao }}</td>
              <td>{{ formatDate(interaction.ocorridoEm) }}</td>
              <td>{{ userName(interaction.criadoPor) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <PaginationBar
        :page="interactionsPagination.page"
        :page-size="interactionsPagination.pageSize"
        :total-pages="interactionsPagination.totalPages"
        :total-items="interactionsPagination.totalItems"
        @update:page="interactionsPagination.setPage"
        @update:pageSize="interactionsPagination.setPageSize"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive } from "vue";
import PageHeader from "../../components/PageHeader.vue";
import PaginationBar from "../../components/PaginationBar.vue";
import { useCrmData } from "../../composables/useCrmData";
import { usePagination } from "../../composables/usePagination";

const { state, ensureLoaded, createInteraction } = useCrmData();

const form = reactive({
  clienteId: null,
  dealId: null,
  tipo: "LIGACAO",
  descricao: "",
  ocorridoEm: "",
  criadoPor: null
});

const interactionsList = computed(() => state.interactions);
const interactionsPagination = usePagination(interactionsList, 10);
const paginatedInteractions = interactionsPagination.paginatedItems;

const customerDeals = computed(() => {
  if (!form.clienteId) return [];
  return state.deals.filter((deal) => deal.clienteId === form.clienteId);
});

onMounted(async () => {
  await ensureLoaded();
  if (!form.criadoPor && state.users.length > 0) {
    form.criadoPor = state.users[0].id;
  }
});

async function saveInteraction() {
  if (!form.clienteId || !form.criadoPor || !form.ocorridoEm || !form.descricao.trim()) return;
  await createInteraction({
    clienteId: form.clienteId,
    dealId: form.dealId,
    tipo: form.tipo,
    descricao: form.descricao.trim(),
    ocorridoEm: new Date(form.ocorridoEm).toISOString(),
    criadoPor: form.criadoPor
  });
  form.dealId = null;
  form.tipo = "LIGACAO";
  form.descricao = "";
  form.ocorridoEm = "";
}

function customerName(id) {
  return state.customers.find((item) => item.id === id)?.corporateName ?? `#${id}`;
}

function userName(id) {
  return state.users.find((item) => item.id === id)?.name ?? `#${id}`;
}

function formatDate(value) {
  if (!value) return "-";
  return new Date(value).toLocaleString("pt-BR");
}
</script>
