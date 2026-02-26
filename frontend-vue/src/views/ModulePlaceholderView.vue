<template>
  <div>
    <PageHeader
      eyebrow="Modulo"
      :title="moduleLabel"
      subtitle="Tela base criada para validar navegacao. Os submodulos especificos podem ser adicionados na proxima etapa."
    />
    <div class="panel">
      <div class="empty-state">
        Estrutura pronta para este setor. Posso incluir dashboards, CRUDs e fluxos especificos depois que voce validar o padrao visual.
      </div>
    </div>
  </div>
</template>

<script setup>
import PageHeader from "../components/PageHeader.vue";

defineProps({
  moduleLabel: { type: String, required: true }
});
</script>
