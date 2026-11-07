import { ref } from 'vue'
import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'
import type { ResourceListItem } from '../api/types'

const STORAGE_KEY = 'courseshare:history:v1'
const MAX_HISTORY = 100

export interface HistoryEntry {
  resource: ResourceListItem
  lastViewedAt: string
}

function snapshot(resource: ResourceListItem): ResourceListItem {
  return {
    id: resource.id,
    title: resource.title,
    summary: resource.summary,
    categoryId: resource.categoryId,
    categoryName: resource.categoryName,
    authorId: resource.authorId,
    authorName: resource.authorName,
    attachmentName: resource.attachmentName,
    attachmentExt: resource.attachmentExt,
    attachmentSize: resource.attachmentSize,
    status: resource.status,
    viewCount: resource.viewCount,
    downloadCount: resource.downloadCount,
    publishedAt: resource.publishedAt,
    createdAt: resource.createdAt,
    updatedAt: resource.updatedAt,
    version: resource.version,
    averageRating: resource.averageRating,
    ratingCount: resource.ratingCount,
    favoriteCount: resource.favoriteCount,
    reviewLikeCount: resource.reviewLikeCount,
    popularScore: resource.popularScore
  }
}

function parseHistory(value: string | null): HistoryEntry[] {
  if (!value) return []
  try {
    const parsed: unknown = JSON.parse(value)
    if (!Array.isArray(parsed)) return []
    return parsed
      .filter((item): item is HistoryEntry =>
        Boolean(item)
        && typeof item === 'object'
        && typeof (item as HistoryEntry).lastViewedAt === 'string'
        && typeof (item as HistoryEntry).resource?.id === 'number'
      )
      .slice(0, MAX_HISTORY)
  } catch {
    return []
  }
}

function readStoredHistory(): string | null {
  try {
    return localStorage.getItem(STORAGE_KEY)
  } catch {
    return null
  }
}

export const useHistoryStore = defineStore('history', () => {
  const initialValue = readStoredHistory()
  const initialItems = parseHistory(initialValue)
  const items = ref<HistoryEntry[]>(initialItems)
  const storageError = ref(Boolean(initialValue && initialValue !== '[]' && !initialItems.length))

  function persist(next: HistoryEntry[]): void {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(next))
      storageError.value = false
    } catch {
      storageError.value = true
      ElMessage.warning('浏览历史保存失败，可能是浏览器存储空间不足')
    }
  }

  function record(resource: ResourceListItem): void {
    const entry: HistoryEntry = {
      resource: snapshot(resource),
      lastViewedAt: new Date().toISOString()
    }
    const next = [entry, ...items.value.filter(item => item.resource.id !== resource.id)]
      .slice(0, MAX_HISTORY)
    items.value = next
    persist(next)
  }

  function remove(resourceId: number): void {
    const next = items.value.filter(item => item.resource.id !== resourceId)
    items.value = next
    persist(next)
  }

  function clear(): void {
    items.value = []
    persist([])
  }

  if (typeof window !== 'undefined') {
    window.addEventListener('storage', event => {
      if (event.key === STORAGE_KEY) {
        items.value = parseHistory(event.newValue)
      }
    })
  }

  return {
    items,
    storageError,
    record,
    remove,
    clear
  }
})
