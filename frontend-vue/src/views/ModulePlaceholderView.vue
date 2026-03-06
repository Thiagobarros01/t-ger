<template>
  <div>
    <PageHeader
      eyebrow="Modulo"
      :title="moduleLabel"
      subtitle=""
    />
    <div class="panel">
      <div class="empty-state">
        Em desenvolvimento.
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
