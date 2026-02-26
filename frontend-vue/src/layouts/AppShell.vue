<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand-panel">
        <p class="eyebrow">Sistema de Gestao</p>
        <h1>T-GER</h1>
        <p class="muted">Modulos por setor com permissao por perfil.</p>
      </div>

      <div class="session-panel">
        <label>Usuario logado</label>
        <div><strong>{{ currentUser?.name }}</strong></div>
        <div class="muted-inline">{{ currentUser?.email }}</div>
        <small>
          {{ currentUser?.profile === "ADMINISTRADOR" ? "Acesso total" : currentUser?.profile === "GESTOR" ? "Acesso ao modulo vinculado" : "Acesso aos proprios dados" }}
        </small>
        <button @click="doLogout">Sair</button>
      </div>

      <nav class="side-nav">
        <p class="nav-title">Gestao</p>
        <RouterLink v-if="hasModule('DIRETORIA')" to="/gestao/diretoria" class="nav-module-link">Diretoria</RouterLink>
        <details v-if="hasModule('TI')" open>
          <summary class="nav-module-summary">
            <span>TI</span>
            <span class="nav-caret"></span>
          </summary>
          <div class="submodule-list">
            <RouterLink to="/gestao/ti/ativos" class="nav-submodule-link"><span class="submodule-dot"></span>Ativos de Informatica</RouterLink>
            <RouterLink to="/gestao/ti/termos-contratos" class="nav-submodule-link"><span class="submodule-dot"></span>Termos e Contratos</RouterLink>
            <RouterLink to="/gestao/ti/chamados" class="nav-submodule-link"><span class="submodule-dot"></span>Chamados</RouterLink>
          </div>
        </details>
        <RouterLink v-if="hasModule('FINANCEIRO')" to="/gestao/financeiro" class="nav-module-link">Financeiro</RouterLink>
        <details v-if="hasModule('COMERCIAL')">
          <summary class="nav-module-summary">
            <span>Comercial</span>
            <span class="nav-caret"></span>
          </summary>
          <div class="submodule-list">
            <RouterLink to="/gestao/comercial/produtos" class="nav-submodule-link"><span class="submodule-dot"></span>Cadastro de Produto</RouterLink>
            <RouterLink to="/gestao/comercial/clientes" class="nav-submodule-link"><span class="submodule-dot"></span>Cadastro de Cliente</RouterLink>
            <RouterLink to="/gestao/comercial/vendedores" class="nav-submodule-link"><span class="submodule-dot"></span>Cadastro de Vendedor</RouterLink>
          </div>
        </details>
        <RouterLink v-if="hasModule('LOGISTICA')" to="/gestao/logistica" class="nav-module-link">Logistica</RouterLink>
        <RouterLink v-if="hasModule('COMPRAS')" to="/gestao/compras" class="nav-module-link">Compras</RouterLink>
        <RouterLink v-if="hasModule('VENDEDOR')" to="/gestao/vendedor" class="nav-module-link">Vendedor</RouterLink>
        <RouterLink v-if="hasModule('RECEBIMENTO')" to="/gestao/recebimento" class="nav-module-link">Recebimento</RouterLink>

        <p class="nav-title nav-title--spaced" v-if="hasModule('CONFIGURACOES')">Configuracoes</p>
        <details v-if="hasModule('CONFIGURACOES')" open>
          <summary class="nav-module-summary">
            <span>Administracao</span>
            <span class="nav-caret"></span>
          </summary>
          <div class="submodule-list">
            <RouterLink to="/configuracoes/usuarios" class="nav-submodule-link"><span class="submodule-dot"></span>Usuarios e Permissoes</RouterLink>
            <RouterLink to="/configuracoes/parametros" class="nav-submodule-link"><span class="submodule-dot"></span>Parametros Globais</RouterLink>
            <details>
              <summary class="nav-module-summary" style="margin-left: 6px; font-weight: 500;">
                <span>Importar Dados</span>
                <span class="nav-caret"></span>
              </summary>
              <div class="submodule-list" style="padding-left: 16px;">
                <RouterLink to="/configuracoes/importar/clientes" class="nav-submodule-link"><span class="submodule-dot"></span>Importar Clientes</RouterLink>
                <RouterLink to="/configuracoes/importar/produtos" class="nav-submodule-link"><span class="submodule-dot"></span>Importar Produtos</RouterLink>
              </div>
            </details>
          </div>
        </details>
      </nav>
    </aside>

    <main class="content">
      <header class="topbar">
        <div>
          <p class="eyebrow">Ambiente</p>
          <strong>{{ currentUser?.name }}</strong>
          <span class="muted-inline"> | {{ currentUser?.email }}</span>
        </div>
        <div class="status-pill">{{ currentUser?.profile }}</div>
      </header>
      <section class="page-wrap">
        <RouterView />
      </section>
    </main>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import { useSession } from "../composables/useSession";
import { useAuth } from "../composables/useAuth";

const router = useRouter();
const { state, currentUser, visibleModuleCodes } = useSession();
const auth = useAuth();

function hasModule(code) {
  return visibleModuleCodes.value.includes(code);
}

function doLogout() {
  auth.logout();
  router.push("/login");
}
</script>
