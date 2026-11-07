import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { favoriteApi } from '../api'

export const useFavoriteStore = defineStore('favorites', () => {
  const ids = ref<number[]>([])
  const loaded = ref(false)
  const loading = ref(false)
  const idSet = computed(() => new Set(ids.value))

  async function load(): Promise<void> {
    loading.value = true
    try {
      ids.value = await favoriteApi.ids()
      loaded.value = true
    } finally {
      loading.value = false
    }
  }

  async function toggle(resourceId: number): Promise<boolean> {
    const favorited = idSet.value.has(resourceId)
    if (favorited) {
      await favoriteApi.remove(resourceId)
      ids.value = ids.value.filter(id => id !== resourceId)
    } else {
      await favoriteApi.add(resourceId)
      ids.value = [resourceId, ...ids.value.filter(id => id !== resourceId)]
    }
    loaded.value = true
    return !favorited
  }

  function isFavorite(resourceId: number): boolean {
    return idSet.value.has(resourceId)
  }

  function clear(): void {
    ids.value = []
    loaded.value = false
  }

  return {
    ids,
    loaded,
    loading,
    load,
    toggle,
    isFavorite,
    clear
  }
})
