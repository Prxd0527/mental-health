import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

/* ============================================
   路由表
   ============================================ */
const routes = [
  // --- 公开页面 ---
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { title: '登录', guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { title: '注册', guest: true }
  },
  // --- 学生端（主布局） ---
  {
    path: '/',
    component: () => import('@/layouts/StudentLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/home/HomeView.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'articles',
        name: 'ArticleList',
        component: () => import('@/views/article/ArticleListView.vue'),
        meta: { title: '心理科普' }
      },
      {
        path: 'article/:id',
        name: 'ArticleDetail',
        component: () => import('@/views/article/ArticleDetailView.vue'),
        meta: { title: '文章详情' }
      },
      {
        path: 'treehole',
        name: 'TreeholeSquare',
        component: () => import('@/views/treehole/TreeholeSquare.vue'),
        meta: { title: '树洞广场' }
      },
      {
        path: 'treehole/:id',
        name: 'TreeholeDetail',
        component: () => import('@/views/treehole/TreeholeDetail.vue'),
        meta: { title: '树洞详情' }
      },
      {
        path: 'quiz',
        name: 'QuizList',
        component: () => import('@/views/quiz/QuizListView.vue'),
        meta: { title: '心理测评' }
      },
      {
        path: 'quiz/:id/do',
        name: 'QuizDo',
        component: () => import('@/views/quiz/QuizDoView.vue'),
        meta: { title: '答题', auth: true }
      },
      {
        path: 'quiz/result/:id',
        name: 'QuizResult',
        component: () => import('@/views/quiz/QuizResultView.vue'),
        meta: { title: '测评结果', auth: true }
      },
      {
        path: 'counselors',
        name: 'TeacherList',
        component: () => import('@/views/appointment/TeacherListView.vue'),
        meta: { title: '咨询师列表' }
      },
      {
        path: 'appointments',
        name: 'MyAppointments',
        component: () => import('@/views/appointment/MyAppointments.vue'),
        meta: { title: '我的预约', auth: true }
      },
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('@/views/chat/ChatView.vue'),
        meta: { title: '在线咨询', auth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/ProfileView.vue'),
        meta: { title: '个人中心', auth: true }
      },
      {
        path: 'mood',
        name: 'MoodCheckIn',
        component: () => import('@/views/profile/MoodView.vue'),
        meta: { title: '心情打卡', auth: true }
      }
    ]
  },
  // --- 教师端 ---
  {
    path: '/teacher',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { auth: true, roles: ['TEACHER', 'ADMIN'] },
    children: [
      {
        path: '',
        redirect: '/teacher/schedule'
      },
      {
        path: 'schedule',
        name: 'TeacherSchedule',
        component: () => import('@/views/teacher/ScheduleView.vue'),
        meta: { title: '排班管理' }
      },
      {
        path: 'approval',
        name: 'TeacherApproval',
        component: () => import('@/views/teacher/ApprovalView.vue'),
        meta: { title: '预约审批' }
      },
      {
        path: 'chat',
        name: 'TeacherChat',
        component: () => import('@/views/chat/ChatView.vue'),
        meta: { title: '在线咨询' }
      }
    ]
  },
  // --- 管理端 ---
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { auth: true, roles: ['ADMIN'] },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/DashboardView.vue'),
        meta: { title: '管理面板' }
      },
      {
        path: 'users',
        name: 'UserManage',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'articles',
        name: 'ArticleManage',
        component: () => import('@/views/admin/ArticleManage.vue'),
        meta: { title: '文章管理' }
      },
      {
        path: 'quizzes',
        name: 'QuizManage',
        component: () => import('@/views/admin/QuizManage.vue'),
        meta: { title: '题库管理' }
      },
      {
        path: 'reports',
        name: 'ReportManage',
        component: () => import('@/views/admin/ReportManage.vue'),
        meta: { title: '举报管理' }
      }
    ]
  },
  // --- 404 ---
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面未找到' }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

/* ============================================
   导航守卫
   ============================================ */
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title
    ? `${to.meta.title} - 心灵树洞`
    : '心灵树洞 - 大学生心理健康平台'

  const authStore = useAuthStore()

  // 需要登录但未登录
  if (to.meta.auth && !authStore.isLoggedIn) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  // 已登录用户访问登录/注册页 → 跳转首页
  if (to.meta.guest && authStore.isLoggedIn) {
    return next('/')
  }

  // 角色权限检查
  if (to.meta.roles && to.meta.roles.length > 0) {
    if (!to.meta.roles.includes(authStore.role)) {
      return next('/')
    }
  }

  next()
})

export default router
