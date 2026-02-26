<template>
  <div>
    <PageHeader eyebrow="Gestao da TI" title="Chamados" subtitle="Visualizacao dos chamados e mensagens vinculadas ao usuario logado." />

    <div class="panel" style="margin-bottom: 14px">
      <div class="actions-row">
        <button :class="{ 'btn-soft': viewMode === 'mine' }" @click="viewMode = 'mine'">Chamados para mim</button>
        <button :class="{ 'btn-soft': viewMode === 'all' }" @click="viewMode = 'all'">Todos do modulo</button>
      </div>
      <p class="muted" style="margin-top: 8px">
        Operador normalmente ve apenas os seus chamados/dados. Aqui a regra ja fica visivel na interface.
      </p>
    </div>

    <div class="ticket-layout">
      <div class="ticket-list">
        <div
          v-for="ticket in paginatedTickets"
          :key="ticket.id"
          class="ticket-item"
          :class="{ active: selectedTicket?.id === ticket.id }"
          @click="selectedTicketId = ticket.id"
        >
          <h4>#{{ ticket.id }} - {{ ticket.subject }}</h4>
          <p>{{ ticket.requester }} | {{ ticket.priority }} | {{ ticket.status }}</p>
        </div>
        <div v-if="visibleTickets.length === 0" class="empty-state">Nenhum chamado visivel para este usuario/perfil.</div>
        <PaginationBar
          :page="ticketsPagination.page"
          :page-size="ticketsPagination.pageSize"
          :total-pages="ticketsPagination.totalPages"
          :total-items="ticketsPagination.totalItems"
          @update:page="ticketsPagination.setPage"
          @update:pageSize="ticketsPagination.setPageSize"
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
        <div class="empty-state">Nenhum chamado cadastrado ainda.</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch, onMounted } from "vue";
import PageHeader from "../../components/PageHeader.vue";
import PaginationBar from "../../components/PaginationBar.vue";
import { useSession } from "../../composables/useSession";
import { useTiData } from "../../composables/useTiData";
import { usePagination } from "../../composables/usePagination";

const { currentUser } = useSession();
const { state: tiState, addTicketMessage, updateTicketStatus, ensureLoaded: ensureTiLoaded } = useTiData();

const selectedTicketId = ref(tiState.tickets[0]?.id ?? null);
const draftMessage = ref("");
const viewMode = ref("mine");

const visibleTickets = computed(() => {
  const user = currentUser.value;
  if (!user) return [];
  if (user.profile === "ADMINISTRADOR") return tiState.tickets;
  if (user.profile === "GESTOR") {
    return viewMode.value === "all"
      ? tiState.tickets
      : tiState.tickets.filter((ticket) => ticket.assignedTo === user.name || ticket.requester === user.name);
  }
  return tiState.tickets.filter((ticket) => ticket.assignedTo === user.name || ticket.requester === user.name);
});
const ticketsPagination = usePagination(visibleTickets, 8);
const paginatedTickets = ticketsPagination.paginatedItems;

const selectedTicket = computed(() => visibleTickets.value.find((ticket) => ticket.id === selectedTicketId.value) ?? visibleTickets.value[0] ?? null);

onMounted(() => {
  ensureTiLoaded();
});

watch(
  visibleTickets,
  (list) => {
    if (!list.some((ticket) => ticket.id === selectedTicketId.value)) {
      selectedTicketId.value = list[0]?.id ?? null;
    }
  },
  { immediate: true }
);

async function sendMessage() {
  const text = draftMessage.value.trim();
  if (!text || !selectedTicket.value) return;
  await addTicketMessage(selectedTicket.value.id, {
    author: currentUser.value.name,
    sentAt: new Date().toLocaleString("pt-BR"),
    message: text
  });
  draftMessage.value = "";
}

async function setTicketStatus(status) {
  if (!selectedTicket.value) return;
  await updateTicketStatus(selectedTicket.value.id, status);
}
</script>
