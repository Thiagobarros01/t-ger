import { computed, ref, watch } from "vue";

export function usePagination(itemsRef, initialPageSize = 10) {
  const page = ref(1);
  const pageSize = ref(initialPageSize);

  const totalItems = computed(() => itemsRef.value.length);
  const totalPages = computed(() => Math.max(1, Math.ceil(totalItems.value / pageSize.value)));

  const paginatedItems = computed(() => {
    const start = (page.value - 1) * pageSize.value;
    return itemsRef.value.slice(start, start + pageSize.value);
  });

  watch([totalItems, pageSize], () => {
    if (page.value > totalPages.value) {
      page.value = totalPages.value;
    }
    if (page.value < 1) {
      page.value = 1;
    }
  });

  function setPage(nextPage) {
    page.value = Math.min(Math.max(1, nextPage), totalPages.value);
  }

  function setPageSize(nextSize) {
    pageSize.value = Number(nextSize) || initialPageSize;
    page.value = 1;
  }

  return {
    page,
    pageSize,
    totalItems,
    totalPages,
    paginatedItems,
    setPage
    ,
    setPageSize
  };
}
