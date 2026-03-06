<template>
  <div class="app-shell app-shell--top">
    <header class="shell-topnav">
      <div class="shell-topnav__brand">
        <div class="brand-logo">TG</div>
        <strong>T-GER</strong>
      </div>

      <nav class="shell-topnav__menu">
        <RouterLink to="/" class="topnav-link">Inicio</RouterLink>
        <RouterLink to="/departamentos" class="topnav-link">Departamentos</RouterLink>
        <RouterLink to="/relatorios" class="topnav-link">Relatorios</RouterLink>
        <RouterLink v-if="hasModule('CONFIGURACOES')" to="/configuracoes/parametros" class="topnav-link">Configuracoes</RouterLink>
      </nav>

      <div class="shell-topnav__user">
        <div class="user-badge">{{ userInitials }}</div>
        <div class="user-meta">
          <strong>{{ currentUser?.name }}</strong>
          <span>{{ currentUser?.profile }}</span>
        </div>
        <button type="button" class="btn-soft" @click="doLogout">Sair</button>
      </div>
    </header>

    <nav class="shell-subnav" v-if="contextLinks.length > 0">
      <span class="shell-subnav__label">{{ currentModuleLabel }}</span>
      <RouterLink
        v-for="item in contextLinks"
        :key="item.to"
        :to="item.to"
        class="subnav-link"
      >
        {{ item.label }}
      </RouterLink>
    </nav>

    <main class="content content--top">
      <section class="page-wrap page-wrap--top">
        <RouterView />
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useSession } from "../composables/useSession";
import { useAuth } from "../composables/useAuth";

const route = useRoute();
const router = useRouter();
const { currentUser, visibleModuleCodes } = useSession();
const auth = useAuth();

const moduleMenus = {
  TI: {
    label: "TI",
    links: [
      { label: "Ativos", to: "/gestao/ti/ativos" },
      { label: "Termos", to: "/gestao/ti/termos-contratos" },
      { label: "Chamados", to: "/gestao/ti/chamados" }
    ]
  },
  COMERCIAL: {
    label: "Comercial",
    links: [
      { label: "Produtos", to: "/gestao/comercial/produtos" },
      { label: "Clientes", to: "/gestao/comercial/clientes" },
      { label: "Vendedores", to: "/gestao/comercial/vendedores" },
      { label: "CRM Kanban", to: "/gestao/comercial/crm/kanban" },
      { label: "Interacoes", to: "/gestao/comercial/crm/interacoes" },
      { label: "Tarefas", to: "/gestao/comercial/crm/tarefas" }
    ]
  },
  VENDEDOR: {
    label: "Vendedor",
    links: [
      { label: "Carteira", to: "/gestao/vendedor/carteira" },
      { label: "CRM Kanban", to: "/gestao/vendedor/crm/kanban" },
      { label: "Interacoes", to: "/gestao/vendedor/crm/interacoes" },
      { label: "Tarefas", to: "/gestao/vendedor/crm/tarefas" }
    ]
  },
  CONFIGURACOES: {
    label: "Configuracoes",
    links: [
      { label: "Usuarios", to: "/configuracoes/usuarios" },
      { label: "Parametros", to: "/configuracoes/parametros" },
      { label: "Importar Clientes", to: "/configuracoes/importar/clientes" },
      { label: "Importar Produtos", to: "/configuracoes/importar/produtos" },
      { label: "Importar Vendedores", to: "/configuracoes/importar/vendedores" },
      { label: "Importar Historico", to: "/configuracoes/importar/historico-vendas" }
    ]
  }
};

const userInitials = computed(() => {
  const name = currentUser.value?.name ?? "TGER";
  return name
    .split(" ")
    .slice(0, 2)
    .map((part) => part[0] ?? "")
    .join("")
    .toUpperCase();
});

function hasModule(code) {
  return visibleModuleCodes.value.includes(code);
}

const currentModuleCode = computed(() => route.meta?.moduleCode ?? null);

const currentModuleLabel = computed(() => {
  const moduleCode = currentModuleCode.value;
  return moduleCode && moduleMenus[moduleCode] ? moduleMenus[moduleCode].label : "";
});

const contextLinks = computed(() => {
  const moduleCode = currentModuleCode.value;
  if (!moduleCode || !moduleMenus[moduleCode]) return [];
  if (!hasModule(moduleCode)) return [];
  return moduleMenus[moduleCode].links;
});

function doLogout() {
  auth.logout();
  router.push("/login");
}
</script>
