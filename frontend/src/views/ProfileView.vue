<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { reviewApi } from '../api'
import type { MyReviewItem } from '../api/types'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const enablingAuthor = ref(false)
const activeTab = ref('profile')
const reviews = ref<MyReviewItem[]>([])
const reviewPage = ref(1)
const reviewSize = 6
const reviewTotal = ref(0)
const reviewPages = ref(0)
const reviewsLoading = ref(false)

async function loadReviews(page = reviewPage.value) {
  reviewPage.value = page
  reviewsLoading.value = true
  try {
    const result = await reviewApi.mine({ page, size: reviewSize })
    reviews.value = result.records
    reviewTotal.value = result.total
    reviewPages.value = result.pages
  } catch {
    reviews.value = []
    reviewTotal.value = 0
    reviewPages.value = 0
  } finally {
    reviewsLoading.value = false
  }
}

async function changeTab(tab: string) {
  activeTab.value = tab
  if (tab === 'reviews') {
    await loadReviews(1)
  }
}

async function removeReview(review: MyReviewItem) {
  await ElMessageBox.confirm('删除后评价和收到的点赞会一并清除，确定继续吗？', '删除评价', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  })
  await reviewApi.remove(review.resourceId)
  ElMessage.success('评价已删除')
  await loadReviews(1)
}

function reviewDate(value: string): string {
  return new Date(value).toLocaleDateString('zh-CN')
}

async function logout() {
  await auth.logout()
  ElMessage.success('已退出登录')
  router.push('/')
}

async function enableAuthor() {
  enablingAuthor.value = true
  try {
    await auth.enableAuthor()
    ElMessage.success('创作功能已开启')
  } finally {
    enablingAuthor.value = false
  }
}

loadReviews(1)
</script>

<template>
  <section class="page-section">
    <div class="page-container profile-layout">
      <aside class="profile-card surface-card">
        <div class="avatar">{{ auth.user?.nickname?.slice(0, 1) || 'C' }}</div>
        <h1>{{ auth.user?.nickname }}</h1>
        <p>@{{ auth.user?.username }}</p>
        <dl>
          <div><dt>邮箱</dt><dd>{{ auth.user?.email || '未填写' }}</dd></div>
        </dl>
        <el-button plain @click="logout">退出登录</el-button>
      </aside>

      <div class="profile-content">
        <div class="surface-card content-panel">
          <div class="profile-tabs">
            <button :class="{ active: activeTab === 'profile' }" @click="changeTab('profile')">个人资料</button>
            <button :class="{ active: activeTab === 'reviews' }" @click="changeTab('reviews')">
              我的评价 <span>{{ reviewTotal || '' }}</span>
            </button>
          </div>
        </div>

        <template v-if="activeTab === 'profile'">
          <div class="surface-card content-panel">
            <h2>我的工作入口</h2>
            <div class="entry-grid">
              <router-link to="/resources">
                <strong>资源库</strong><span>浏览和下载学习资源</span>
              </router-link>
              <router-link :to="{ name: 'my-learning', query: { tab: 'favorites' } }">
                <strong>我的收藏</strong><span>查看跟随账号同步的收藏资源</span>
              </router-link>
              <router-link :to="{ name: 'my-learning', query: { tab: 'history' } }">
                <strong>浏览记录</strong><span>查看当前浏览器最近浏览的资源</span>
              </router-link>
              <router-link v-if="auth.canAuthor" to="/author/resources">
                <strong>创作中心</strong><span>创作和整理你的学习资源</span>
              </router-link>
              <router-link v-if="auth.canAudit" to="/admin/resources">
                <strong>审核中心</strong><span>处理待审核资源</span>
              </router-link>
              <router-link v-if="auth.isAdmin" to="/admin/users">
                <strong>控制台</strong><span>管理资源、用户和站点内容</span>
              </router-link>
            </div>
          </div>

          <div v-if="!auth.canAuthor" class="surface-card content-panel share-panel">
            <div>
              <h2>分享你的学习资料</h2>
              <p>开启创作功能后，你可以创建、整理并分享自己的课程笔记和学习资源。</p>
            </div>
            <el-button type="primary" :loading="enablingAuthor" @click="enableAuthor">
              开通创作功能
            </el-button>
          </div>
        </template>

        <div v-else v-loading="reviewsLoading" class="surface-card content-panel reviews-panel">
          <div class="reviews-heading">
            <div>
              <h2>我的评价</h2>
            </div>
            <span>{{ reviewTotal }} 条</span>
          </div>
          <div v-if="reviews.length" class="my-review-list">
            <article v-for="review in reviews" :key="review.id" class="my-review-item">
              <div class="review-main">
                <div class="review-title-row">
                  <router-link
                    v-if="!review.resourceDeleted"
                    :to="`/resources/${review.resourceId}`"
                  >
                    {{ review.resourceTitle }}
                  </router-link>
                  <strong v-else>{{ review.resourceTitle }}</strong>
                  <el-tag v-if="review.status === 0" type="warning" size="small">已隐藏</el-tag>
                  <el-tag v-if="review.resourceDeleted || review.resourceStatus !== 2" type="info" size="small">
                    资源不可用
                  </el-tag>
                </div>
                <div class="review-meta">
                  <el-rate :model-value="review.score" disabled />
                  <span>{{ review.likeCount }} 个赞</span>
                  <span>{{ reviewDate(review.updatedAt) }} 更新</span>
                </div>
                <p v-if="review.content">{{ review.content }}</p>
                <p v-else class="text-muted">仅评分，没有文字短评。</p>
              </div>
              <div class="review-buttons">
                <router-link
                  v-if="!review.resourceDeleted && review.resourceStatus === 2"
                  :to="`/resources/${review.resourceId}`"
                >
                  <el-button plain>查看资源</el-button>
                </router-link>
                <el-button text type="danger" @click="removeReview(review)">删除</el-button>
              </div>
            </article>
          </div>
          <el-empty v-else description="还没有发布过评价" />
          <el-pagination
            v-if="reviewPages > 1"
            background
            layout="prev, pager, next"
            :current-page="reviewPage"
            :page-size="reviewSize"
            :total="reviewTotal"
            @current-change="loadReviews"
          />
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.profile-layout {
  display: grid;
  grid-template-columns: 310px minmax(0, 1fr);
  gap: 24px;
  align-items: start;
}

