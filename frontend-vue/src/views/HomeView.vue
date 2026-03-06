<template>
  <div>
    <PageHeader
      eyebrow="Inicio"
      title="Painel do sistema"
      subtitle="Visao consolidada com indicadores e atalhos operacionais."
    />

    <div class="stats-row">
      <div class="stat-card">
        <span>Usuario logado</span>
        <strong style="font-size: 1rem">{{ currentUser.name }}</strong>
      </div>
      <div class="stat-card">
        <span>Perfil</span>
        <strong>{{ currentUser.profile }}</strong>
      </div>
      <div class="stat-card">
        <span>Modulos ativos</span>
        <strong>{{ visibleModules.length }}</strong>
      </div>
      <div class="stat-card">
        <span>Status do painel</span>
        <strong>{{ loading ? "Atualizando..." : "Atualizado" }}</strong>
      </div>
    </div>

    <div class="panel">
      <div class="section-head">
        <div>
          <h3>Indicadores rapidos</h3>
          <p>Resumo inicial para tomada de decisao.</p>
        </div>
        <button type="button" class="btn-soft" @click="loadMetrics" :disabled="loading">
          {{ loading ? "Atualizando..." : "Atualizar painel" }}
        </button>
      </div>

      <div class="grid-cards">
        <article class="module-card">
          <div class="module-card__icon">CL</div>
          <div>
            <h3>Clientes</h3>
            <p>Total cadastrado no comercial.</p>
            <strong>{{ metrics.customers }}</strong>
          </div>
        </article>
        <article class="module-card">
          <div class="module-card__icon">PD</div>
          <div>
            <h3>Produtos</h3>
            <p>Catalogo ativo para vendas.</p>
            <strong>{{ metrics.products }}</strong>
          </div>
        </article>
        <article class="module-card">
          <div class="module-card__icon">CR</div>
          <div>
            <h3>Deals CRM</h3>
            <p>Oportunidades no pipeline.</p>
            <strong>{{ metrics.deals }}</strong>
          </div>
        </article>
        <article class="module-card">
          <div class="module-card__icon">TI</div>
          <div>
            <h3>Chamados TI</h3>
            <p>Volume atual de tickets.</p>
            <strong>{{ metrics.tickets }}</strong>
          </div>
        </article>
        <article class="module-card">
          <div class="module-card__icon">US</div>
          <div>
            <h3>Usuarios</h3>
            <p>Contas administrativas/operacionais.</p>
            <strong>{{ metrics.users }}</strong>
          </div>
        </article>
        <article class="module-card">
          <div class="module-card__icon">AT</div>
          <div>
            <h3>Ativos TI</h3>
            <p>Inventario de equipamentos.</p>
            <strong>{{ metrics.assets }}</strong>
          </div>
        </article>
      </div>
      <p v-if="error" class="danger-text" style="margin-top: 8px;">{{ error }}</p>
    </div>

    <div class="panel">
      <div class="section-head">
        <div>
          <h3>Atalhos de trabalho</h3>
          <p>Acesso direto as telas mais usadas.</p>
        </div>
      </div>
      <div class="actions-row">
        <RouterLink class="btn-soft" to="/departamentos">Abrir departamentos</RouterLink>
        <RouterLink v-if="hasModule('COMERCIAL')" class="btn-soft" to="/gestao/comercial/crm/kanban">Ir para CRM Kanban</RouterLink>
        <RouterLink v-if="hasModule('TI')" class="btn-soft" to="/gestao/ti/ativos">Abrir equipamentos TI</RouterLink>
        <RouterLink v-if="hasModule('CONFIGURACOES')" class="btn-soft" to="/configuracoes/usuarios">Gerenciar usuarios</RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import PageHeader from "../components/PageHeader.vue";
import { useSession } from "../composables/useSession";
import { apiRequest } from "../services/api";

const { currentUser, visibleModules, canAccessModule } = useSession();
const loading = ref(false);
const error = ref("");
const metrics = reactive({
  customers: 0,
  products: 0,
  deals: 0,
  tickets: 0,
  users: 0,
  assets: 0
});

onMounted(() => {
  loadMetrics();
});

function hasModule(code) {
  return canAccessModule(currentUser.value, code);
}

async function safeCount(path, fallback = 0) {
  try {
    const data = await apiRequest(path);
    return Array.isArray(data) ? data.length : fallback;
  } catch {
    return fallback;
  }
}

async function loadMetrics() {
  loading.value = true;
  error.value = "";
  try {
    const [
      customers,
      products,
      deals,
      tickets,
      users,
      assets
    ] = await Promise.all([
      hasModule("COMERCIAL") || hasModule("VENDEDOR") ? safeCount("/api/commercial/customers") : 0,
      hasModule("COMERCIAL") ? safeCount("/api/commercial/products") : 0,
      hasModule("COMERCIAL") || hasModule("VENDEDOR") ? safeCount("/api/crm/deals") : 0,
      hasModule("TI") ? safeCount("/api/ti/tickets") : 0,
      hasModule("CONFIGURACOES") ? safeCount("/api/admin/users") : 0,
      hasModule("TI") ? safeCount("/api/ti/assets") : 0
    ]);

    metrics.customers = customers;
    metrics.products = products;
    metrics.deals = deals;
    metrics.tickets = tickets;
    metrics.users = users;
    metrics.assets = assets;
  } catch {
    error.value = "Nao foi possivel carregar todos os indicadores agora.";
  } finally {
    loading.value = false;
  }
}
</script>
