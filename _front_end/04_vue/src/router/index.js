import { createRouter, createWebHistory } from "vue-router"

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      name: "home",
      component: () => import("../views/Index.vue"),
    },
    {
      // 组件的创建和使用
      path: "/defining-and-using",
      name: "defining-and-using",
      component: () => import("../views/01-defining-and-using/Father.vue"),
    },
    {
      // 属性
      path: "/passing-props",
      name: "passing-props",
      component: () => import("../views/02-passing-props/Father.vue"),
    },
    {
      // 事件
      path: "/listening-to-events",
      name: "listening-to-events",
      component: () => import("../views/03-listening-to-events/Father.vue"),
    },
    {
      // 插槽
      path: "/slots",
      name: "slots",
      component: () => import("../views/04-slots/Father.vue"),
    },
    {
      // 动态组件
      path: "/dynamic-components",
      name: "dynamic-components",
      component: () => import("../views/05-dynamic-components/Father.vue"),
    },
  ],
})

export default router
