<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { categoryApi, resourceApi } from '../api'
import type { CategoryNode, ResourceListItem } from '../api/types'
import CategoryTree from '../components/CategoryTree.vue'
import ResourceCard from '../components/ResourceCard.vue'

const route = useRoute()
const router = useRouter()
const categories = ref<CategoryNode[]>([])
const resources = ref<ResourceListItem[]>([])
const total = ref(0)
const loading = ref(false)
const keyword = ref('')
const categoryId = ref<number>()
const page = ref(1)
const size = 12
const sort = ref('publishedAt,desc')

async function load() {
  loading.value = true
  try {
    const [sortBy, sortDirection] = sort.value.split(',')
    const data = await resourceApi.page({
      keyword: typeof route.query.keyword === 'string' ? route.query.keyword : undefined,
      categoryId: categoryId.value,
      page: page.value,
      size,
      sortBy,
      sortDirection
    })
    resources.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function updateRoute() {
  const query: Record<string, string> = {}
  if (keyword.value.trim()) query.keyword = keyword.value.trim()
  if (categoryId.value) query.categoryId = String(categoryId.value)
  if (page.value > 1) query.page = String(page.value)
  if (sort.value !== 'publishedAt,desc') query.sort = sort.value
  router.push({ name: 'resources', query })
}

function search() {
  page.value = 1
  updateRoute()
}

function selectCategory(id?: number) {
  categoryId.value = id
  page.value = 1
  updateRoute()
}

function changeSort() {
  page.value = 1
  updateRoute()
}

function changePage(value: number) {
  page.value = value
  updateRoute()
}

watch(
  () => route.query,
  async query => {
    keyword.value = typeof query.keyword === 'string' ? query.keyword : ''
    categoryId.value = query.categoryId ? Number(query.categoryId) : undefined
    page.value = query.page ? Number(query.page) : 1
    sort.value = typeof query.sort === 'string' ? query.sort : 'publishedAt,desc'
    await load()
  },
  { immediate: true }
)

onMounted(async () => {
  categories.value = await categoryApi.tree()
})
</script>

<template>
  <section class="page-section">
    <div class="page-container">
      <div class="catalog-heading">
        <div>
          <span class="eyebrow">RESOURCE CATALOG</span>
          <h1>资源库</h1>
          <p>共找到 {{ total }} 份符合条件的学习资源</p>
        </div>
        <form class="catalog-search" @submit.prevent="search">
          <el-input v-model="keyword" clearable placeholder="搜索标题或摘要" />
          <el-button type="primary" native-type="submit">搜索</el-button>
        </form>
      </div>

      <div class="catalog-layout">
        <aside>
          <CategoryTree :nodes="categories" :current-id="categoryId" @select="selectCategory" />
        </aside>

        <div class="catalog-content">
          <div class="toolbar surface-card">
            <span class="text-muted">按条件浏览</span>
            <el-radio-group v-model="sort" @change="changeSort">
              <el-radio-button value="publishedAt,desc">最新发布</el-radio-button>
              <el-radio-button value="viewCount,desc">浏览最多</el-radio-button>
              <el-radio-button value="downloadCount,desc">下载最多</el-radio-button>
            </el-radio-group>
          </div>

          <div v-loading="loading" class="resource-grid">
            <ResourceCard v-for="resource in resources" :key="resource.id" :resource="resource" />
          </div>
          <div v-if="!loading && !resources.length" class="empty-block">
            <el-empty description="没有找到符合条件的资源">
              <el-button type="primary" @click="selectCategory(undefined)">查看全部</el-button>
            </el-empty>
          </div>
          <el-pagination
            v-if="total > size"
            :current-page="page"
            :page-size="size"
            :total="total"
            background
            layout="prev, pager, next"
            @current-change="changePage"
          />
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.catalog-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 32px;
  margin-bottom: 30px;
}

.eyebrow {
  color: var(--cs-primary);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.12em;
}

.catalog-heading h1 {
  margin: 10px 0 8px;
  font-size: 42px;
  letter-spacing: -0.045em;
}

.catalog-heading p {
  margin: 0;
  color: var(--cs-muted);
}

.catalog-search {
  width: min(430px, 100%);
  display: flex;
  gap: 8px;
}

.catalog-layout {
  display: grid;
  grid-template-columns: 250px minmax(0, 1fr);
  gap: 24px;
  align-items: start;
}

.catalog-layout aside {
  position: sticky;
  top: 92px;
}

.catalog-content {
  min-width: 0;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 20px;
  padding: 14px 16px;
}

.resource-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  min-height: 180px;
}

@media (max-width: 900px) {
  .catalog-layout {
    grid-template-columns: 1fr;
  }

  .catalog-layout aside {
    position: static;
  }
}

@media (max-width: 700px) {
  .catalog-heading,
  .toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .resource-grid {
    grid-template-columns: 1fr;
  }
}
</style>

