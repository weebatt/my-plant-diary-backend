<template>
  <div>
    <section class="hero">
      <div class="container hero-inner">
        <div>
          <div class="title">My Plants</div>
          <div class="subtitle">Your personal collection and care log</div>
        </div>
        <div class="actions">
          <el-button type="primary" @click="create">Add Plant</el-button>
        </div>
      </div>
    </section>

    <div class="container mt-2">
      <el-table :data="rows" stripe>
        <el-table-column prop="id" label="ID" width="220" />
        <el-table-column prop="plantName" label="Plant" />
        <el-table-column label="Actions" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/diary/plants/${row.id}/care`)">Care</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '@/api/client'
const rows = ref<any[]>([])
async function load() {
  const { data } = await api.get('/diary/plants')
  rows.value = Array.isArray(data) ? data : (data.content ?? [])
}
async function create() {
  await api.post('/diary/plants', { plantId: null, nickname: 'My plant' })
  await load()
}
onMounted(load)
</script>

<style scoped>
.mt-2 { margin-top: 16px; }
</style>
