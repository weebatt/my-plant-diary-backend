<template>
  <div>
    <h2>Calendar — Week</h2>
    <el-form inline @submit.prevent class="toolbar">
      <el-form-item label="Start">
        <el-date-picker v-model="start" type="date" format="YYYY-MM-DD" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">Load</el-button>
      </el-form-item>
    </el-form>
    <pre class="mt-1">{{ JSON.stringify(data, null, 2) }}</pre>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '@/api/client'

const start = ref<string>('')
const data = ref<any>(null)

async function load() {
  const { data: d } = await api.get('/calendar/week', { params: { start: start.value } })
  data.value = d
}

onMounted(() => {
  const today = new Date()
  start.value = today.toISOString().slice(0, 10)
  load()
})
</script>

<style scoped>
.mt-1 { margin-top: 8px; }
.toolbar { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.toolbar :deep(.el-form-item) { margin-bottom: 0; }
</style>
