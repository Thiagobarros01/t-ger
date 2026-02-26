<template>
  <div class="pagination-bar" v-if="totalItems > 0">
    <div class="pagination-bar__meta">
      <span>{{ totalItems }} item(ns)</span>
      <label>
        <span>Por pagina</span>
        <select :value="pageSize" @change="$emit('update:pageSize', Number($event.target.value))">
          <option v-for="size in pageSizeOptions" :key="size" :value="size">{{ size }}</option>
        </select>
      </label>
    </div>

    <div class="pagination-bar__controls">
      <button type="button" @click="$emit('update:page', 1)" :disabled="page <= 1"><<</button>
      <button type="button" @click="$emit('update:page', page - 1)" :disabled="page <= 1"><</button>
      <span>Pagina {{ page }} / {{ totalPages }}</span>
      <button type="button" @click="$emit('update:page', page + 1)" :disabled="page >= totalPages">></button>
      <button type="button" @click="$emit('update:page', totalPages)" :disabled="page >= totalPages">>></button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  page: { type: Number, required: true },
  pageSize: { type: Number, required: true },
  totalPages: { type: Number, required: true },
  totalItems: { type: Number, required: true },
  pageSizeOptions: { type: Array, default: () => [5, 10, 20, 50] }
});

defineEmits(["update:page", "update:pageSize"]);
</script>
