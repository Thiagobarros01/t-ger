# T-GER (Base Inicial)

Sistema de gerenciamento e gestao por setores, com controle de acesso por perfil:

- `Administrador`: ve tudo
- `Gestor`: ve somente modulo(s) vinculado(s)
- `Operador`: ve somente os proprios dados (regra demonstrada nas telas de TI)

## Estrutura

- `backend-java`: API Java (Spring Boot) com endpoints mockados em memoria
- `frontend-vue`: Interface Vue 3 com layout, permissao e submodulos de TI

## Modulos criados (nesta etapa)

- Gestao da Diretoria (placeholder)
- Gestao da TI
  - Ativos de Informatica
  - Termos e Contratos (Responsabilidade, CLT, Comodato)
  - Chamados (com mensagens)
- Gestao do Financeiro (placeholder)
- Gestao do Comercial (placeholder)
- Gestao da Logistica (placeholder)
- Gestao de Compras (placeholder)
- Gestao do Vendedor (placeholder)
- Gestao do Recebimento (placeholder)
- Configuracoes
  - Cadastro de usuarios e permissoes

## Como rodar

### Docker Compose (recomendado para teste rapido)

```powershell
cd e:\T-GESTAO
docker compose up --build -d
```

App: `http://localhost:5173`

Observacao:
- O backend roda dentro do Docker e e acessado pelo frontend via proxy `/api`.
- Agora o banco e PostgreSQL no Docker Compose.
- Os dados persistem em `./storage/postgres-data` (pasta externa ao build do app).
- Isso permite atualizar/corrigir o sistema sem perder os dados, mantendo a pasta `storage` intacta.

### Variaveis de banco (opcional)

Voce pode criar um arquivo `.env` na raiz (`e:\\T-GESTAO`) para trocar credenciais:

```env
POSTGRES_DB=tger
POSTGRES_USER=postgres
POSTGRES_PASSWORD=troque-essa-senha
```

### Backend (Java, rodando local sem Docker)

```powershell
cd backend-java
mvn spring-boot:run
```

API base: `http://localhost:8080/api`

Observacao:
- Para rodar local sem Docker, ajuste `DB_URL/DB_USERNAME/DB_PASSWORD` para um PostgreSQL local.

### Frontend (Vue)

```powershell
cd frontend-vue
npm install
npm run dev
```

App: `http://localhost:5173`

## Observacoes para evitar retrabalho

- Corrigi `DCHP` para `DHCP` no cadastro de ativos.
- O frontend usa dados mockados para validacao visual/fluxo; a API Java ja expõe endpoints equivalentes para integrar na proxima etapa.
- A regra de `Operador` esta demonstrada nas telas de TI (ativos/termos/chamados). Na integracao final, a mesma regra deve ser aplicada no backend/autenticacao.
- Login simples implementado em `POST /api/auth/login` (homologacao local, sem JWT).
- Usuarios de teste sao criados automaticamente no banco na primeira subida (seed inicial).

## Login de teste

- Senha padrao: `123456`
- Admin: `thiago@tger.local`
- Gestor TI: `marina.ti@tger.local`
- Operador TI: `joao.suporte@tger.local`
