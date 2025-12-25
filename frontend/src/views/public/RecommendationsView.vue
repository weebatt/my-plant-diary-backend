<template>
  <div>
    <h2>Recommendations</h2>
    <el-row :gutter="12" class="mb-2">
      <el-col :span="4"><el-input v-model.number="light" placeholder="light"></el-input></el-col>
      <el-col :span="4"><el-input v-model.number="water" placeholder="water"></el-input></el-col>
      <el-col :span="4"><el-input v-model.number="minTempC" placeholder="minTempC"></el-input></el-col>
      <el-col :span="4"><el-input v-model.number="maxTempC" placeholder="maxTempC"></el-input></el-col>
      <el-col><el-button type="primary" @click="load">Apply</el-button></el-col>
    </el-row>
    <el-row :gutter="12">
      <el-col v-for="p in list" :key="p.id" :span="6">
        <el-card class="mb-2">
          <div class="name">{{ p.commonName || p.latinName }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '@/api/client'

const light = ref<number | undefined>()
const water = ref<number | undefined>()
const minTempC = ref<number | undefined>()
const maxTempC = ref<number | undefined>()
const list = ref<any[]>([])

async function load() {
  const params: any = { limit: 12 }
  if (light.value != null) params.light = light.value
  if (water.value != null) params.water = water.value
  if (minTempC.value != null) params.minTempC = minTempC.value
  if (maxTempC.value != null) params.maxTempC = maxTempC.value
  const { data } = await api.get('/plants/recommendations', { params })
  list.value = data
}

onMounted(load)
</script>

<style scoped>
.mb-2 { margin-bottom: 12px; }
.name { font-weight: 600; }
</style>

