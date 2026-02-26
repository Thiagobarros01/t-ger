import { createRouter, createWebHistory } from "vue-router";
import AppShell from "../layouts/AppShell.vue";
import HomeView from "../views/HomeView.vue";
import LoginView from "../views/LoginView.vue";
import ModulePlaceholderView from "../views/ModulePlaceholderView.vue";
import UsersConfigView from "../views/config/UsersConfigView.vue";
import ParametersConfigView from "../views/config/ParametersConfigView.vue";
import DataImportView from "../views/config/DataImportView.vue";
import TiAssetsView from "../views/ti/TiAssetsView.vue";
import TiTermsView from "../views/ti/TiTermsView.vue";
import TiTicketsView from "../views/ti/TiTicketsView.vue";
import ProductsView from "../views/commercial/ProductsView.vue";
import CustomersView from "../views/commercial/CustomersView.vue";
import SellersView from "../views/commercial/SellersView.vue";
import { useSession } from "../composables/useSession";

const routes = [
  { path: "/login", name: "login", component: LoginView },
  {
    path: "/",
    component: AppShell,
    children: [
      { path: "", name: "home", component: HomeView, meta: { moduleCode: null } },
      { path: "gestao/diretoria", name: "diretoria", component: ModulePlaceholderView, props: { moduleLabel: "Gestao da Diretoria" }, meta: { moduleCode: "DIRETORIA" } },
      { path: "gestao/financeiro", name: "financeiro", component: ModulePlaceholderView, props: { moduleLabel: "Gestao do Financeiro" }, meta: { moduleCode: "FINANCEIRO" } },
      { path: "gestao/comercial", name: "comercial", redirect: "/gestao/comercial/produtos", meta: { moduleCode: "COMERCIAL" } },
      { path: "gestao/comercial/produtos", name: "comercial-produtos", component: ProductsView, meta: { moduleCode: "COMERCIAL" } },
      { path: "gestao/comercial/clientes", name: "comercial-clientes", component: CustomersView, meta: { moduleCode: "COMERCIAL" } },
      { path: "gestao/comercial/vendedores", name: "comercial-vendedores", component: SellersView, meta: { moduleCode: "COMERCIAL" } },
      { path: "gestao/logistica", name: "logistica", component: ModulePlaceholderView, props: { moduleLabel: "Gestao da Logistica" }, meta: { moduleCode: "LOGISTICA" } },
      { path: "gestao/compras", name: "compras", component: ModulePlaceholderView, props: { moduleLabel: "Gestao de Compras" }, meta: { moduleCode: "COMPRAS" } },
      { path: "gestao/vendedor", name: "vendedor", component: ModulePlaceholderView, props: { moduleLabel: "Gestao do Vendedor" }, meta: { moduleCode: "VENDEDOR" } },
      { path: "gestao/recebimento", name: "recebimento", component: ModulePlaceholderView, props: { moduleLabel: "Gestao do Recebimento" }, meta: { moduleCode: "RECEBIMENTO" } },
      { path: "gestao/ti/ativos", name: "ti-ativos", component: TiAssetsView, meta: { moduleCode: "TI" } },
      { path: "gestao/ti/termos-contratos", name: "ti-termos", component: TiTermsView, meta: { moduleCode: "TI" } },
      { path: "gestao/ti/chamados", name: "ti-chamados", component: TiTicketsView, meta: { moduleCode: "TI" } },
      { path: "configuracoes/usuarios", name: "config-users", component: UsersConfigView, meta: { moduleCode: "CONFIGURACOES" } },
      { path: "configuracoes/parametros", name: "config-params", component: ParametersConfigView, meta: { moduleCode: "CONFIGURACOES" } },
      { path: "configuracoes/importar/clientes", name: "config-import-clientes", component: DataImportView, props: { entity: "clientes" }, meta: { moduleCode: "CONFIGURACOES" } },
      { path: "configuracoes/importar/produtos", name: "config-import-produtos", component: DataImportView, props: { entity: "produtos" }, meta: { moduleCode: "CONFIGURACOES" } }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to) => {
  const { currentUser, canAccessModule } = useSession();

  if (to.path !== "/login" && !currentUser.value) {
    return "/login";
  }

  if (to.path === "/login" && currentUser.value) {
    return "/";
  }

  const requiredModule = to.meta?.moduleCode;
  if (!requiredModule) return true;

  return canAccessModule(currentUser.value, requiredModule) ? true : "/";
});

export default router;
