import { reactive } from "vue";
import { apiRequest } from "../services/api";

const state = reactive({
  products: [],
  customers: [],
  sellers: [],
  loaded: false,
  loading: false
});

export function useCommercialData() {
  async function ensureLoaded() {
    if (state.loaded || state.loading) return;
    state.loading = true;
    try {
      const [sellers, products, customers] = await Promise.all([
        apiRequest("/api/commercial/sellers"),
        apiRequest("/api/commercial/products"),
        apiRequest("/api/commercial/customers")
      ]);
      state.sellers = sellers;
      state.products = products;
      state.customers = customers;
      state.loaded = true;
    } catch {
      state.loaded = true;
    } finally {
      state.loading = false;
    }
  }

  async function addProduct(payload) {
    const created = await apiRequest("/api/commercial/products", {
      method: "POST",
      body: JSON.stringify(payload)
    });
    state.products.unshift(created);
    return created;
  }

  async function addCustomer(payload) {
    const created = await apiRequest("/api/commercial/customers", {
      method: "POST",
      body: JSON.stringify(payload)
    });
    state.customers.unshift(created);
    return created;
  }

  async function addSeller(payload) {
    const created = await apiRequest("/api/commercial/sellers", {
      method: "POST",
      body: JSON.stringify(payload)
    });
    state.sellers.unshift(created);
    return created;
  }

  async function bulkUpsertProducts(rows) {
    const result = await apiRequest("/api/commercial/products/bulk-upsert", {
      method: "POST",
      body: JSON.stringify(rows)
    });
    state.products = await apiRequest("/api/commercial/products");
    return result;
  }

  async function bulkUpsertCustomers(rows) {
    const result = await apiRequest("/api/commercial/customers/bulk-upsert", {
      method: "POST",
      body: JSON.stringify(rows)
    });
    state.customers = await apiRequest("/api/commercial/customers");
    return result;
  }

  async function updateProduct(id, payload) {
    const updated = await apiRequest(`/api/commercial/products/${id}`, {
      method: "PUT",
      body: JSON.stringify(payload)
    });
    const idx = state.products.findIndex((item) => item.id === id);
    if (idx >= 0) state.products[idx] = updated;
    return updated;
  }

  async function removeProduct(id) {
    await apiRequest(`/api/commercial/products/${id}`, { method: "DELETE" });
    state.products = state.products.filter((item) => item.id !== id);
  }

  async function updateCustomer(id, payload) {
    const updated = await apiRequest(`/api/commercial/customers/${id}`, {
      method: "PUT",
      body: JSON.stringify(payload)
    });
    const idx = state.customers.findIndex((item) => item.id === id);
    if (idx >= 0) state.customers[idx] = updated;
    return updated;
  }

  async function removeCustomer(id) {
    await apiRequest(`/api/commercial/customers/${id}`, { method: "DELETE" });
    state.customers = state.customers.filter((item) => item.id !== id);
  }

  async function updateSeller(id, payload) {
    const updated = await apiRequest(`/api/commercial/sellers/${id}`, {
      method: "PUT",
      body: JSON.stringify(payload)
    });
    const idx = state.sellers.findIndex((item) => item.id === id);
    if (idx >= 0) state.sellers[idx] = updated;
    return updated;
  }

  async function removeSeller(id) {
    await apiRequest(`/api/commercial/sellers/${id}`, { method: "DELETE" });
    state.sellers = state.sellers.filter((item) => item.id !== id);
  }

  return {
    state,
    ensureLoaded,
    addProduct,
    addCustomer,
    addSeller,
    updateProduct,
    removeProduct,
    updateCustomer,
    removeCustomer,
    updateSeller,
    removeSeller,
    bulkUpsertProducts,
    bulkUpsertCustomers
  };
}
