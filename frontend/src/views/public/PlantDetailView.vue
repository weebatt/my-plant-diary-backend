<template>
  <div>
    <el-page-header @back="$router.back()" content="Plant details" class="mb-2" />
    <el-card v-if="plant">
      <h2>{{ plant.commonName || plant.latinName }}</h2>
      <div v-if="plant.latinName">Latin: {{ plant.latinName }}</div>
      <div v-if="plant.description">{{ plant.description }}</div>
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
</style>

