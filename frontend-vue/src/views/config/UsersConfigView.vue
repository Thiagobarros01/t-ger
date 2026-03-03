<template>
  <div>
    <PageHeader
      eyebrow="Configuracoes"
      title="Usuarios e Permissoes"
      subtitle="Administrador ve tudo. Gestor ve modulo vinculado. Operador ve apenas seus dados."
    />

    <div class="panel">
      <h3 style="margin-top: 0">Cadastro de usuario</h3>
      <form class="form-grid" @submit.prevent="submitUser">
        <label>
          Nome
          <input v-model="form.name" required />
        </label>
        <label>
          E-mail
          <input v-model="form.email" type="email" required />
        </label>
        <label>
          Codigo ERP (string)
          <input v-model="form.erpCode" placeholder="Pode conter letras/numeros" />
        </label>
        <label>
          Perfil
          <select v-model="form.profile">
            <option value="ADMINISTRADOR">Administrador</option>
            <option value="GESTOR">Gestor</option>
            <option value="OPERADOR">Operador</option>
          </select>
        </label>
        <label>
          Modulos permitidos
          <select multiple v-model="form.modules" :disabled="form.profile === 'ADMINISTRADOR'" style="min-height: 110px">
            <option v-for="module in assignableModules" :key="module.code" :value="module.code">
              {{ module.label }}
            </option>
          </select>
        </label>
        <div class="full actions-row">
          <button class="btn-primary" type="submit">Cadastrar usuario</button>
          <span class="muted" v-if="form.profile === 'ADMINISTRADOR'">Administrador recebe acesso total automaticamente.</span>
        </div>
      </form>
    </div>

    <div class="table-panel">
      <h3 style="margin-top: 0">Usuarios cadastrados</h3>

      <div class="filters-toolbar">
        <div class="filters-grid">
          <label>
            Nome
            <input v-model="filters.name" placeholder="Ex.: joao" />
          </label>
          <label>
            E-mail
            <input v-model="filters.email" placeholder="Ex.: usuario@empresa.com" />
          </label>
          <label>
            Perfil
            <select v-model="filters.profile">
              <option value="">Todos</option>
              <option value="ADMINISTRADOR">ADMINISTRADOR</option>
              <option value="GESTOR">GESTOR</option>
              <option value="OPERADOR">OPERADOR</option>
            </select>
          </label>
          <label>
            Status
            <select v-model="filters.active">
              <option value="">Todos</option>
              <option value="true">Ativo</option>
              <option value="false">Inativo</option>
            </select>
          </label>
        </div>
        <div class="filters-actions">
          <button type="button" class="btn-soft" @click="clearFilters">Limpar filtros</button>
        </div>
      </div>

      <div class="panel" style="padding: 10px; margin-bottom: 10px" v-if="selectedUser">
        <div class="section-head" style="margin-bottom: 8px">
          <div>
            <h3 style="font-size: 1rem">Acoes do usuario</h3>
            <p>{{ selectedUser.name }} ({{ selectedUser.profile }})</p>
          </div>
          <span class="tag" :class="{ 'danger-tag': !selectedUser.active }">
            {{ selectedUser.active ? "Ativo" : "Inativo" }}
          </span>
        </div>
        <div class="form-grid">
          <label>
            Editar e-mail
            <input v-model="editEmailValue" type="email" :disabled="!selectedUser.active" />
          </label>
          <label>
            Perfil
            <select v-model="editPermissions.profile" :disabled="!selectedUser.active">
              <option value="ADMINISTRADOR">Administrador</option>
              <option value="GESTOR">Gestor</option>
              <option value="OPERADOR">Operador</option>
            </select>
          </label>
          <label>
            Modulos permitidos
            <select
              multiple
              v-model="editPermissions.modules"
              :disabled="!selectedUser.active || editPermissions.profile === 'ADMINISTRADOR'"
              style="min-height: 110px"
            >
              <option v-for="module in assignableModules" :key="module.code" :value="module.code">
                {{ module.label }}
              </option>
            </select>
          </label>
          <div class="actions-row" style="align-self: end">
            <button type="button" class="btn-primary" :disabled="!selectedUser.active" @click="saveUserEmail">Salvar e-mail</button>
            <button type="button" class="btn-primary" :disabled="!selectedUser.active" @click="saveUserPermissions">Salvar permissoes</button>
            <button type="button" :disabled="!selectedUser.active" @click="resetPasswordForSelected">Redefinir senha</button>
            <button type="button" :disabled="!selectedUser.active" @click="deactivateSelected">Desativar</button>
            <button type="button" :disabled="selectedUser.active" @click="reactivateSelected">Reativar</button>
          </div>
        </div>
        <p class="muted" v-if="actionMessage" style="margin-top: 8px">{{ actionMessage }}</p>
      </div>

      <div v-if="loading" class="empty-state">Carregando usuarios...</div>
      <div v-else-if="loadError" class="empty-state">{{ loadError }}</div>
      <div v-else class="table-scroll">
        <table>
          <thead>
            <tr>
              <th>Nome</th>
              <th>E-mail</th>
              <th>Codigo ERP</th>
              <th>Perfil</th>
              <th>Status</th>
              <th>Modulos</th>
              <th>Regra efetiva</th>
              <th>Acoes</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in rows" :key="user.id">
              <td>{{ user.name }}</td>
              <td>{{ user.email }}</td>
              <td>{{ user.erpCode || "-" }}</td>
              <td><span class="tag">{{ user.profile }}</span></td>
              <td>
                <span class="tag" :class="{ 'danger-tag': !user.active }">{{ user.active ? "Ativo" : "Inativo" }}</span>
              </td>
              <td>
                <span v-if="user.profile === 'ADMINISTRADOR'" class="tag">Todos</span>
                <span v-else>{{ formatModules(user.modules) }}</span>
              </td>
              <td>{{ effectiveRule(user.profile) }}</td>
              <td>
                <button type="button" @click="selectUser(user)">Gerenciar</button>
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
  </div>
