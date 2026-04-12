// 修复 Zensical 即时导航后 Mermaid 图表无法渲染的问题
//
// 根因：Zensical bundle ($s 函数) 使用 ko||fp().pipe(se(1)) 缓存模式。
// ko 在首次渲染后 take(1) 已完成，后续 SPA 导航的新订阅者收不到值。
// 同时 $s() 同步移除 mermaid 类名，导致渲染失败后元素无法被外部识别。
//
// 修复策略：
// 1. 预加载 Mermaid 库（通过 extra_javascript），避免 bundle 从 unpkg 懒加载
// 2. 监听 document$，在页面切换后查找渲染失败的 Mermaid 元素并重新渲染
// 3. 使用 Mermaid v11 的 render API + Shadow DOM，保持与 bundle 一致的样式
(function () {
    var initialized = false;
    var renderCounter = 0;

    // Mermaid 主题 CSS（与 Zensical bundle 中的 themeCSS 保持一致）
    var themeCSS = [
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
    ].join("");

    // 初始化 Mermaid（仅执行一次）
    function initMermaid() {
        if (initialized || typeof mermaid === "undefined") return false;
        mermaid.initialize({
            startOnLoad: false,
            themeCSS: themeCSS,
            sequence: {
                actorFontSize: "16px",
                messageFontSize: "16px",
                noteFontSize: "16px"
            }
        });
        initialized = true;
        return true;
    }

    // 渲染单个 Mermaid pre 元素（复刻 bundle 的 Shadow DOM 方式）
    function renderElement(pre) {
        if (!initMermaid()) return;

        var id = "__mermaid_fix_" + renderCounter++;
        var container = document.createElement("div");
        container.className = "mermaid";
        var source = pre.textContent;

        mermaid.render(id, source).then(function (result) {
            var shadow = container.attachShadow({ mode: "closed" });
            shadow.innerHTML = result.svg;
            pre.replaceWith(container);
            if (result.fn) result.fn(shadow);
        }).catch(function (err) {
            console.warn("Mermaid 渲染失败:", err);
        });
    }

    // 检测并渲染所有未成功渲染的 Mermaid 元素
    function processPage() {
        if (typeof mermaid === "undefined") return;

        // 情况 1：bundle 的 $s() 成功渲染的元素已被替换为 div.mermaid（含 Shadow DOM）
        // 情况 2：bundle 的 $s() 失败，元素变为 <pre>（无 mermaid 类名），需要我们处理

        // 查找内容区域中所有 pre 元素
        var content = document.querySelector("[data-md-component='content']");
        if (!content) return;

        // 查找所有 pre 元素中包含 Mermaid 语法的（bundle 已移除 mermaid 类名但未渲染）
        var pres = content.querySelectorAll("pre");
        pres.forEach(function (pre) {
            // 跳过已处理的（有 code 子元素的代码块）
            if (pre.querySelector("code")) return;
            // 跳过已有 mermaid 类名的（bundle 可能还没处理）
            if (pre.classList.contains("mermaid")) return;

            var text = pre.textContent.trim();
            if (!text) return;

            // 检测 Mermaid 语法关键字
            if (/^(graph |flowchart |sequenceDiagram|classDiagram|stateDiagram|erDiagram|gantt|pie|gitGraph|journey |mindmap|timeline|quadrantChart|sankey|xychart|block|architecture )/i.test(text)) {
                renderElement(pre);
            }
        });
    }

    // 监听页面切换
    if (typeof document$ !== "undefined") {
        document$.subscribe(function () {
            // 延迟处理：等待 Zensical bundle 的 RxJS 管道执行完毕
            // bundle 的 $s() 同步移除类名但异步渲染（可能失败）
            // 我们在下一个宏任务中查找并修复渲染失败的元素
            setTimeout(processPage, 300);
        });
    }
})();
