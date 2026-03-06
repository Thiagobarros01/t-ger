# T-GER

MVP de sistema de gestao por modulos (TI, Comercial/CRM e Configuracoes), com perfis de acesso:

- `ADMINISTRADOR`: acesso total
- `GESTOR`: acesso aos modulos vinculados
- `OPERADOR`: acesso restrito ao proprio escopo

## Objetivo do MVP

Validar fluxo de operacao por setor, com base unica de usuarios e dados iniciais, incluindo:

- cadastro e consulta de clientes/produtos/vendedores
- funil comercial (CRM) com deals, tarefas e interacoes
- gestao de TI (ativos, termos e chamados)
- configuracoes administrativas (usuarios, parametros, layout de importacao)

## Arquitetura

- `backend-java`: Spring Boot 3, Java 17, JPA, PostgreSQL
- `frontend-vue`: Vue 3 + Vue Router + Vite
- `docker-compose.yml`: orquestra `postgres`, `backend`, `frontend`
- `storage/postgres-data`: persistencia local do banco

## Modulos existentes

- `Gestao da TI`
  - ativos de informatica
  - termos/contratos
  - chamados
- `Gestao Comercial`
  - clientes, produtos, vendedores
  - CRM (kanban, interacoes, tarefas)
- `Gestao do Vendedor`
  - visoes focadas no vendedor
- `Configuracoes`
  - usuarios administrativos
  - parametros globais
  - layouts de importacao
- `Placeholders`
  - diretoria, financeiro, logistica, compras, recebimento

## Como subir (recomendado)

1. Na raiz do projeto:

```powershell
docker compose up --build -d
```

2. Acesse:
- Frontend: `http://localhost:5173`
- Backend (interno): `http://backend:8080` dentro da rede Docker
- PostgreSQL: `localhost:5555`

## Variaveis de ambiente opcionais

Crie `.env` na raiz:

```env
POSTGRES_DB=tger
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
```

## Execucao local sem Docker

Backend:

```powershell
cd backend-java
mvn spring-boot:run
```

Frontend:

```powershell
cd frontend-vue
npm install
npm run dev
```

Observacao:
- O frontend local (`vite`) chama `/api` relativo.
- Sem proxy local no Vite, o modo mais simples para MVP e usar Docker Compose completo.

## Endpoints principais

- Auth: `/api/auth`
- TI: `/api/ti`
- Comercial: `/api/commercial`
- CRM:
  - `/api/crm/deals`
  - `/api/crm` (tarefas/interacoes)
  - `/api/crm/catalog` (pipelines, stages, motivos de perda)
- Config:
  - `/api/admin/users`
  - `/api/config/companies`
  - `/api/config/global-params`
  - `/api/config/import-layouts`

## Credenciais de teste

- Senha padrao: `123456`
- Admin: `thiago@tger.local`
- Gestor TI: `marina.ti@tger.local`
- Operador TI: `joao.suporte@tger.local`

Os usuarios seed sao criados automaticamente na inicializacao do backend.

## Estado atual e limites conhecidos

- autenticacao simplificada para homologacao local (sem JWT real)
- parte dos modulos ainda esta em placeholder
- nao ha suite de testes automatizados consolidada no repositorio
- foco atual do codigo esta em validar fluxo funcional do produto

## Estrutura de pastas

```text
.
|-- backend-java
|-- frontend-vue
|-- storage
`-- docker-compose.yml
```
