<script setup>
import Child from "@/views/03-listening-to-events/Child.vue"

function method1(){
  alert("父组件接收到事件")
}
</script>

<template>
<child @event1="method1"/>
</template>

<style scoped>

</style>
