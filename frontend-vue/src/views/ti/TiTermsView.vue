<template>
  <div>
    <PageHeader
      eyebrow="Gestao da TI"
      title="Termos e Contratos"
      subtitle="Cadastro e consulta de termos."
    />

    <div class="stats-row">
      <div class="stat-card">
        <span>Total</span>
        <strong>{{ visibleTerms.length }}</strong>
      </div>
      <div class="stat-card">
        <span>CLT</span>
        <strong>{{ visibleTerms.filter((t) => t.type === 'CLT').length }}</strong>
      </div>
      <div class="stat-card">
        <span>Comodato</span>
        <strong>{{ visibleTerms.filter((t) => t.type === 'COMODATO').length }}</strong>
      </div>
    </div>

    <div class="panel" v-if="currentUser.profile !== 'OPERADOR'">
      <h3 style="margin-top: 0; display:flex; align-items:center; gap:8px;">
        Cadastrar termo / contrato
        <button
          type="button"
          class="hint-icon"
          title="Termo com nome padrao 'Termo de Responsabilidade'. O cadastro define tipo (CLT/Comodato), vinculacao, status e caminho do documento."
          aria-label="Ajuda sobre cadastro de termos"
        >
          ?
        </button>
      </h3>
      <form class="form-grid" @submit.prevent="saveTerm">
        <label>
          <span class="label-inline">
            Termo (padrao)
            <button
              type="button"
              class="hint-icon"
              title="Nome padrao do documento. O tipo e a vinculacao sao os dados que mudam no cadastro."
              aria-label="Ajuda sobre termo padrao"
            >
              ?
            </button>
          </span>
          <input value="Termo de Responsabilidade" disabled />
        </label>
        <label>
          Tipo
          <select v-model="form.type">
            <option value="CLT">CLT</option>
            <option value="COMODATO">Comodato</option>
          </select>
        </label>
        <label>
          Vinculado a (usuario)
          <select v-model="form.linkedUserName">
            <option value="">Selecione</option>
            <option v-for="user in sessionState.allUsers" :key="user.id" :value="user.name">{{ user.name }}</option>
          </select>
        </label>
        <label>
          Data de inicio
          <input v-model="form.startDate" type="date" />
        </label>
        <label>
          Status
          <select v-model="form.status">
            <option value="Ativo">Ativo</option>
            <option value="Concluido">Concluido</option>
          </select>
        </label>
        <label>
          Caminho do documento (string)
          <input v-model="form.documentPath" placeholder="C:/documentos/ti/comodato/arquivo.pdf" />
        </label>
        <div class="full actions-row">
          <button class="btn-primary" type="submit">Salvar termo</button>
        </div>
      </form>
    </div>

    <div class="table-panel">
      <h3 style="margin-top: 0">Lista (padrao: todos)</h3>
      <div v-if="visibleTerms.length === 0" class="empty-state" style="margin-bottom: 10px">
        Nenhum termo cadastrado ainda.
      </div>
      <div class="table-scroll">
        <table>
          <thead>
            <tr>
              <th>Termo</th>
              <th>Tipo</th>
              <th>Vinculado a</th>
              <th>Inicio</th>
              <th>Status</th>
              <th>Caminho do documento</th>
              <th>Acoes</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="term in paginatedTerms" :key="term.id">
              <td>{{ term.defaultTermName }}</td>
              <td><span class="tag">{{ term.type }}</span></td>
              <td>{{ term.linkedUserName }}</td>
              <td>{{ formatDate(term.startDate) }}</td>
              <td><span class="tag" :class="{ 'danger-tag': term.status !== 'Ativo' }">{{ term.status }}</span></td>
              <td><code>{{ term.documentPath || "-" }}</code></td>
              <td>
                <div class="actions-row">
                  <button type="button" @click="editTerm(term)">Editar</button>
                  <button type="button" @click="deleteTermRow(term)">Remover</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <PaginationBar
        :page="termsPagination.page"
        :page-size="termsPagination.pageSize"
        :total-pages="termsPagination.totalPages"
        :total-items="termsPagination.totalItems"
        @update:page="termsPagination.setPage"
        @update:pageSize="termsPagination.setPageSize"
      />
    </div>

    <div class="panel" v-if="editingTerm || termToRemove">
      <div class="section-head">
        <div>
          <h3>{{ editingTerm ? "Editar termo" : "Confirmar remocao de termo" }}</h3>
          <p v-if="editingTerm">Edite sem sair da listagem.</p>
          <p v-else>Remocao fisica do termo selecionado.</p>
        </div>
        <button type="button" class="btn-soft" @click="closeTermActions">Fechar</button>
      </div>

      <form v-if="editingTerm" class="form-grid" @submit.prevent="submitTermEdit">
        <label>
          Tipo
          <select v-model="editTermForm.type">
            <option value="CLT">CLT</option>
            <option value="COMODATO">Comodato</option>
          </select>
        </label>
        <label>
          Vinculado a
          <input v-model="editTermForm.linkedUserName" />
        </label>
        <label>
          Data de inicio
          <input v-model="editTermForm.startDate" type="date" />
        </label>
        <label>
          Status
          <select v-model="editTermForm.status">
            <option value="Ativo">Ativo</option>
            <option value="Concluido">Concluido</option>
          </select>
        </label>
        <label class="full">
          Caminho do documento
          <input v-model="editTermForm.documentPath" />
        </label>
        <div class="full actions-row">
          <button class="btn-primary" type="submit">Salvar alteracoes</button>
          <button type="button" @click="closeTermActions">Cancelar</button>
        </div>
      </form>

      <div v-else-if="termToRemove" class="crud-box">
        <p><strong>{{ termToRemove.defaultTermName }}</strong></p>
        <p class="muted">{{ termToRemove.type }} | {{ termToRemove.linkedUserName }}</p>
        <div class="actions-row">
          <button type="button" class="btn-soft" @click="closeTermActions">Cancelar</button>
          <button type="button" @click="confirmTermRemoval">Remover</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, onMounted, ref } from "vue";
