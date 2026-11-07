<script setup lang="ts">
import type { CategoryNode } from '../api/types'

const props = defineProps<{ nodes: CategoryNode[]; currentId?: number }>()
const emit = defineEmits<{ select: [id?: number] }>()

function handleClick(node: CategoryNode) {
  emit('select', node.id)
}

function handleCurrent(id?: number) {
  emit('select', id)
}
</script>

<template>
  <div class="category-tree">
    <button class="all-button" @click="handleCurrent(undefined)">全部分类</button>
    <el-tree
      :data="props.nodes"
      node-key="id"
      :props="{ label: 'name', children: 'children' }"
      :current-node-key="props.currentId"
      highlight-current
      default-expand-all
      :expand-on-click-node="false"
      @node-click="handleClick"
    />
  </div>
</template>

<style scoped>
.category-tree {
  padding: 16px;
  background: var(--cs-surface);
  border: 1px solid var(--cs-border);
  border-radius: var(--cs-radius);
}

.all-button {
  width: 100%;
  margin-bottom: 10px;
  padding: 10px 12px;
  border: 0;
  border-radius: 8px;
  background: #f8fafc;
  color: var(--cs-text);
  text-align: left;
  cursor: pointer;
}

.all-button:hover {
  color: var(--cs-primary);
  background: #eff6ff;
}
</style>

