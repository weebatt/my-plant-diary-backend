<template>
  <div>
    <el-page-header @back="$router.back()" content="Edit plant" class="mb-2" />
    <el-form label-position="top" v-if="model" @submit.prevent>
      <el-form-item label="Common name">
        <el-input v-model="model.commonName" />
      </el-form-item>
      <el-form-item label="Latin name">
        <el-input v-model="model.latinName" />
      </el-form-item>
      <el-form-item label="Light">
        <el-select
          v-model="model.light"
          filterable
          clearable
          allow-create
          default-first-option
          placeholder="e.g., bright, medium, low"
        >
          <el-option
            v-for="opt in lightOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="Water">
        <el-select
          v-model="model.water"
          filterable
          clearable
          allow-create
          default-first-option
          placeholder="e.g., moderate, high, low"
        >
          <el-option
            v-for="opt in waterOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="Min temp (°C)">
        <el-input-number v-model="model.minTempC" :step="1" :min="-20" :max="50" />
      </el-form-item>
      <el-form-item label="Max temp (°C)">
        <el-input-number v-model="model.maxTempC" :step="1" :min="-20" :max="60" />
      </el-form-item>
      <el-space>
        <el-button type="primary" @click="save">Save</el-button>
        <el-popconfirm title="Delete this plant?" @confirm="remove">
          <template #reference>
            <el-button type="danger">Delete</el-button>
          </template>
        </el-popconfirm>
      </el-space>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '@/api/client'
import { fetchDistinctLightWater } from '@/api/plants'

const route = useRoute()
const router = useRouter()
const model = ref<any>(null)
type Option = { label: string; value: string }
const lightOptions = ref<Option[]>([])
const waterOptions = ref<Option[]>([])

async function load() {
  const { data } = await api.get(`/admin/plants/${route.params.id}`)
  model.value = data
  ensureInOptions(model.value?.light, lightOptions)
  ensureInOptions(model.value?.water, waterOptions)
}

async function save() {
  const payload: any = { ...model.value }
  if (typeof payload.light === 'string') payload.light = payload.light.trim().toLowerCase()
  if (typeof payload.water === 'string') payload.water = payload.water.trim().toLowerCase()
  await api.patch(`/admin/plants/${route.params.id}`, payload)
  router.back()
}

async function remove() {
  await api.delete(`/admin/plants/${route.params.id}`)
  router.push('/admin/plants')
}

onMounted(load)

function ensureInOptions(value: string | undefined, list: typeof lightOptions) {
  const v = (value || '').trim().toLowerCase()
  if (!v) return
  if (!list.value.some((o) => o.value === v)) list.value.push({ value: v, label: v })
}

onMounted(async () => {
  try {
    const d = await fetchDistinctLightWater()
    lightOptions.value = d.light.map((x) => ({ value: x, label: x }))
    waterOptions.value = d.water.map((x) => ({ value: x, label: x }))
    ensureInOptions(model.value?.light, lightOptions)
    ensureInOptions(model.value?.water, waterOptions)
  } catch {
    // ignore
  }
})

watch(
  () => model.value?.light,
  (val) => {
    const v = (val || '').trim().toLowerCase()
    if (model.value && v && v !== model.value.light) model.value.light = v
    ensureInOptions(v, lightOptions)
  }
)
watch(
  () => model.value?.water,
  (val) => {
    const v = (val || '').trim().toLowerCase()
    if (model.value && v && v !== model.value.water) model.value.water = v
    ensureInOptions(v, waterOptions)
  }
)
</script>

<style scoped>
.mb-2 { margin-bottom: 12px; }
</style>
