<template>
  <div>
    <PageHeader
      eyebrow="Configuracoes / Importar Dados"
      :title="title"
      :subtitle="subtitle"
    />

    <div class="import-layout">
      <div class="panel">
        <div class="section-head">
          <div>
            <h3>Upload de planilha</h3>
            <p>Envie Excel/CSV e ajuste as regras antes da importacao.</p>
          </div>
        </div>
        <div class="form-grid">
          <label class="full">
            Arquivo Excel (.xlsx, .xls) ou CSV
            <input type="file" accept=".xlsx,.xls,.csv" @change="onFileChange" />
          </label>
        </div>
        <p class="muted" style="margin-top: 8px" v-if="selectedFile">
          Arquivo selecionado: <strong>{{ selectedFile.name }}</strong>
        </p>
        <div class="actions-row" style="margin-top: 8px">
          <button class="btn-primary" type="button" :disabled="!selectedFile || importing" @click="runBulkImport">
            {{ importing ? "Processando..." : "Atualizacao em massa" }}
          </button>
          <span class="muted">
            {{ isSalesHistory ? "Sobrescreve por numero do pedido (ou NF quando nao houver pedido)." : "Sobrescreve por Codigo ERP quando encontrar registro existente." }}
          </span>
        </div>
      </div>

      <div class="panel" v-if="importing || progress.total > 0">
        <div class="section-head">
          <div>
            <h3>Progresso do processamento</h3>
            <p>{{ progress.phase }}</p>
          </div>
          <strong>{{ progress.percent }}%</strong>
        </div>
        <div class="import-progress-track">
          <div class="import-progress-fill" :style="{ width: `${progress.percent}%` }"></div>
        </div>
        <p class="muted" style="margin-top: 8px;">
          {{ progress.processed }} de {{ progress.total }} linhas processadas
          <span v-if="progress.batchesTotal > 0"> | Lote {{ progress.batchesDone }} de {{ progress.batchesTotal }}</span>
        </p>
      </div>

      <div class="panel" v-if="lastResult">
        <div class="section-head">
          <div>
            <h3>Resultado da importacao</h3>
            <p>Resumo da ultima atualizacao em massa.</p>
          </div>
        </div>
        <div class="stats-row">
          <div class="stat-card">
            <span>Total processado</span>
            <strong>{{ lastResult.total }}</strong>
          </div>
          <div class="stat-card">
            <span>Salvos (novos)</span>
            <strong>{{ lastResult.created }}</strong>
          </div>
          <div class="stat-card">
            <span>Atualizados (ERP)</span>
            <strong>{{ lastResult.updated }}</strong>
          </div>
          <div class="stat-card">
            <span>Erros</span>
            <strong>{{ lastResult.errors }}</strong>
          </div>
        </div>
      </div>

      <div class="panel" v-if="importError">
        <div class="section-head">
          <div>
            <h3>Falha na importacao</h3>
            <p class="muted">{{ importError }}</p>
          </div>
        </div>
      </div>

      <div class="panel" v-if="errorRows.length > 0">
        <div class="section-head">
          <div>
            <h3>Linhas com erro</h3>
            <p>Revise os registros com falha e exporte para correcao.</p>
          </div>
          <button class="btn-soft" type="button" @click="downloadErrorCsv">Exportar erros (CSV)</button>
        </div>
        <div class="table-scroll">
          <table>
            <thead>
              <tr>
                <th>Linha</th>
                <th>Motivo</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, idx) in errorRows.slice(0, 200)" :key="`err-${idx}`">
                <td>{{ row.row }}</td>
                <td>{{ row.message }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <p class="muted" style="margin-top: 8px;" v-if="errorRows.length > 200">
          Mostrando 200 de {{ errorRows.length }} erros. Use o CSV para ver todos.
        </p>
      </div>

      <div class="table-panel">
        <div class="section-head">
          <div>
            <h3>Mapeamento de colunas</h3>
            <p>Defina alias e quais campos sao obrigatorios neste layout.</p>
          </div>
        </div>
        <div class="table-scroll">
          <table>
            <thead>
              <tr>
                <th>Campo</th>
                <th>Alias da planilha</th>
                <th>Obrigatorio</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="field in fields" :key="field.key">
                <td>{{ field.name }}</td>
                <td>
                  <input
                    :value="field.alias"
                    @input="updateAlias(field.key, $event.target.value)"
                  />
                </td>
                <td>
                  <label class="switch-inline">
                    <input
                      :checked="field.required"
                      type="checkbox"
                      @change="updateRequired(field.key, $event.target.checked)"
                    />
                  </label>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, watch } from "vue";
