<template>
  <div>
    <PageHeader eyebrow="Gestao da TI" title="Chamados" subtitle="Visualizacao dos chamados e mensagens vinculadas ao usuario logado." />

    <div class="panel" style="margin-bottom: 14px">
      <div class="actions-row">
        <button :class="{ 'btn-soft': viewMode === 'mine' }" @click="viewMode = 'mine'">Chamados para mim</button>
        <button :class="{ 'btn-soft': viewMode === 'all' }" @click="viewMode = 'all'">Todos do modulo</button>
      </div>

      <div class="filters-toolbar" style="margin-top: 10px; margin-bottom: 0">
        <div class="filters-grid">
          <label>
            Assunto
            <input v-model="filters.subject" placeholder="Ex.: vpn, mouse, inventario" />
          </label>
          <label>
            Status
            <input v-model="filters.status" placeholder="Ex.: pendente, resolvido" />
          </label>
          <label>
            Responsavel
            <input v-model="filters.assignedTo" placeholder="Ex.: joao suporte" />
          </label>
        </div>
        <div class="filters-actions">
          <button type="button" class="btn-soft" @click="clearFilters">Limpar filtros</button>
        </div>
      </div>
    </div>

    <div class="ticket-layout">
      <div class="ticket-list">
        <div v-if="loading" class="empty-state">Carregando chamados...</div>
        <div v-else-if="loadError" class="empty-state">{{ loadError }}</div>
        <template v-else>
          <div
            v-for="ticket in rows"
            :key="ticket.id"
            class="ticket-item"
            :class="{ active: selectedTicket?.id === ticket.id }"
            @click="selectedTicketId = ticket.id"
          >
            <h4>#{{ ticket.id }} - {{ ticket.subject }}</h4>
            <p>{{ ticket.requester }} | {{ ticket.priority }} | {{ ticket.status }}</p>
          </div>
          <div v-if="rows.length === 0" class="empty-state">Nenhum chamado encontrado.</div>
        </template>

        <PaginationBar
          :page="page"
          :page-size="pageSize"
          :total-pages="Math.max(totalPages, 1)"
          :total-items="totalItems"
          @update:page="setPage"
          @update:pageSize="setPageSize"
        />
      </div>

      <div class="ticket-chat" v-if="selectedTicket">
        <div>
          <h3 style="margin: 0 0 4px">#{{ selectedTicket.id }} - {{ selectedTicket.subject }}</h3>
          <p class="muted" style="margin: 0">Solicitante: {{ selectedTicket.requester }} | Responsavel: {{ selectedTicket.assignedTo }}</p>
          <div class="actions-row" style="margin-top: 10px">
            <span class="tag">{{ selectedTicket.status }}</span>
            <button type="button" :class="{ 'btn-soft': selectedTicket.status === 'Pendente' }" @click="setTicketStatus('Pendente')">
              Pendente
            </button>
            <button type="button" :class="{ 'btn-soft': selectedTicket.status === 'Resolvido' }" @click="setTicketStatus('Resolvido')">
              Resolvido
            </button>
            <button type="button" :class="{ 'btn-soft': selectedTicket.status === 'Fechado' }" @click="setTicketStatus('Fechado')">
              Fechado
            </button>
          </div>
        </div>

        <div class="chat-messages">
          <div v-for="message in selectedTicket.messages" :key="message.id" class="bubble" :class="{ self: message.author === currentUser.name }">
            <div>{{ message.message }}</div>
            <small>{{ message.author }} - {{ message.sentAt }}</small>
          </div>
        </div>

        <form class="form-inline" @submit.prevent="sendMessage">
          <input v-model="draftMessage" placeholder="Responder chamado..." />
          <button class="btn-primary" type="submit">Enviar</button>
        </form>
      </div>

      <div class="ticket-chat" v-else>
        <div class="empty-state">Nenhum chamado selecionado.</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from "vue";
import PageHeader from "../../components/PageHeader.vue";
import PaginationBar from "../../components/PaginationBar.vue";
import { useSession } from "../../composables/useSession";
import { useTiData } from "../../composables/useTiData";
import { apiRequest } from "../../services/api";

const { currentUser } = useSession();
const { addTicketMessage, updateTicketStatus, ensureLoaded: ensureTiLoaded } = useTiData();
const selectedTicketId = ref(null);
const draftMessage = ref("");
const viewMode = ref("mine");
const rows = ref([]);
const totalItems = ref(0);
const totalPages = ref(1);
const page = ref(1);
const pageSize = ref(8);
const loading = ref(false);
const loadError = ref("");
let filtersDebounce = null;

const filters = reactive({
  subject: "",
  status: "",
  assignedTo: ""
});

const selectedTicket = computed(() => rows.value.find((ticket) => ticket.id === selectedTicketId.value) ?? rows.value[0] ?? null);

onMounted(async () => {
  await ensureTiLoaded();
  await loadTickets();
});

onBeforeUnmount(() => {
  if (filtersDebounce) clearTimeout(filtersDebounce);
});

watch([page, pageSize, viewMode], () => {
  loadTickets();
});

watch(
  () => [filters.subject, filters.status, filters.assignedTo],
  () => {
    if (page.value !== 1) {
      page.value = 1;
      return;
    }
    if (filtersDebounce) clearTimeout(filtersDebounce);
    filtersDebounce = setTimeout(() => loadTickets(), 300);
  }
);

watch(rows, (list) => {
  if (!list.some((ticket) => ticket.id === selectedTicketId.value)) {
    selectedTicketId.value = list[0]?.id ?? null;
  }
});

async function sendMessage() {
  const text = draftMessage.value.trim();
  if (!text || !selectedTicket.value) return;
  await addTicketMessage(selectedTicket.value.id, {
    author: currentUser.value.name,
    sentAt: new Date().toLocaleString("pt-BR"),
    message: text
  });
  draftMessage.value = "";
  await loadTickets();
}

async function setTicketStatus(status) {
  if (!selectedTicket.value) return;
  await updateTicketStatus(selectedTicket.value.id, status);
  await loadTickets();
}

function clearFilters() {
  filters.subject = "";
  filters.status = "";
  filters.assignedTo = "";
  if (page.value !== 1) page.value = 1;
  else loadTickets();
}

function setPage(nextPage) {
  page.value = nextPage;
}

function setPageSize(nextSize) {
  pageSize.value = Number(nextSize) || 8;
  page.value = 1;
}

async function loadTickets() {
  loading.value = true;
  loadError.value = "";
  try {
    const params = new URLSearchParams({
      page: String(page.value),
      pageSize: String(pageSize.value)
    });

    if (filters.subject.trim()) params.set("subject", filters.subject.trim());
    if (filters.status.trim()) params.set("status", filters.status.trim());

    const isOperator = currentUser.value?.profile === "OPERADOR";
    if (filters.assignedTo.trim()) {
      params.set("assignedTo", filters.assignedTo.trim());
    } else if (!isOperator && viewMode.value === "mine") {
      params.set("assignedTo", currentUser.value?.name ?? "");
    }

    const response = await apiRequest(`/api/ti/tickets/paged?${params.toString()}`);
    rows.value = response.items ?? [];
    totalItems.value = response.totalItems ?? 0;
    totalPages.value = Math.max(response.totalPages ?? 1, 1);
  } catch {
    rows.value = [];
    totalItems.value = 0;
    totalPages.value = 1;
    loadError.value = "Nao foi possivel carregar chamados.";
  } finally {
    loading.value = false;
  }
}
</script>