.profile-card {
  position: sticky;
  top: 96px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 30px;
}

.avatar {
  width: 72px;
  height: 72px;
  display: grid;
  place-items: center;
  border-radius: 22px;
  background: linear-gradient(135deg, #2563eb, #0f766e);
  color: white;
  font-size: 28px;
  font-weight: 700;
}

.profile-card h1 {
  margin: 20px 0 4px;
  font-size: 25px;
}

.profile-card > p {
  margin: 0 0 15px;
  color: var(--cs-muted);
}

.profile-card .el-tag {
  margin: 0 6px 6px 0;
}

dl {
  width: 100%;
  margin: 24px 0;
  padding-top: 16px;
  border-top: 1px solid var(--cs-border);
}

dl div {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 8px 0;
  font-size: 13px;
}

dt {
  color: var(--cs-muted);
}

dd {
  margin: 0;
}

.profile-content {
  display: grid;
  gap: 20px;
}

.content-panel {
  padding: 28px;
}

.content-panel h2 {
  margin: 0 0 6px;
}

.content-panel > p {
  margin: 0;
}

.profile-tabs {
  display: flex;
  gap: 8px;
}

.profile-tabs button {
  padding: 9px 16px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: var(--cs-muted);
  cursor: pointer;
}

.profile-tabs button.active {
  background: #eff6ff;
  color: var(--cs-primary);
  font-weight: 700;
}

.profile-tabs span {
  margin-left: 4px;
  font-size: 11px;
}

.entry-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
  gap: 12px;
  margin-top: 22px;
}

.entry-grid a {
  display: grid;
  gap: 7px;
  padding: 18px;
  border: 1px solid var(--cs-border);
  border-radius: 10px;
  background: #f8fafc;
}

.entry-grid a:hover {
  border-color: #bfdbfe;
  background: #eff6ff;
}

.entry-grid span {
  color: var(--cs-muted);
  font-size: 12px;
}

.share-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.share-panel p {
  margin: 8px 0 0;
  color: var(--cs-muted);
}

.reviews-heading,
.review-title-row,
.review-meta,
.review-buttons {
  display: flex;
  align-items: center;
  gap: 10px;
}

.reviews-heading {
  justify-content: space-between;
}

.reviews-heading p {
  margin: 7px 0 0;
  color: var(--cs-muted);
}

.reviews-heading > span {
  color: var(--cs-muted);
  font-size: 13px;
}

.my-review-list {
  display: grid;
  gap: 12px;
  margin-top: 22px;
}

.my-review-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 22px;
  padding: 18px;
  border: 1px solid var(--cs-border);
  border-radius: 10px;
  background: #f8fafc;
}

.review-title-row a {
  font-weight: 700;
}

.review-title-row a:hover {
  color: var(--cs-primary);
}

.review-meta {
  flex-wrap: wrap;
  margin-top: 8px;
  color: var(--cs-muted);
  font-size: 12px;
}

.my-review-item p {
  margin: 10px 0 0;
  white-space: pre-wrap;
  line-height: 1.7;
}

.review-buttons {
  flex: none;
}

@media (max-width: 900px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }

  .profile-card {
    position: static;
  }
}

@media (max-width: 620px) {
  .entry-grid {
    grid-template-columns: 1fr;
  }

  .share-panel {
    align-items: flex-start;
    flex-direction: column;
  }

  .my-review-item {
    flex-direction: column;
  }
}
</style>

