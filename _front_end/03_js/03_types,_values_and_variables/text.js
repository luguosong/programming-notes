//码点超出16位的Unicode字符使用UTF-16规则编码为两个16位值的序列
console.log("中".length) //1
console.log("🤡".length) //2

/*
* 字符串字面量
* */
console.log("") //空字符串
console.log("testing") //单引号字符串
console.log("3.14") //双引号字符串
console.log("name=\"form\"")
console.log("one \
long \
line") //换行符

/*
* 字符串字面量中的转义序列
* */
console.log("\u03c0")


/*
* 字符串拼接
* */
console.log("hello " + "world")

/*
* 字符串属性
* */
// 获取字符串的一部分
console.log("hello,world".length) //11,获取字符串长度
console.log("hello,world".substring(6, 11)) //world,获取子字符串
console.log("hello,world".slice(6, 11)) //world,获取子字符串
console.log("hello,world".split(",")) //["hello","world"],将字符串拆分为字符串数组
//搜索字符串
console.log("hello,world".indexOf("l")) //2,返回字符串中第一个匹配子串的位置
console.log("hello,world".indexOf("l", 3)) //3,位置3后面第一个"1"的位置
console.log("hello,world".indexOf("zz")) //-1,未找到返回-1
console.log("hello,world".lastIndexOf("l")) //9,返回字符串中最后一个匹配子串的位置
//ES6及之后版本中的布尔值搜索函数
console.log("hello,world".startsWith("hell")) //true,字符串是否以指定子串开头
console.log("hello,world".endsWith("!")) //false,字符串是否以指定子串结尾
console.log("hello,world".includes("o")) //true,字符串是否包含指定子串
//创建字符串的修改版本
console.log("hello,world".replace("llo", "ya")) //heya,world,字符串中第一个匹配子串被替换
console.log("hello,world".toUpperCase()) //HELLO,WORLD,字符串中的字母被转换为大写
console.log("Hello,world".toLowerCase()) //hello,world,字符串中的字母被转换为小写
console.log("hello,world".normalize()) //Unicode NFC 归一化：ES6 新增
console.log("hello,world".normalize("NFD")) //Unicode NFD 归一化：ES6 新增
///访问字符串中的个别（16位值）字符
console.log("hello,world".charAt(0)) //h,返回指定位置的字符
console.log("hello,world".charCodeAt(0)) //104：指定位置的16位数值
console.log("hello,world".codePointAt(0)) //104:ES6,适用于码点大于16位的情形
//E52017新增的字符串填充函数
console.log("x".padStart(3)) //"  x"，在字符串前面填充空格
console.log("x".padEnd(3)) //"x  "，在字符串后面填充空格
console.log("x".padStart(3, "*")) //"**x"，在字符串前面填充指定字符
console.log("x".padEnd(3, "-")) //"x--"，在字符串后面填充指定字符
///删除空格函数。trim是ES5就有的，其他是ES2019增加的
console.log("  hello,world  ".trim()) //"hello,world"，删除字符串前后的空格
console.log("  hello,world  ".trimStart()) //"hello,world  "，删除字符串前面的空格
console.log("  hello,world  ".trimEnd()) //"  hello,world"，删除字符串后面的空格
//未分类字符串方法
console.log("hello,world".concat("!")) //"Hello, world!"：可以用+操作符代替
console.log("hello,world ".repeat(5)) //"hello,world hello,world hello,world hello,world hello,world "：重复字符串

/*
* 模板字面量
* */
let name="Bill";
console.log(`Hello, ${name}.`) //Hello, Bill.
console.log(`${10>20}`) //false
console.log(String.raw`\n`) //\n
