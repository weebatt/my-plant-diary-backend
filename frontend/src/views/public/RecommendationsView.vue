<template>
  <div>
    <h2>Recommendations</h2>
    <el-form inline class="toolbar mb-2" @submit.prevent>
      <el-form-item label="Light">
        <el-select v-model="light" filterable clearable placeholder="select">
          <el-option v-for="opt in lightOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="Water">
        <el-select v-model="water" filterable clearable placeholder="select">
          <el-option v-for="opt in waterOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="Min °C">
        <el-input-number v-model="minTempC" :step="1" :min="-20" :max="50" />
      </el-form-item>
      <el-form-item label="Max °C">
        <el-input-number v-model="maxTempC" :step="1" :min="-20" :max="60" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">Apply</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="12">
      <template v-if="loading">
        <el-col v-for="n in 8" :key="n" :span="6">
          <el-skeleton animated :throttle="200">
            <template #template>
              <el-skeleton-item variant="rect" style="height: 84px; border-radius: 10px" />
            </template>
          </el-skeleton>
        </el-col>
      </template>
      <template v-else>
        <el-col v-for="p in list" :key="p.id" :span="6">
          <el-card class="mb-2 clickable fadein" @click="open(p.id)">
            <div class="name">{{ p.commonName || p.latinName }}</div>
            <div class="tags">
              <el-tag v-if="p.light" size="small" type="success">{{ p.light }}</el-tag>
              <el-tag v-if="p.water" size="small" type="info">{{ p.water }}</el-tag>
            </div>
          </el-card>
        </el-col>
      </template>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { api } from '@/api/client'
import { fetchDistinctLightWater } from '@/api/plants'

type Option = { label: string; value: string }
const lightOptions = ref<Option[]>([])
const waterOptions = ref<Option[]>([])

const light = ref<string | undefined>()
const water = ref<string | undefined>()
const minTempC = ref<number | undefined>()
const maxTempC = ref<number | undefined>()
const list = ref<any[]>([])
const router = useRouter()
const route = useRoute()
const loading = ref(false)

async function load() {
  const params: any = { limit: 12 }
  if (light.value != null) params.light = String(light.value).trim().toLowerCase()
  if (water.value != null) params.water = String(water.value).trim().toLowerCase()
  if (minTempC.value != null) params.minTempC = minTempC.value
  if (maxTempC.value != null) params.maxTempC = maxTempC.value
  loading.value = true
  try {
    const { data } = await api.get('/plants/recommendations', { params })
    list.value = data
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    const d = await fetchDistinctLightWater()
    lightOptions.value = d.light.map((x) => ({ value: x, label: x }))
    waterOptions.value = d.water.map((x) => ({ value: x, label: x }))
  } catch {}
  // init from query
  const ql = String(route.query.light || '').trim().toLowerCase()
  const qw = String(route.query.water || '').trim().toLowerCase()
  light.value = ql || undefined
  water.value = qw || undefined
  await load()
})

function open(id: string) {
  router.push(`/plants/${id}`)
}
</script>

<style scoped>
.mb-2 { margin-bottom: 12px; }
.name { font-weight: 600; }
.clickable { cursor: pointer; }
.toolbar :deep(.el-form-item) { margin-bottom: 0; }
.toolbar { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.tags { margin-top: 6px; display: flex; gap: 6px; }
.fadein { animation: fadein .25s ease-in; }
@keyframes fadein { from { opacity: 0; transform: translateY(2px); } to { opacity: 1; transform: translateY(0); } }
</style>
