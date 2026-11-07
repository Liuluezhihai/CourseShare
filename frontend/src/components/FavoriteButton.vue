<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { useFavoriteStore } from '../stores/favorites'

const props = withDefaults(defineProps<{
  resourceId: number
  compact?: boolean
  label?: boolean
}>(), {
  compact: false,
  label: false
})

const emit = defineEmits<{
  change: [resourceId: number, favorited: boolean]
}>()

const auth = useAuthStore()
const favorites = useFavoriteStore()
const route = useRoute()
const router = useRouter()
const pending = ref(false)
const favorited = computed(() => favorites.isFavorite(props.resourceId))

async function toggle() {
  if (!auth.isAuthenticated) {
    ElMessage.warning('登录后即可收藏资源')
    await router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  pending.value = true
  try {
    const next = await favorites.toggle(props.resourceId)
    emit('change', props.resourceId, next)
    ElMessage.success(next ? '已加入收藏' : '已取消收藏')
  } finally {
    pending.value = false
  }
}
</script>

<template>
  <el-button
    class="favorite-button"
    :class="{ compact, active: favorited }"
    :loading="pending"
    :circle="compact"
    :text="!label"
    :type="favorited ? 'warning' : 'default'"
    :aria-label="favorited ? '取消收藏' : '收藏资源'"
    @click.stop.prevent="toggle"
  >
    <span class="favorite-mark">{{ favorited ? '★' : '☆' }}</span>
    <span v-if="label">{{ favorited ? '已收藏' : '收藏' }}</span>
  </el-button>
</template>

<style scoped>
.favorite-button.active {
  color: #b45309;
  background: #fffbeb;
}

.favorite-mark {
  font-size: 19px;
  line-height: 1;
}

.favorite-button:not(.compact) .favorite-mark {
  margin-right: 6px;
}
</style>
