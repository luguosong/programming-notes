console.log("整数字面量:")
console.log(0)
console.log(3)
console.log(100000000)
//十六进制整数字面量
console.log(0xff) //255
console.log(0XBADCAFE) //195939070
console.log(0b10101) //21
console.log(0o377) //255


console.log("浮点数字面量:")
console.log(3.14)
console.log(2345.6789)
console.log(.333333333)
//指数计数法表示浮点数
console.log(6.02e23) //6.02*10^23
console.log(1.4738223E-32) //1.4738223*10^-32

console.log("算术运算符:")
console.log(10 + 10)
console.log(10 - 10)
console.log(10 * 10)
console.log(10 / 10)
console.log(10 % 3) //取余
console.log(2 ** 3) //2的3次方

//Math对象
console.log(Math.pow(2, 53)) //2的53次方
console.log(Math.round(.6)) //四舍五入
console.log(Math.ceil(.6)) //向上取整
console.log(Math.floor(.6)) //向下取整
console.log(Math.abs(-5)) //绝对值
console.log(Math.max(1, 2, 3, 4, 5)) //最大值
console.log(Math.min(1, 2, 3, 4, 5)) //最小值
console.log(Math.random()) //0-1之间的随机数
console.log(Math.PI) //π
console.log(Math.E) //自然对数的底数e
console.log(Math.sqrt(3)) //3的平方根
console.log(Math.pow(3, 1 / 3)) //3的立方根
console.log(Math.sin(0)) //三角函数
console.log(Math.log(10)) //10的自然对数
console.log(Math.log(100) / Math.LN10) //以10为底100的对数
console.log(Math.log(512) / Math.LN2) //以2为底512的对数
console.log(Math.exp(3)) //e的3次方
console.log(Math.cbrt(27)) //27的立方根
console.log(Math.hypot(3, 4)) //3,4的平方和的平方根
console.log(Math.log10(100)) //以10为底100的对数
console.log(Math.log2(1024)) //以2为底1024的对数
console.log(Math.log1p(1)) //以e为底1+1的对数
console.log(Math.expm1(1)) //e的1次方-1
console.log(Math.sign(0)) //判断正负
console.log(Math.imul(2, 3)) //优化的32位整数乘法
console.log(Math.clz32(0xf)) //32位整数中前导0的位数
console.log(Math.trunc(3.1)) //去除小数部分
console.log(Math.fround(3.14)) //最接近32位单精度浮点数表示的32位整数
console.log(Math.sinh(0)) //双曲正弦函数
console.log(Math.asinh(0)) //双曲反正弦函数

console.log("正无穷和负无穷以及NaN：")
//Infinity
console.log(Infinity)
console.log(Number.POSITIVE_INFINITY)
console.log(1 / 0)
console.log(Number.MAX_VALUE * 2)
//-Infinity
console.log(-Infinity)
console.log(Number.NEGATIVE_INFINITY)
console.log(-1 / 0)
console.log(-Number.MAX_VALUE * 2)

//NaN,Not a Number
console.log(NaN)
console.log(Number.NaN)
console.log(0 / 0)
console.log(Infinity / Infinity)

console.log(Number.MIN_VALUE / 2) //0
console.log(-Number.MIN_VALUE / 2) //-0
console.log(-1 / Infinity) //-0
console.log(-0)

//ES6定义了下列Number属性
console.log(Number.parseInt("3")) //3
console.log(Number.parseFloat("3.14")) //3.14
console.log(Number.isNaN(NaN)) //true
console.log(Number.isFinite(Infinity)) //false,判断是否是有限数
console.log(Number.isFinite(100000)) //true,判断是否是有限数
console.log(Number.isInteger(100)) //true,判断是否是整数
console.log(Number.isSafeInteger(100)) //true,判断是否是安全整数
console.log(Number.MIN_SAFE_INTEGER) //-2^53+1
console.log(Number.MAX_SAFE_INTEGER) //2^53-1
console.log(Number.EPSILON) //最小精度

//舍入错误
let x = .3 - .2
let y = .2 - .1 //舍入误差相互抵消
console.log("x==y:" + (x === y))
console.log(x === .1) //false
console.log(y === .1) //true

console.log("BigInt:")
console.log(1234n)
console.log(0b1111n)
console.log(0o777n)
console.log(0x123n)
console.log(Number.MAX_SAFE_INTEGER) //9007199254740991
console.log(BigInt(Number.MAX_SAFE_INTEGER)) //9007199254740991n
console.log(BigInt("1"+"0".repeat(100)))

//日期和时间
console.log(Date.now()) //获取当前时间戳
console.log(new Date()) //获取当前时间
console.log(new Date().getTime()) //获取当前时间戳
console.log(new Date().toISOString()) //转化为标准格式的字符串


aaaaaaa
