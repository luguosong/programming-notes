<script setup>

import Child from "@/views/01-defining-and-using/Child.vue"
</script>

<template>
  <h1>组件的创建和使用</h1>
  <p>你会注意到，每当点击这些按钮时，每一个组件都维护着自己的状态，是不同的
    count。这是因为每当你使用一个组件，就创建了一个新的实例。</p>
  <child></child>
  <child></child>
  <child></child>
  <child></child>
</template>

<style scoped>

</style>
