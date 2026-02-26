<template>
  <RouterLink class="module-card" :to="to">
    <div class="module-card__icon">{{ icon }}</div>
    <div>
      <h3>{{ title }}</h3>
      <p>{{ description }}</p>
    </div>
  </RouterLink>
</template>

<script setup>
defineProps({
  title: String,
  description: String,
  to: [String, Object],
  icon: { type: String, default: "MG" }
});
</script>
