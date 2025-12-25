<template>
  <div>
    <section class="hero">
      <div class="container hero-inner">
        <div>
          <div class="title">Create account</div>
          <div class="subtitle">Start your plant journey</div>
        </div>
      </div>
    </section>
    <div class="container mt-2">
      <el-card class="auth-card">
        <template #header>Register</template>
        <el-form label-position="top" @submit.prevent>
          <el-form-item label="Email">
            <el-input v-model="email" type="email" autocomplete="username" />
          </el-form-item>
          <el-form-item label="Password">
            <el-input v-model="password" type="password" autocomplete="new-password" />
          </el-form-item>
          <el-button type="primary" :loading="loading" @click="doRegister">Register</el-button>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const email = ref('')
const password = ref('')
const loading = ref(false)
const router = useRouter()
const auth = useAuthStore()

async function doRegister() {
  loading.value = true
  try {
    await auth.register(email.value, password.value)
    if (auth.isAuthenticated) {
      router.replace('/')
    } else {
      ElMessage.success('Регистрация успешна! Теперь войдите.')
      router.replace('/auth/login')
    }
  } catch (e: any) {
    const detail = e?.response?.data?.detail
    ElMessage.error(detail || 'Не удалось зарегистрироваться. Проверьте email и пароль. Возможно, email уже занят.')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-card { max-width: 440px; margin: 20px auto; }
.mt-2 { margin-top: 16px; }
</style>
