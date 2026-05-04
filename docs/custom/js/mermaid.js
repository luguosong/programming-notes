// 修复 Zensical Mermaid 图表渲染
//
// 问题：Zensical bundle 将 <pre class="mermaid"><code>源码</code></pre>
// 转换为空的 <div class="mermaid"></div>，但 Mermaid 库没有渲染。
//
// 修复策略：
// 1. 在 bundle 处理前保存所有 Mermaid 源码
// 2. 页面加载/导航后，找到空的 div.mermaid 并渲染
(function () {
    var sources = [];
    var initialized = false;

    function initMermaid() {
        if (initialized || typeof mermaid === "undefined") return false;
        mermaid.initialize({
            startOnLoad: false,
            themeCSS: [
                ".node circle,.node ellipse,.node path,.node polygon,.node rect{fill:var(--md-mermaid-node-bg-color);stroke:var(--md-mermaid-node-fg-color)}",
                "marker{fill:var(--md-mermaid-edge-color)!important}",
                ".edgeLabel .label rect{fill:#0000}",
                ".flowchartTitleText,.classDiagramTitleText,.statediagramTitleText{fill:var(--md-mermaid-label-fg-color)}",
                ".label{color:var(--md-mermaid-label-fg-color);font-family:var(--md-mermaid-font-family)}",
                ".label foreignObject{line-height:normal;overflow:visible}",
                ".label div .edgeLabel{color:var(--md-mermaid-label-fg-color)}",
                ".edgeLabel,.edgeLabel p,.label div .edgeLabel{background-color:var(--md-mermaid-label-bg-color)}",
                ".edgeLabel,.edgeLabel p{fill:var(--md-mermaid-label-bg-color);color:var(--md-mermaid-edge-color)}",
                ".edgePath .path,.flowchart-link{stroke:var(--md-mermaid-edge-color)}",
                ".edgePath .arrowheadPath{fill:var(--md-mermaid-edge-color)}",
                ".cluster rect{fill:var(--md-mermaid-cluster-bg-color);stroke:var(--md-mermaid-cluster-border-color)}",
                ".cluster-label span{color:var(--md-mermaid-label-fg-color);font-family:var(--md-mermaid-font-family)}",
                "g.classGroup line,g.classGroup rect{fill:var(--md-mermaid-node-bg-color);stroke:var(--md-mermaid-node-fg-color)}",
                "g.classGroup text{fill:var(--md-mermaid-label-fg-color);font-family:var(--md-mermaid-font-family)}",
                ".classLabel .box{fill:var(--md-mermaid-label-bg-color);background-color:var(--md-mermaid-label-bg-color);opacity:1}",
                ".classLabel .label{fill:var(--md-mermaid-label-fg-color);font-family:var(--md-mermaid-font-family)}",
                ".node .divider{stroke:var(--md-mermaid-node-fg-color)}",
                ".relation{stroke:var(--md-mermaid-edge-color)}",
                ".cardinality{fill:var(--md-mermaid-label-fg-color);font-family:var(--md-mermaid-font-family)}",
                "g.stateGroup rect{fill:var(--md-mermaid-node-bg-color);stroke:var(--md-mermaid-node-fg-color)}",
                "g.stateGroup .state-title{fill:var(--md-mermaid-label-fg-color)!important;font-family:var(--md-mermaid-font-family)}",
                ".nodeLabel,.nodeLabel p{color:var(--md-mermaid-label-fg-color);font-family:var(--md-mermaid-font-family)}",
                "a .nodeLabel{text-decoration:underline}",
                ".node circle.state-end,.node circle.state-start,.start-state{fill:var(--md-mermaid-edge-color);stroke:none}",
                ".end-state-inner,.end-state-outer{fill:var(--md-mermaid-edge-color)}",
                ".end-state-inner,.node circle.state-end{stroke:var(--md-mermaid-label-bg-color)}",
                ".transition{stroke:var(--md-mermaid-edge-color)}",
                ".actor{fill:var(--md-mermaid-sequence-actor-bg-color);stroke:var(--md-mermaid-sequence-actor-border-color)}",
                "text.actor>tspan{fill:var(--md-mermaid-sequence-actor-fg-color);font-family:var(--md-mermaid-font-family)}",
                "line{stroke:var(--md-mermaid-sequence-actor-line-color)}",
                ".actor-man circle,.actor-man line{fill:var(--md-mermaid-sequence-actorman-bg-color);stroke:var(--md-mermaid-sequence-actorman-line-color)}",
                ".messageLine0,.messageLine1{stroke:var(--md-mermaid-sequence-message-line-color)}",
                ".note{fill:var(--md-mermaid-sequence-note-bg-color);stroke:var(--md-mermaid-sequence-note-border-color)}",
                ".loopText,.loopText>tspan,.messageText,.noteText>tspan{stroke:none;font-family:var(--md-mermaid-font-family)!important}",
                ".messageText{fill:var(--md-mermaid-sequence-message-fg-color)}",
                ".loopText,.loopText>tspan{fill:var(--md-mermaid-sequence-loop-fg-color)}",
                ".noteText>tspan{fill:var(--md-mermaid-sequence-note-fg-color)}",
                "#arrowhead path{fill:var(--md-mermaid-sequence-message-line-color);stroke:none}",
                ".loopLine{fill:var(--md-mermaid-sequence-loop-bg-color);stroke:var(--md-mermaid-sequence-loop-border-color)}",
                ".labelBox{fill:var(--md-mermaid-sequence-label-bg-color);stroke:none}",
                ".labelText,.labelText>span{fill:var(--md-mermaid-sequence-label-fg-color)}"
            ].join(""),
            sequence: {
                actorFontSize: "16px",
                messageFontSize: "16px",
                noteFontSize: "16px"
            }
        });
        initialized = true;
        return true;
    }

    // 从页面原始 HTML 中提取 Mermaid 源码
    // bundle 在本脚本之前加载，已经将 <pre class="mermaid"><code> 转为空的 <div class="mermaid">
    // 需要从服务器获取原始 HTML 来解析源码
    function captureSourcesFromHTML(html) {
        var parser = new DOMParser();
        var doc = parser.parseFromString(html, "text/html");
        sources = [];
        doc.querySelectorAll("pre.mermaid code").forEach(function (code) {
            sources.push(code.textContent);
        });
        return sources.length;
    }

    function captureSources() {
        // 先尝试从 DOM 中获取（SPA 导航后可能 bundle 尚未处理）
        var preElements = document.querySelectorAll("pre.mermaid code");
        if (preElements.length > 0) {
            sources = [];
            preElements.forEach(function (code) {
                sources.push(code.textContent);
            });
            return Promise.resolve(sources.length);
        }

        // DOM 中已无 pre.mermaid，从服务器获取原始 HTML
        return fetch(window.location.href)
            .then(function (r) { return r.text(); })
            .then(function (html) {
                return captureSourcesFromHTML(html);
            })
            .catch(function () {
                return 0;
            });
    }

    // 渲染空的 div.mermaid 元素
    // bundle 会创建 closed shadow root，无法从外部访问
    // 策略：用全新的 div 替换原元素，直接插入 SVG（不使用 shadow DOM）
    var renderIndex = 0;
    function renderEmptyDivs() {
        if (!initMermaid()) return;
        var counter = 0;
        document.querySelectorAll("div.mermaid").forEach(function (div) {
            if (div.querySelector("svg")) return;

            var src = sources[counter];
            if (!src) return;

            var id = "__mermaid_" + renderIndex + "_" + counter;
            mermaid.render(id, src).then(function (result) {
                // 创建新 div 替换原有元素（绕过 closed shadow root）
                var newDiv = document.createElement("div");
                newDiv.className = "mermaid";
                newDiv.innerHTML = result.svg;
                div.parentNode.replaceChild(newDiv, div);
                if (result.fn) result.fn(newDiv);
            }).catch(function (err) {
                console.warn("Mermaid 渲染失败:", err);
            });
            counter++;
        });
        renderIndex++;
    }

    // 立即同步尝试捕获源码
    // 如果 DOM 中仍有 pre.mermaid（bundle 尚未处理），同步捕获成功
    // 否则 captureSources 返回 Promise，需要异步处理
    captureSources().then(function () {
        // 延迟渲染：等待 bundle 处理完毕后再渲染空的 div.mermaid
        if (document.readyState === "loading") {
            document.addEventListener("DOMContentLoaded", function () {
                setTimeout(renderEmptyDivs, 500);
            });
        } else {
            setTimeout(renderEmptyDivs, 500);
        }
    });

    // SPA 导航：重新捕获并渲染
    if (typeof document$ !== "undefined") {
        document$.subscribe(function () {
            setTimeout(function () {
                captureSources().then(function () {
                    renderEmptyDivs();
                });
            }, 300);
        });
    }
})();
