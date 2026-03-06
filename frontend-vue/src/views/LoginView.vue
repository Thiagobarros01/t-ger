<template>
  <div class="login-page">
    <header class="login-topbar">
      <div class="login-topbar__brand">
        <div class="brand-logo">TG</div>
        <div>
          <strong>T-GER</strong>
          <p>Plataforma de gestao integrada</p>
        </div>
      </div>
    </header>

    <div class="login-shell">
      <section class="login-hero">
        <div class="login-hero__badge">Acesso Corporativo</div>
        <h1>Gestao por departamentos, em um unico painel</h1>
        <p>
          Fluxo organizado por modulo com visual operacional e produtividade no dia a dia.
        </p>

        <div class="login-hero__highlights">
          <div>
            <strong>Visao centralizada</strong>
            <span>ti, comercial, crm e configuracoes em uma base unica</span>
          </div>
          <div>
            <strong>Operacao por contexto</strong>
            <span>navegacao por departamento e funcoes do modulo atual</span>
          </div>
          <div>
            <strong>Base pronta para evolucao</strong>
            <span>mvp funcional com foco em uso real e iteracao rapida</span>
          </div>
        </div>
      </section>

      <section class="login-card">
        <div class="login-card__head">
          <p class="eyebrow">Login</p>
          <h2>Entrar no T-GER</h2>
          <p class="muted">Use suas credenciais para acessar.</p>
        </div>

        <form class="login-form" @submit.prevent="submit">
          <label>
            E-mail
            <input v-model="form.email" type="email" required placeholder="seuemail@empresa.com" />
          </label>
          <label>
            Senha
            <input v-model="form.password" type="password" required placeholder="Digite sua senha" />
          </label>

          <button class="btn-primary login-submit" type="submit" :disabled="auth.state.loading">
            {{ auth.state.loading ? "Entrando..." : "Entrar" }}
          </button>
        </form>

        <p v-if="auth.state.error" class="danger-text">{{ auth.state.error }}</p>

        <div class="login-credentials">
          <strong>Credenciais de teste</strong>
          <div class="login-credential-row">
            <span>Senha padrao</span>
            <code>123456</code>
          </div>
          <div class="login-credential-row">
            <span>Administrador</span>
            <code>thiago@tger.local</code>
          </div>
          <div class="login-credential-row">
            <span>Gestor TI</span>
            <code>marina.ti@tger.local</code>
          </div>
          <div class="login-credential-row">
            <span>Operador TI</span>
            <code>joao.suporte@tger.local</code>
          </div>
        </div>
      </section>
    </div>
    <div class="login-footer-note">Ambiente local de testes.</div>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import { useRouter } from "vue-router";
import { useAuth } from "../composables/useAuth";

const router = useRouter();
const auth = useAuth();

const form = reactive({
  email: "thiago@tger.local",
  password: "123456"
});

async function submit() {
  try {
    await auth.login(form.email, form.password);
    router.push("/");
  } catch {
    // erro tratado no composable
  }
}
</script>
