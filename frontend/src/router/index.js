import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../views/Layout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: Layout,
    redirect: to => {
      const role = localStorage.getItem('role')
      return role === 'student' ? '/courses' : '/students'
    },
    children: [
      {
        path: 'students',
        name: 'Students',
        component: () => import('../views/Student.vue'),
        meta: { title: '学生管理', roles: ['admin'] }
      },
      {
        path: 'majors',
        name: 'Majors',
        component: () => import('../views/Major.vue'),
        meta: { title: '专业管理', roles: ['admin'] }
      },
      {
        path: 'courses',
        name: 'Courses',
        component: () => import('../views/Course.vue'),
        meta: { title: '课程管理', roles: ['admin', 'student'] }
      },
      {
        path: 'selections',
        name: 'Selections',
        component: () => import('../views/Selection.vue'),
        meta: { title: '选课管理', roles: ['admin', 'student'] }
      },
      {
        path: 'stats',
        name: 'Stats',
        component: () => import('../views/Stats.vue'),
        meta: { title: '统计报表', roles: ['admin'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.meta.roles && !to.meta.roles.includes(role)) {
    // If user has no permission for this route
    if (role === 'student') {
      next('/courses')
    } else {
      next('/students')
    }
  } else {
    next()
  }
})

export default router
