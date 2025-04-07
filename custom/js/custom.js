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

/*
* 如果是本地使用默认地址,如果是在线使用cdn加速地址(存在缓存问题)
* */
const backupImgAddress = () => {
    const hostname = window.location.hostname;
    console.log(hostname)
    if (hostname.includes("luguosong")) {
        // 获取所有 img 元素
        const images = document.querySelectorAll('img');
        // 为每个 img 元素添加 error 事件监听器
        images.forEach(img => {
            img.src = img.src.replace(
                "https://raw.githubusercontent.com/luguosong/images/master",
                "https://gcore.jsdelivr.net/gh/luguosong/images@master")
        });

        const as = document.querySelectorAll(".glightbox");

        as.forEach(a => {
            a.href = a.href.replace(
                "https://raw.githubusercontent.com/luguosong/images/master",
                "https://gcore.jsdelivr.net/gh/luguosong/images@master")
        })
    }
}

// 页面切换时执行
document$.subscribe(async function () {
    backupImgAddress();
    // 自适应iframe高度
    adaptiveHeight();
})









