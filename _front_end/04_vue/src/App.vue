<script setup>

</script>

<template>
  <el-container>
    <el-aside>
      <h1>Vue</h1>
      <h2>基础</h2>
      <div>
        <router-link to="/defining-and-using">组件的创建和使用</router-link>
      </div>
      <div>
        <router-link to="/passing-props">传递props</router-link>
      </div>
      <div>
        <router-link to="/listening-to-events">监听事件</router-link>
      </div>
      <div>
        <router-link to="/slots">插槽</router-link>
      </div>
      <div>
        <router-link to="/dynamic-components">动态组件</router-link>
      </div>
    </el-aside>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<style scoped>

</style>
