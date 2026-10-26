<template>
  <section class="page-section">
    <div class="page-container">
      <div class="empty-block">
        <el-result icon="warning" title="暂时无法访问" sub-title="请返回上一页，或联系站点工作人员协助处理。">
          <template #extra><el-button type="primary" @click="$router.push('/')">返回首页</el-button></template>
        </el-result>
      </div>
    </div>
  </section>
</template>