import PageHeader from "../../components/PageHeader.vue";
import PaginationBar from "../../components/PaginationBar.vue";
import { useSession } from "../../composables/useSession";
import { useTiData } from "../../composables/useTiData";
import { usePagination } from "../../composables/usePagination";

const { currentUser, state: sessionState, ensureLoaded: ensureUsersLoaded } = useSession();
const { state, addTerm, updateTerm, removeTerm, ensureLoaded: ensureTiLoaded } = useTiData();
const editingTerm = ref(null);
const termToRemove = ref(null);
const editTermForm = reactive({
  type: "COMODATO",
  linkedUserName: "",
  startDate: "",
  status: "Ativo",
  documentPath: ""
});

const form = reactive({
  type: "COMODATO",
  linkedUserName: "",
  startDate: "",
  status: "Ativo",
  documentPath: ""
});

const visibleTerms = computed(() => {
  if (currentUser.value.profile !== "OPERADOR") return state.terms;
  const firstName = currentUser.value.name.split(" ")[0].toLowerCase();
  return state.terms.filter((term) => term.linkedUserName.toLowerCase().includes(firstName));
});
const termsPagination = usePagination(visibleTerms, 10);
const paginatedTerms = termsPagination.paginatedItems;

onMounted(() => {
  ensureUsersLoaded();
  ensureTiLoaded();
});

async function saveTerm() {
  if (!form.linkedUserName) return;
  await addTerm({
    type: form.type,
    linkedUserName: form.linkedUserName,
    startDate: form.startDate || new Date().toISOString().slice(0, 10),
    status: form.status,
    documentPath: form.documentPath.trim()
  });
  form.type = "COMODATO";
  form.linkedUserName = "";
  form.startDate = "";
  form.status = "Ativo";
  form.documentPath = "";
}

function formatDate(date) {
  if (!date) return "-";
  return new Date(`${date}T00:00:00`).toLocaleDateString("pt-BR");
}

function editTerm(term) {
  editingTerm.value = term;
  termToRemove.value = null;
  editTermForm.type = term.type ?? "COMODATO";
  editTermForm.linkedUserName = term.linkedUserName ?? "";
  editTermForm.startDate = term.startDate ?? "";
  editTermForm.status = term.status ?? "Ativo";
  editTermForm.documentPath = term.documentPath ?? "";
}

function deleteTermRow(term) {
  termToRemove.value = term;
  editingTerm.value = null;
}

async function submitTermEdit() {
  if (!editingTerm.value || !editTermForm.linkedUserName.trim()) return;
  await updateTerm(editingTerm.value.id, { ...editTermForm });
  closeTermActions();
}

async function confirmTermRemoval() {
  if (!termToRemove.value) return;
  await removeTerm(termToRemove.value.id);
  closeTermActions();
}

function closeTermActions() {
  editingTerm.value = null;
  termToRemove.value = null;
}
</script>
