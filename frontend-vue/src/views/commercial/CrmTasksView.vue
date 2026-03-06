<template>
  <div>
    <PageHeader eyebrow="CRM Comercial" title="Tarefas" subtitle="">
      <template #actions>
        <button class="btn-primary" type="button" @click="openCreateModal">Criar tarefa</button>
      </template>
    </PageHeader>

    <div class="stats-row">
      <div class="stat-card">
        <span>Total</span>
        <strong>{{ filteredTasks.length }}</strong>
      </div>
      <div class="stat-card">
        <span>Pendentes</span>
        <strong>{{ pendingCount }}</strong>
      </div>
      <div class="stat-card">
        <span>Concluidas</span>
        <strong>{{ doneCount }}</strong>
      </div>
      <div class="stat-card">
        <span>Atrasadas</span>
        <strong>{{ overdueCount }}</strong>
      </div>
    </div>

    <div class="table-panel">
      <div class="filters-toolbar filters-toolbar--enhanced">
        <div class="filters-toolbar__head">
          <strong>Filtros</strong>
        </div>
        <div class="filters-grid filters-grid--4">
          <label>
            Buscar tarefa
            <input v-model="filters.search" placeholder="Ex.: retorno proposta" />
          </label>
          <label>
            Status
            <select v-model="filters.status">
              <option value="">Todos</option>
              <option value="PENDENTE">Pendente</option>
              <option value="CONCLUIDA">Concluida</option>
              <option value="CANCELADA">Cancelada</option>
            </select>
          </label>
          <label>
            Prioridade
            <select v-model="filters.prioridade">
              <option value="">Todas</option>
              <option value="BAIXA">Baixa</option>
              <option value="MEDIA">Media</option>
              <option value="ALTA">Alta</option>
            </select>
          </label>
          <label>
            Responsavel
            <select v-model.number="filters.responsavelId">
              <option :value="null">Todos</option>
              <option v-for="user in state.users" :key="user.id" :value="user.id">{{ user.name }}</option>
            </select>
          </label>
        </div>
        <div class="filters-actions">
          <label class="label-inline" style="margin-right: auto">
            <input type="checkbox" v-model="filters.onlyOverdue" />
            Apenas atrasadas
          </label>
          <button type="button" class="btn-soft" @click="clearFilters">Limpar filtros</button>
        </div>
      </div>

      <div v-if="filteredTasks.length === 0" class="empty-state">Nenhuma tarefa encontrada.</div>
      <div v-else class="table-scroll">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Titulo</th>
              <th>Cliente</th>
              <th>Negociacao</th>
              <th>Status</th>
              <th>Prioridade</th>
              <th>Data limite</th>
              <th>Responsavel</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="task in paginatedTasks"
              :key="task.id"
              class="task-row-clickable"
              @click="openDetailsModal(task)"
            >
              <td>#{{ task.id }}</td>
              <td>
                <strong>{{ task.titulo }}</strong>
                <div class="muted-inline" v-if="task.descricao">{{ task.descricao }}</div>
              </td>
              <td>{{ customerName(task.clienteId) }}</td>
              <td>{{ task.dealId ? `#${task.dealId}` : "-" }}</td>
              <td><span class="tag" :class="statusTagClass(task.status)">{{ task.status }}</span></td>
              <td><span class="tag" :class="priorityTagClass(task.prioridade)">{{ task.prioridade }}</span></td>
              <td :class="{ 'danger-text': isOverdue(task) }">{{ formatDate(task.vencimentoEm) }}</td>
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

    <div class="modal-overlay" v-if="showCreateModal" @click.self="closeCreateModal">
      <div class="modal-card modal-card--small task-modal">
        <div class="section-head">
          <div>
            <h3>Nova tarefa</h3>
            <p>Cadastre rapidamente uma atividade comercial.</p>
          </div>
          <button type="button" class="btn-soft" @click="closeCreateModal">Fechar</button>
        </div>
        <form class="form-grid" @submit.prevent="saveTask">
          <label>
            Cliente
            <SearchSelect
              v-model="form.clienteId"
              :fetch-options="searchCustomerOptions"
              placeholder="Selecione cliente"
            />
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
          <label class="full">
            Titulo
            <input v-model="form.titulo" required />
          </label>
          <label>
            Prioridade
            <select v-model="form.prioridade">
              <option value="BAIXA">Baixa</option>
              <option value="MEDIA">Media</option>
              <option value="ALTA">Alta</option>
            </select>
          </label>
          <label>
            Status
            <select v-model="form.status">
              <option value="PENDENTE">Pendente</option>
              <option value="CONCLUIDA">Concluida</option>
              <option value="CANCELADA">Cancelada</option>
            </select>
          </label>
          <label>
            Vencimento
            <input v-model="form.vencimentoEm" type="datetime-local" />
          </label>
          <label>
            Responsavel
            <select v-model.number="form.responsavelId" required>
              <option :value="null">Selecione</option>
              <option v-for="user in state.users" :key="user.id" :value="user.id">
                {{ user.name }}
              </option>
            </select>
          </label>
          <label class="full">
            Descricao
            <textarea v-model="form.descricao" />
          </label>
          <div class="full actions-row">
            <button class="btn-primary" type="submit">Salvar tarefa</button>
            <button type="button" @click="closeCreateModal">Cancelar</button>
          </div>
        </form>
      </div>
    </div>

    <div class="modal-overlay" v-if="selectedTask" @click.self="closeDetailsModal">
      <div class="modal-card modal-card--small task-modal">
        <div class="section-head">
          <div>
            <h3>Tarefa #{{ selectedTask.id }}</h3>
            <p>{{ selectedTask.titulo }}</p>
          </div>
          <button type="button" class="btn-soft" @click="closeDetailsModal">Fechar</button>
        </div>
        <div class="task-detail-grid">
          <div>
            <span class="muted">Cliente</span>
            <strong>{{ customerName(selectedTask.clienteId) }}</strong>
          </div>
          <div>
            <span class="muted">Negociacao</span>
            <strong>{{ selectedTask.dealId ? `#${selectedTask.dealId}` : "-" }}</strong>
          </div>
          <div>
            <span class="muted">Status</span>
            <strong>{{ selectedTask.status }}</strong>
          </div>
          <div>
            <span class="muted">Prioridade</span>
            <strong>{{ selectedTask.prioridade }}</strong>
          </div>
          <div>
            <span class="muted">Responsavel</span>
            <strong>{{ userName(selectedTask.responsavelId) }}</strong>
          </div>
          <div>
            <span class="muted">Vencimento</span>
            <strong>{{ formatDate(selectedTask.vencimentoEm) }}</strong>
          </div>
        </div>
        <div class="actions-row" style="margin-top: 10px">
          <button type="button" class="btn-primary" :disabled="selectedTask.status === 'CONCLUIDA'" @click="changeTaskStatus('CONCLUIDA')">
            Marcar como concluida
          </button>
          <button type="button" class="btn-soft" :disabled="selectedTask.status === 'PENDENTE'" @click="changeTaskStatus('PENDENTE')">
            Voltar para pendente
          </button>
          <button type="button" :disabled="selectedTask.status === 'CANCELADA'" @click="changeTaskStatus('CANCELADA')">
            Cancelar tarefa
          </button>
        </div>
        <div class="panel" style="margin-top: 10px; padding: 10px">
          <strong>Descricao</strong>
          <p style="margin: 6px 0 0">{{ selectedTask.descricao || "Sem descricao." }}</p>
        </div>
        <div class="panel" style="margin-top: 10px; padding: 10px">
          <strong>Historico de status</strong>
          <div class="empty-state" v-if="historyLoading" style="margin-top: 8px">Carregando historico...</div>
          <div class="empty-state" v-else-if="historyError" style="margin-top: 8px">{{ historyError }}</div>
          <div class="empty-state" v-else-if="taskHistory.length === 0" style="margin-top: 8px">Sem eventos de historico.</div>
          <div class="table-scroll" v-else style="margin-top: 8px">
            <table>
              <thead>
                <tr>
                  <th>Quando</th>
                  <th>Alteracao</th>
                  <th>Quem alterou</th>
                  <th>Nota</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="event in taskHistory" :key="event.id">
                  <td>{{ formatDate(event.changedAt) }}</td>
                  <td>{{ event.previousStatus || "-" }} -> {{ event.newStatus }}</td>
                  <td>{{ event.changedByName || "-" }}</td>
                  <td>{{ event.note || "-" }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import PageHeader from "../../components/PageHeader.vue";
import PaginationBar from "../../components/PaginationBar.vue";
import SearchSelect from "../../components/SearchSelect.vue";
import { useCrmData } from "../../composables/useCrmData";
import { usePagination } from "../../composables/usePagination";

const { state, ensureLoaded, createTask, updateTaskStatus, listTaskHistory, searchCustomers, customerNameById } = useCrmData();
const showCreateModal = ref(false);
const selectedTask = ref(null);
const taskHistory = ref([]);
const historyLoading = ref(false);
const historyError = ref("");

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

const filters = reactive({
  search: "",
  status: "",
  prioridade: "",
  responsavelId: null,
  onlyOverdue: false
});

const filteredTasks = computed(() =>
  state.tasks.filter((task) => {
    const search = filters.search.trim().toLowerCase();
    if (search) {
      const title = String(task.titulo ?? "").toLowerCase();
      const desc = String(task.descricao ?? "").toLowerCase();
      if (!title.includes(search) && !desc.includes(search)) return false;
    }
    if (filters.status && task.status !== filters.status) return false;
    if (filters.prioridade && task.prioridade !== filters.prioridade) return false;
    if (filters.responsavelId != null && task.responsavelId !== filters.responsavelId) return false;
    if (filters.onlyOverdue && !isOverdue(task)) return false;
    return true;
  })
);

const tasksPagination = usePagination(filteredTasks, 10);
const paginatedTasks = tasksPagination.paginatedItems;

const customerDeals = computed(() => {
  if (!form.clienteId) return [];
  return state.deals.filter((deal) => deal.clienteId === form.clienteId);
});

const pendingCount = computed(() => filteredTasks.value.filter((task) => task.status === "PENDENTE").length);
const doneCount = computed(() => filteredTasks.value.filter((task) => task.status === "CONCLUIDA").length);
const overdueCount = computed(() => filteredTasks.value.filter((task) => isOverdue(task)).length);

onMounted(async () => {
  await ensureLoaded();
  if (!form.responsavelId && state.users.length > 0) {
    form.responsavelId = state.users[0].id;
  }
});

function openCreateModal() {
  showCreateModal.value = true;
}

function closeCreateModal() {
  showCreateModal.value = false;
}

function openDetailsModal(task) {
  selectedTask.value = task;
  loadTaskHistory(task.id);
}

function closeDetailsModal() {
  selectedTask.value = null;
  taskHistory.value = [];
  historyError.value = "";
}

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
  closeCreateModal();
}

