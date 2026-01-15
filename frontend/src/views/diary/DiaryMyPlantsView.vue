<template>
  <div>
    <section class="hero">
      <div class="container hero-inner">
        <div>
          <div class="title">My Plants</div>
          <div class="subtitle">Your personal collection and care log</div>
        </div>
        <div class="actions">
          <el-form inline @submit.prevent class="toolbar add-form">
            <el-form-item label="Plant">
              <el-select v-model="selectedPlantId" filterable remote reserve-keyword clearable placeholder="Search plants" :remote-method="searchPlants" :loading="loadingPlants" style="width: 260px">
                <el-option v-for="p in plantOptions" :key="p.id" :label="p.commonName || p.latinName" :value="p.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="Nickname">
              <el-input v-model="nickname" placeholder="Optional" style="width: 220px" />
            </el-form-item>
            <el-button type="primary" @click="create">Add</el-button>
          </el-form>
        </div>
      </div>
    </section>

    <div class="container mt-2">
      <el-table :data="rows" stripe>
        <el-table-column label="Plant">
          <template #default="{ row }">
            {{ row.nickname || row.plantCommonName || row.plantLatinName || row.plantId || '—' }}
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/diary/plants/${row.id}/care`)">Care</el-button>
            <el-popconfirm title="Remove from My Diary?" @confirm="remove(row.id)">
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
import { api } from '@/api/client'
const rows = ref<any[]>([])
const selectedPlantId = ref<string>('')
const nickname = ref<string>('')
const plantOptions = ref<any[]>([])
const loadingPlants = ref(false)
async function load() {
  const { data } = await api.get('/diary/plants')
  rows.value = Array.isArray(data) ? data : (data.items ?? data.content ?? [])
}
async function create() {
  if (!selectedPlantId.value && !nickname.value) {
    // eslint-disable-next-line no-alert
    alert('Select a plant or provide a nickname')
    return
  }
  await api.post('/diary/plants', { plantId: selectedPlantId.value || null, nickname: nickname.value || null })
  selectedPlantId.value = ''
  nickname.value = ''
  await load()
}
async function searchPlants(query: string) {
  loadingPlants.value = true
  try {
    const { data } = await api.get('/plants', { params: { q: query, page: 0, size: 20 } })
    const items = Array.isArray(data) ? data : (data.items ?? data.content ?? [])
    plantOptions.value = items
  } finally {
    loadingPlants.value = false
  }
}

async function remove(id: string) {
  await api.delete(`/diary/plants/${id}`)
  await load()
}
onMounted(load)
</script>

<style scoped>
.mt-2 { margin-top: 16px; }
.toolbar { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.toolbar :deep(.el-form-item) { margin-bottom: 0; }
</style>
