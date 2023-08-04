;(function() {
  let name = "module_b.js"

  function method1() {
    console.log("hello iife from " + name)
  }

  window.moduleB = {
    method1: method1,
  }
})()
