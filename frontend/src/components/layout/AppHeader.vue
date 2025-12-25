<template>
  <header class="app-header header-sheen">
    <div class="container header-inner">
      <div class="brand" @click="$router.push('/')">
        <span class="logo" aria-hidden>🌿</span>
        <span>My Plant Diary</span>
      </div>
      <nav class="nav">
        <router-link to="/plants">Plants</router-link>
        <router-link to="/plants/recommendations">Recommendations</router-link>
        <template v-if="authEnabled">
          <router-link to="/diary/plants">My Diary</router-link>
          <router-link to="/calendar/day">Calendar</router-link>
          <router-link v-if="auth.isAdmin" to="/admin/plants">Admin</router-link>
          <router-link to="/profile">Profile</router-link>
        </template>
      </nav>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import { authEnabled } from '@/config'
const auth = useAuthStore()
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: linear-gradient(180deg, var(--brand-600), var(--brand-500));
  color: #fff;
  border-bottom: 1px solid rgba(255,255,255,0.08);
  box-shadow: var(--shadow-sm);
}
.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
}
.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-weight: 700;
  letter-spacing: 0.2px;
  cursor: pointer;
}
.logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px; height: 28px;
  border-radius: 50%;
  background: rgba(255,255,255,0.16);
  box-shadow: inset 0 0 0 1px rgba(255,255,255,0.12);
}
.nav {
  display: flex;
  align-items: center;
  gap: 8px;
}
.nav a {
  color: #fff;
  opacity: 0.92;
  padding: 6px 10px;
  border-radius: 20px;
}
.nav a:hover { opacity: 1; background: rgba(255,255,255,0.10); }
.nav a.router-link-active {
  background: rgba(255,255,255,0.18);
  box-shadow: inset 0 0 0 1px rgba(255,255,255,0.16);
}
@media (max-width: 860px) {
  .nav { gap: 2px; }
  .nav a { padding: 6px 8px; font-size: 13px; }
}
</style>
