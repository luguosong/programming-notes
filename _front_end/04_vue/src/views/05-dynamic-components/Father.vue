<script setup>
import Home from "@/views/05-dynamic-components/Home.vue"
import Posts from "@/views/05-dynamic-components/Posts.vue"
import Archive from "@/views/05-dynamic-components/Archive.vue"
import { ref } from "vue"

const currentTab = ref('Home')

const tabs = {
  Home,
  Posts,
  Archive
}
</script>

<template>
  <button
    v-for="(_, tab) in tabs"
    :key="tab"
    :class="['tab-button', { active: currentTab === tab }]"
    @click="currentTab = tab">
    {{ tab }}
  </button>

  <component :is="tabs[currentTab]" class="tab"></component>
</template>

<style scoped>

</style>
