<template>
  <div>
    <section class="hero">
      <div class="container hero-inner">
        <div>
          <div class="title">Plant Care</div>
          <div class="subtitle">Track watering, fertilizing and other care</div>
        </div>
        <div class="actions">
          <el-button @click="$router.back()">Back</el-button>
        </div>
      </div>
    </section>

    <div class="container mt-2">
      <el-form inline @submit.prevent>
        <el-form-item label="Note">
          <el-input v-model="note" placeholder="Watered, fertilized..." />
        </el-form-item>
        <el-button type="primary" @click="add">Add</el-button>
      </el-form>
      <el-table :data="rows" stripe class="mt-1">
        <el-table-column prop="id" label="ID" width="220" />
        <el-table-column prop="note" label="Note" />
        <el-table-column prop="createdAt" label="When" />
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { api } from '@/api/client'

const route = useRoute()
const rows = ref<any[]>([])
const note = ref('')

async function load() {
  const { data } = await api.get(`/diary/care/${route.params.id}`)
  rows.value = Array.isArray(data) ? data : (data.content ?? [])
}

async function add() {
  await api.post(`/diary/care/${route.params.id}`, { note: note.value })
  note.value = ''
  await load()
}

onMounted(load)
</script>

<style scoped>
.mt-1 { margin-top: 8px; }
.mt-2 { margin-top: 16px; }
</style>
