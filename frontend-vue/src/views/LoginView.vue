<template>
  <div class="login-page">
    <div class="login-shell">
      <section class="login-hero">
        <div class="login-hero__badge">T-GER</div>
        <h1>Sistema de gestao por setores</h1>
        <p>
          Modulos organizados por area, permissoes por perfil e submodulos por departamento.
        </p>

        <div class="login-hero__highlights">
          <div>
            <strong>Administrador</strong>
            <span>visao completa do sistema</span>
          </div>
          <div>
            <strong>Gestor</strong>
            <span>acesso ao modulo vinculado</span>
          </div>
          <div>
            <strong>Operador</strong>
            <span>dados e chamados do proprio fluxo</span>
          </div>
        </div>
      </section>

      <section class="login-card">
        <div class="login-card__head">
          <p class="eyebrow">Acesso</p>
          <h2>Entrar no T-GER</h2>
          <p class="muted">Use uma credencial de teste para validar a navegacao.</p>
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
            <span>Admin</span>
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
    <div class="login-footer-note">
      Ambiente de teste local com login simples (`auth/login`)
    </div>
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
