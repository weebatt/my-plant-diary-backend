<template>
  <div>
    <el-form inline class="toolbar mb-3" @submit.prevent>
      <el-form-item>
        <el-input v-model="q" placeholder="Search plants..." @keyup.enter="load" clearable style="width: 280px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">Search</el-button>
      </el-form-item>
      <el-form-item>
        <el-switch v-model="onlyFavs" active-text="Favorites" />
      </el-form-item>
    </el-form>

    <el-row :gutter="12">
      <template v-if="loading">
        <el-col v-for="n in 8" :key="n" :span="6">
          <el-skeleton animated :throttle="200">
            <template #template>
              <el-skeleton-item variant="rect" style="height: 96px; border-radius: 10px" />
            </template>
          </el-skeleton>
        </el-col>
      </template>
      <template v-else>
        <el-col v-for="p in filtered" :key="p.id" :span="6">
          <el-card class="plant-card mb-3 clickable fadein" @click="open(p.id)">
            <div class="row1">
              <div>
                <div class="name">{{ p.commonName || p.latinName }}</div>
                <div class="latin" v-if="p.latinName">{{ p.latinName }}</div>
              </div>
              <el-button text class="heart" @click.stop="fav.toggle(p.id)">
                <span :class="['icon', { active: fav.isFav(p.id) }]"></span>
              </el-button>
            </div>
            <div class="tags">
              <el-tag v-if="p.light" size="small" type="success" @click.stop="goRecommend({ light: p.light })">
                {{ p.light }}
              </el-tag>
              <el-tag v-if="p.water" size="small" type="info" @click.stop="goRecommend({ water: p.water })">
                {{ p.water }}
              </el-tag>
            </div>
          </el-card>
        </el-col>
      </template>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '@/api/client'
import { useFavoritesStore } from '@/stores/favorites'

interface Plant {
  id: string
  commonName?: string
  latinName?: string
}

const q = ref('')
const plants = ref<Plant[]>([])
const router = useRouter()
const fav = useFavoritesStore()
const loading = ref(false)
const onlyFavs = ref(false)

const filtered = computed(() => {
  return onlyFavs.value ? plants.value.filter((p) => fav.isFav(p.id)) : plants.value
})

async function load() {
  loading.value = true
  try {
    const { data } = await api.get('/plants', { params: { q: q.value, page: 0, size: 12 } })
    plants.value = Array.isArray(data) ? data : (data.items ?? data.content ?? [])
  } finally {
    loading.value = false
  }
}

onMounted(load)

function open(id: string) {
  router.push(`/plants/${id}`)
}

function goRecommend(params: { light?: string; water?: string }) {
  const q: any = {}
  if (params.light) q.light = String(params.light).toLowerCase()
  if (params.water) q.water = String(params.water).toLowerCase()
  router.push({ path: '/plants/recommendations', query: q })
}
</script>

<style scoped>
.name { font-weight: 700; color: var(--ink-700); }
.latin { color: var(--ink-400); font-size: 12px; }
.plant-card {
  border-radius: var(--radius-md);
  border: 1px solid var(--ink-100);
  transition: transform .15s ease, box-shadow .15s ease, border-color .15s ease;
}
.plant-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--brand-200);
}
.clickable { cursor: pointer; }
.toolbar :deep(.el-form-item) { margin-bottom: 0; }
.toolbar { display: flex; align-items: center; gap: 12px; }
.row1 { display: flex; justify-content: space-between; align-items: start; }
.heart .icon::before { content: '♡'; font-size: 18px; }
.heart .icon.active::before { content: '♥'; color: #f56c6c; }
.tags { margin-top: 6px; display: flex; gap: 6px; }
.fadein { animation: fadein .25s ease-in; }
@keyframes fadein { from { opacity: 0; transform: translateY(2px); } to { opacity: 1; transform: translateY(0); } }
</style>
