<template>
  <div>
    <h2>Profile</h2>
    <el-alert v-if="!authEnabled" title="Authentication is disabled in this environment" type="info" class="mb-2" />
    <el-card class="panel-card">
      <el-descriptions v-if="auth.profile" :column="1" border>
        <el-descriptions-item label="ID">{{ auth.profile.id }}</el-descriptions-item>
        <el-descriptions-item label="Email">{{ auth.profile.email }}</el-descriptions-item>
        <el-descriptions-item label="Role">{{ auth.profile.role || 'USER' }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="authEnabled && auth.profile" class="mt-1">
        <el-button type="primary" @click="linkTelegram" :disabled="!botUser">Connect Telegram</el-button>
        <el-button v-if="bindToken" class="ml-1" @click="copyStartCommand">Copy /start command</el-button>
        <div class="hint" v-if="botUser">
          If nothing happens in Telegram after opening, send the copied
          <code>/start &lt;token&gt;</code> message to the bot once to complete linking.
        </div>
        <span v-if="!botUser" class="hint">Set VITE_TELEGRAM_BOT_USERNAME in frontend env</span>
      </div>
      <el-button v-if="auth.profile" class="mt-1" @click="auth.logout(); $router.push('/')">Logout</el-button>
    </el-card>
  </div>
  
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { authEnabled, telegramBotUsername as botUser } from '@/config'
import { api } from '@/api/client'
import { ref } from 'vue'
const auth = useAuthStore()
onMounted(() => { if (authEnabled) auth.fetchProfile() })

const bindToken = ref('')
async function linkTelegram() {
  if (!botUser) return
  const { data } = await api.post<{ token: string }>(`/telegram/link/start`)
  bindToken.value = data.token
  // Вариант B: короткий одноразовый bind‑token, компактный для диплинка
  const url = `https://t.me/${botUser}?start=${encodeURIComponent(bindToken.value)}`
  window.open(url, '_blank')
}

async function copyStartCommand() {
  if (!bindToken.value) return
  const text = `/start ${bindToken.value}`
  try {
    await navigator.clipboard.writeText(text)
    // eslint-disable-next-line no-alert
    alert('Copied: ' + text)
  } catch {
    // eslint-disable-next-line no-alert
    alert(text)
  }
}
</script>

<style scoped>
.mt-1 { margin-top: 8px; }
.mb-2 { margin-bottom: 12px; }
.ml-1 { margin-left: 8px; }
.hint { margin-left: 8px; color: #888; }
code { background: #f3f3f3; padding: 0 4px; border-radius: 3px; }
</style>
