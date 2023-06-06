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
      // 组件的创建和使用
      path: "/passing-props",
      name: "passing-props",
      component: () => import("../views/02-passing-props/Father.vue"),
    },
    {
      // 组件的创建和使用
      path: "/listening-to-events",
      name: "listening-to-events",
      component: () => import("../views/03-listening-to-events/Father.vue"),
    },
    {
      // 组件的创建和使用
      path: "/slots",
      name: "slots",
      component: () => import("../views/04-slots/Father.vue"),
    },
  ],
})

export default router
