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
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '@/api/client'

const route = useRoute()
const router = useRouter()
const model = ref<any>(null)

async function load() {
  const { data } = await api.get(`/admin/plants/${route.params.id}`)
  model.value = data
}

async function save() {
  await api.patch(`/admin/plants/${route.params.id}`, model.value)
  router.back()
}

async function remove() {
  await api.delete(`/admin/plants/${route.params.id}`)
  router.push('/admin/plants')
}

onMounted(load)
</script>

<style scoped>
.mb-2 { margin-bottom: 12px; }
</style>

