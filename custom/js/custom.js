/*
* 自适应iframe高度
* */
const adaptiveHeight = () => {
    // 获取所有的 iframe 元素
    var iframes = document.querySelectorAll('iframe');

// 遍历所有的 iframe 元素并添加加载事件监听器
    iframes.forEach(function (iframe) {
        iframe.addEventListener('load', function () {
            // 排除评论区
            if (iframe.className !== "giscus-frame") {
                // 当 iframe 加载完成时执行的代码
                const bHeight = iframe.contentWindow.document.body.scrollHeight;
                const dHeight = iframe.contentWindow.document.documentElement.scrollHeight;
                iframe.height = Math.max(bHeight, dHeight) + 10 + "px";
            }
        });
    });
}

// 页面切换时执行
document$.subscribe(async function () {
    // 自适应iframe高度
    adaptiveHeight();
})









