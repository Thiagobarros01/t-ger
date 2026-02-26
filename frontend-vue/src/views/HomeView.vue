<template>
  <div>
    <PageHeader
      eyebrow="Visao Geral"
      title="Modulos disponiveis"
      subtitle="A exibicao muda conforme o perfil e os modulos vinculados ao usuario."
    />

    <div class="stats-row">
      <div class="stat-card">
        <span>Perfil atual</span>
        <strong>{{ currentUser.profile }}</strong>
      </div>
      <div class="stat-card">
        <span>Modulos visiveis</span>
        <strong>{{ visibleModules.length }}</strong>
      </div>
      <div class="stat-card">
        <span>Usuario</span>
        <strong style="font-size: 1rem">{{ currentUser.name }}</strong>
      </div>
    </div>

    <div class="panel">
      <div class="grid-cards">
        <ModuleCard
          v-for="module in visibleModules"
          :key="module.code"
          :title="module.label"
          :description="module.description"
          :icon="moduleIcon(module.code)"
          :to="moduleRoute(module.code)"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import PageHeader from "../components/PageHeader.vue";
import ModuleCard from "../components/ModuleCard.vue";
import { useSession } from "../composables/useSession";

const { currentUser, visibleModules } = useSession();

function moduleIcon(code) {
  return (
    {
      DIRETORIA: "DR",
      TI: "TI",
      FINANCEIRO: "FN",
      COMERCIAL: "CM",
      LOGISTICA: "LG",
      COMPRAS: "CP",
      VENDEDOR: "VD",
      RECEBIMENTO: "RC",
      CONFIGURACOES: "CF"
    }[code] ?? "MD"
  );
}

function moduleRoute(code) {
  const map = {
    DIRETORIA: "/gestao/diretoria",
    TI: "/gestao/ti/ativos",
    FINANCEIRO: "/gestao/financeiro",
    COMERCIAL: "/gestao/comercial/produtos",
    LOGISTICA: "/gestao/logistica",
    COMPRAS: "/gestao/compras",
    VENDEDOR: "/gestao/vendedor",
    RECEBIMENTO: "/gestao/recebimento",
    CONFIGURACOES: "/configuracoes/parametros"
  };
  return map[code] ?? "/";
}
</script>
