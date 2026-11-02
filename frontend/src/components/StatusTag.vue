<script setup lang="ts">
const props = defineProps<{ status: number }>()

const map: Record<number, { label: string; type: 'info' | 'warning' | 'success' | 'danger' | 'primary' }> = {
  0: { label: '草稿', type: 'info' },
  1: { label: '待审核', type: 'warning' },
  2: { label: '已发布', type: 'success' },
  3: { label: '已驳回', type: 'danger' },
  4: { label: '已下架', type: 'primary' }
}
</script>

<template>
  <el-tag :type="map[props.status]?.type || 'info'" effect="light" round>
    {{ map[props.status]?.label || '未知状态' }}
  </el-tag>
</template>

