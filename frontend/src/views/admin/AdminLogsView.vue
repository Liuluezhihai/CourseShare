<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { logApi } from '../../api'
import type { OperationLog } from '../../api/types'

const loading = ref(false)
const rows = ref<OperationLog[]>([])
const total = ref(0)
const detailVisible = ref(false)
const detail = ref<OperationLog>()
const dateRange = ref<[string, string] | null>(null)
const query = reactive({
  keyword: '',
  module: '',
  success: undefined as number | undefined,
  page: 1,
  size: 10
})

const prettyParams = computed(() => {
  if (!detail.value?.paramsJson) return ''
  try {
    return JSON.stringify(JSON.parse(detail.value.paramsJson), null, 2)
  } catch {
    return detail.value.paramsJson
  }
})

async function load() {
  loading.value = true
  try {
    const data = await logApi.page({
      ...query,
      dateFrom: dateRange.value?.[0],
      dateTo: dateRange.value?.[1]
    })
    rows.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function search() {
  query.page = 1
  load()
}

function openDetail(row: OperationLog) {
  detail.value = row
  detailVisible.value = true
}

function formatDate(value?: string) {
  return value ? new Date(value).toLocaleString('zh-CN') : '-'
}

onMounted(load)
</script>

<template>
  <div class="admin-page">
    <div class="page-toolbar">
      <div>
        <h2>操作日志</h2>
        <p>追踪写操作、登录结果和异常请求，敏感参数已脱敏。</p>
      </div>
    </div>

    <div class="surface-card filter-panel">
      <form @submit.prevent="search">
        <el-input v-model="query.keyword" clearable placeholder="请求 ID、用户或 URI" />
        <el-select v-model="query.module" clearable placeholder="全部模块">
          <el-option label="认证" value="认证" />
          <el-option label="资源" value="资源" />
          <el-option label="审核" value="审核" />
          <el-option label="分类" value="分类" />
          <el-option label="用户" value="用户" />
        </el-select>
        <el-select v-model="query.success" clearable placeholder="全部结果">
          <el-option label="成功" :value="1" />
          <el-option label="失败" :value="0" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        />
        <el-button type="primary" native-type="submit">查询</el-button>
      </form>
    </div>

    <div class="surface-card table-panel">
      <el-table v-loading="loading" :data="rows" stripe>
        <el-table-column prop="module" label="模块" width="90" />
        <el-table-column prop="action" label="操作" width="130" />
        <el-table-column label="用户" width="130">
          <template #default="{ row }">{{ row.usernameSnapshot || '匿名' }}</template>
        </el-table-column>
        <el-table-column prop="uri" label="请求地址" min-width="240" show-overflow-tooltip />
        <el-table-column label="结果" width="95">
          <template #default="{ row }">
            <el-tag :type="row.success === 1 ? 'success' : 'danger'" effect="light">
              {{ row.success === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="90">
          <template #default="{ row }">{{ row.durationMs }} ms</template>
        </el-table-column>
        <el-table-column label="时间" width="180">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }"><el-button link @click="openDetail(row)">详情</el-button></template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && !rows.length" description="暂无日志记录" />
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

    <el-drawer v-model="detailVisible" title="日志详情" size="560px">
      <template v-if="detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="请求 ID">{{ detail.requestId }}</el-descriptions-item>
          <el-descriptions-item label="操作">{{ detail.module }} / {{ detail.action }}</el-descriptions-item>
          <el-descriptions-item label="用户">{{ detail.usernameSnapshot || '匿名' }}</el-descriptions-item>
          <el-descriptions-item label="请求">{{ detail.httpMethod }} {{ detail.uri }}</el-descriptions-item>
          <el-descriptions-item label="IP">{{ detail.ip }}</el-descriptions-item>
          <el-descriptions-item label="耗时">{{ detail.durationMs }} ms</el-descriptions-item>
          <el-descriptions-item label="结果">
            {{ detail.success === 1 ? '成功' : `失败 ${detail.errorCode || ''}` }}
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.errorMessage" label="错误信息">
            {{ detail.errorMessage }}
          </el-descriptions-item>
        </el-descriptions>
        <h3>脱敏参数</h3>
        <pre class="params-view">{{ prettyParams || '无参数' }}</pre>
      </template>
    </el-drawer>
  </div>
</template>

<style scoped>
.page-toolbar {
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
  grid-template-columns: minmax(200px, 1fr) 130px 130px 320px auto;
  gap: 10px;
}

.table-panel {
  overflow: hidden;
  padding: 4px 18px 20px;
}

.params-view {
  overflow-x: auto;
  padding: 18px;
  border-radius: 10px;
  background: #0f172a;
  color: #dbeafe;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 12px;
  line-height: 1.7;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

@media (max-width: 1100px) {
  .filter-panel form {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 640px) {
  .filter-panel form {
    grid-template-columns: 1fr;
  }
}
</style>

