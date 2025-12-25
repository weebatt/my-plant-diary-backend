<template>
  <div>
    <el-page-header @back="$router.back()" content="New plant" class="mb-2" />
    <el-form label-position="top" @submit.prevent>
      <el-form-item label="Common name">
        <el-input v-model="model.commonName" />
      </el-form-item>
      <el-form-item label="Latin name">
        <el-input v-model="model.latinName" />
      </el-form-item>
      <el-button type="primary" @click="create">Create</el-button>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '@/api/client'

const router = useRouter()
const model = reactive<any>({ commonName: '', latinName: '' })

async function create() {
  const { data } = await api.post('/admin/plants', model)
  router.push(`/admin/plants/${data.id}`)
}
</script>

<style scoped>
.mb-2 { margin-bottom: 12px; }
</style>

