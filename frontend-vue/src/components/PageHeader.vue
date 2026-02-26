<template>
  <div class="page-header">
    <div>
      <p class="eyebrow">{{ eyebrow }}</p>
      <h1>{{ title }}</h1>
      <p class="page-subtitle">{{ subtitle }}</p>
    </div>
    <slot name="actions" />
  </div>
</template>

<script setup>
defineProps({
  eyebrow: { type: String, default: "T-GER" },
  title: { type: String, required: true },
  subtitle: { type: String, default: "" }
});
</script>
