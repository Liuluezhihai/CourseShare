<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { reviewApi } from '../api'
import type { ReviewFeed, ReviewItem } from '../api/types'
import { useAuthStore } from '../stores/auth'

const props = defineProps<{
  resourceId: number
  authorId: number
}>()

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const feed = ref<ReviewFeed>()
const loading = ref(false)
const saving = ref(false)
const score = ref(0)
const content = ref('')
const page = ref(1)
const size = 8

const isAuthor = computed(() => auth.user?.id === props.authorId)
const canModerate = computed(() => auth.can('review:moderate'))

async function load(nextPage = page.value) {
  page.value = nextPage
  loading.value = true
  try {
    const result = await reviewApi.list(props.resourceId, { page: page.value, size })
    feed.value = result
    if (result.mine) {
      score.value = result.mine.score
      content.value = result.mine.content || ''
    } else if (!isAuthor.value) {
      score.value = 0
      content.value = ''
    }
  } catch {
    feed.value = undefined
  } finally {
    loading.value = false
  }
}

async function submit() {
  if (!auth.isAuthenticated) {
    await login()
    return
  }
  if (!score.value) {
    ElMessage.warning('请先选择评分')
    return
  }
  saving.value = true
  try {
    await reviewApi.save(props.resourceId, {
      score: score.value,
      content: content.value.trim() || undefined
    })
    ElMessage.success(feed.value?.mine ? '评价已更新' : '评价已发布')
    await load(1)
  } finally {
    saving.value = false
  }
}

async function removeMine() {
  await ElMessageBox.confirm('删除后评价和收到的点赞会一并清除，确定继续吗？', '删除评价', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  })
  await reviewApi.remove(props.resourceId)
  ElMessage.success('评价已删除')
  score.value = 0
  content.value = ''
  await load(1)
}

async function toggleLike(review: ReviewItem) {
  if (!auth.isAuthenticated) {
    await login()
    return
  }
  if (review.likedByMe) {
    await reviewApi.unlike(review.id)
    review.likedByMe = false
    review.likeCount = Math.max(0, review.likeCount - 1)
  } else {
    await reviewApi.like(review.id)
    review.likedByMe = true
    review.likeCount += 1
  }
}

async function moderate(review: ReviewItem) {
  const visible = review.status === 0
  const action = visible ? '恢复' : '隐藏'
  await ElMessageBox.confirm(
    visible
      ? '恢复后该评价会重新进入公开列表和评分统计，确定继续吗？'
      : '隐藏后该评价将从公开列表和评分统计中移除，确定继续吗？',
    `${action}评价`,
    {
    type: 'warning',
    confirmButtonText: action,
    cancelButtonText: '取消'
    }
  )
  await reviewApi.visibility(review.id, visible)
  ElMessage.success(visible ? '评价已恢复' : '评价已隐藏')
  await load(page.value)
}

async function login() {
  ElMessage.warning('登录后即可评价和点赞')
  await router.push({ name: 'login', query: { redirect: route.fullPath } })
}

