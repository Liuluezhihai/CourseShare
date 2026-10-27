<template>
  <section class="page-section">
    <div class="page-container">
      <div class="empty-block">
        <el-result icon="info" title="页面不存在" sub-title="链接可能已失效，或者页面地址输入有误。">
          <template #extra><el-button type="primary" @click="$router.push('/')">返回首页</el-button></template>
        </el-result>
      </div>
    </div>
  </section>
</template>

