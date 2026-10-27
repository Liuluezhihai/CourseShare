<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { categoryApi } from '../../api'
import type { CategoryNode } from '../../api/types'

const loading = ref(false)
const tree = ref<CategoryNode[]>([])
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const editing = ref<CategoryNode>()

const form = reactive({
  parentId: undefined as number | undefined,
  name: '',
  description: '',
  sortOrder: 0,
  status: 1,
  version: 0
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const parentOptions = computed(() => {
  const options: Array<{ id: number; label: string }> = []
  const walk = (nodes: CategoryNode[]) => {
    nodes.forEach(node => {
      if (node.id !== editing.value?.id) {
        options.push({ id: node.id, label: `${'　'.repeat(node.level - 1)}${node.name}` })
      }
      walk(node.children)
    })
  }
  walk(tree.value)
  return options
})

async function load() {
  loading.value = true
  try {
    tree.value = await categoryApi.adminTree()
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editing.value = undefined
  Object.assign(form, { parentId: undefined, name: '', description: '', sortOrder: 0, status: 1, version: 0 })
  dialogVisible.value = true
}

function openEdit(row: CategoryNode) {
  editing.value = row
  Object.assign(form, {
    parentId: row.parentId,
    name: row.name,
    description: row.description || '',
    sortOrder: row.sortOrder,
    status: row.status,
    version: row.version
  })
  dialogVisible.value = true
}

async function save() {
  if (!(await formRef.value?.validate().catch(() => false))) return
  const payload = {
    parentId: form.parentId,
    name: form.name,
    description: form.description,
    sortOrder: form.sortOrder,
    status: form.status,
    version: editing.value ? form.version : undefined
  }
  if (editing.value) {
    await categoryApi.update(editing.value.id, payload)
  } else {
    await categoryApi.create(payload)
  }
  dialogVisible.value = false
  ElMessage.success(editing.value ? '分类已更新' : '分类已创建')
  await load()
}

async function toggleStatus(row: CategoryNode) {
  try {
    await categoryApi.status(row.id, row.status, row.version)
    ElMessage.success(row.status === 1 ? '分类已启用' : '分类及后代已停用')
  } finally {
    await load()
  }
}

async function removeCategory(row: CategoryNode) {
  await ElMessageBox.confirm(`确定删除分类“${row.name}”吗？`, '删除分类', { type: 'warning' })
  await categoryApi.delete(row.id)
  ElMessage.success('分类已删除')
  await load()
}

onMounted(load)
</script>

<template>
  <div class="admin-page">
    <div class="page-toolbar">
      <div>
        <h2>分类树管理</h2>
        <p>最多三级；停用父分类会同步停用所有后代。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增分类</el-button>
    </div>

    <div class="surface-card table-panel">
      <el-table
        v-loading="loading"
        :data="tree"
        row-key="id"
        default-expand-all
        :tree-props="{ children: 'children' }"
      >
        <el-table-column prop="name" label="分类名称" min-width="220" />
        <el-table-column prop="level" label="层级" width="80" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="resourceCount" label="已发布资源" width="120" />
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
        <el-table-column prop="description" label="说明" min-width="240" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="removeCategory(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑分类' : '新增分类'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="父分类">
          <el-select v-model="form.parentId" clearable filterable placeholder="留空表示一级分类" style="width: 100%">
            <el-option
              v-for="option in parentOptions"
              :key="option.id"
              :label="option.label"
              :value="option.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" maxlength="50" />
        </el-form-item>
        <el-form-item label="分类说明">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="255" />
        </el-form-item>
        <div class="form-row">
          <el-form-item label="排序值">
            <el-input-number v-model="form.sortOrder" :min="0" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="form.status">
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
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

.table-panel {
  padding: 12px 18px 20px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

@media (max-width: 600px) {
  .page-toolbar {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>

