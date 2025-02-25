import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/redirect/:path(.*)*',
      name: 'Redirect',
      redirect: to => {
        const path = to.params.path
        return { path: `/${Array.isArray(path) ? path.join('/') : path}`, query: to.query, replace: true }
      }
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/index.vue'),
      meta: { title: '登录' }
    },
    {
      path: '',
      component: Layout,
      redirect: '',
      children: [
        {
          path: '',
          name: 'Dashboard',
          component: () => import('@/views/dashboard/index.vue'),
          meta: { title: '工作台', showTabbar: true }
        },
        {
          path: '/crud',
          name: 'Crud',
          component: () => import('@/views/crud/index.vue'),
          meta: { title: '示例', showTabbar: true }
        },
        {
          path: '/list',
          name: 'ListExample',
          component: () => import('@/views/crud/example/List.vue'),
          meta: { title: '列表示例' }
        },
        {
          path: '/form',
          name: 'FormExample',
          component: () => import('@/views/crud/example/Form.vue'),
          meta: { title: '表单示例' }
        },
        {
          path: '/detail',
          name: 'DetailExample',
          component: () => import('@/views/crud/example/Detail.vue'),
          meta: { title: '详情示例' }
        }
        // 业务 相关页面 (相对路由)
      ]
    },
    {
      path: '/mine',
      component: Layout,
      children: [
        {
          path: '',
          name: 'Mine',
          component: () => import('@/views/mine/index.vue'),
          meta: { title: '我的', showTabbar: true }
        }
        // 我的 相关页面 (相对路由)
      ]
    }
  ]
})

export default router
