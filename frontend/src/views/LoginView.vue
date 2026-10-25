<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function submit() {
  if (!(await formRef.value?.validate().catch(() => false))) return
  loading.value = true
  try {
    await auth.login(form.username, form.password)
    ElMessage.success(`欢迎回来，${auth.user?.nickname}`)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    router.push(redirect)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="auth-page">
    <div class="auth-panel surface-card">
      <div class="auth-copy">
        <span class="auth-badge">COURSESHARE</span>
        <h1>继续你的学习旅程</h1>
        <p>登录后可下载资源，也可以分享自己的课程笔记和学习资料。</p>
        <div class="auth-note">
          <strong>测试账号</strong>
          <span>admin / auditor / author / student</span>
          <span>初始密码：password</span>
        </div>
      </div>
      <div class="auth-form">
        <h2>账号登录</h2>
        <p class="text-muted">使用注册账号或课程测试账号。</p>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="submit">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" size="large" autocomplete="username" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              show-password
              size="large"
              autocomplete="current-password"
              @keyup.enter="submit"
            />
          </el-form-item>
          <el-button type="primary" size="large" :loading="loading" class="submit-button" @click="submit">
            登录
          </el-button>
        </el-form>
        <p class="switch-link">还没有账号？<router-link to="/register">立即注册</router-link></p>
      </div>
    </div>
  </section>
</template>

<style scoped>
.auth-page {
  min-height: calc(100vh - 72px);
  display: grid;
  place-items: center;
  padding: 48px 24px;
  background:
    radial-gradient(circle at 16% 20%, rgba(37, 99, 235, 0.15), transparent 28%),
    radial-gradient(circle at 82% 76%, rgba(15, 118, 110, 0.12), transparent 24%),
    #f7f9fc;
}

.auth-panel {
  width: min(920px, 100%);
  display: grid;
  grid-template-columns: 1fr 1fr;
  overflow: hidden;
}

.auth-copy,
.auth-form {
  padding: 54px;
}

.auth-copy {
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: linear-gradient(145deg, #0f172a, #172554);
  color: white;
}

.auth-badge {
  color: #93c5fd;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.12em;
}

.auth-copy h1 {
  margin: 22px 0 16px;
  font-size: 38px;
  line-height: 1.18;
  letter-spacing: -0.045em;
}

.auth-copy > p {
  margin: 0;
  color: #cbd5e1;
  line-height: 1.8;
}

.auth-note {
  display: grid;
  gap: 7px;
  margin-top: 44px;
  padding: 16px;
  border: 1px solid rgba(147, 197, 253, 0.22);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.06);
  color: #cbd5e1;
  font-size: 12px;
}

.auth-note strong {
  color: white;
}

.auth-form {
  background: white;
}

.auth-form h2 {
  margin: 0 0 8px;
  font-size: 28px;
}

.auth-form > p {
  margin: 0 0 30px;
}

.submit-button {
  width: 100%;
  margin-top: 10px;
}

.switch-link {
  margin: 22px 0 0 !important;
  color: var(--cs-muted);
  text-align: center;
  font-size: 13px;
}

.switch-link a {
  color: var(--cs-primary);
  font-weight: 600;
}

@media (max-width: 760px) {
  .auth-panel {
    grid-template-columns: 1fr;
  }

  .auth-copy {
    display: none;
  }

  .auth-form {
    padding: 34px 24px;
  }
}
</style>

