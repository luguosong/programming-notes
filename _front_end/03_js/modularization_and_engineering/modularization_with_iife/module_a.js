;(function () {
  let name = "module_a.js"

  function method1() {
    console.log("hello iife from " + name)
  }

  window.moduleA = {
    method1: method1,
  }
})()
