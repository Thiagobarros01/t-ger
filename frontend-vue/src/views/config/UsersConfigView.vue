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
          <div class="actions-row" style="align-self: end">
            <button type="button" class="btn-primary" :disabled="!selectedUser.active" @click="saveUserEmail">
              Salvar e-mail
            </button>
            <button type="button" :disabled="!selectedUser.active" @click="resetPasswordForSelected">
              Redefinir senha
            </button>
            <button type="button" :disabled="!selectedUser.active" @click="deactivateSelected">
              Desativar
            </button>
            <button type="button" :disabled="selectedUser.active" @click="reactivateSelected">
              Reativar
            </button>
          </div>
        </div>
        <p class="muted" v-if="actionMessage" style="margin-top: 8px">{{ actionMessage }}</p>
      </div>

      <div class="table-scroll">
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
            <tr v-for="user in paginatedUsers" :key="user.id">
              <td>{{ user.name }}</td>
              <td>{{ user.email }}</td>
              <td>{{ user.erpCode || "-" }}</td>
              <td><span class="tag">{{ user.profile }}</span></td>
              <td>
                <span class="tag" :class="{ 'danger-tag': !user.active }">
                  {{ user.active ? "Ativo" : "Inativo" }}
                </span>
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
        :page="usersPagination.page"
        :page-size="usersPagination.pageSize"
        :total-pages="usersPagination.totalPages"
        :total-items="usersPagination.totalItems"
        @update:page="usersPagination.setPage"
        @update:pageSize="usersPagination.setPageSize"
      />
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, watch, ref, onMounted } from "vue";
import PageHeader from "../../components/PageHeader.vue";
import PaginationBar from "../../components/PaginationBar.vue";
import { modules } from "../../data/mock";
import { useSession } from "../../composables/useSession";
import { usePagination } from "../../composables/usePagination";

const { state, ensureLoaded, addUser, updateUserEmail, resetUserPassword, deactivateUser, reactivateUser } = useSession();
const usersList = computed(() => state.allUsers);
const usersPagination = usePagination(usersList, 10);
const paginatedUsers = usersPagination.paginatedItems;

const assignableModules = computed(() => modules.filter((module) => module.code !== "CONFIGURACOES"));

const form = reactive({
  name: "",
  email: "",
  erpCode: "",
  profile: "OPERADOR",
  modules: ["TI"]
});
const selectedUserId = ref(null);
const editEmailValue = ref("");
const actionMessage = ref("");

const selectedUser = computed(() => state.allUsers.find((user) => user.id === selectedUserId.value) ?? null);

watch(
  () => form.profile,
  (value) => {
    if (value === "ADMINISTRADOR") {
      form.modules = [];
    }
    if (value === "GESTOR" && form.modules.length === 0) {
      form.modules = ["TI"];
    }
    if (value === "OPERADOR" && form.modules.length === 0) {
      form.modules = ["TI"];
    }
  }
);

onMounted(() => {
  ensureLoaded();
});

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
}

function selectUser(user) {
  selectedUserId.value = user.id;
  editEmailValue.value = user.email;
  actionMessage.value = "";
}

async function saveUserEmail() {
  if (!selectedUser.value) return;
  if (!editEmailValue.value.trim()) return;
  await updateUserEmail(selectedUser.value.id, editEmailValue.value);
  actionMessage.value = "E-mail atualizado com sucesso.";
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
}

async function reactivateSelected() {
  if (!selectedUser.value) return;
  await reactivateUser(selectedUser.value.id);
  actionMessage.value = "Usuario reativado.";
}

function effectiveRule(profile) {
  if (profile === "ADMINISTRADOR") return "Ve todos os modulos e dados";
  if (profile === "GESTOR") return "Ve apenas o(s) modulo(s) vinculado(s)";
  return "Ve apenas os proprios dados (regra por tela/API)";
}

function formatModules(moduleCodes) {
  return moduleCodes
    .map((code) => modules.find((item) => item.code === code)?.label ?? code)
    .join(", ");
}
</script>
