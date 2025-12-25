<template>
  <div>
    <section class="hero">
      <div class="container hero-inner">
        <div>
          <div class="title">Welcome back</div>
          <div class="subtitle">Log in to your plant diary</div>
        </div>
      </div>
    </section>
    <div class="container mt-2">
      <el-card class="auth-card">
        <template #header>Login</template>
        <el-form label-position="top" @submit.prevent>
          <el-form-item label="Email">
            <el-input v-model="email" type="email" autocomplete="username" />
          </el-form-item>
          <el-form-item label="Password">
            <el-input v-model="password" type="password" autocomplete="current-password" />
          </el-form-item>
          <el-button type="primary" :loading="loading" @click="doLogin">Login</el-button>
        </el-form>
        <div class="mt-1">
          <router-link to="/auth/register">Register</router-link>
        </div>
      </el-card>
    </div>
  </div>
  
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const email = ref('')
const password = ref('')
const loading = ref(false)
const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

async function doLogin() {
  loading.value = true
  try {
    await auth.login(email.value, password.value)
    const redirect = (route.query.redirect as string) || '/'
    router.replace(redirect)
  } catch (e) {
    ElMessage.error('Не удалось войти. Проверьте email/пароль.')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-card { max-width: 440px; margin: 20px auto; }
.mt-1 { margin-top: 8px; }
.mt-2 { margin-top: 16px; }
</style>