</template>

<script setup>
import { computed, reactive, watch, ref, onMounted, onBeforeUnmount } from "vue";
import PageHeader from "../../components/PageHeader.vue";
import PaginationBar from "../../components/PaginationBar.vue";
import { modules } from "../../data/mock";
import { useSession } from "../../composables/useSession";
import { apiRequest } from "../../services/api";

const { addUser, updateUserEmail, updateUserPermissions, resetUserPassword, deactivateUser, reactivateUser } = useSession();
const rows = ref([]);
const totalItems = ref(0);
const totalPages = ref(1);
const page = ref(1);
const pageSize = ref(10);
const loading = ref(false);
const loadError = ref("");
let filtersDebounce = null;

const assignableModules = computed(() => modules.filter((module) => module.code !== "CONFIGURACOES"));

const filters = reactive({
  name: "",
  email: "",
  profile: "",
  active: ""
});

const form = reactive({
  name: "",
  email: "",
  erpCode: "",
  profile: "OPERADOR",
  modules: ["TI"]
});
const selectedUserId = ref(null);
const editEmailValue = ref("");
const editPermissions = reactive({
  profile: "OPERADOR",
  modules: []
});
const actionMessage = ref("");

const selectedUser = computed(() => rows.value.find((user) => user.id === selectedUserId.value) ?? null);

watch(
  () => form.profile,
  (value) => {
    if (value === "ADMINISTRADOR") form.modules = [];
    if ((value === "GESTOR" || value === "OPERADOR") && form.modules.length === 0) form.modules = ["TI"];
  }
);

watch(
  () => editPermissions.profile,
  (value) => {
    if (value === "ADMINISTRADOR") editPermissions.modules = [];
    if (value !== "ADMINISTRADOR" && editPermissions.modules.length === 0) editPermissions.modules = ["TI"];
  }
);

onMounted(() => {
  loadUsers();
});

onBeforeUnmount(() => {
  if (filtersDebounce) clearTimeout(filtersDebounce);
});

watch([page, pageSize], () => {
  loadUsers();
});

