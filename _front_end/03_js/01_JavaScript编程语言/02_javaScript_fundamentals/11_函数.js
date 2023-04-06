/**
 * 函数声明方式一
 * @param num1 参数一
 * @param num2 参数二
 */
function sum1(num1, num2) {
    return num1 + num2;
}

/**
 * 函数声明方式二
 * @param num1
 * @param num2
 * @returns {*}
 */
let sum2 = function (num1, num2) {
    return num1 + num2;
}

/**
 * 函数调用
 */
console.log(sum1(2, 3));
console.log(sum2(2, 3));


/**
 * arguments对象
 * @returns {number}
 */
function sum3() {
    let sum = 0;
    for (let i = 0; i < arguments.length; i++) {
        sum += arguments[i];
    }
    return sum;
}

console.log(aaa)
