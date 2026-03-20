<template>
  <div class="not-found-page">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <h2 class="error-title">页面走丢了</h2>
      <p class="error-desc">你访问的页面不存在或已被移除</p>
      <el-button type="primary" round @click="$router.push('/')">
        回到首页
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.not-found-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
}

.not-found-content {
  text-align: center;
}

.error-code {
  font-size: 120px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent-purple));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
  margin-bottom: 16px;
}

.error-title {
  font-size: 24px;
  color: var(--color-text-primary);
  margin-bottom: 8px;
}

.error-desc {
  color: var(--color-text-muted);
  margin-bottom: 24px;
}
</style>
