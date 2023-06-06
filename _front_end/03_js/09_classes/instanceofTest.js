// 当
function C() {}

function D() {}
D.prototype = Object.create(C.prototype);

var o = new D();

console.log(o instanceof C); // true
console.log(o instanceof D); // true
console.log(o instanceof Object); // true
