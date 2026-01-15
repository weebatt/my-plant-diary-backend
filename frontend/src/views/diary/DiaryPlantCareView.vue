<template>
  <div>
    <section class="hero">
      <div class="container hero-inner">
        <div>
          <div class="title">Plant Care</div>
          <div class="subtitle">Track watering, fertilizing and other care</div>
        </div>
        <div class="actions">
          <el-button @click="$router.back()">Back</el-button>
        </div>
      </div>
    </section>

    <div class="container mt-2">
      <el-form inline @submit.prevent class="toolbar">
        <el-form-item label="Kind">
          <el-select v-model="kind" placeholder="Select care type" style="width: 160px">
            <el-option label="Water" value="water" />
            <el-option label="Fertilize" value="fertilize" />
            <el-option label="Mist" value="mist" />
            <el-option label="Repot" value="repot" />
          </el-select>
        </el-form-item>
        <el-form-item label="Note">
          <el-input v-model="note" placeholder="Watered, fertilized..." />
        </el-form-item>
        <el-button type="primary" @click="add">Add</el-button>
        <el-divider direction="vertical" />
        <el-form-item label="Reminder date">
          <el-date-picker v-model="dueDate" type="date" format="YYYY-MM-DD" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="Time">
          <el-time-picker v-model="dueTime" format="HH:mm" value-format="HH:mm" placeholder="HH:mm" />
        </el-form-item>
        <el-form-item label="Note">
          <el-input v-model="reminderNote" placeholder="Optional reminder note" />
        </el-form-item>
        <el-button @click="createReminder">Create reminder</el-button>
      </el-form>
      <el-table :data="rows" stripe class="mt-1">
        <el-table-column prop="kind" label="Type" width="140" />
        <el-table-column prop="notes" label="Note" />
        <el-table-column label="Actions" width="120">
          <template #default="{ row }">
            <el-popconfirm title="Delete care entry?" @confirm="remove(row.id)">
              <template #reference>
                <el-button size="small" type="danger">Delete</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { api } from '@/api/client'

const route = useRoute()
const rows = ref<any[]>([])
const kind = ref('water')
const note = ref('')
const dueDate = ref('')
const dueTime = ref('09:00')
const reminderNote = ref('')

async function load() {
  const { data } = await api.get(`/diary/care/${route.params.id}`)
  rows.value = Array.isArray(data) ? data : (data.items ?? data.content ?? [])
}

async function add() {
  try {
    if (!kind.value) {
      // eslint-disable-next-line no-alert
      alert('Please select care type')
      return
    }
    await api.post(`/diary/care/${route.params.id}`, { kind: kind.value, notes: note.value })
  } catch (e: any) {
    const detail = e?.response?.data?.detail || 'Failed to add care entry'
    // eslint-disable-next-line no-alert
    alert(detail)
    return
  }
  note.value = ''
  await load()
}

async function createReminder() {
  try {
    if (!dueDate.value) {
      // eslint-disable-next-line no-alert
      alert('Pick reminder date')
      return
    }
    // Build local date-time from selected date and time, then convert to UTC ISO string.
    // This preserves the user's wall-clock time regardless of timezone.
    const [hh, mm] = (dueTime.value || '09:00').split(':')
    const [y, mo, d] = dueDate.value.split('-')
    const local = new Date(Number(y), Number(mo) - 1, Number(d), Number(hh), Number(mm), 0)
    const dueAt = local.toISOString()
    await api.post(`/diary/reminders/${route.params.id}`, { kind: kind.value, dueAt, notes: reminderNote.value || undefined })
    // eslint-disable-next-line no-alert
    alert('Reminder created')
  } catch (e: any) {
    const detail = e?.response?.data?.detail || 'Failed to create reminder'
    // eslint-disable-next-line no-alert
    alert(detail)
  }
}

async function remove(careId: string) {
  await api.delete(`/diary/care/${route.params.id}/${careId}`)
  await load()
}

onMounted(load)
</script>

<style scoped>
.mt-1 { margin-top: 8px; }
.mt-2 { margin-top: 16px; }
.toolbar { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.toolbar :deep(.el-form-item) { margin-bottom: 0; }
</style>
