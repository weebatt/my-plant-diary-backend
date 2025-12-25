<template>
  <div class="layout">
    <AppHeader />
    <main class="content">
      <div class="container">
        <slot />
      </div>
    </main>
  </div>
  
</template>

<script setup lang="ts">
import AppHeader from './AppHeader.vue'
</script>

<style scoped>
.layout { display: flex; flex-direction: column; min-height: 100vh; }
.content {
  flex: 1;
  padding: 24px 16px 40px;
}
</style>
