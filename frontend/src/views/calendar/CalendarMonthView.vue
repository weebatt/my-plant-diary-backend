<template>
  <div>
    <h2>Calendar — Month</h2>
    <el-form inline @submit.prevent class="toolbar">
      <el-form-item label="Year">
        <el-input v-model.number="year" type="number" placeholder="Year" style="width:120px" />
      </el-form-item>
      <el-form-item label="Month">
        <el-input v-model.number="month" type="number" placeholder="Month" style="width:120px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">Load</el-button>
      </el-form-item>
    </el-form>
    <pre class="mt-1">{{ JSON.stringify(data, null, 2) }}</pre>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '@/api/client'

const year = ref<number>(new Date().getFullYear())
const month = ref<number>(new Date().getMonth() + 1)
const data = ref<any>(null)

async function load() {
  const { data: d } = await api.get('/calendar/month', { params: { year: year.value, month: month.value } })
  data.value = d
}

onMounted(load)
</script>

<style scoped>
.mt-1 { margin-top: 8px; }
.toolbar { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.toolbar :deep(.el-form-item) { margin-bottom: 0; }
</style>
