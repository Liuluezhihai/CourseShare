<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { favoriteApi } from '../api'
import type { ResourceListItem } from '../api/types'
import ResourceCard from '../components/ResourceCard.vue'
import { useAuthStore } from '../stores/auth'
import { useHistoryStore } from '../stores/history'

const auth = useAuthStore()
const history = useHistoryStore()
const route = useRoute()
const router = useRouter()
const activeTab = ref(route.query.tab === 'history' ? 'history' : 'favorites')
const favorites = ref<ResourceListItem[]>([])
const total = ref(0)
const pages = ref(0)
const page = ref(1)
const size = 9
const loading = ref(false)

const historyResources = computed(() => history.items.map(item => item.resource))

async function loadFavorites(nextPage = page.value) {
  if (!auth.isAuthenticated) {
    favorites.value = []
    total.value = 0
    pages.value = 0
    return
  }
  page.value = nextPage
  loading.value = true
  try {
    const result = await favoriteApi.page({ page: page.value, size })
    favorites.value = result.records
    total.value = result.total
    pages.value = result.pages
  } catch {
    favorites.value = []
  } finally {
    loading.value = false
  }
}

async function changeTab(name: string | number) {
  const tab = String(name)
  await router.replace({ name: 'my-learning', query: { tab } })
}

async function favoriteChanged(resourceId: number, favorited: boolean) {
  if (activeTab.value === 'favorites' && !favorited) {
    favorites.value = favorites.value.filter(item => item.id !== resourceId)
    total.value = Math.max(0, total.value - 1)
  }
}

async function clearHistory() {
  await ElMessageBox.confirm('清空后无法恢复本机浏览历史，确定继续吗？', '清空浏览历史', {
    type: 'warning',
    confirmButtonText: '清空',
    cancelButtonText: '取消'
  })
  history.clear()
  ElMessage.success('浏览历史已清空')
}

async function login() {
  await router.push({ name: 'login', query: { redirect: '/my-learning?tab=favorites' } })
}

watch(activeTab, tab => {
  if (tab === 'favorites') loadFavorites(1)
})

watch(() => route.query.tab, value => {
  activeTab.value = value === 'history' ? 'history' : 'favorites'
})

watch(() => auth.isAuthenticated, () => loadFavorites(1))
loadFavorites(1)
</script>

<template>
  <section class="page-section">
    <div class="page-container">
      <div class="learning-heading">
        <div>
          <span class="eyebrow">MY LEARNING</span>
          <h1>我的学习</h1>
          <p>收藏会跟随账号同步，浏览记录仅保存在当前浏览器。</p>
        </div>
      </div>

      <div class="learning-tabs">
        <button :class="{ active: activeTab === 'favorites' }" @click="changeTab('favorites')">
          我的收藏 <span>{{ total }}</span>
        </button>
        <button :class="{ active: activeTab === 'history' }" @click="changeTab('history')">
          浏览记录 <span>{{ historyResources.length }}</span>
        </button>
      </div>

      <template v-if="activeTab === 'favorites'">
        <div v-if="!auth.isAuthenticated" class="empty-block login-block">
          <el-empty description="登录后可以查看并跨设备同步收藏">
            <el-button type="primary" @click="login">登录查看收藏</el-button>
          </el-empty>
        </div>
        <template v-else>
          <div v-loading="loading" class="resource-grid">
            <ResourceCard
              v-for="resource in favorites"
              :key="resource.id"
              :resource="resource"
              @favorite-change="favoriteChanged"
            />
          </div>
          <div v-if="!loading && !favorites.length" class="empty-block">
            <el-empty description="还没有收藏资源，去资源库看看吧">
              <el-button type="primary" @click="router.push('/resources')">浏览资源库</el-button>
            </el-empty>
          </div>
          <el-pagination
            v-if="pages > 1"
            background
            layout="prev, pager, next"
            :current-page="page"
            :page-size="size"
            :total="total"
            @current-change="loadFavorites"
          />
        </template>
      </template>

      <template v-else>
        <el-alert
          v-if="history.storageError"
          class="history-alert"
          type="warning"
          :closable="false"
          title="浏览记录数据无法读取或写入，当前内容可能不完整。"
        />
        <div class="history-toolbar">
          <p>同一浏览器下的账号共用浏览记录，最多保留最近 100 项。</p>
          <el-button v-if="historyResources.length" plain @click="clearHistory">清空历史</el-button>
        </div>
        <div class="resource-grid history-grid">
          <div v-for="entry in history.items" :key="entry.resource.id" class="history-card">
            <ResourceCard
              :resource="entry.resource"
              @favorite-change="favoriteChanged"
            />
            <el-button
              class="history-remove"
              text
              type="danger"
              @click="history.remove(entry.resource.id)"
            >
              移除记录
            </el-button>
          </div>
        </div>
        <div v-if="!historyResources.length" class="empty-block">
          <el-empty description="还没有浏览记录，打开一份资源后会自动记录" />
        </div>
      </template>
    </div>
  </section>
</template>

<style scoped>
.learning-heading {
  margin-bottom: 28px;
}

.eyebrow {
  color: var(--cs-primary);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.14em;
}

.learning-heading h1 {
  margin: 10px 0 8px;
  font-size: clamp(34px, 5vw, 52px);
  letter-spacing: -0.05em;
}

.learning-heading p,
.history-toolbar p {
  margin: 0;
  color: var(--cs-muted);
}

.learning-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  padding: 6px;
  border: 1px solid var(--cs-border);
  border-radius: 12px;
  background: white;
}

.learning-tabs button {
  flex: 1;
  padding: 11px 18px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: var(--cs-muted);
  cursor: pointer;
}

.learning-tabs button.active {
  background: #eff6ff;
  color: var(--cs-primary);
  font-weight: 700;
}

.learning-tabs span {
  margin-left: 5px;
  font-size: 12px;
}

.history-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 20px;
}

.history-alert {
  margin-bottom: 16px;
}

.resource-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
  min-height: 220px;
}

.history-card {
  display: grid;
}

.history-remove {
  justify-self: end;
  margin-top: 6px;
}

.login-block {
  padding: 54px 24px;
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

  .history-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
