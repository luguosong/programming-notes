console.log("=== 数学运算符 ===")
console.log("加法：3 + 4 = " + (3 + 4)) //加法：3 + 4 = 7
console.log("减法：3 - 4 = " + (3 - 4)) //减法：3 - 4 = -1
console.log("乘法：3 * 4 = " + (3 * 4)) //乘法：3 * 4 = 12
console.log("除法：3 / 4 = " + (3 / 4)) //除法：3 / 4 = 0.75
console.log("取余：3 % 4 = " + (3 % 4)) //取余：3 % 4 = 3
console.log("求幂：3 ** 4 = " + (3 ** 4)) //求幂：3 ** 4 = 81

console.log("=== 自增/自减 ===");
let i = 1;
//前置递增运算符：i = 1,++i = 2,运算后的i：2
console.log("前置递增运算符：i = 1,++i = " + ++i + ",运算后的i：" + i);
i = 1;
//后置递增运算符：i = 1,i++ = 1,运算后的i：2
console.log("后置递增运算符：i = 1,i++ = " + i++ + ",运算后的i：" + i);
i = 1;
//前置递减运算符：i = 1,--i = 0,运算后的i：0
console.log("前置递减运算符：i = 1,--i = " + --i + ",运算后的i：" + i);
i = 1;
//后置递减运算符：i = 1,i-- = 1,运算后的i：0
console.log("后置递减运算符：i = 1,i-- = " + i-- + ",运算后的i：" + i);


console.log("=== 比较运算符 ===");
console.log("小于号：1 < 2 = " + (1 < 2)); //小于号：1 < 2 = true
console.log("大于号：1 > 2 = " + (1 > 2)); //大于号：1 > 2 = false
console.log("小于等于号：1 <= 2 = " + (1 <= 2)); //小于等于号：1 <= 2 = true
console.log("大于等于号：1 >= 2 = " + (1 >= 2)); //大于等于号：1 >= 2 = false
console.log("判等号：1 == 2 = " + (1 == 2)); //判等号：1 == 2 = false
console.log("不等号：1 != 2 = " + (1 != 2)); //不等号：1 != 2 = true
console.log("全等：1 === '1' = " + (1 === '1')); //全等：1 === '1' = false

console.log("=== 逻辑运算符 ===");
console.log("逻辑与运算符：true && false = " + (true && false)); //false
console.log("逻辑或运算符：true || false = " + (true || false)); //true
console.log("逻辑非运算符：!true = " + !true) //false

console.log("=== 赋值运算符 ===");
console.log("num = 10");
console.log("num += 10");
console.log("num -= 10");
console.log("num *= 10");
console.log("num /= 10");
