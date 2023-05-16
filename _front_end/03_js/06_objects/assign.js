// 定义目标对象
const target = {
  name: 'Alice',
  age: 30
};

// 定义源对象
const source1 = {
  age: 25,
  city: 'New York'
};

const source2 = {
  occupation: 'Engineer',
  hobbies: ['reading', 'painting']
};

// 合并源对象的属性到目标对象
const mergedObject = Object.assign(target, source1, source2);

// 输出合并后的对象
console.log(mergedObject);


/*
输出结果：
{
  name: 'Alice',
  age: 25,
  city: 'New York',
  occupation: 'Engineer',
  hobbies: [ 'reading', 'painting' ]
}
* */


/*
* 使用扩展操作符完成以上功能
* */

// 合并源对象的属性到目标对象
const mergedObject2 = { ...target, ...source1, ...source2 };

// 输出合并后的对象
console.log(mergedObject2);
