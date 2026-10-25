<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const displayName = computed(() => auth.user?.nickname || auth.user?.username || '')

async function logout() {
  await auth.logout()
  ElMessage.success('已退出登录')
  router.push('/')
}
</script>

<template>
  <div class="public-shell">
    <header class="site-header">
      <div class="page-container header-inner">
        <router-link class="brand" to="/">
          <span class="brand-mark">CS</span>
          <span>
            <strong>CourseShare</strong>
            <small>学习资源分享平台</small>
          </span>
        </router-link>

        <nav class="main-nav">
          <router-link to="/">首页</router-link>
          <router-link to="/resources">资源库</router-link>
          <router-link to="/discover">发现</router-link>
          <router-link v-if="auth.isAdmin" to="/admin">控制台</router-link>
          <router-link v-else-if="auth.canAudit" to="/admin/resources">审核中心</router-link>
          <router-link v-if="auth.canAuthor" to="/author/resources">我的资源</router-link>
        </nav>

        <div class="account-actions">
          <template v-if="auth.isAuthenticated">
            <router-link class="account-name" to="/profile">{{ displayName }}</router-link>
            <el-button text @click="logout">退出</el-button>
          </template>
          <template v-else>
            <router-link to="/login"><el-button text>登录</el-button></router-link>
            <router-link to="/register"><el-button type="primary">注册</el-button></router-link>
          </template>
        </div>
      </div>
    </header>

    <main>
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <footer class="site-footer">
      <div class="page-container footer-inner">
        <span>CourseShare · Java 框架技术课程项目</span>
        <span>资源发布、审核与共享</span>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.public-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.site-header {
  position: sticky;
  top: 0;
  z-index: 30;
  border-bottom: 1px solid rgba(229, 231, 235, 0.9);
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(14px);
}

.header-inner {
  height: 72px;
  display: flex;
  align-items: center;
  gap: 36px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 11px;
  min-width: 210px;
}

.brand-mark {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border-radius: 11px;
  background: linear-gradient(135deg, #2563eb, #0f766e);
  color: #fff;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: -0.04em;
}

.brand strong,
.brand small {
  display: block;
}

.brand strong {
  font-size: 16px;
}

.brand small {
  margin-top: 3px;
  color: var(--cs-muted);
  font-size: 10px;
}

.main-nav {
  display: flex;
  gap: 6px;
  flex: 1;
}

.main-nav a {
  padding: 9px 13px;
  border-radius: 8px;
  color: var(--cs-muted);
  font-size: 14px;
}

.main-nav a:hover,
.main-nav a.router-link-exact-active {
  color: var(--cs-primary);
  background: #eff6ff;
}

.account-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

.account-name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}

.site-footer {
  margin-top: auto;
  padding: 28px 0;
  border-top: 1px solid var(--cs-border);
  background: #fff;
  color: var(--cs-muted);
  font-size: 13px;
}

.footer-inner {
  display: flex;
  justify-content: space-between;
  gap: 20px;
}

@media (max-width: 768px) {
  .header-inner {
    height: auto;
    min-height: 64px;
    flex-wrap: wrap;
    gap: 12px;
    padding-top: 12px;
    padding-bottom: 12px;
  }

  .brand {
    min-width: 0;
  }

  .main-nav {
    order: 3;
    width: 100%;
    overflow-x: auto;
  }

  .account-actions {
    margin-left: auto;
  }

  .footer-inner {
    flex-direction: column;
  }
}
</style>

