<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { resourceApi } from '../../api'
import type { ResourceListItem } from '../../api/types'
import StatusTag from '../../components/StatusTag.vue'

const router = useRouter()
const loading = ref(false)
const records = ref<ResourceListItem[]>([])
const total = ref(0)
const activeStatus = ref('all')
const query = reactive({ page: 1, size: 10, keyword: '' })

const statusTabs = [
  { value: 'all', label: '全部' },
  { value: '0', label: '草稿' },
  { value: '1', label: '待审核' },
  { value: '3', label: '已驳回' },
  { value: '2', label: '已发布' },
  { value: '4', label: '已下架' }
]

async function load() {
  loading.value = true
  try {
    const data = await resourceApi.authorPage({
      page: query.page,
      size: query.size,
      keyword: query.keyword || undefined,
      status: activeStatus.value === 'all' ? undefined : Number(activeStatus.value)
    })
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function search() {
  query.page = 1
  load()
}

async function action(row: ResourceListItem, type: 'submit' | 'withdraw' | 'offline' | 'delete') {
  const labels = {
    submit: '提交审核',
    withdraw: '撤回审核',
    offline: '下架资源',
    delete: '删除资源'
  }
  if (type === 'delete') {
    await ElMessageBox.confirm(`确定删除“${row.title}”吗？该操作将逻辑删除资源。`, '删除确认', {
      type: 'warning'
    })
  }
  if (type === 'submit') await resourceApi.submit(row.id, row.version)
  if (type === 'withdraw') await resourceApi.withdraw(row.id, row.version)
  if (type === 'offline') await resourceApi.offline(row.id, row.version)
  if (type === 'delete') await resourceApi.authorDelete(row.id)
  ElMessage.success(`${labels[type]}成功`)
  await load()
}

function formatDate(value?: string) {
  return value ? new Date(value).toLocaleString('zh-CN') : '-'
}

onMounted(load)
</script>

<template>
  <div class="author-page">
    <div class="page-toolbar">
      <div>
        <h2>我的学习资源</h2>
        <p>创建资源后提交审核，审核通过后进入公共资源库。</p>
      </div>
      <el-button type="primary" @click="router.push('/author/resources/new')">新建资源</el-button>
    </div>

    <div class="surface-card table-panel">
      <div class="filter-row">
        <el-tabs v-model="activeStatus" @tab-change="search">
          <el-tab-pane v-for="tab in statusTabs" :key="tab.value" :label="tab.label" :name="tab.value" />
        </el-tabs>
        <form class="search-box" @submit.prevent="search">
          <el-input v-model="query.keyword" clearable placeholder="搜索标题或摘要" />
          <el-button native-type="submit">搜索</el-button>
        </form>
      </div>

      <el-table v-loading="loading" :data="records" stripe>
        <el-table-column prop="title" label="资源标题" min-width="260">
          <template #default="{ row }">
            <button class="title-link" @click="row.status === 2 ? router.push(`/resources/${row.id}`) : null">
              {{ row.title }}
            </button>
            <div class="subline">{{ row.categoryName }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }"><StatusTag :status="row.status" /></template>
        </el-table-column>
        <el-table-column label="数据" width="150">
          <template #default="{ row }">{{ row.viewCount }} 浏览 / {{ row.downloadCount }} 下载</template>
        </el-table-column>
        <el-table-column label="更新时间" width="180">
          <template #default="{ row }">{{ formatDate(row.updatedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="[0, 3, 4].includes(row.status)"
              link
              type="primary"
              @click="router.push(`/author/resources/${row.id}/edit`)"
            >
              编辑
            </el-button>
            <el-button v-if="[0, 3, 4].includes(row.status)" link @click="action(row, 'submit')">提交</el-button>
            <el-button v-if="row.status === 1" link @click="action(row, 'withdraw')">撤回</el-button>
            <el-button v-if="row.status === 2" link @click="action(row, 'offline')">下架</el-button>
            <el-button v-if="[0, 3, 4].includes(row.status)" link type="danger" @click="action(row, 'delete')">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && !records.length" description="暂无资源，先创建第一份学习资料" />
      <el-pagination
        v-if="total > query.size"
        v-model:current-page="query.page"
        :page-size="query.size"
        :total="total"
        background
        layout="prev, pager, next"
        @current-change="load"
      />
    </div>
  </div>
</template>

<style scoped>
.page-toolbar {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 22px;
}

.page-toolbar h2 {
  margin: 0 0 6px;
  font-size: 26px;
}

.page-toolbar p {
  margin: 0;
  color: var(--cs-muted);
}

.table-panel {
  overflow: hidden;
  padding: 4px 20px 20px;
}

.filter-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.filter-row :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.search-box {
  width: 330px;
  display: flex;
  gap: 8px;
}

.title-link {
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--cs-text);
  font-weight: 600;
  text-align: left;
  cursor: pointer;
}

.title-link:hover {
  color: var(--cs-primary);
}

.subline {
  margin-top: 4px;
  color: var(--cs-muted);
  font-size: 12px;
}

@media (max-width: 760px) {
  .filter-row {
    align-items: stretch;
    flex-direction: column;
  }

  .search-box {
    width: 100%;
  }
}
</style>

