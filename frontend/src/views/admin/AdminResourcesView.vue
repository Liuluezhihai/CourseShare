<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { categoryApi, resourceApi } from '../../api'
import type { CategoryNode, ResourceDetail, ResourceListItem, ResourceQuery } from '../../api/types'
import MarkdownContent from '../../components/MarkdownContent.vue'
import StatusTag from '../../components/StatusTag.vue'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()
const loading = ref(false)
const records = ref<ResourceListItem[]>([])
const total = ref(0)
const selectedRows = ref<ResourceListItem[]>([])
const categories = ref<CategoryNode[]>([])
const detailVisible = ref(false)
const detail = ref<ResourceDetail>()
const attachmentLoading = ref(false)
const rejectVisible = ref(false)
const rejectRow = ref<ResourceListItem>()
const rejectRemark = ref('')

const query = reactive<ResourceQuery & { keyword: string }>({
  keyword: '',
  page: 1,
  size: 10,
  status: undefined,
  categoryId: undefined,
  sortBy: 'updatedAt',
  sortDirection: 'desc'
})

const pendingSelected = computed(() => selectedRows.value.filter(row => row.status === 1))

const categoryOptions = computed(() => {
  const options: Array<{ id: number; label: string }> = []
  const walk = (nodes: CategoryNode[]) => {
    nodes.forEach(node => {
      options.push({ id: node.id, label: `${'　'.repeat(node.level - 1)}${node.name}` })
      walk(node.children)
    })
  }
  walk(categories.value)
  return options
})

