<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules, UploadFile, UploadFiles } from 'element-plus'
import { ElMessage } from 'element-plus'
import { categoryApi, resourceApi } from '../../api'
import type { CategoryNode, ResourceDetail } from '../../api/types'
import MarkdownContent from '../../components/MarkdownContent.vue'
import StatusTag from '../../components/StatusTag.vue'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)
const categories = ref<CategoryNode[]>([])
const file = ref<File>()
const existing = ref<ResourceDetail>()

const isEdit = computed(() => Boolean(route.params.id))
const form = reactive({
  title: '',
  summary: '',
  content: '',
  categoryId: undefined as number | undefined,
  version: 0
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入资源标题', trigger: 'blur' }],
  summary: [{ required: true, message: '请输入资源摘要', trigger: 'blur' }],
  content: [{ required: true, message: '请输入 Markdown 正文', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

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
    categories.value = await categoryApi.tree()
    if (isEdit.value) {
      const detail = await resourceApi.authorDetail(Number(route.params.id))
      existing.value = detail
      form.title = detail.title
      form.summary = detail.summary
      form.content = detail.content
      form.categoryId = detail.categoryId
      form.version = detail.version
    }
  } finally {
    loading.value = false
  }
}

function onFileChange(uploadFile: UploadFile, _files: UploadFiles) {
  file.value = uploadFile.raw
}

function onFileRemove() {
  file.value = undefined
}

async function save() {
  if (!(await formRef.value?.validate().catch(() => false))) return
  saving.value = true
  try {
    const payload = {
      title: form.title,
      summary: form.summary,
      content: form.content,
      categoryId: form.categoryId,
      version: isEdit.value ? form.version : undefined
    }
    if (isEdit.value) {
      await resourceApi.update(Number(route.params.id), payload, file.value)
    } else {
      await resourceApi.create(payload, file.value)
    }
    ElMessage.success(isEdit.value ? '资源已保存' : '草稿已创建')
    router.push('/author/resources')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <div v-loading="loading" class="editor-page">
    <div class="editor-heading">
      <div>
        <div class="heading-line">
          <h2>{{ isEdit ? '编辑学习资源' : '新建学习资源' }}</h2>
          <StatusTag v-if="existing" :status="existing.status" />
        </div>
        <p>正文使用 Markdown，保存后仍为草稿或原状态；提交审核需回到资源列表操作。</p>
      </div>
      <div class="heading-actions">
        <el-button @click="router.push('/author/resources')">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存草稿</el-button>
      </div>
    </div>

    <div class="editor-grid">
      <section class="surface-card form-panel">
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="资源标题" prop="title">
            <el-input v-model="form.title" maxlength="200" show-word-limit size="large" />
          </el-form-item>
          <el-form-item label="所属分类" prop="categoryId">
            <el-select v-model="form.categoryId" filterable placeholder="请选择分类" style="width: 100%">
              <el-option
                v-for="option in categoryOptions"
                :key="option.id"
                :label="option.label"
                :value="option.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="资源摘要" prop="summary">
            <el-input
              v-model="form.summary"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="用一段话说明资源内容、适用对象和学习目标"
            />
          </el-form-item>
          <el-form-item label="学习附件">
            <div class="upload-block">
              <el-upload
                :auto-upload="false"
                :limit="1"
                :on-change="onFileChange"
                :on-remove="onFileRemove"
              >
                <el-button>选择附件</el-button>
                <template #tip>
                  <div class="el-upload__tip">支持 PDF、Office、Markdown、TXT、ZIP，最大 50 MB。</div>
                </template>
              </el-upload>
              <div v-if="existing?.attachmentName && !file" class="existing-file">
                当前附件：{{ existing.attachmentName }}
              </div>
            </div>
          </el-form-item>
          <el-form-item label="Markdown 正文" prop="content">
            <el-input
              v-model="form.content"
              class="content-editor"
              type="textarea"
              :rows="24"
              placeholder="# 资源标题&#10;&#10;在这里编写学习内容..."
            />
          </el-form-item>
        </el-form>
      </section>

      <aside class="surface-card preview-panel">
        <div class="preview-heading">
          <span>实时预览</span>
          <small>渲染后自动清理不安全内容</small>
        </div>
        <MarkdownContent :content="form.content || '# 内容预览\n\n开始输入 Markdown 正文。'" />
      </aside>
    </div>
  </div>
</template>

<style scoped>
.editor-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 22px;
}

.heading-line {
  display: flex;
  align-items: center;
  gap: 12px;
}

.editor-heading h2 {
  margin: 0;
  font-size: 26px;
}

.editor-heading p {
  margin: 7px 0 0;
  color: var(--cs-muted);
}

.heading-actions {
  display: flex;
  gap: 8px;
}

.editor-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(360px, 0.8fr);
  gap: 20px;
  align-items: start;
}

.form-panel,
.preview-panel {
  padding: 24px;
}

.preview-panel {
  position: sticky;
  top: 100px;
  max-height: calc(100vh - 124px);
  overflow-y: auto;
}

.preview-heading {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 22px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--cs-border);
}

.preview-heading span {
  font-weight: 700;
}

.preview-heading small,
.existing-file {
  color: var(--cs-muted);
  font-size: 12px;
}

.content-editor :deep(textarea) {
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 13px;
  line-height: 1.7;
}

.upload-block {
  width: 100%;
}

.existing-file {
  margin-top: 8px;
}

@media (max-width: 1100px) {
  .editor-grid {
    grid-template-columns: 1fr;
  }

  .preview-panel {
    position: static;
    max-height: none;
  }
}

@media (max-width: 700px) {
  .editor-heading {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>

