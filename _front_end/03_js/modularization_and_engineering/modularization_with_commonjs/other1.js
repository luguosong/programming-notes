const name1='张三';

function sayHi1() {
    console.log(`hi,i am ${name1}`);
}

//使用module.exports导出时，可以直接导出一个对象，对象中可以包含多个方法
module.exports = {
    sayHi1
}
