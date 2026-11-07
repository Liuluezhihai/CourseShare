<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { categoryApi, discoverApi, resourceApi } from '../api'
import type { CategoryNode, ResourceListItem } from '../api/types'
import ResourceCard from '../components/ResourceCard.vue'

const router = useRouter()
const keyword = ref('')
const categories = ref<CategoryNode[]>([])
const latest = ref<ResourceListItem[]>([])
const popular = ref<ResourceListItem[]>([])
const total = ref(0)
const loading = ref(true)

async function load() {
  loading.value = true
  try {
    const [categoryData, latestData, popularData] = await Promise.all([
      categoryApi.tree(),
      resourceApi.page({ page: 1, size: 6, sortBy: 'publishedAt' }),
      discoverApi.page({ sort: 'popular', page: 1, size: 3 })
    ])
    categories.value = categoryData
    latest.value = latestData.records
    popular.value = popularData.records
    total.value = latestData.total
  } finally {
    loading.value = false
  }
}

function search() {
  router.push({ name: 'resources', query: keyword.value ? { keyword: keyword.value } : {} })
}

onMounted(load)
</script>

<template>
  <section class="hero">
    <div class="page-container hero-inner">
      <div class="hero-copy">
        <span class="eyebrow">COURSESHARE · 学习资源库</span>
        <h1>把值得分享的知识，<br /><em>留在课程里。</em></h1>
        <p>浏览经过审核的课程资料、工程笔记和学习路线。资源由作者发布，审核后与所有学习者共享。</p>
        <form class="hero-search" @submit.prevent="search">
          <el-input v-model="keyword" size="large" clearable placeholder="搜索 Spring、MySQL、Vue 等资源" />
          <el-button native-type="submit" type="primary" size="large">搜索资源</el-button>
        </form>
        <div class="hero-stats">
          <span><strong>{{ total }}</strong> 份已发布资源</span>
          <span><strong>{{ categories.length }}</strong> 个一级分类</span>
          <span>审核后公开</span>
        </div>
      </div>
      <div class="hero-visual" aria-hidden="true">
        <div class="floating-card card-one">
          <span>SPRING MVC</span>
          <strong>请求处理流程</strong>
          <small>框架技术 · 已发布</small>
        </div>
        <div class="floating-card card-two">
          <span>MYSQL 8</span>
          <strong>索引与查询优化</strong>
          <small>数据库 · 18 次浏览</small>
        </div>
        <div class="visual-orbit orbit-one"></div>
        <div class="visual-orbit orbit-two"></div>
      </div>
    </div>
  </section>

  <section class="page-section">
    <div class="page-container">
      <div class="section-heading">
        <div>
          <h2>从分类开始探索</h2>
          <p>按学习方向快速进入资源集合。</p>
        </div>
        <router-link to="/resources" class="more-link">查看全部资源 →</router-link>
      </div>
      <div v-if="categories.length" class="category-grid">
        <router-link
          v-for="(category, index) in categories"
          :key="category.id"
          :to="{ name: 'resources', query: { categoryId: category.id } }"
          class="category-card surface-card pressable"
        >
          <span class="category-index">0{{ index + 1 }}</span>
          <div>
            <h3>{{ category.name }}</h3>
            <p>{{ category.description || '查看该分类下的全部学习资源' }}</p>
          </div>
          <span class="category-count">{{ category.children.length }} 个子分类</span>
        </router-link>
      </div>
      <el-skeleton v-else :rows="3" animated />
    </div>
  </section>

  <section class="latest-section">
    <div class="page-container">
      <div class="section-heading">
        <div>
          <h2>最近发布</h2>
          <p>最新通过审核的学习资料。</p>
        </div>
      </div>
      <div v-loading="loading" class="resource-grid">
        <ResourceCard v-for="resource in latest" :key="resource.id" :resource="resource" />
      </div>
      <div v-if="!loading && !latest.length" class="empty-block">
        <el-empty description="暂时还没有已发布资源" />
      </div>
    </div>
  </section>

  <section class="page-section">
    <div class="page-container">
      <div class="section-heading">
        <div>
          <h2>全站热门</h2>
          <p>综合浏览、下载、收藏、评分和评价点赞整理的学习资料。</p>
        </div>
        <router-link to="/discover" class="more-link">查看发现 →</router-link>
      </div>
      <div class="popular-list">
        <router-link
          v-for="(resource, index) in popular"
          :key="resource.id"
          :to="`/resources/${resource.id}`"
          class="popular-item surface-card"
        >
          <span class="popular-rank">{{ String(index + 1).padStart(2, '0') }}</span>
          <div>
            <h3>{{ resource.title }}</h3>
            <p>{{ resource.categoryName }} · {{ resource.authorName }}</p>
          </div>
          <span class="popular-views">{{ resource.viewCount }} 浏览</span>
        </router-link>
      </div>
    </div>
  </section>
