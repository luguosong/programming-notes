<script setup>
/*
* 通过defineProps宏定义一个props
*
* defineProps不需要显式地导入
* */
defineProps(["title"])
</script>

<template>
  <h4>BlogPost.vue子组件，标题：{{ title }}</h4>
</template>

<style scoped>

</style>