function dateText(value: string): string {
  return new Date(value).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

watch(() => props.resourceId, () => load(1))
onMounted(() => load(1))
</script>

<template>
  <section class="review-panel surface-card">
    <div class="review-heading">
      <div>
        <span class="eyebrow">LEARNER FEEDBACK</span>
        <h2>学习评价</h2>
        <p>五星评分与短评会帮助其他学习者判断资源质量。</p>
      </div>
      <div class="rating-summary">
        <strong>{{ feed?.averageScore?.toFixed(1) || '0.0' }}</strong>
        <div>
          <el-rate :model-value="feed?.averageScore || 0" disabled />
          <span>{{ feed?.ratingCount || 0 }} 人评价</span>
        </div>
      </div>
    </div>

    <div v-if="!auth.isAuthenticated" class="login-review">
      <span>登录后可以发布评价，并为有帮助的评价点赞。</span>
      <el-button type="primary" @click="login">登录参与评价</el-button>
    </div>
    <div v-else-if="isAuthor" class="login-review">
      <span>这是你发布的资源，作者不能评价自己的资源。</span>
    </div>
    <div v-else class="review-editor">
      <div class="editor-row">
        <span>我的评分</span>
        <el-rate v-model="score" />
        <el-tag v-if="feed?.mine?.status === 0" type="warning">修改后仍保持隐藏</el-tag>
      </div>
      <el-input
        v-model="content"
        type="textarea"
        :rows="3"
        maxlength="500"
        show-word-limit
        placeholder="写下这份资源对你的帮助，可选"
      />
      <div class="editor-actions">
        <el-button
          v-if="feed?.mine"
          plain
          type="danger"
          @click="removeMine"
        >
          删除评价
        </el-button>
        <el-button type="primary" :loading="saving" @click="submit">
          {{ feed?.mine ? '更新评价' : '发布评价' }}
        </el-button>
      </div>
    </div>

    <div v-loading="loading" class="review-list">
      <article v-for="review in feed?.reviews.records || []" :key="review.id" class="review-item">
        <div class="review-author">
          <span class="review-avatar">{{ review.nickname.slice(0, 1) }}</span>
          <div>
            <strong>{{ review.nickname }}</strong>
            <small>{{ dateText(review.createdAt) }}</small>
          </div>
          <el-tag v-if="review.mine" size="small">我的评价</el-tag>
          <el-tag v-if="review.status === 0" type="warning" size="small">已隐藏</el-tag>
        </div>
        <el-rate :model-value="review.score" disabled />
        <p v-if="review.content">{{ review.content }}</p>
        <p v-else class="empty-content">仅评分，没有文字短评。</p>
        <div class="review-actions">
          <el-button
            text
            :type="review.likedByMe ? 'primary' : 'default'"
            :disabled="review.status === 0"
            :aria-label="review.likedByMe ? '取消点赞' : '点赞'"
            @click="toggleLike(review)"
          >
            <span class="like-icon" aria-hidden="true">{{ review.likedByMe ? '♥' : '♡' }}</span>
            <span>{{ review.likeCount }}</span>
          </el-button>
          <el-button
            v-if="canModerate"
            text
            :type="review.status === 0 ? 'success' : 'danger'"
            @click="moderate(review)"
          >
            {{ review.status === 0 ? '恢复' : '隐藏' }}
          </el-button>
        </div>
      </article>
      <el-empty
        v-if="!loading && !feed?.reviews.records.length"
        description="还没有公开评价，来发布第一条吧"
      />
    </div>

    <el-pagination
      v-if="(feed?.reviews.pages || 0) > 1"
      background
      layout="prev, pager, next"
      :current-page="page"
      :page-size="size"
      :total="feed?.reviews.total || 0"
      @current-change="load"
    />
  </section>
</template>

<style scoped>
.review-panel {
  padding: 32px;
}

.review-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
}

.eyebrow {
  color: var(--cs-primary);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.12em;
}

.review-heading h2 {
  margin: 8px 0 7px;
}

.review-heading p {
  margin: 0;
  color: var(--cs-muted);
}

.rating-summary {
  display: flex;
  align-items: center;
  gap: 14px;
}

.rating-summary > strong {
  color: var(--cs-primary);
  font-size: 38px;
  line-height: 1;
}

.rating-summary span {
  color: var(--cs-muted);
  font-size: 12px;
}

.login-review,
.review-editor {
  display: grid;
  gap: 14px;
  margin-top: 26px;
  padding: 20px;
  border: 1px solid #dbeafe;
  border-radius: 12px;
  background: #f8fbff;
}

.login-review {
  grid-template-columns: 1fr auto;
  align-items: center;
  color: var(--cs-muted);
}

.editor-row,
.editor-actions,
.review-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.editor-row > span:first-child {
  font-size: 13px;
  font-weight: 700;
}

.editor-actions {
  justify-content: flex-end;
}

.review-list {
  display: grid;
  gap: 14px;
  min-height: 100px;
  margin-top: 28px;
}

.review-item {
  padding: 20px;
  border-top: 1px solid var(--cs-border);
}

.review-item:first-child {
  border-top: 0;
}

.review-author {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.review-avatar {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 10px;
  background: #e0ecff;
  color: var(--cs-primary);
  font-weight: 800;
}

.review-author div {
  display: grid;
}

.review-author small {
  color: var(--cs-muted);
  font-size: 11px;
}

.review-item p {
  margin: 12px 0 6px;
  white-space: pre-wrap;
  line-height: 1.75;
}

.review-item .empty-content {
  color: var(--cs-muted);
  font-size: 13px;
}

.review-actions {
  margin-top: 10px;
}

.like-icon {
  margin-right: 5px;
  color: #ef4444;
  font-size: 15px;
  line-height: 1;
}

@media (max-width: 700px) {
  .review-panel {
    padding: 22px;
  }

  .review-heading,
  .login-review {
    grid-template-columns: 1fr;
    flex-direction: column;
  }
}
</style>
