<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { resourceApi } from '../api'
import type { ResourceDetail, ResourceListItem } from '../api/types'
import MarkdownContent from '../components/MarkdownContent.vue'
import ResourceCard from '../components/ResourceCard.vue'
import FavoriteButton from '../components/FavoriteButton.vue'
import ReviewSection from '../components/ReviewSection.vue'
import { useAuthStore } from '../stores/auth'
import { useHistoryStore } from '../stores/history'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const history = useHistoryStore()
const resource = ref<ResourceDetail>()
const related = ref<ResourceListItem[]>([])
const loading = ref(true)
const notFound = ref(false)

const fileSize = computed(() => {
  const size = resource.value?.attachmentSize
  if (!size) return ''
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
})

async function load() {
  loading.value = true
  try {
    resource.value = await resourceApi.detail(Number(route.params.id))
    history.record(resource.value)
    const data = await resourceApi.page({
      categoryId: resource.value.categoryId,
      page: 1,
      size: 4
    })
    related.value = data.records.filter(item => item.id !== resource.value?.id).slice(0, 3)
  } catch {
    notFound.value = true
  } finally {
    loading.value = false
  }
}

async function download() {
  if (!auth.isAuthenticated) {
    ElMessage.warning('登录后即可下载资源')
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  if (!resource.value?.attachmentName) {
    ElMessage.info('该资源没有附件')
    return
  }
  await resourceApi.download(resource.value.id, resource.value.attachmentName)
  resource.value.downloadCount += 1
}

onMounted(load)
</script>

<template>
  <section v-loading="loading" class="detail-shell">
    <div v-if="notFound" class="page-container page-section">
      <div class="empty-block">
        <el-empty description="资源不存在、未发布或已下架">
          <el-button type="primary" @click="router.push('/resources')">返回资源库</el-button>
        </el-empty>
      </div>
    </div>

    <template v-else-if="resource">
      <header class="detail-hero">
        <div class="page-container">
          <div class="breadcrumb">
            <router-link to="/resources">资源库</router-link>
            <span>/</span>
            <router-link :to="{ name: 'resources', query: { categoryId: resource.categoryId } }">
              {{ resource.categoryName }}
            </router-link>
          </div>
          <div class="detail-grid">
            <div>
              <span class="resource-type">{{ resource.attachmentExt?.toUpperCase() || 'MARKDOWN' }}</span>
              <h1>{{ resource.title }}</h1>
              <p>{{ resource.summary }}</p>
              <div class="detail-meta">
                <span>作者 {{ resource.authorName }}</span>
                <span>{{ resource.viewCount }} 浏览</span>
                <span>{{ resource.downloadCount }} 下载</span>
                <span v-if="resource.publishedAt">
                  {{ new Date(resource.publishedAt).toLocaleDateString('zh-CN') }} 发布
                </span>
              </div>
            </div>
            <aside class="download-card surface-card">
              <span class="download-label">资源附件</span>
              <strong>{{ resource.attachmentName || '无附件' }}</strong>
              <small>{{ fileSize || '正文资源' }}</small>
              <el-button type="primary" size="large" :disabled="!resource.attachmentName" @click="download">
                {{ auth.isAuthenticated ? '下载附件' : '登录后下载' }}
              </el-button>
              <FavoriteButton :resource-id="resource.id" label />
              <p>请尊重作者版权，仅用于课程学习。</p>
            </aside>
          </div>
        </div>
      </header>

      <div class="page-container detail-body">
        <MarkdownContent :content="resource.content" />
        <ReviewSection
          class="detail-reviews"
          :resource-id="resource.id"
          :author-id="resource.authorId"
        />
      </div>

      <section v-if="related.length" class="page-section related-section">
        <div class="page-container">
          <div class="section-heading">
            <div><h2>同分类资源</h2><p>继续浏览 {{ resource.categoryName }} 下的内容。</p></div>
          </div>
          <div class="related-grid">
            <ResourceCard v-for="item in related" :key="item.id" :resource="item" />
          </div>
        </div>
      </section>
    </template>
  </section>
</template>

<style scoped>
.detail-shell {
  min-height: 60vh;
}

.detail-hero {
  padding: 36px 0 48px;
  background:
    radial-gradient(circle at 88% 8%, rgba(37, 99, 235, 0.13), transparent 26%),
    linear-gradient(135deg, #f8fbff, #eef4ff 64%, #f4fbf9);
  border-bottom: 1px solid var(--cs-border);
}

.breadcrumb {
  display: flex;
  gap: 10px;
  color: var(--cs-muted);
  font-size: 13px;
}

.breadcrumb a:hover {
  color: var(--cs-primary);
}

.detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 54px;
  align-items: end;
  margin-top: 45px;
}

.resource-type {
  color: var(--cs-primary);
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
}

.detail-grid h1 {
  max-width: 850px;
  margin: 18px 0;
  font-size: clamp(36px, 4.5vw, 58px);
  line-height: 1.12;
  letter-spacing: -0.05em;
}

.detail-grid > div > p {
  max-width: 760px;
  margin: 0;
  color: var(--cs-muted);
  font-size: 17px;
  line-height: 1.8;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  margin-top: 26px;
  color: var(--cs-muted);
  font-size: 13px;
}

.download-card {
  display: flex;
  flex-direction: column;
  padding: 24px;
}

.download-label {
  color: var(--cs-primary);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.1em;
}

.download-card strong {
  margin: 16px 0 6px;
  overflow-wrap: anywhere;
}

.download-card small {
  color: var(--cs-muted);
}

.download-card .el-button {
  margin-top: 22px;
}

.download-card p {
  margin: 13px 0 0;
  color: var(--cs-muted);
  font-size: 11px;
  text-align: center;
}

.detail-body {
  display: grid;
  gap: 28px;
  padding-top: 54px;
  padding-bottom: 64px;
}

.detail-reviews {
  max-width: 920px;
}

.related-section {
  background: white;
  border-top: 1px solid var(--cs-border);
}

.related-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

@media (max-width: 900px) {
  .detail-grid {
    grid-template-columns: 1fr;
    align-items: start;
    gap: 28px;
  }

  .download-card {
    max-width: 420px;
  }
}

@media (max-width: 700px) {
  .related-grid {
    grid-template-columns: 1fr;
  }
}
</style>

