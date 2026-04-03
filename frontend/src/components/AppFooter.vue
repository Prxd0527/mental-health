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
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  color: var(--color-text-secondary);
  padding: 40px 0 24px;
  margin-top: 60px;
}

.footer-inner {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: -0.02em;
}

.brand-icon {
  font-size: 22px;
  filter: drop-shadow(0 2px 4px rgba(42, 157, 143, 0.2));
}

.footer-links {
  display: flex;
  gap: 12px;
  font-size: 14px;
  font-weight: 600;
}

.footer-links a {
  color: var(--color-text-secondary);
  transition: all var(--transition-fast);
  padding: 4px 8px;
  border-radius: 8px;
}

.footer-links a:hover {
  color: var(--color-primary);
  background: rgba(42, 157, 143, 0.08); /* 极淡的主题悬浮光晕 */
  transform: translateY(-1px);
}

.divider {
  color: rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
}

.footer-copy {
  margin-top: 8px;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-muted);
}
</style>