import * as XLSX from "xlsx";
import PageHeader from "../../components/PageHeader.vue";
import { useImportConfig } from "../../composables/useImportConfig";
import { useCommercialData } from "../../composables/useCommercialData";

const props = defineProps({
  entity: { type: String, required: true }
});

const selectedFile = ref(null);
const importing = ref(false);
const lastResult = ref(null);
const importError = ref("");
const errorRows = ref([]);
const progress = ref({
  total: 0,
  processed: 0,
  percent: 0,
  batchesDone: 0,
  batchesTotal: 0,
  phase: "Aguardando importacao."
});
const { ensureLoaded: ensureImportLoaded, getFields, updateField } = useImportConfig();
const { ensureLoaded: ensureCommercialLoaded, bulkUpsertCustomers, bulkUpsertProducts, bulkUpsertSellers, bulkUpsertSalesHistory } = useCommercialData();

const isClient = computed(() => props.entity === "clientes");
const isProduct = computed(() => props.entity === "produtos");
const isSeller = computed(() => props.entity === "vendedores");
const isSalesHistory = computed(() => props.entity === "historico_vendas");
const title = computed(() => {
  if (isClient.value) return "Importar Clientes";
  if (isProduct.value) return "Importar Produtos";
  if (isSeller.value) return "Importar Vendedores";
  return "Importar Historico de Vendas";
});
const subtitle = computed(() =>
  isClient.value
    ? "Recebe planilha com os campos do cadastro de cliente informados no modulo Comercial."
    : isProduct.value
      ? "Recebe planilha com os campos do cadastro de produto informados no modulo Comercial."
      : isSeller.value
        ? "Recebe planilha do cadastro de vendedores e atualiza por Codigo ERP."
        : "Recebe planilha de historico comercial (pedido/NF) para alimentar o CRM."
);

const fields = computed(() => getFields(props.entity));

async function loadEntityContext(entity) {
  await ensureImportLoaded(entity);
  await ensureCommercialLoaded();
}

onMounted(async () => {
  await loadEntityContext(props.entity);
});

watch(
  () => props.entity,
  async (entity) => {
    selectedFile.value = null;
    lastResult.value = null;
    importError.value = "";
    errorRows.value = [];
    progress.value = { total: 0, processed: 0, percent: 0, batchesDone: 0, batchesTotal: 0, phase: "Aguardando importacao." };
    await loadEntityContext(entity);
  }
);

function onFileChange(event) {
  selectedFile.value = event.target.files?.[0] ?? null;
}

async function updateAlias(key, value) {
  await updateField(props.entity, key, { alias: value });
}

async function updateRequired(key, value) {
  await updateField(props.entity, key, { required: value });
}

function normalizeHeader(value) {
  return String(value ?? "")
    .trim()
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/\s+/g, "_");
}

function parseDate(value) {
  if (typeof value === "number" && Number.isFinite(value)) {
    const excelEpoch = new Date(Date.UTC(1899, 11, 30));
    const parsed = new Date(excelEpoch.getTime() + value * 86400000);
    return parsed.toISOString().slice(0, 10);
  }
  const raw = String(value ?? "").trim();
  if (!raw) return null;
  const fromIso = new Date(raw);
  if (!Number.isNaN(fromIso.getTime())) return fromIso.toISOString().slice(0, 10);
  const m = raw.match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
  if (m) return `${m[3]}-${m[2]}-${m[1]}`;
  return null;
}

function parseNumber(value) {
  if (typeof value === "number" && Number.isFinite(value)) return value;
  const raw = String(value ?? "").trim();
  if (!raw) return null;
  const normalized = raw.includes(",") && raw.includes(".")
    ? raw.replace(/\./g, "").replace(",", ".")
    : raw.replace(",", ".");
  const num = Number(normalized);
  return Number.isFinite(num) ? num : null;
}

