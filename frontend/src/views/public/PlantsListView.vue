<template>
  <div>
    <el-row :gutter="12" align="middle" class="mb-3">
      <el-col :span="6">
        <el-input v-model="q" placeholder="Search plants..." @keyup.enter="load" clearable />
      </el-col>
      <el-col>
        <el-button type="primary" @click="load">Search</el-button>
      </el-col>
    </el-row>

    <el-row :gutter="12">
      <el-col v-for="p in plants" :key="p.id" :span="6">
        <el-card class="plant-card mb-3">
          <div class="name">{{ p.commonName || p.latinName }}</div>
          <div class="latin" v-if="p.latinName">{{ p.latinName }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '@/api/client'

interface Plant {
  id: string
  commonName?: string
  latinName?: string
}

const q = ref('')
const plants = ref<Plant[]>([])

async function load() {
  const { data } = await api.get('/plants', { params: { q: q.value, page: 0, size: 12 } })
  plants.value = data.content ?? data
}

onMounted(load)
</script>

<style scoped>
.name { font-weight: 700; color: var(--ink-700); }
.latin { color: var(--ink-400); font-size: 12px; }
.plant-card {
  border-radius: var(--radius-md);
  border: 1px solid var(--ink-100);
  transition: transform .15s ease, box-shadow .15s ease, border-color .15s ease;
}
.plant-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--brand-200);
}
</style>
