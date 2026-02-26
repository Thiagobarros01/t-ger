export const modules = [
  { code: "DIRETORIA", label: "Gestao da Diretoria", group: "gestao", description: "Indicadores e acompanhamento da diretoria." },
  { code: "TI", label: "Gestao da TI", group: "gestao", description: "Ativos, contratos, chamados e operacao de TI." },
  { code: "FINANCEIRO", label: "Gestao do Financeiro", group: "gestao", description: "Contas, fluxo de caixa e pagamentos." },
  { code: "COMERCIAL", label: "Gestao do Comercial", group: "gestao", description: "Pipeline, clientes e oportunidades." },
  { code: "LOGISTICA", label: "Gestao da Logistica", group: "gestao", description: "Movimentacoes, expedicao e entregas." },
  { code: "COMPRAS", label: "Gestao de Compras", group: "gestao", description: "Solicitacoes, cotacoes e pedidos." },
  { code: "VENDEDOR", label: "Gestao do Vendedor", group: "gestao", description: "Rotina do vendedor e metas." },
  { code: "RECEBIMENTO", label: "Gestao do Recebimento", group: "gestao", description: "Recebimento de materiais e conferencia." },
  { code: "CONFIGURACOES", label: "Configuracoes", group: "config", description: "Usuarios, perfis e permissoes." }
];

export const users = [
  { id: 1, name: "Thiago Admin", email: "thiago@tger.local", erpCode: "USR-ADMIN", profile: "ADMINISTRADOR", modules: [], active: true, lastPasswordResetAt: null },
  { id: 2, name: "Marina TI", email: "marina.ti@tger.local", erpCode: "USR-0002", profile: "GESTOR", modules: ["TI"], active: true, lastPasswordResetAt: null },
  { id: 3, name: "Joao Suporte", email: "joao.suporte@tger.local", erpCode: "USR-0003", profile: "OPERADOR", modules: ["TI"], active: true, lastPasswordResetAt: null },
  { id: 4, name: "Ana Financeiro", email: "ana.fin@tger.local", erpCode: "USR-0004", profile: "GESTOR", modules: ["FINANCEIRO"], active: true, lastPasswordResetAt: null }
];

export const tiTerms = [];
export const tiAssets = [];
export const tiTickets = [];
