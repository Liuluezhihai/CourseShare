<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '../../api'
import type { RoleOption, UserListItem } from '../../api/types'

const loading = ref(false)
const rows = ref<UserListItem[]>([])
const total = ref(0)
const roles = ref<RoleOption[]>([])
const dialogVisible = ref(false)
const editingUser = ref<UserListItem>()
const selectedRoles = ref<string[]>([])
const query = reactive({ keyword: '', status: undefined as number | undefined, page: 1, size: 10 })

async function load() {
  loading.value = true
  try {
    const data = await userApi.page(query)
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

function openRoles(user: UserListItem) {
  editingUser.value = user
  selectedRoles.value = [...user.roleCodes]
  dialogVisible.value = true
}

async function saveRoles() {
  if (!editingUser.value || !selectedRoles.value.length) {
    ElMessage.warning('用户至少需要一个角色')
    return
  }
  await userApi.assignRoles(editingUser.value.id, selectedRoles.value)
  dialogVisible.value = false
  ElMessage.success('角色已更新')
  await load()
}

async function toggleStatus(user: UserListItem) {
  try {
    await userApi.status(user.id, user.status)
    ElMessage.success(user.status === 1 ? '账号已启用' : '账号已停用')
  } finally {
    await load()
  }
}

function formatDate(value?: string) {
  return value ? new Date(value).toLocaleString('zh-CN') : '-'
}

onMounted(async () => {
  roles.value = await userApi.roles()
  await load()
})
</script>

<template>
  <div class="admin-page">
    <div class="page-toolbar">
      <div>
        <h2>用户与角色</h2>
        <p>维护账号状态和可用功能。</p>
      </div>
    </div>

    <div class="surface-card filter-panel">
      <form @submit.prevent="search">
        <el-input v-model="query.keyword" clearable placeholder="用户名、昵称或邮箱" />
        <el-select v-model="query.status" clearable placeholder="全部状态">
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
        <el-button type="primary" native-type="submit">查询</el-button>
      </form>
    </div>

    <div class="surface-card table-panel">
      <el-table v-loading="loading" :data="rows" stripe>
        <el-table-column label="用户" min-width="220">
          <template #default="{ row }">
            <strong>{{ row.nickname }}</strong>
            <div class="subline">@{{ row.username }} · {{ row.email || '未填写邮箱' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="角色" min-width="210">
          <template #default="{ row }">
            <el-tag v-for="role in row.roleNames" :key="role" size="small" effect="plain" class="role-tag">
              {{ role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最后登录" width="180">
          <template #default="{ row }">{{ formatDate(row.lastLoginAt) }}</template>
        </el-table-column>
        <el-table-column label="注册时间" width="180">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="130">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              inline-prompt
              active-text="启用"
              inactive-text="停用"
              @change="toggleStatus(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openRoles(row)">分配角色</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && !rows.length" description="没有符合条件的用户" />
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

    <el-dialog v-model="dialogVisible" title="分配角色" width="480px">
      <p class="text-muted">当前用户：{{ editingUser?.nickname }}（@{{ editingUser?.username }}）</p>
      <el-checkbox-group v-model="selectedRoles" class="role-options">
        <el-checkbox v-for="role in roles" :key="role.code" :value="role.code">
          {{ role.name }}
          <small>{{ role.description }}</small>
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRoles">保存角色</el-button>
      </template>
    </el-dialog>
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
  grid-template-columns: minmax(240px, 1fr) 160px auto;
  gap: 10px;
}

.table-panel {
  overflow: hidden;
  padding: 4px 18px 20px;
}

.subline {
  margin-top: 4px;
  color: var(--cs-muted);
  font-size: 12px;
}

.role-tag {
  margin: 2px 4px 2px 0;
}

.role-options {
  display: grid;
  gap: 10px;
}

.role-options :deep(.el-checkbox) {
  height: auto;
  align-items: flex-start;
  padding: 10px 12px;
  border: 1px solid var(--cs-border);
  border-radius: 8px;
}

.role-options small {
  display: block;
  color: var(--cs-muted);
  font-size: 11px;
}

@media (max-width: 640px) {
  .filter-panel form {
    grid-template-columns: 1fr;
  }
}
</style>

