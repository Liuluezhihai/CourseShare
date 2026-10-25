<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const mobileOpen = ref(false)

const workspaceName = computed(() => {
  if (auth.isAdmin) return '控制台'
  if (auth.canAudit) return '审核中心'
  return '创作中心'
})
const pageTitles: Record<string, string> = {
  'author-resources': '我的资源',
  'resource-create': '新建资源',
  'resource-edit': '编辑资源',
  'admin-resources': '资源审核',
  'admin-categories': '分类管理',
  'admin-users': '用户管理',
  'admin-logs': '操作日志'
}
const title = computed(() => pageTitles[String(route.name)] || workspaceName.value)
const userName = computed(() => auth.user?.nickname || auth.user?.username || '')

const menuItems = computed(() => {
  const items = [
    { path: '/admin/resources', label: '资源审核', permission: 'resource:audit:list' },
    { path: '/author/resources', label: '我的资源', permission: 'resource:create:own' },
    { path: '/admin/categories', label: '分类管理', permission: 'category:manage' },
    { path: '/admin/users', label: '用户管理', permission: 'user:manage' },
    { path: '/admin/logs', label: '操作日志', permission: 'log:view' }
  ]
  return items.filter(item => auth.can(item.permission))
})

async function logout() {
  await auth.logout()
  ElMessage.success('已退出登录')
  router.push('/')
}

function navigate(path: string) {
  mobileOpen.value = false
  router.push(path)
}
</script>

<template>
  <div class="console-shell">
    <aside class="desktop-sidebar">
      <router-link class="console-brand" to="/">
        <span class="brand-mark">CS</span>
        <span><strong>CourseShare</strong><small>{{ workspaceName }}</small></span>
      </router-link>
      <nav class="console-nav">
        <button
          v-for="item in menuItems"
          :key="item.path"
          :class="{ active: route.path.startsWith(item.path) }"
          @click="navigate(item.path)"
        >
          {{ item.label }}
        </button>
      </nav>
      <div class="sidebar-footer">
        <router-link to="/">返回资源门户</router-link>
      </div>
    </aside>

    <div class="console-main">
      <header class="console-header">
        <el-button class="mobile-trigger" text @click="mobileOpen = true">菜单</el-button>
        <div>
          <h1>{{ title }}</h1>
          <p>CourseShare {{ workspaceName }}</p>
        </div>
        <el-button class="home-button" plain @click="navigate('/')">返回首页</el-button>
        <el-dropdown>
          <button class="user-trigger">{{ userName }}</button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-if="!auth.isAdmin" @click="router.push('/profile')">
                个人资料
              </el-dropdown-item>
              <el-dropdown-item :divided="!auth.isAdmin" @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>

      <main class="console-content">
        <router-view />
      </main>
    </div>

    <el-drawer v-model="mobileOpen" :title="workspaceName" direction="ltr" size="280px">
      <nav class="mobile-nav">
        <button
          v-for="item in menuItems"
          :key="item.path"
          :class="{ active: route.path.startsWith(item.path) }"
          @click="navigate(item.path)"
        >
          {{ item.label }}
        </button>
        <button @click="navigate('/')">返回资源门户</button>
      </nav>
    </el-drawer>
  </div>
</template>

<style scoped>
.console-shell {
  min-height: 100vh;
  display: flex;
}

.desktop-sidebar {
  position: fixed;
  inset: 0 auto 0 0;
  width: 236px;
  display: flex;
  flex-direction: column;
  padding: 22px 16px;
  background: #0f172a;
  color: #e2e8f0;
}

.console-brand {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 4px 8px 24px;
}

.brand-mark {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border-radius: 11px;
  background: linear-gradient(135deg, #2563eb, #0f766e);
  color: white;
  font-size: 13px;
  font-weight: 800;
}

.console-brand strong,
.console-brand small {
  display: block;
}

.console-brand small {
  margin-top: 3px;
  color: #94a3b8;
  font-size: 10px;
}

.console-nav {
  display: grid;
  gap: 6px;
}

.console-nav button,
.mobile-nav button {
  width: 100%;
  padding: 11px 13px;
  border: 0;
  border-radius: 9px;
  background: transparent;
  color: #cbd5e1;
  text-align: left;
  cursor: pointer;
}

.console-nav button:hover,
.console-nav button.active {
  background: rgba(37, 99, 235, 0.22);
  color: white;
}

.sidebar-footer {
  margin-top: auto;
  padding: 16px 10px;
  color: #94a3b8;
  font-size: 13px;
}

.console-main {
  width: calc(100% - 236px);
  margin-left: 236px;
  min-height: 100vh;
}

.console-header {
  height: 76px;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 0 32px;
  border-bottom: 1px solid var(--cs-border);
  background: rgba(255, 255, 255, 0.96);
}

.console-header h1 {
  margin: 0;
  font-size: 20px;
}

.console-header p {
  margin: 4px 0 0;
  color: var(--cs-muted);
  font-size: 12px;
}

.home-button {
  margin-left: auto;
}

.console-header .el-dropdown {
  margin-left: 0;
}

.user-trigger {
  padding: 8px 12px;
  border: 1px solid var(--cs-border);
  border-radius: 9px;
  background: white;
  color: var(--cs-text);
  cursor: pointer;
}

.console-content {
  padding: 28px 32px 48px;
}

.mobile-trigger {
  display: none;
}

.mobile-nav {
  display: grid;
  gap: 6px;
}

.mobile-nav button {
  color: var(--cs-text);
  background: #f8fafc;
}

.mobile-nav button.active {
  color: var(--cs-primary);
  background: #eff6ff;
}

@media (max-width: 900px) {
  .desktop-sidebar {
    display: none;
  }

  .console-main {
    width: 100%;
    margin-left: 0;
  }

  .mobile-trigger {
    display: inline-flex;
  }

  .console-header,
  .console-content {
    padding-left: 18px;
    padding-right: 18px;
  }
}
</style>

