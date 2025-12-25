<template>
  <div>
    <h2>Calendar — Month</h2>
    <el-form inline @submit.prevent>
      <el-input v-model.number="year" type="number" placeholder="Year" style="width:120px" />
      <el-input v-model.number="month" type="number" placeholder="Month" style="width:120px" />
      <el-button class="ml-1" type="primary" @click="load">Load</el-button>
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
.ml-1 { margin-left: 8px; }
</style>

