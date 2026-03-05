<template>
  <div class="search-select" ref="root">
    <button
      type="button"
      class="search-select__trigger"
      :class="{ 'search-select__trigger--empty': selectedOption == null }"
      @click="toggleOpen"
    >
      <span>{{ selectedOption ? selectedOption.label : placeholder }}</span>
      <span class="search-select__caret"></span>
    </button>

    <div class="search-select__dropdown" v-if="open">
      <input
        ref="searchInput"
        class="search-select__input"
        type="text"
        v-model="query"
        :placeholder="searchPlaceholder"
      />

      <div class="search-select__list" ref="listRef" @scroll.passive="onListScroll">
        <button
          v-if="allowEmpty"
          type="button"
          class="search-select__option"
          :class="{ 'search-select__option--active': modelValue == null }"
          @click="selectOption(null)"
        >
          {{ emptyLabel }}
        </button>

        <button
          v-for="option in filteredOptions"
          :key="option.value"
          type="button"
          class="search-select__option"
          :class="{ 'search-select__option--active': option.value === modelValue }"
          @click="selectOption(option.value)"
        >
          <span>{{ option.label }}</span>
          <small v-if="option.meta">{{ option.meta }}</small>
        </button>

        <p class="muted" style="margin: 6px 8px;" v-if="loading">
          Carregando...
        </p>
        <p class="muted" style="margin: 6px 8px;" v-else-if="filteredOptions.length === 0">
          Nenhum resultado.
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from "vue";

const props = defineProps({
  modelValue: { type: [Number, String, null], default: null },
  options: { type: Array, default: () => [] },
  fetchOptions: { type: Function, default: null },
  pageSize: { type: Number, default: 20 },
  placeholder: { type: String, default: "Selecione" },
  searchPlaceholder: { type: String, default: "Buscar por nome ou codigo..." },
  emptyLabel: { type: String, default: "Sem selecao" },
  allowEmpty: { type: Boolean, default: false }
});

const emit = defineEmits(["update:modelValue"]);

const open = ref(false);
const query = ref("");
const root = ref(null);
const searchInput = ref(null);
const listRef = ref(null);
const loading = ref(false);
const remotePage = ref(1);
const remoteTotalPages = ref(1);
const remoteOptions = ref([]);
let debounceHandle = null;

const selectedOption = computed(() => {
  const all = [...(props.options ?? []), ...(remoteOptions.value ?? [])];
  return all.find((item) => item.value === props.modelValue) ?? null;
});
const localFilteredOptions = computed(() => {
  const q = normalize(query.value);
  if (!q) return props.options;
  return props.options.filter((item) => {
    const haystack = normalize(`${item.label ?? ""} ${item.meta ?? ""} ${item.searchText ?? ""} ${item.value ?? ""}`);
    return haystack.includes(q);
  });
});
const filteredOptions = computed(() => props.fetchOptions ? remoteOptions.value : localFilteredOptions.value);

function normalize(value) {
  return String(value ?? "")
    .trim()
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "");
}

function toggleOpen() {
  open.value = !open.value;
  if (open.value) {
    if (props.fetchOptions) {
      remotePage.value = 1;
      loadRemoteOptions(1);
    }
    nextTick(() => searchInput.value?.focus());
  }
}

function selectOption(value) {
  emit("update:modelValue", value);
  open.value = false;
  query.value = "";
}

function onDocumentClick(event) {
  if (!open.value) return;
  if (!root.value) return;
  if (root.value.contains(event.target)) return;
  open.value = false;
  query.value = "";
}

async function loadRemoteOptions(page = 1) {
  if (!props.fetchOptions) return;
  loading.value = true;
  try {
    const result = await props.fetchOptions(query.value, page, props.pageSize);
    const incoming = result?.items ?? [];
    if (page <= 1) {
      remoteOptions.value = incoming;
    } else {
      const seen = new Set(remoteOptions.value.map((item) => item.value));
      for (const item of incoming) {
        if (!seen.has(item.value)) {
          remoteOptions.value.push(item);
          seen.add(item.value);
        }
      }
    }
    remoteTotalPages.value = Math.max(1, Number(result?.totalPages ?? 1));
    remotePage.value = Math.min(Math.max(1, page), remoteTotalPages.value);
  } finally {
    loading.value = false;
  }
}

function onListScroll(event) {
  if (!props.fetchOptions || loading.value) return;
  if (remotePage.value >= remoteTotalPages.value) return;
  const target = event?.target;
  if (!target) return;
  const nearBottom = target.scrollTop + target.clientHeight >= target.scrollHeight - 20;
  if (nearBottom) {
    loadRemoteOptions(remotePage.value + 1);
  }
}

onMounted(() => {
  document.addEventListener("mousedown", onDocumentClick);
});

onBeforeUnmount(() => {
  if (debounceHandle) clearTimeout(debounceHandle);
  document.removeEventListener("mousedown", onDocumentClick);
});

watch(query, () => {
  if (!open.value || !props.fetchOptions) return;
  if (debounceHandle) clearTimeout(debounceHandle);
  debounceHandle = setTimeout(() => {
    loadRemoteOptions(1);
  }, 250);
});
</script>
