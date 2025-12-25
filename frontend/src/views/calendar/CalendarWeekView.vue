<template>
  <div>
    <h2>Calendar — Week</h2>
    <el-form inline @submit.prevent>
      <el-date-picker v-model="start" type="date" format="YYYY-MM-DD" value-format="YYYY-MM-DD" />
      <el-button class="ml-1" type="primary" @click="load">Load</el-button>
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
.ml-1 { margin-left: 8px; }
</style>

