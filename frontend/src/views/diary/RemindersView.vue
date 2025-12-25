<template>
  <div>
    <section class="hero">
      <div class="container hero-inner">
        <div>
          <div class="title">Reminders</div>
          <div class="subtitle">What’s due soon for your plants</div>
        </div>
        <div class="actions">
          <el-form inline @submit.prevent>
            <el-form-item label="Before">
              <el-date-picker v-model="before" type="date" placeholder="Pick a day" format="YYYY-MM-DD" value-format="YYYY-MM-DD" />
            </el-form-item>
            <el-button type="primary" @click="load">Load</el-button>
          </el-form>
        </div>
      </div>
    </section>
    
    <div class="container mt-2">
      <el-table :data="rows" stripe class="mt-1">
        <el-table-column prop="id" label="ID" width="220" />
        <el-table-column prop="title" label="Title" />
        <el-table-column prop="dueAt" label="Due" />
        <el-table-column label="Actions" width="160">
          <template #default="{ row }">
            <el-button size="small" type="success" @click="complete(row.id)">Complete</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '@/api/client'

const before = ref<string>('')
const rows = ref<any[]>([])

async function load() {
  const params: any = {}
  if (before.value) params.before = before.value
  const { data } = await api.get('/diary/reminders/due', { params })
  rows.value = Array.isArray(data) ? data : (data.content ?? [])
}

async function complete(reminderId: string) {
  await api.post(`/diary/reminders/complete/${reminderId}`)
  await load()
}

onMounted(() => {
  const today = new Date()
  before.value = today.toISOString().slice(0, 10)
  load()
})
</script>

<style scoped>
.mt-1 { margin-top: 8px; }
.mt-2 { margin-top: 16px; }
</style>
