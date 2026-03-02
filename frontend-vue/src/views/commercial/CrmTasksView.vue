<template>
  <div>
    <PageHeader eyebrow="CRM Comercial" title="Tarefas" subtitle="Gestão de pendências por cliente/deal e responsável." />

    <div class="panel">
      <h3 style="margin-top: 0">Nova tarefa</h3>
      <form class="form-grid" @submit.prevent="saveTask">
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
          Título
          <input v-model="form.titulo" required />
        </label>
        <label>
          Prioridade
          <select v-model="form.prioridade">
            <option value="BAIXA">Baixa</option>
            <option value="MEDIA">Média</option>
            <option value="ALTA">Alta</option>
          </select>
        </label>
        <label>
          Status
          <select v-model="form.status">
            <option value="PENDENTE">Pendente</option>
            <option value="CONCLUIDA">Concluída</option>
            <option value="CANCELADA">Cancelada</option>
          </select>
        </label>
        <label>
          Vencimento
          <input v-model="form.vencimentoEm" type="datetime-local" />
        </label>
        <label>
          Responsável
          <select v-model.number="form.responsavelId" required>
            <option :value="null">Selecione</option>
            <option v-for="user in state.users" :key="user.id" :value="user.id">
              {{ user.name }}
            </option>
          </select>
        </label>
        <label class="full">
          Descrição
          <textarea v-model="form.descricao" />
        </label>
        <div class="full actions-row">
          <button class="btn-primary" type="submit">Salvar tarefa</button>
        </div>
      </form>
    </div>

    <div class="table-panel">
      <h3 style="margin-top: 0">Lista de tarefas</h3>
      <div v-if="state.tasks.length === 0" class="empty-state">Nenhuma tarefa cadastrada.</div>
      <div v-else class="table-scroll">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Título</th>
              <th>Cliente</th>
              <th>Deal</th>
              <th>Prioridade</th>
              <th>Status</th>
              <th>Vencimento</th>
              <th>Responsável</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="task in paginatedTasks" :key="task.id">
              <td>#{{ task.id }}</td>
              <td>{{ task.titulo }}</td>
              <td>{{ customerName(task.clienteId) }}</td>
              <td>{{ task.dealId ? `#${task.dealId}` : "-" }}</td>
              <td>{{ task.prioridade }}</td>
              <td><span class="tag">{{ task.status }}</span></td>
              <td>{{ formatDate(task.vencimentoEm) }}</td>
              <td>{{ userName(task.responsavelId) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <PaginationBar
        :page="tasksPagination.page"
        :page-size="tasksPagination.pageSize"
        :total-pages="tasksPagination.totalPages"
        :total-items="tasksPagination.totalItems"
        @update:page="tasksPagination.setPage"
        @update:pageSize="tasksPagination.setPageSize"
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

const { state, ensureLoaded, createTask } = useCrmData();

const form = reactive({
  clienteId: null,
  dealId: null,
  titulo: "",
  descricao: "",
  prioridade: "MEDIA",
  status: "PENDENTE",
  vencimentoEm: "",
  responsavelId: null
});

const tasksList = computed(() => state.tasks);
const tasksPagination = usePagination(tasksList, 10);
const paginatedTasks = tasksPagination.paginatedItems;

const customerDeals = computed(() => {
  if (!form.clienteId) return [];
  return state.deals.filter((deal) => deal.clienteId === form.clienteId);
});

onMounted(async () => {
  await ensureLoaded();
  if (!form.responsavelId && state.users.length > 0) {
    form.responsavelId = state.users[0].id;
  }
});

async function saveTask() {
  if (!form.clienteId || !form.titulo.trim() || !form.responsavelId) return;
  await createTask({
    clienteId: form.clienteId,
    dealId: form.dealId,
    titulo: form.titulo.trim(),
    descricao: form.descricao.trim(),
    prioridade: form.prioridade,
    status: form.status,
    vencimentoEm: form.vencimentoEm ? new Date(form.vencimentoEm).toISOString() : null,
    responsavelId: form.responsavelId
  });

  form.dealId = null;
  form.titulo = "";
  form.descricao = "";
  form.prioridade = "MEDIA";
  form.status = "PENDENTE";
  form.vencimentoEm = "";
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