function asText(value) {
  const raw = String(value ?? "").trim();
  return raw || null;
}

function mapRowByConfig(rawRow, fieldDefs) {
  const entries = Object.entries(rawRow ?? {});
  const normalizedMap = new Map(entries.map(([key, value]) => [normalizeHeader(key), value]));
  const mapped = {};
  let hasError = false;
  const missingRequired = [];

  for (const field of fieldDefs) {
    const aliasKey = normalizeHeader(field.alias);
    const value = normalizedMap.get(aliasKey);
    mapped[field.key] = value ?? "";

    const isEmpty = String(value ?? "").trim() === "";
    if (field.required && isEmpty) {
      hasError = true;
      missingRequired.push(field.name);
    }
  }

  return { mapped, hasError, missingRequired };
}

function chunkArray(items, size) {
  if (!Array.isArray(items) || size <= 0) return [];
  const chunks = [];
  for (let i = 0; i < items.length; i += size) {
    chunks.push(items.slice(i, i + size));
  }
  return chunks;
}

function toFriendlyImportError(error) {
  const message = String(error?.message ?? "").trim();
  if (!message) return "Erro inesperado ao importar arquivo.";
  if (message.includes("413")) {
    return "Arquivo muito grande para envio unico. O sistema agora envia em lotes; tente novamente.";
  }
  return message;
}

function updateProgress(patch) {
  progress.value = { ...progress.value, ...patch };
  const total = progress.value.total || 0;
  const processed = Math.min(progress.value.processed || 0, total);
  const percent = total > 0 ? Math.floor((processed / total) * 100) : 0;
  progress.value.percent = Math.min(100, percent);
}

