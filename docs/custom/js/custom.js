/*
* 当cdn.jsdelivr.net无效时，自动切换为其它域名
* */
const setImageDomain = (document) => {
    "use strict"
    let fastNode
    let failed
    let isRunning
    const DEST_LIST = [
        "cdn.jsdelivr.net",
        "fastly.jsdelivr.net",
        "gcore.jsdelivr.net",
    ]
    const PREFIX = "//"
    const SOURCE = DEST_LIST[0]
    const starTime = Date.now()
    const TIMEOUT = 2000
    const STORE_KEY = "jsdelivr-auto-fallback"
    const TEST_PATH =
        "/gh/luguosong/images@master/jsdelivr-auto-fallback/test.css?"
    const shouldReplace = (text) => text && text.includes(PREFIX + SOURCE)
    const replace = (text) => text.replace(PREFIX + SOURCE, PREFIX + fastNode)
    const setTimeout = window.setTimeout
    const $ = document.querySelectorAll.bind(document)

    const replaceElementSrc = () => {
        let element
        let value
        for (element of $('link[rel="stylesheet"]')) {
            value = element.href
            if (shouldReplace(value) && !value.includes(TEST_PATH)) {
                element.href = replace(value)
            }
        }

        for (element of $("script")) {
            value = element.src
            if (shouldReplace(value)) {
                const newNode = document.createElement("script")
                newNode.src = replace(value)
                element.defer = true
                element.src = ""
                element.before(newNode)
                element.remove()
            }
        }

        for (element of $("img")) {
            value = element.src
            if (shouldReplace(value)) {
                // Used to cancel loading. Without this line it will remain pending status.
                element.src = ""
                element.src = replace(value)
            }
        }

        // All elements that have a style attribute
        for (element of $("*[style]")) {
            value = element.getAttribute("style")
            if (shouldReplace(value)) {
                element.setAttribute("style", replace(value))
            }
        }

        for (element of $("style")) {
            value = element.innerHTML
            if (shouldReplace(value)) {
                element.innerHTML = replace(value)
            }
        }
    }

    const tryReplace = () => {
        if (!isRunning && failed && fastNode) {
            console.warn(SOURCE + " is not available. Use " + fastNode)
            isRunning = true
            setTimeout(replaceElementSrc, 0)
            // Some need to wait for a while
            setTimeout(replaceElementSrc, 20)
            // Replace dynamically added elements
            setInterval(replaceElementSrc, 500)
        }
    }

    //  检查可用性
    const checkAvailable = (url, callback) => {
        let timeoutId
        const newNode = document.createElement("link")
        const handleResult = (isSuccess) => {
            if (!timeoutId) {
                return
            }

            clearTimeout(timeoutId)
            timeoutId = 0
            // Used to cancel loading. Without this line it will remain pending status.
            if (!isSuccess) newNode.href = "data:text/plain;base64,"
            newNode.remove()
            callback(isSuccess)
        }

        timeoutId = setTimeout(handleResult, TIMEOUT)

        newNode.addEventListener("error", () => handleResult(false))
        newNode.addEventListener("load", () => handleResult(true))
        newNode.rel = "stylesheet"
        newNode.text = "text/css"
        newNode.href = url + TEST_PATH + starTime
        document.head.insertAdjacentElement("afterbegin", newNode)
    }

    const cached = (() => {
        try {
            return Object.assign(
                {},
                JSON.parse(localStorage.getItem(STORE_KEY) || "{}")
            )
        } catch {
            return {}
        }
    })()

    const main = () => {
        cached.time = starTime
        cached.failed = false
        cached.fastNode = null

        for (const url of DEST_LIST) {
            checkAvailable("https://" + url, (isAvailable) => {
                // console.log(url, Date.now() - starTime, Boolean(isAvailable));
                if (!isAvailable && url === SOURCE) {
                    failed = true
                    cached.failed = true
                }

                if (isAvailable && !fastNode) {
                    fastNode = url
                }

                if (isAvailable && !cached.fastNode) {
                    cached.fastNode = url
                }

                tryReplace()
            })
        }

        setTimeout(() => {
            // If all domains are timeout
            if (failed && !fastNode) {
                fastNode = DEST_LIST[1]
                tryReplace()
            }

            localStorage.setItem(STORE_KEY, JSON.stringify(cached))
        }, TIMEOUT + 100)
    }

    if (
        cached.time &&
        starTime - cached.time < 60 * 60 * 1000 &&
        cached.failed &&
        cached.fastNode
    ) {
        failed = true
        fastNode = cached.fastNode
        tryReplace()
        setTimeout(main, 1000)
    } else {
        main()
    }
}

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
document$.subscribe(function () {
    setImageDomain(document);
    adaptiveHeight();
})







