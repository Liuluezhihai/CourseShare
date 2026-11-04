import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('../layouts/PublicLayout.vue'),
    children: [
      { path: '', name: 'home', component: () => import('../views/HomeView.vue') },
      { path: 'resources', name: 'resources', component: () => import('../views/ResourceListView.vue') },
      { path: 'resources/:id', name: 'resource-detail', component: () => import('../views/ResourceDetailView.vue') },
      { path: 'discover', name: 'discover', component: () => import('../views/DiscoverView.vue') },
      { path: 'my-learning', name: 'my-learning', component: () => import('../views/MyLearningView.vue') },
      { path: 'login', name: 'login', component: () => import('../views/LoginView.vue') },
      { path: 'register', name: 'register', component: () => import('../views/RegisterView.vue') },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('../views/ProfileView.vue'),
        meta: { requiresAuth: true }
      },
      { path: '403', name: 'forbidden', component: () => import('../views/ForbiddenView.vue') }
    ]
  },
  {
    path: '/author',
    component: () => import('../layouts/ConsoleLayout.vue'),
    meta: { requiresAuth: true, requiresAuthor: true },
    children: [
      { path: '', redirect: '/author/resources' },
      { path: 'resources', name: 'author-resources', component: () => import('../views/author/AuthorResourcesView.vue') },
      { path: 'resources/new', name: 'resource-create', component: () => import('../views/author/ResourceEditorView.vue') },
      {
        path: 'resources/:id/edit',
        name: 'resource-edit',
        component: () => import('../views/author/ResourceEditorView.vue')
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('../layouts/ConsoleLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/admin/resources' },
      {
        path: 'resources',
        name: 'admin-resources',
        component: () => import('../views/admin/AdminResourcesView.vue'),
        meta: { requiresPermission: 'resource:audit:list' }
      },
      {
        path: 'categories',
        name: 'admin-categories',
        component: () => import('../views/admin/AdminCategoriesView.vue'),
        meta: { requiresPermission: 'category:manage' }
      },
      {
        path: 'users',
        name: 'admin-users',
        component: () => import('../views/admin/AdminUsersView.vue'),
        meta: { requiresPermission: 'user:manage' }
      },
      {
        path: 'logs',
        name: 'admin-logs',
        component: () => import('../views/admin/AdminLogsView.vue'),
        meta: { requiresPermission: 'log:view' }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('../views/NotFoundView.vue') }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach(async to => {
  const auth = useAuthStore()
  await auth.load()
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresAuthor && !auth.canAuthor) {
    return { name: 'forbidden' }
  }
  const permission = to.meta.requiresPermission as string | undefined
  if (permission && !auth.can(permission)) {
    return { name: 'forbidden' }
  }
  return true
})

window.addEventListener('courseshare:unauthorized', () => {
  const auth = useAuthStore()
  auth.clear()
  const current = router.currentRoute.value
  if (current.meta.requiresAuth && !['login', 'home'].includes(String(current.name))) {
    router.push({ name: 'login', query: { redirect: current.fullPath } })
  }
})

export default router

