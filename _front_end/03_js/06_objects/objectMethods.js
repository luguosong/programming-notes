// 声明一个 person 对象
const person = { name: 'Alice', age: 30 };

// 使用 Object.prototype.toString()
// 返回对象的字符串表示，输出结果: [object Object]
// 想要展示有用信息，需要对toString方法进行重写
console.log(person.toString());

// 使用 Object.prototype.hasOwnProperty()
console.log(person.hasOwnProperty('name')); // 检查对象是否具有指定的属性，输出结果: true
console.log(person.hasOwnProperty('toString')); // 检查对象是否具有指定的属性，输出结果: false

// 使用 Object.prototype.isPrototypeOf()
const obj = Object.create(person);
console.log(person.isPrototypeOf(obj)); // 检查对象是否是另一个对象的原型，输出结果: true
console.log(Object.prototype.isPrototypeOf(person)); // 检查对象是否是另一个对象的原型，输出结果: false

// 使用 Object.prototype.valueOf()
console.log(person.valueOf()); // 返回对象的原始值，输出结果: { name: 'Alice', age: 30 }

// 使用 Object.prototype.toLocaleString()
console.log(person.toLocaleString()); // 返回对象的本地化字符串表示，输出结果: [object Object]
