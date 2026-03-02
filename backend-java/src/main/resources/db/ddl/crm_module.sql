-- CRM module DDL for PostgreSQL
-- Compatible with existing tables: customers, companies, sellers, app_users

CREATE TABLE IF NOT EXISTS crm_pipeline (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    tipo_negocio VARCHAR(10) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS crm_stage (
    id BIGSERIAL PRIMARY KEY,
    pipeline_id BIGINT NOT NULL REFERENCES crm_pipeline(id),
    nome VARCHAR(255) NOT NULL,
    ordem INTEGER NOT NULL,
    is_won BOOLEAN NOT NULL DEFAULT FALSE,
    is_lost BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_crm_stage_pipeline_ordem UNIQUE (pipeline_id, ordem)
);

CREATE TABLE IF NOT EXISTS crm_motivo_perda (
    id BIGSERIAL PRIMARY KEY,
    descricao VARCHAR(255) NOT NULL UNIQUE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS crm_deal (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES customers(id),
    empresa_id BIGINT NULL REFERENCES companies(id),
    vendedor_id BIGINT NOT NULL REFERENCES sellers(id),
    tipo_negocio VARCHAR(10) NOT NULL,
    pipeline_id BIGINT NOT NULL REFERENCES crm_pipeline(id),
    stage_id BIGINT NOT NULL REFERENCES crm_stage(id),
    valor_estimado NUMERIC(18,2),
    probabilidade INTEGER,
    status VARCHAR(10) NOT NULL,
    data_prevista_fechamento DATE,
    motivo_perda_id BIGINT NULL REFERENCES crm_motivo_perda(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS crm_interaction (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES customers(id),
    deal_id BIGINT NULL REFERENCES crm_deal(id),
    tipo VARCHAR(20) NOT NULL,
    descricao TEXT NOT NULL,
    ocorrido_em TIMESTAMPTZ NOT NULL,
    criado_por BIGINT NOT NULL REFERENCES app_users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS crm_task (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES customers(id),
    deal_id BIGINT NULL REFERENCES crm_deal(id),
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT,
    prioridade VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    vencimento_em TIMESTAMPTZ,
    responsavel_id BIGINT NOT NULL REFERENCES app_users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_crm_deal_stage_status ON crm_deal(stage_id, status);
CREATE INDEX IF NOT EXISTS idx_crm_deal_vendedor ON crm_deal(vendedor_id);
CREATE INDEX IF NOT EXISTS idx_crm_interaction_cliente ON crm_interaction(cliente_id);
CREATE INDEX IF NOT EXISTS idx_crm_task_responsavel_status ON crm_task(responsavel_id, status);
