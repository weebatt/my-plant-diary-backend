<template>
  <div>
    <el-row justify="space-between" class="mb-2">
      <h2>Admin Plants</h2>
      <el-button type="primary" @click="$router.push('/admin/plants/new')">New</el-button>
    </el-row>
    <el-table :data="rows" stripe>
      <el-table-column prop="id" label="ID" width="220" />
      <el-table-column prop="commonName" label="Name" />
      <el-table-column label="Actions" width="150">
        <template #default="{ row }">
          <el-button size="small" @click="$router.push(`/admin/plants/${row.id}`)">Edit</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '@/api/client'
const rows = ref<any[]>([])
async function load() {
  const { data } = await api.get('/plants', { params: { page: 0, size: 50 } })
  rows.value = data.content ?? data
}
onMounted(load)
</script>

<style scoped>
.mb-2 { margin-bottom: 12px; }
</style>

