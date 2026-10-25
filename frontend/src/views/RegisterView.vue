<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { authApi } from '../api'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  email: '',
  enableAuthor: false
})
const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_]{4,30}$/, message: '4-30 位字母、数字或下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 32, message: '密码长度为 8-32 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        value === form.password ? callback() : callback(new Error('两次输入的密码不一致'))
      },
      trigger: 'blur'
    }
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

async function submit() {
  if (!(await formRef.value?.validate().catch(() => false))) return
  loading.value = true
  try {
    await authApi.register({
      username: form.username,
      password: form.password,
      nickname: form.nickname,
      email: form.email || undefined,
      enableAuthor: form.enableAuthor
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="register-page">
    <div class="register-card surface-card">
      <div class="register-heading">
        <span class="eyebrow">JOIN COURSESHARE</span>
        <h1>创建学习账号</h1>
        <p>加入 CourseShare，浏览、下载并分享优质学习资源。</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-grid">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" autocomplete="username" />
          </el-form-item>
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" show-password autocomplete="new-password" />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" show-password autocomplete="new-password" />
          </el-form-item>
        </div>
        <el-form-item label="邮箱（可选）" prop="email">
          <el-input v-model="form.email" autocomplete="email" />
        </el-form-item>
        <el-checkbox v-model="form.enableAuthor" class="author-option">
          同时开通创作功能
          <span>之后也可以随时在个人资料中开启</span>
        </el-checkbox>
        <el-button type="primary" size="large" :loading="loading" class="submit-button" @click="submit">
          创建账号
        </el-button>
      </el-form>
      <p class="switch-link">已有账号？<router-link to="/login">返回登录</router-link></p>
    </div>
  </section>
</template>

<style scoped>
.register-page {
  min-height: calc(100vh - 72px);
  display: grid;
  place-items: center;
  padding: 48px 24px;
  background:
    radial-gradient(circle at 12% 16%, rgba(37, 99, 235, 0.12), transparent 24%),
    var(--cs-bg);
}

.register-card {
  width: min(720px, 100%);
  padding: 46px;
}

.eyebrow {
  color: var(--cs-primary);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.12em;
}

.register-heading h1 {
  margin: 14px 0 8px;
  font-size: 34px;
  letter-spacing: -0.04em;
}

.register-heading p {
  margin: 0 0 30px;
  color: var(--cs-muted);
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 18px;
}

.submit-button {
  width: 100%;
  margin-top: 10px;
}

.author-option {
  width: 100%;
  margin: -2px 0 20px;
}

.author-option span {
  margin-left: 6px;
  color: var(--cs-muted);
  font-size: 12px;
}

.switch-link {
  margin: 20px 0 0;
  color: var(--cs-muted);
  text-align: center;
  font-size: 13px;
}

.switch-link a {
  color: var(--cs-primary);
}

@media (max-width: 620px) {
  .register-card {
    padding: 30px 20px;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>

