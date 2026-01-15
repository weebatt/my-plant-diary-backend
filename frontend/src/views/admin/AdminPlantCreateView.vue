<template>
  <div>
    <el-page-header @back="$router.back()" content="New plant" class="mb-2" />
    <el-form label-position="top" @submit.prevent>
      <el-form-item label="Common name">
        <el-input v-model="model.commonName" />
      </el-form-item>
      <el-form-item label="Latin name" required>
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
      <el-form-item label="Notes">
        <el-input v-model="model.notes" type="textarea" />
      </el-form-item>
      <el-button type="primary" @click="create">Create</el-button>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '@/api/client'
import { fetchDistinctLightWater } from '@/api/plants'

const router = useRouter()
const model = reactive<any>({
  commonName: '',
  latinName: '',
  light: '',
  water: '',
  minTempC: undefined as any,
  maxTempC: undefined as any,
  notes: ''
})

type Option = { label: string; value: string }
const lightOptions = ref<Option[]>([])
const waterOptions = ref<Option[]>([])

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
    // If form has prefilled values, ensure they are present too
    ensureInOptions(model.light, lightOptions)
    ensureInOptions(model.water, waterOptions)
  } catch {
    // ignore network errors; user can still type custom values
  }
})

watch(
  () => model.light,
  (val) => {
    const v = (val || '').trim().toLowerCase()
    if (v && v !== val) model.light = v
    ensureInOptions(v, lightOptions)
  }
)
watch(
  () => model.water,
  (val) => {
    const v = (val || '').trim().toLowerCase()
    if (v && v !== val) model.water = v
    ensureInOptions(v, waterOptions)
  }
)

async function create() {
  try {
    const ln = (model.latinName || '').trim()
    if (!ln) {
      // quick client-side validation to avoid 400
      // eslint-disable-next-line no-alert
      alert('Latin name is required')
      return
    }
    // Build payload to avoid sending empty strings as fields
    const payload: any = { latinName: ln }
    if (model.commonName?.trim()) payload.commonName = model.commonName.trim()
    if (model.light?.trim()) payload.light = model.light.trim().toLowerCase()
    if (model.water?.trim()) payload.water = model.water.trim().toLowerCase()
    if (typeof model.minTempC === 'number') payload.minTempC = model.minTempC
    if (typeof model.maxTempC === 'number') payload.maxTempC = model.maxTempC
    if (model.notes?.trim()) payload.notes = model.notes.trim()
    const { data } = await api.post('/admin/plants', payload)
    router.push(`/admin/plants/${data.id}`)
  } catch (e: any) {
    // Basic error surfacing; backend returns ProblemDetails with 'detail'
    const detail = e?.response?.data?.detail || 'Failed to create plant'
    // eslint-disable-next-line no-alert
    alert(detail)
  }
}
</script>

<style scoped>
.mb-2 { margin-bottom: 12px; }
</style>