</template>

<style scoped>
.hero {
  overflow: hidden;
  background:
    radial-gradient(circle at 86% 18%, rgba(37, 99, 235, 0.2), transparent 28%),
    radial-gradient(circle at 74% 82%, rgba(15, 118, 110, 0.12), transparent 26%),
    linear-gradient(135deg, #f8fbff 0%, #eef4ff 58%, #f4fbf9 100%);
  border-bottom: 1px solid #e2e8f0;
}

.hero-inner {
  min-height: 570px;
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(360px, 0.85fr);
  align-items: center;
  gap: 64px;
  padding-top: 60px;
  padding-bottom: 60px;
}

.eyebrow {
  color: var(--cs-primary);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.14em;
}

.hero h1 {
  margin: 20px 0 22px;
  font-size: clamp(42px, 5vw, 68px);
  line-height: 1.08;
  letter-spacing: -0.055em;
}

.hero h1 em {
  color: var(--cs-primary);
  font-style: normal;
}

.hero-copy > p {
  max-width: 620px;
  margin: 0;
  color: var(--cs-muted);
  font-size: 17px;
  line-height: 1.8;
}

.hero-search {
  max-width: 650px;
  display: flex;
  gap: 10px;
  margin-top: 34px;
  padding: 8px;
  border: 1px solid #dbe3ee;
  border-radius: 14px;
  background: white;
  box-shadow: 0 16px 45px rgba(37, 99, 235, 0.1);
}

.hero-search :deep(.el-input__wrapper) {
  box-shadow: none;
}

.hero-stats {
  display: flex;
  gap: 28px;
  margin-top: 24px;
  color: var(--cs-muted);
  font-size: 13px;
}

.hero-stats strong {
  color: var(--cs-text);
  font-size: 18px;
}

.hero-visual {
  position: relative;
  height: 390px;
}

.floating-card {
  position: absolute;
  width: 270px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.9);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 24px 70px rgba(37, 99, 235, 0.15);
  backdrop-filter: blur(12px);
}

.floating-card span {
  color: var(--cs-primary);
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 11px;
  font-weight: 800;
}

.floating-card strong,
.floating-card small {
  display: block;
}

.floating-card strong {
  margin: 22px 0 12px;
  font-size: 21px;
}

.floating-card small {
  color: var(--cs-muted);
}

.card-one {
  top: 38px;
  right: 58px;
  transform: rotate(3deg);
}

.card-two {
  right: 8px;
  bottom: 34px;
  transform: rotate(-4deg);
}

.visual-orbit {
  position: absolute;
  border: 1px solid rgba(37, 99, 235, 0.18);
  border-radius: 50%;
}

.orbit-one {
  inset: 12px 0 22px 20px;
}

.orbit-two {
  inset: 70px -20px -8px 70px;
}

.more-link {
  color: var(--cs-primary);
  font-size: 14px;
  font-weight: 600;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.category-card {
  min-height: 180px;
  display: flex;
  flex-direction: column;
  padding: 22px;
}

.category-index {
  color: #93c5fd;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 13px;
  font-weight: 800;
}

.category-card h3 {
  margin: 18px 0 7px;
  font-size: 20px;
}

.category-card p {
  margin: 0;
  color: var(--cs-muted);
  font-size: 13px;
  line-height: 1.65;
}

.category-count {
  margin-top: auto;
  color: var(--cs-muted);
  font-size: 12px;
}

.latest-section {
  padding: 54px 0;
  background: #fff;
  border-top: 1px solid var(--cs-border);
  border-bottom: 1px solid var(--cs-border);
}

.resource-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
  min-height: 180px;
}

.popular-list {
  display: grid;
  gap: 12px;
}

.popular-item {
  display: flex;
  align-items: center;
  gap: 22px;
  padding: 18px 22px;
}

.popular-rank {
  color: #93c5fd;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 20px;
  font-weight: 800;
}

.popular-item h3 {
  margin: 0 0 5px;
  font-size: 16px;
}

.popular-item p {
  margin: 0;
  color: var(--cs-muted);
  font-size: 12px;
}

.popular-views {
  margin-left: auto;
  color: var(--cs-muted);
  font-size: 13px;
}

@media (max-width: 1000px) {
  .hero-inner {
    grid-template-columns: 1fr;
  }

  .hero-visual {
    display: none;
  }

  .category-grid,
  .resource-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .hero-inner {
    min-height: auto;
  }

  .hero-search {
    flex-direction: column;
  }

  .hero-stats {
    flex-wrap: wrap;
    gap: 12px 20px;
  }

  .category-grid,
  .resource-grid {
    grid-template-columns: 1fr;
  }

  .popular-views {
    display: none;
  }
}
</style>

