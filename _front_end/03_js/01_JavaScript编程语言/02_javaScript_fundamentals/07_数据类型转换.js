// 字符串转换
let value = true;
console.log(typeof value);
value=String(value);
console.log(typeof value);

// 数字类型转换
let str = "123";
console.log(typeof str); // string
let num = Number(str); // 变成 number 类型 123
console.log(typeof num); // number
console.log(parseInt("123.1"));
console.log(parseFloat("123.1"));

