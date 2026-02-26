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
          <span class="muted">Sobrescreve por Codigo ERP quando encontrar registro existente.</span>
        </div>
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
import { computed, ref, onMounted } from "vue";
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
const { ensureLoaded: ensureImportLoaded, getFields, updateField } = useImportConfig();
const { ensureLoaded: ensureCommercialLoaded, bulkUpsertCustomers, bulkUpsertProducts } = useCommercialData();

const isClient = computed(() => props.entity === "clientes");
const title = computed(() => (isClient.value ? "Importar Clientes" : "Importar Produtos"));
const subtitle = computed(() =>
  isClient.value
    ? "Recebe planilha com os campos do cadastro de cliente informados no modulo Comercial."
    : "Recebe planilha com os campos do cadastro de produto informados no modulo Comercial."
);

const fields = computed(() => getFields(props.entity));

onMounted(async () => {
  await ensureImportLoaded(props.entity);
  await ensureCommercialLoaded();
});

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

function mapRowByConfig(rawRow, fieldDefs) {
  const entries = Object.entries(rawRow ?? {});
  const normalizedMap = new Map(entries.map(([key, value]) => [normalizeHeader(key), value]));
  const mapped = {};
  let hasError = false;

  for (const field of fieldDefs) {
    const aliasKey = normalizeHeader(field.alias);
    const value = normalizedMap.get(aliasKey);
    mapped[field.key] = value ?? "";

    const isEmpty = String(value ?? "").trim() === "";
    if (field.required && isEmpty) {
      hasError = true;
    }
  }

  return { mapped, hasError };
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
  try {
    const rawRows = await parseRowsFromFile(selectedFile.value);
    const fieldDefs = fields.value;

    let validationErrors = 0;
    const validRows = [];

    for (const rawRow of rawRows) {
      const { mapped, hasError } = mapRowByConfig(rawRow, fieldDefs);
      if (hasError) {
        validationErrors++;
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
      } else {
        validRows.push({
          erpCode: mapped.codigo_erp,
          description: mapped.descricao,
          department: mapped.departamento,
          category: mapped.categoria,
          line: mapped.linha,
          manufacturer: mapped.fabricante
        });
      }
    }

    const result = isClient.value ? await bulkUpsertCustomers(validRows) : await bulkUpsertProducts(validRows);
    lastResult.value = {
      ...result,
      errors: result.errors + validationErrors,
      total: rawRows.length
    };
  } catch {
    lastResult.value = { total: 0, created: 0, updated: 0, errors: 1 };
  } finally {
    importing.value = false;
  }
}
</script>
