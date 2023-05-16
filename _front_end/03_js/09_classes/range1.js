// 这是一个工厂函数，返回一个新的范围对象。
function range(from, to) {
    // 使用 Object.create() 创建一个继承自下面定义的原型对象的对象。
    // 原型对象作为该函数的属性存储，并为所有范围对象定义共享的方法（行为）。
    let r = Object.create(range.methods);

    // 存储这个新范围对象的起始点和结束点（状态）。
    // 这些是非继承的属性，是该对象特有的。
    r.from = from;
    r.to = to;

    // 最后返回新对象
    return r;
}

// 这个原型对象定义了所有范围对象继承的方法。
//达到复用的效果
range.methods = {
    // 如果 x 在范围内，则返回 true，否则返回 false。
    // 这个方法适用于文本、日期范围以及数值范围。
    includes(x) { return this.from <= x && x <= this.to; },

    // 一个生成器函数，用于使类的实例可迭代。
    // 注意，它仅适用于数值范围。
    *[Symbol.iterator]() {
        for(let x = Math.ceil(this.from); x <= this.to; x++) yield x;
    },

    // 返回范围的字符串表示形式
    toString() { return "(" + this.from + "..." + this.to + ")"; }
};


/*
* 测试
* */
let r = range(1,3); //创建一个范围对象
console.log(r.includes(2)); // => true: 2 在这个范围内
console.log(r.toString()); // => "(1...3)"
console.log([...r]); // => [1, 2, 3]; 通过迭代器迭代这个范围对象