async function load() {
  loading.value = true
  try {
    const data = await resourceApi.adminPage(query)
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

async function approve(row: ResourceListItem) {
  await ElMessageBox.confirm(`确认审核通过“${row.title}”吗？`, '审核确认')
  await resourceApi.approve(row.id, row.version)
  ElMessage.success('资源已发布')
  await load()
}

function openReject(row: ResourceListItem) {
  rejectRow.value = row
  rejectRemark.value = ''
  rejectVisible.value = true
}

async function submitReject() {
  if (!rejectRow.value || !rejectRemark.value.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  await resourceApi.reject(rejectRow.value.id, rejectRow.value.version, rejectRemark.value.trim())
  rejectVisible.value = false
  ElMessage.success('资源已驳回')
  await load()
}

async function offline(row: ResourceListItem) {
  await ElMessageBox.confirm(`确认下架“${row.title}”吗？`, '下架确认', { type: 'warning' })
  await resourceApi.adminOffline(row.id, row.version)
  ElMessage.success('资源已下架')
  await load()
}

async function deleteResource(row: ResourceListItem) {
  await ElMessageBox.confirm(`确认删除“${row.title}”吗？`, '删除确认', { type: 'warning' })
  await resourceApi.adminDelete(row.id)
  ElMessage.success('资源已删除')
  await load()
}

async function batchPublish() {
  if (!pendingSelected.value.length) {
    ElMessage.warning('请选择待审核资源')
    return
  }
  await ElMessageBox.confirm(`将发布 ${pendingSelected.value.length} 条资源，确认继续吗？`, '批量发布')
  const count = await resourceApi.batchPublish(
    pendingSelected.value.map(row => ({ id: row.id, version: row.version }))
  )
  ElMessage.success(`已发布 ${count} 条资源`)
  await load()
}

async function openDetail(row: ResourceListItem) {
  detail.value = await resourceApi.adminDetail(row.id)
  detailVisible.value = true
}

async function downloadAttachment() {
  if (!detail.value?.attachmentName) return
  attachmentLoading.value = true
  try {
    await resourceApi.adminDownload(detail.value.id, detail.value.attachmentName)
  } finally {
    attachmentLoading.value = false
  }
}

function formatFileSize(size?: number) {
  if (!size) return '未提供大小'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

function formatDate(value?: string) {
  return value ? new Date(value).toLocaleString('zh-CN') : '-'
}

onMounted(async () => {
  try {
    categories.value = await categoryApi.tree()
  } catch {
    categories.value = []
  }
  await load()
})
</script>

<template>
  <div class="admin-page">
    <div class="page-toolbar">
      <div>
        <h2>资源审核中心</h2>
        <p>处理待审核资源、批量发布和维护已发布内容。</p>
      </div>
      <el-button
        v-if="auth.can('resource:publish')"
        type="primary"
        :disabled="!pendingSelected.length"
        @click="batchPublish"
      >
        批量发布 {{ pendingSelected.length ? `(${pendingSelected.length})` : '' }}
      </el-button>
    </div>

    <div class="surface-card filter-panel">
      <form @submit.prevent="search">
        <el-input v-model="query.keyword" clearable placeholder="标题或摘要" />
        <el-select v-model="query.status" clearable placeholder="全部状态">
          <el-option label="草稿" :value="0" />
          <el-option label="待审核" :value="1" />
          <el-option label="已发布" :value="2" />
          <el-option label="已驳回" :value="3" />
          <el-option label="已下架" :value="4" />
        </el-select>
        <el-select v-model="query.categoryId" clearable filterable placeholder="全部分类">
          <el-option
            v-for="option in categoryOptions"
            :key="option.id"
            :label="option.label"
            :value="option.id"
          />
        </el-select>
        <el-button type="primary" native-type="submit">查询</el-button>
      </form>
    </div>

    <div class="surface-card table-panel">
      <el-table
        v-loading="loading"
        :data="records"
        row-key="id"
        stripe
        @selection-change="selectedRows = $event"
      >
        <el-table-column type="selection" width="46" />
        <el-table-column label="资源" min-width="300">
          <template #default="{ row }">
            <button class="title-link" @click="openDetail(row)">{{ row.title }}</button>
            <div class="subline">{{ row.categoryName }} · {{ row.authorName }}</div>
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
        <el-table-column label="操作" width="245" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1 && auth.can('resource:audit')"
              link
              type="success"
              @click="approve(row)"
            >
              通过
            </el-button>
            <el-button
              v-if="row.status === 1 && auth.can('resource:audit')"
              link
              type="danger"
              @click="openReject(row)"
            >
              驳回
            </el-button>
            <el-button
              v-if="row.status === 2 && auth.can('resource:offline:any')"
              link
              @click="offline(row)"
            >
              下架
            </el-button>
            <el-button
              v-if="[0, 3, 4].includes(row.status) && auth.can('resource:delete:any')"
              link
              type="danger"
              @click="deleteResource(row)"
            >
              删除
            </el-button>
            <el-button link @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && !records.length" description="没有符合条件的资源" />
      <el-pagination
        v-if="total > (query.size || 10)"
        v-model:current-page="query.page"
        :page-size="query.size"
        :total="total"
        background
        layout="prev, pager, next"
        @current-change="load"
      />
    </div>

    <el-drawer v-model="detailVisible" title="资源详情" size="560px">
      <template v-if="detail">
        <div class="detail-head">
          <StatusTag :status="detail.status" />
          <span class="text-muted">{{ detail.authorName }} · {{ formatDate(detail.updatedAt) }}</span>
        </div>
        <h2>{{ detail.title }}</h2>
        <p class="detail-summary">{{ detail.summary }}</p>
        <div v-if="detail.auditRemark" class="audit-note">审核意见：{{ detail.auditRemark }}</div>
        <div v-if="detail.attachmentName" class="attachment-panel">
          <span class="attachment-type">{{ detail.attachmentExt?.toUpperCase() || 'FILE' }}</span>
          <div class="attachment-info">
            <small>资源附件</small>
            <strong>{{ detail.attachmentName }}</strong>
            <span>{{ formatFileSize(detail.attachmentSize) }}</span>
          </div>
          <el-button type="primary" plain :loading="attachmentLoading" @click="downloadAttachment">
            下载附件
          </el-button>
        </div>
        <div v-else class="attachment-empty">该资源未上传附件</div>
        <MarkdownContent :content="detail.content" />
      </template>
    </el-drawer>

    <el-dialog v-model="rejectVisible" title="驳回资源" width="480px">
      <p class="text-muted">请给作者提供明确的修改意见。</p>
      <el-input v-model="rejectRemark" type="textarea" :rows="5" maxlength="500" show-word-limit />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="submitReject">确认驳回</el-button>
      </template>
    </el-dialog>
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

.filter-panel {
  margin-bottom: 16px;
  padding: 16px;
}

.filter-panel form {
  display: grid;
  grid-template-columns: minmax(200px, 1.2fr) 150px 190px auto;
  gap: 10px;
}

.table-panel {
  overflow: hidden;
  padding: 4px 18px 20px;
}

.title-link {
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--cs-text);
  font-weight: 600;
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

.detail-head {
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-head + h2 {
  margin: 18px 0 10px;
}

.detail-summary {
  margin: 0 0 22px;
  color: var(--cs-muted);
  line-height: 1.75;
}

.audit-note {
  margin-bottom: 22px;
  padding: 12px 14px;
  border-radius: 8px;
  background: #fff7ed;
  color: #9a3412;
  font-size: 13px;
}

.attachment-panel {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 22px;
  padding: 14px;
  border: 1px solid #dbeafe;
  border-radius: 10px;
  background: #f8fbff;
}

.attachment-type {
  flex: none;
  padding: 5px 8px;
  border-radius: 7px;
  background: #dbeafe;
  color: var(--cs-primary);
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 11px;
  font-weight: 800;
}

.attachment-info {
  min-width: 0;
  display: grid;
  gap: 3px;
  flex: 1;
}

.attachment-info small,
.attachment-info span {
  color: var(--cs-muted);
  font-size: 12px;
}

.attachment-info strong {
  overflow-wrap: anywhere;
}

.attachment-empty {
  margin-bottom: 22px;
  padding: 13px 14px;
  border-radius: 9px;
  background: #f8fafc;
  color: var(--cs-muted);
  font-size: 13px;
}

@media (max-width: 560px) {
  .attachment-panel {
    align-items: flex-start;
    flex-wrap: wrap;
  }

  .attachment-panel .el-button {
    width: 100%;
  }
}

@media (max-width: 900px) {
  .filter-panel form {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 600px) {
  .page-toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .filter-panel form {
    grid-template-columns: 1fr;
  }
}
</style>

