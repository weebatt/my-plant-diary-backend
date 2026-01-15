import { defineStore } from 'pinia'

const KEY = 'favorites'

function load(): string[] {
  try {
    const raw = localStorage.getItem(KEY)
    if (!raw) return []
    const arr = JSON.parse(raw)
    return Array.isArray(arr) ? (arr.filter((x) => typeof x === 'string') as string[]) : []
  } catch {
    return []
  }
}

export const useFavoritesStore = defineStore('favorites', {
  state: () => ({ ids: load() as string[] }),
  getters: {
    isFav: (s) => (id: string) => s.ids.includes(id),
    set(state) {
      return new Set(state.ids)
    },
  },
  actions: {
    save() {
      try {
        localStorage.setItem(KEY, JSON.stringify(this.ids))
      } catch {}
    },
    add(id: string) {
      if (!this.ids.includes(id)) {
        this.ids.push(id)
        this.save()
      }
    },
    remove(id: string) {
      const i = this.ids.indexOf(id)
      if (i >= 0) {
        this.ids.splice(i, 1)
        this.save()
      }
    },
    toggle(id: string) {
      this.isFav(id) ? this.remove(id) : this.add(id)
    },
    clear() {
      this.ids = []
      this.save()
    },
  },
})

