function Range(from, to) {
    this.from = from
    this.to = to
}

Range.prototype = {

    constructor: Range,

    includes: function(x) {
        return this.from <= x && x <= this.to
    },

    [Symbol.iterator]: function* () {
        for (let x = Math.ceil(this.from); x <= this.to; x++) yield x
    },

    toString: function() {
        return "(" + this.from + "..." + this.to + ")"
    },
}

// 这是我们子类的构造函数
function Span(start, span) {
    if (span >= 0) {
        this.from = start;
        this.to = start + span;
    } else {
        this.to = start;
        this.from = start + span;
    }
}

//  确保 Span 原型继承自 Range 原型
Span.prototype = Object.create(Range.prototype);

// 我们不希望继承 Range.prototype.constructor，所以我们定义自己的 constructor 属性。
Span.prototype.constructor = Span;

// By defining its own toString() method, Span overrides the
// toString() method that it would otherwise inherit from Range.
Span.prototype.toString = function() {
    return `(${this.from}... +${this.to - this.from})`;
};
