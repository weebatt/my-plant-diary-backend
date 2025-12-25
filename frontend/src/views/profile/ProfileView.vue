<template>
  <div>
    <h2>Profile</h2>
    <el-alert v-if="!authEnabled" title="Authentication is disabled in this environment" type="info" class="mb-2" />
    <el-descriptions v-if="auth.profile" :column="1" border>
      <el-descriptions-item label="ID">{{ auth.profile.id }}</el-descriptions-item>
      <el-descriptions-item label="Email">{{ auth.profile.email }}</el-descriptions-item>
      <el-descriptions-item label="Role">{{ auth.profile.role || 'USER' }}</el-descriptions-item>
    </el-descriptions>
    <el-button v-if="auth.profile" class="mt-1" @click="auth.logout(); $router.push('/')">Logout</el-button>
  </div>
  
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { authEnabled } from '@/config'
const auth = useAuthStore()
onMounted(() => { if (authEnabled) auth.fetchProfile() })
</script>

<style scoped>
.mt-1 { margin-top: 8px; }
.mb-2 { margin-bottom: 12px; }
</style>
