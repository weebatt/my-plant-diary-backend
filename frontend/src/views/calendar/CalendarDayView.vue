<template>
  <div>
    <h2>Calendar — Day</h2>
    <el-form inline @submit.prevent class="toolbar">
      <el-form-item label="Date">
        <el-date-picker v-model="date" type="date" format="YYYY-MM-DD" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="load">Load</el-button>
      </el-form-item>
    </el-form>

    <el-empty v-if="!rows.length && !loading" description="No reminders for this day" class="mt-2" />

    <el-table v-else :data="rows" stripe class="mt-2">
      <el-table-column label="Time" width="120">
        <template #default="{ row }">{{ formatTime(row.dueAt) }}</template>
      </el-table-column>
      <el-table-column label="Plant">
        <template #default="{ row }">{{ plantName(row) }}</template>
      </el-table-column>
      <el-table-column prop="kind" label="Kind" width="140">
        <template #default="{ row }">{{ capitalize(row.kind) }}</template>
      </el-table-column>
      <el-table-column prop="notes" label="Note" />
      <el-table-column label="Done" width="120">
        <template #default="{ row }">
          <el-checkbox :model-value="row.completed" :disabled="completing.has(row.reminderId)" @change="(val: boolean) => toggleComplete(row, val)"></el-checkbox>
        </template>
      </el-table-column>
    </el-table>
  </div>
  </template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '@/api/client'

interface CalRow {
  reminderId: string
  userPlantId: string
  kind: string
  dueAt: string
  nickname?: string
  plantLatinName?: string
  plantCommonName?: string
}

const date = ref<string>('')
const rows = ref<CalRow[]>([])
const loading = ref(false)
const completing = ref<Set<string>>(new Set())

async function load() {
  loading.value = true
  try {
    const { data } = await api.get('/calendar/day', { params: { date: date.value } })
    rows.value = Array.isArray(data) ? data : (data.items ?? data.content ?? [])
  } finally {
    loading.value = false
  }
}

function plantName(r: CalRow) {
  return r.nickname || r.plantCommonName || r.plantLatinName || r.userPlantId
}

function formatTime(iso: string) {
  try {
    const d = new Date(iso)
    return d.toISOString().slice(11, 16) // HH:mm
  } catch {
    return iso
  }
}

function capitalize(s: string) {
  if (!s) return s
  return s.charAt(0).toUpperCase() + s.slice(1)
}

async function toggleComplete(row: CalRow, val: boolean) {
  if (completing.value.has(row.reminderId)) return
  completing.value.add(row.reminderId)
  try {
    if (val) await api.post(`/diary/reminders/complete/${row.reminderId}`)
    else await api.post(`/diary/reminders/uncomplete/${row.reminderId}`)
    row.completed = val
  } catch (e: any) {
    const detail = e?.response?.data?.detail || 'Failed to update reminder'
    // eslint-disable-next-line no-alert
    alert(detail)
  } finally {
    completing.value.delete(row.reminderId)
  }
}

onMounted(() => {
  const today = new Date()
  date.value = today.toISOString().slice(0, 10)
  load()
})
</script>

<style scoped>
.mt-1 { margin-top: 8px; }
.mt-2 { margin-top: 16px; }
.toolbar { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.toolbar :deep(.el-form-item) { margin-bottom: 0; }
</style>
