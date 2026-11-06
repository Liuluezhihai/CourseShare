<script setup lang="ts">
import { computed } from 'vue'
import type { ResourceListItem } from '../api/types'
import FavoriteButton from './FavoriteButton.vue'

const props = defineProps<{
  resource: ResourceListItem
  metric?: 'latest' | 'popular' | 'rating' | 'likes'
}>()
const emit = defineEmits<{
  favoriteChange: [resourceId: number, favorited: boolean]
}>()

const fileLabel = computed(() => props.resource.attachmentExt?.toUpperCase() || 'MD')
const dateText = computed(() => {
  const value = props.resource.publishedAt || props.resource.updatedAt
  return value ? new Date(value).toLocaleDateString('zh-CN') : '尚未发布'
})
const metricText = computed(() => {
  if (props.metric === 'popular' && props.resource.popularScore !== undefined) {
    return `综合热度 ${props.resource.popularScore}`
  }
  if (props.metric === 'rating' && props.resource.ratingCount) {
    return `${props.resource.averageRating?.toFixed(1)} 分 · ${props.resource.ratingCount} 人评价`
  }
  if (props.metric === 'likes' && props.resource.reviewLikeCount !== undefined) {
    return `${props.resource.reviewLikeCount} 个评价赞`
  }
  if (props.resource.ratingCount) {
    return `${props.resource.averageRating?.toFixed(1)} 分 · ${props.resource.ratingCount} 人评价`
  }
  return `${props.resource.viewCount} 浏览 · ${props.resource.downloadCount} 下载`
})
</script>

<template>
  <article class="resource-card surface-card pressable">
    <router-link :to="`/resources/${resource.id}`" class="resource-card-link">
      <div class="resource-meta">
        <div class="meta-start">
          <span class="file-type">{{ fileLabel }}</span>
          <span class="category-label">{{ resource.categoryName }}</span>
        </div>
        <span>{{ dateText }}</span>
      </div>
      <h3>{{ resource.title }}</h3>
      <p>{{ resource.summary }}</p>
      <div class="resource-footer">
        <span>{{ resource.authorName }}</span>
        <span class="footer-metrics">
          <span>{{ metricText }}</span>
        </span>
      </div>
    </router-link>
    <FavoriteButton
      class="card-favorite"
      :resource-id="resource.id"
      compact
      @change="(id, favorited) => emit('favoriteChange', id, favorited)"
    />
  </article>
</template>

<style scoped>
.resource-card {
  position: relative;
  display: flex;
  min-height: 220px;
}

.resource-card-link {
  width: 100%;
  min-height: 220px;
  display: flex;
  flex-direction: column;
  padding: 22px;
}

.card-favorite {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 2;
}

.resource-meta,
.resource-footer {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--cs-muted);
  font-size: 12px;
}

.resource-meta {
  min-width: 0;
  padding-right: 34px;
}

.meta-start {
  display: flex;
  align-items: center;
  gap: 7px;
  min-width: 0;
}

.file-type,
.category-label {
  flex: none;
  padding: 2px 7px;
  border-radius: 999px;
  font-size: 11px;
  line-height: 1.5;
  white-space: nowrap;
}

.file-type {
  background: #eff6ff;
  color: var(--cs-primary);
  font-family: "JetBrains Mono", Consolas, monospace;
  font-weight: 800;
}

.category-label {
  max-width: 150px;
  overflow: hidden;
  background: #f1f5f9;
  color: #475569;
  text-overflow: ellipsis;
}

.resource-meta > span {
  margin-left: auto;
  white-space: nowrap;
}

h3 {
  margin: 12px 0 9px;
  font-size: 19px;
  line-height: 1.42;
  letter-spacing: -0.02em;
}

p {
  margin: 0;
  color: var(--cs-muted);
  font-size: 14px;
  line-height: 1.7;
  display: -webkit-box;
  overflow: hidden;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.resource-footer {
  margin-top: auto;
  padding-top: 18px;
  border-top: 1px solid #f1f5f9;
}

.footer-metrics {
  display: flex;
  gap: 10px;
  margin-left: auto;
  white-space: nowrap;
}
</style>

