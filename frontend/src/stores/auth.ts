import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { authApi } from '../api'
import type { CurrentUser } from '../api/types'
import { useFavoriteStore } from './favorites'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<CurrentUser | null>(null)
  const loaded = ref(false)

  const isAuthenticated = computed(() => Boolean(user.value))
  const isAdmin = computed(() => user.value?.roles.includes('ADMIN') ?? false)
  const canAuthor = computed(() =>
    user.value?.permissions.includes('resource:create:own') ?? false
  )
  const canAudit = computed(() =>
    user.value?.permissions.includes('resource:audit:list') ?? false
  )

  async function load(): Promise<void> {
    if (loaded.value) return
    const favorites = useFavoriteStore()
    try {
      user.value = await authApi.me()
    } catch {
      user.value = null
    } finally {
      loaded.value = true
    }
    if (user.value) {
      try {
        await favorites.load()
      } catch {
        favorites.clear()
      }
    } else {
      favorites.clear()
    }
  }

  async function login(username: string, password: string): Promise<void> {
    user.value = await authApi.login({ username, password })
    loaded.value = true
    const favorites = useFavoriteStore()
    try {
      await favorites.load()
    } catch {
      favorites.clear()
    }
  }

  async function logout(): Promise<void> {
    try {
      await authApi.logout()
    } finally {
      user.value = null
      loaded.value = true
      useFavoriteStore().clear()
    }
  }

  async function enableAuthor(): Promise<void> {
    user.value = await authApi.enableAuthor()
  }

  function can(permission: string): boolean {
    return user.value?.permissions.includes(permission) ?? false
  }

  function clear(): void {
    user.value = null
    useFavoriteStore().clear()
  }

  return {
    user,
    loaded,
    isAuthenticated,
    isAdmin,
    canAuthor,
    canAudit,
    load,
    login,
    logout,
    enableAuthor,
    can,
    clear
  }
})

