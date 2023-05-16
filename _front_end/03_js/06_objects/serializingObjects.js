// 定义一个 JavaScript 对象
const person = {
  name: 'Alice',
  age: 30,
  hobbies: ['reading', 'painting'],
  number: NaN,
  positiveInfinity: Infinity,
  address: undefined
};

// 使用 JSON.stringify() 将对象转换为 JSON 字符串
const jsonString = JSON.stringify(person);

console.log(jsonString);
// 输出结果: {"name":"Alice","age":30,"hobbies":["reading","painting"],"number":null,"positiveInfinity":null}

// 使用 JSON.parse() 将 JSON 字符串转换为 JavaScript 对象
const parsedObject = JSON.parse(jsonString);

console.log(parsedObject);
/*
输出结果:
{
  name: 'Alice',
  age: 30,
  hobbies: [ 'reading', 'painting' ],
  number: null,
  positiveInfinity: null
}
* */