watch(
  () => [filters.name, filters.email, filters.profile, filters.active],
  () => {
    if (page.value !== 1) {
      page.value = 1;
      return;
    }
    if (filtersDebounce) clearTimeout(filtersDebounce);
    filtersDebounce = setTimeout(() => loadUsers(), 300);
  }
);

async function submitUser() {
  await addUser({
    name: form.name.trim(),
    email: form.email.trim(),
    erpCode: form.erpCode.trim(),
    profile: form.profile,
    modules: form.profile === "ADMINISTRADOR" ? [] : [...form.modules]
  });

  form.name = "";
  form.email = "";
  form.erpCode = "";
  form.profile = "OPERADOR";
  form.modules = ["TI"];
  await loadUsers();
}

function selectUser(user) {
  selectedUserId.value = user.id;
  editEmailValue.value = user.email;
  editPermissions.profile = user.profile;
  editPermissions.modules = [...(user.modules ?? [])];
  actionMessage.value = "";
}

async function saveUserEmail() {
  if (!selectedUser.value) return;
  if (!editEmailValue.value.trim()) return;
  await updateUserEmail(selectedUser.value.id, editEmailValue.value);
  actionMessage.value = "E-mail atualizado com sucesso.";
  await loadUsers();
}

async function saveUserPermissions() {
  if (!selectedUser.value) return;
  await updateUserPermissions(selectedUser.value.id, editPermissions.profile, editPermissions.modules);
  actionMessage.value = "Permissoes atualizadas com sucesso.";
  await loadUsers();
}

async function resetPasswordForSelected() {
  if (!selectedUser.value) return;
  await resetUserPassword(selectedUser.value.id);
  actionMessage.value = "Senha redefinida. Senha temporaria de teste: 123456.";
}

async function deactivateSelected() {
  if (!selectedUser.value) return;
  await deactivateUser(selectedUser.value.id);
  actionMessage.value = "Usuario desativado (sem exclusao).";
  await loadUsers();
}

async function reactivateSelected() {
  if (!selectedUser.value) return;
  await reactivateUser(selectedUser.value.id);
  actionMessage.value = "Usuario reativado.";
  await loadUsers();
}

function effectiveRule(profile) {
  if (profile === "ADMINISTRADOR") return "Ve todos os modulos e dados";
  if (profile === "GESTOR") return "Ve apenas o(s) modulo(s) vinculado(s)";
  return "Ve apenas os proprios dados (regra por tela/API)";
}

function formatModules(moduleCodes) {
  return moduleCodes.map((code) => modules.find((item) => item.code === code)?.label ?? code).join(", ");
}

function clearFilters() {
  filters.name = "";
  filters.email = "";
  filters.profile = "";
  filters.active = "";
  if (page.value !== 1) page.value = 1;
  else loadUsers();
}

function setPage(nextPage) {
  page.value = nextPage;
}

function setPageSize(nextSize) {
  pageSize.value = Number(nextSize) || 10;
  page.value = 1;
}

async function loadUsers() {
  loading.value = true;
  loadError.value = "";
  try {
    const params = new URLSearchParams({
      page: String(page.value),
      pageSize: String(pageSize.value)
    });
    if (filters.name.trim()) params.set("name", filters.name.trim());
    if (filters.email.trim()) params.set("email", filters.email.trim());
    if (filters.profile) params.set("profile", filters.profile);
    if (filters.active) params.set("active", filters.active);
    const response = await apiRequest(`/api/admin/users/paged?${params.toString()}`);
    rows.value = response.items ?? [];
    totalItems.value = response.totalItems ?? 0;
    totalPages.value = Math.max(response.totalPages ?? 1, 1);
    if (!rows.value.some((user) => user.id === selectedUserId.value)) {
      selectedUserId.value = null;
    }
  } catch {
    rows.value = [];
    totalItems.value = 0;
    totalPages.value = 1;
    loadError.value = "Nao foi possivel carregar usuarios.";
  } finally {
    loading.value = false;
  }
}
</script>
