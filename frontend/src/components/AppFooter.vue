<template>
  <footer class="app-footer">
    <div class="footer-inner container">
      <div class="footer-brand">
        <span class="brand-icon">🌿</span>
        <span>心灵树洞 · 大学生心理健康平台</span>
      </div>
      <div class="footer-links">
        <router-link to="/articles">心理科普</router-link>
        <span class="divider">·</span>
        <router-link to="/treehole">树洞广场</router-link>
        <span class="divider">·</span>
        <router-link to="/quiz">心理测评</router-link>
      </div>
      <div class="footer-copy">
        &copy; {{ new Date().getFullYear() }} 心灵树洞 · 用心倾听每一个声音
      </div>
    </div>
  </footer>
</template>

<style scoped>
.app-footer {
  background: #2C3E50;
  color: #A0AEC0;
  padding: 32px 0 24px;
  margin-top: auto;
}

.footer-inner {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  color: #CBD5E0;
}

.brand-icon {
  font-size: 20px;
}

.footer-links {
  display: flex;
  gap: 6px;
  font-size: 13px;
}

.footer-links a {
  color: #8CA0B3;
  transition: color 0.2s;
}

.footer-links a:hover {
  color: var(--color-primary);
}

.divider {
  color: #4A5568;
}

.footer-copy {
  font-size: 12px;
  color: #4A5568;
}
</style>