function clearFilters() {
  filters.search = "";
  filters.status = "";
  filters.prioridade = "";
  filters.responsavelId = null;
  filters.onlyOverdue = false;
}

function customerName(id) {
  return customerNameById(id);
}

function userName(id) {
  return state.users.find((item) => item.id === id)?.name ?? `#${id}`;
}

function isOverdue(task) {
  if (!task?.vencimentoEm) return false;
  if (task.status === "CONCLUIDA" || task.status === "CANCELADA") return false;
  return new Date(task.vencimentoEm).getTime() < Date.now();
}

function formatDate(value) {
  if (!value) return "-";
  return new Date(value).toLocaleString("pt-BR");
}

function statusTagClass(status) {
  if (status === "CONCLUIDA") return "task-status--done";
  if (status === "CANCELADA") return "task-status--cancelled";
  return "task-status--pending";
}

function priorityTagClass(prioridade) {
  if (prioridade === "ALTA") return "task-priority--high";
  if (prioridade === "BAIXA") return "task-priority--low";
  return "task-priority--medium";
}

async function changeTaskStatus(status) {
  if (!selectedTask.value?.id) return;
  const updated = await updateTaskStatus(selectedTask.value.id, status);
  selectedTask.value = updated;
  await loadTaskHistory(updated.id);
}

async function loadTaskHistory(taskId) {
  if (!taskId) return;
  historyLoading.value = true;
  historyError.value = "";
  try {
    taskHistory.value = await listTaskHistory(taskId);
  } catch {
    taskHistory.value = [];
    historyError.value = "Nao foi possivel carregar o historico da tarefa.";
  } finally {
    historyLoading.value = false;
  }
}

async function searchCustomerOptions(query, page, pageSize) {
  const response = await searchCustomers(query, page, pageSize);
  return {
    items: (response.items ?? []).map((customer) => ({
      value: customer.id,
      label: customer.corporateName,
      meta: customer.code || customer.erpCode || null,
      searchText: `${customer.corporateName ?? ""} ${customer.code ?? ""} ${customer.erpCode ?? ""}`
    })),
    totalPages: response.totalPages ?? 1
  };
}
</script>
