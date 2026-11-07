<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { discoverApi } from '../api'
import type { DiscoverResource, DiscoverSort } from '../api/types'
import ResourceCard from '../components/ResourceCard.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const records = ref<DiscoverResource[]>([])
const total = ref(0)
const pages = ref(0)
const page = ref(1)
const size = 12

const options: Array<{ key: DiscoverSort; label: string; description: string }> = [
  { key: 'popular', label: '综合热门', description: '综合浏览、下载、收藏、评分和评价点赞' },
  { key: 'latest', label: '最新发布', description: '最近通过审核并公开的学习资源' },
  { key: 'rating', label: '高分资源', description: '按公开评价的平均星级排序' },
  { key: 'likes', label: '评价获赞', description: '按资源所获评价点赞总数排序' }
]

function normalizedSort(value: unknown): DiscoverSort {
  return ['latest', 'popular', 'rating', 'likes'].includes(String(value))
    ? String(value) as DiscoverSort
    : 'popular'
}

const sort = computed(() => normalizedSort(route.query.sort))
const selected = computed(() => options.find(item => item.key === sort.value) || options[0])

async function load() {
  loading.value = true
  page.value = Math.max(1, Number(route.query.page) || 1)
  try {
    const result = await discoverApi.page({ sort: sort.value, page: page.value, size })
    records.value = result.records
    total.value = result.total
    pages.value = result.pages
  } catch {
    records.value = []
    total.value = 0
    pages.value = 0
  } finally {
    loading.value = false
  }
}

async function changeSort(next: DiscoverSort) {
  if (next === sort.value) return
  await router.replace({ name: 'discover', query: { sort: next, page: 1 } })
}

async function changePage(next: number) {
  await router.replace({ name: 'discover', query: { sort: sort.value, page: next } })
}

watch(() => [route.query.sort, route.query.page], load, { immediate: true })
</script>

<template>
  <section class="discover-hero">
    <div class="page-container">
      <span class="eyebrow">DISCOVER COURSESHARE</span>
      <h1>发现值得学习的资源</h1>
      <p>从最新发布、全站热度、学习评价和同伴认可中发现下一份课程资料。</p>
    </div>
  </section>

  <section class="page-section">
    <div class="page-container">
      <div class="sort-tabs">
        <button
          v-for="option in options"
          :key="option.key"
          :class="{ active: option.key === sort }"
          @click="changeSort(option.key)"
        >
          {{ option.label }}
        </button>
      </div>
      <div class="section-heading discover-heading">
        <div>
          <h2>{{ selected.label }}</h2>
          <p>{{ selected.description }}</p>
        </div>
        <span class="result-count">{{ total }} 份资源</span>
      </div>

      <div v-loading="loading" class="resource-grid">
        <ResourceCard
          v-for="resource in records"
          :key="resource.id"
          :resource="resource"
          :metric="sort"
        />
      </div>
      <div v-if="!loading && !records.length" class="empty-block">
        <el-empty :description="sort === 'rating' ? '暂时还没有公开评分' : sort === 'likes' ? '暂时还没有评价获得点赞' : '暂时没有可展示的资源'" />
      </div>
      <el-pagination
        v-if="pages > 1"
        background
        layout="prev, pager, next"
        :current-page="page"
        :page-size="size"
        :total="total"
        @current-change="changePage"
      />
    </div>
  </section>
</template>

<style scoped>
.discover-hero {
  padding: 68px 0 58px;
  border-bottom: 1px solid var(--cs-border);
  background:
    radial-gradient(circle at 82% 20%, rgba(37, 99, 235, 0.16), transparent 28%),
    linear-gradient(135deg, #f8fbff, #eef4ff 60%, #f4fbf9);
}

.eyebrow {
  color: var(--cs-primary);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.14em;
}

.discover-hero h1 {
  margin: 14px 0;
  font-size: clamp(36px, 5vw, 58px);
  letter-spacing: -0.05em;
}

.discover-hero p {
  max-width: 680px;
  margin: 0;
  color: var(--cs-muted);
  font-size: 17px;
  line-height: 1.8;
}

.sort-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 28px;
  padding: 6px;
  border: 1px solid var(--cs-border);
  border-radius: 14px;
  background: white;
}

.sort-tabs button {
  flex: 1;
  min-width: 128px;
  padding: 11px 16px;
  border: 0;
  border-radius: 9px;
  background: transparent;
  color: var(--cs-muted);
  cursor: pointer;
}

.sort-tabs button.active {
  background: var(--cs-primary);
  color: white;
  font-weight: 700;
}

.discover-heading {
  align-items: center;
}

.result-count {
  color: var(--cs-muted);
  font-size: 13px;
}

.resource-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
  min-height: 240px;
}

@media (max-width: 960px) {
  .resource-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .resource-grid {
    grid-template-columns: 1fr;
  }

  .sort-tabs button {
    min-width: 45%;
  }
}
</style>
