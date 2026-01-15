<template>
  <div>
    <el-page-header @back="$router.back()" content="Plant details" class="mb-2" />
    <el-card v-if="plant">
      <h2>{{ plant.commonName || plant.latinName }}</h2>
      <div v-if="plant.latinName" class="latin">Latin: {{ plant.latinName }}</div>
      <el-descriptions :column="2" border class="mt-2">
        <el-descriptions-item label="Light" v-if="plant.light">
          <span class="capitalize">{{ plant.light }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="Water" v-if="plant.water">
          <span class="capitalize">{{ plant.water }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="Min temp (°C)" v-if="plant.minTempC != null">
          {{ plant.minTempC }}
        </el-descriptions-item>
        <el-descriptions-item label="Max temp (°C)" v-if="plant.maxTempC != null">
          {{ plant.maxTempC }}
        </el-descriptions-item>
      </el-descriptions>
      <div v-if="plant.notes" class="notes mt-2">{{ plant.notes }}</div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { api } from '@/api/client'

const route = useRoute()
const plant = ref<any>(null)

async function load() {
  const { data } = await api.get(`/plants/${route.params.id}`)
  plant.value = data
}

onMounted(load)
</script>

<style scoped>
.mb-2 { margin-bottom: 12px; }
.mt-2 { margin-top: 12px; }
.latin { color: var(--ink-400); font-size: 12px; }
.notes { white-space: pre-wrap; }
.capitalize { text-transform: capitalize; }
</style>