function downloadErrorCsv() {
  if (!errorRows.value.length) return;
  const csvLines = ["linha,motivo"];
  for (const row of errorRows.value) {
    const safeMessage = String(row.message ?? "").replaceAll("\"", "\"\"");
    csvLines.push(`${row.row},\"${safeMessage}\"`);
  }
  const blob = new Blob([csvLines.join("\n")], { type: "text/csv;charset=utf-8;" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `import_erros_${props.entity}.csv`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}

async function parseRowsFromFile(file) {
  const buffer = await file.arrayBuffer();
  const workbook = XLSX.read(buffer, { type: "array" });
  const firstSheetName = workbook.SheetNames[0];
  if (!firstSheetName) return [];
  const sheet = workbook.Sheets[firstSheetName];
  return XLSX.utils.sheet_to_json(sheet, { defval: "" });
}

async function runBulkImport() {
  if (!selectedFile.value) return;
  importing.value = true;
  importError.value = "";
  errorRows.value = [];
  let rawCount = 0;
  try {
    updateProgress({
      total: 0,
      processed: 0,
      percent: 0,
      batchesDone: 0,
      batchesTotal: 0,
      phase: "Lendo arquivo..."
    });
    const rawRows = await parseRowsFromFile(selectedFile.value);
    rawCount = rawRows.length;
    updateProgress({
      total: rawRows.length,
      processed: 0,
      phase: "Validando e mapeando linhas..."
    });
    const fieldDefs = fields.value;

    let validationErrors = 0;
    const validRows = [];
    const validRowsOriginalLine = [];

    for (let i = 0; i < rawRows.length; i++) {
      const rawRow = rawRows[i];
      const sheetLine = i + 2;
      const { mapped, hasError, missingRequired } = mapRowByConfig(rawRow, fieldDefs);
      if (hasError) {
        validationErrors++;
        errorRows.value.push({
          row: sheetLine,
          message: `Campos obrigatorios ausentes: ${missingRequired.join(", ")}`
        });
        continue;
      }

      if (isClient.value) {
        validRows.push({
          erpCode: mapped.codigo_erp,
          corporateName: mapped.razao_social,
          email: mapped.email,
          type: mapped.tipo,
          tradeName: mapped.nome_fantasia,
          phone: mapped.telefone,
          erpSellerCode: mapped.codigo_erp_vendedor
        });
        validRowsOriginalLine.push(sheetLine);
      } else {
        if (isSeller.value) {
          validRows.push({
            erpCode: mapped.codigo_erp,
            name: mapped.nome,
            email: mapped.email,
            phone: mapped.telefone
          });
          validRowsOriginalLine.push(sheetLine);
          continue;
        }
        if (isSalesHistory.value) {
          validRows.push({
            companyErpCode: asText(mapped.cd_emp),
            orderNumber: asText(mapped.nu_ped),
            sequence: asText(mapped.seq),
            nfNumber: asText(mapped.nu_nf),
            orderDate: parseDate(mapped.dt_ped),
            billedDate: parseDate(mapped.dt_fatur),
            returnedDate: parseDate(mapped.dt_devol),
            canceledDate: parseDate(mapped.dt_canc),
            orderStatusCode: asText(mapped.cd_situacao_pedido),
            customerErpCode: asText(mapped.cd_clien),
            sellerErpCode: asText(mapped.cd_vend),
            productErpCode: asText(mapped.cd_prod),
            quantity: parseNumber(mapped.qtde),
            netValue: parseNumber(mapped.vl_liquido),
            totalNfValue: parseNumber(mapped.vl_tot_nf),
            canceledValue: parseNumber(mapped.vl_cancelado),
            returnedValue: parseNumber(mapped.vl_devolvido)
          });
          validRowsOriginalLine.push(sheetLine);
          continue;
        }
        validRows.push({
          erpCode: mapped.codigo_erp,
          description: mapped.descricao,
          department: mapped.departamento,
          category: mapped.categoria,
          line: mapped.linha,
          manufacturer: mapped.fabricante
        });
        validRowsOriginalLine.push(sheetLine);
      }
    }
    updateProgress({
      processed: validationErrors,
      phase: validRows.length === 0 ? "Finalizado." : "Enviando lotes..."
    });

    if (validRows.length === 0) {
      lastResult.value = { total: rawRows.length, created: 0, updated: 0, errors: validationErrors };
      updateProgress({ processed: rawRows.length, percent: 100, phase: "Finalizado." });
      return;
    }

    const batchSize = 1000;
    const batches = chunkArray(validRows, batchSize);
    const summary = { total: rawRows.length, created: 0, updated: 0, errors: validationErrors };
    updateProgress({
      batchesDone: 0,
      batchesTotal: batches.length,
      phase: `Enviando lote 1 de ${batches.length}...`
    });

    for (let batchIndex = 0; batchIndex < batches.length; batchIndex++) {
      const batch = batches[batchIndex];
      const batchStart = batchIndex * batchSize;
      const result = isClient.value
        ? await bulkUpsertCustomers(batch)
        : isProduct.value
          ? await bulkUpsertProducts(batch)
          : isSeller.value
            ? await bulkUpsertSellers(batch)
          : await bulkUpsertSalesHistory(batch);

      summary.created += Number(result?.created ?? 0);
      summary.updated += Number(result?.updated ?? 0);
      summary.errors += Number(result?.errors ?? 0);
      const details = Array.isArray(result?.errorDetails) ? result.errorDetails : [];
      for (const detail of details) {
        const rowInBatch = Number(detail?.row ?? 0);
        const absoluteRow = rowInBatch > 0
          ? validRowsOriginalLine[batchStart + rowInBatch - 1]
          : validRowsOriginalLine[batchStart] ?? null;
        errorRows.value.push({
          row: absoluteRow ?? "N/A",
          message: detail?.message || "Erro de processamento no backend"
        });
      }

      updateProgress({
        batchesDone: batchIndex + 1,
        processed: Math.min(rawRows.length, validationErrors + ((batchIndex + 1) * batchSize)),
        phase: batchIndex + 1 < batches.length
          ? `Enviando lote ${batchIndex + 2} de ${batches.length}...`
          : "Finalizando processamento..."
      });
    }

    lastResult.value = {
      ...summary
    };
    updateProgress({ processed: rawRows.length, percent: 100, phase: "Finalizado." });
  } catch (err) {
    importError.value = toFriendlyImportError(err);
    lastResult.value = { total: rawCount, created: 0, updated: 0, errors: 1 };
    updateProgress({ phase: "Falha na importacao." });
  } finally {
    importing.value = false;
  }
}
</script>
