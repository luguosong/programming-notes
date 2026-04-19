---
title: Git
icon: simple/git
---

# Git

「我的代码明明昨天还好好的，今天怎么就崩了？」——这句话每个程序员都说过。Git 就是你的时光机：它记录代码每一次变化，让你随时回到过去，还能让多人同时修改同一份代码而互不干扰。

``` mermaid
graph LR
    A[工作目录] -->|git add| B[暂存区]
    B -->|git commit| C[本地仓库]
    C -->|git push| D[远程仓库]
    D -->|git pull| A
    classDef area fill:transparent,stroke:#539bf5,color:#adbac7,stroke-width:2px
    classDef remote fill:transparent,stroke:#388e3c,color:#adbac7,stroke-width:2px
    class A,B,C area
    class D remote
```

## 📚 学习路径

按以下顺序学习，可以建立从基础到精通的完整知识体系：

<div class="grid cards" markdown>

- :simple-git: `Git 起步`

    版本控制简史、Git 的快照思维、三个区域、安装与初始配置

    [→ Git 起步](getting-started/index.md)

- :lucide-file-plus: `基础操作`

    init/clone、add/commit、status/diff、.gitignore 完整工作流

    [→ 基础操作](basics/index.md)

- :lucide-history: `查看历史`

    git log 高级用法、格式化输出、git grep、git blame 追责

    [→ 查看历史](history/index.md)

- :lucide-rotate-ccw: `撤销时光机`

    restore/reset/revert 三种撤销策略，什么时候用哪个？

    [→ 撤销时光机](undo/index.md)

- :lucide-git-branch: `分支机制`

    分支本质（指针）、合并策略（fast-forward vs 3-way）、冲突解决

    [→ 分支机制](branching/index.md)

- :lucide-git-merge: `变基详解`

    rebase vs merge 的本质区别、交互式 rebase、黄金法则

    [→ 变基详解](rebase/index.md)

- :lucide-cloud: `远程协作`

    remote 管理、fetch/pull/push、远程跟踪分支、多人协作模型

    [→ 远程协作](remote/index.md)

- :lucide-archive: `贮藏与清理`

    git stash 临时搁置工作、git clean 清理未跟踪文件

    [→ 贮藏与清理](stash-clean/index.md)

- :lucide-wrench: `高级工具`

    cherry-pick、bisect 二分法找 bug、reflog 后悔药、tag 版本发布

    [→ 高级工具](advanced-tools/index.md)

- :lucide-pencil: `重写历史`

    amend 修改最近提交、rebase -i 整理提交记录、filter-repo 清理大文件

    [→ 重写历史](rewrite-history/index.md)

- :lucide-package: `子模块`

    submodule 管理多仓库依赖、克隆/更新/删除子模块

    [→ 子模块](submodule/index.md)

- :lucide-zap: `Git 钩子`

    pre-commit/commit-msg/pre-push 自动化守卫、代码质量门禁实战

    [→ Git 钩子](hooks/index.md)

- :lucide-cpu: `内部原理`

    blob/tree/commit 对象模型、引用系统、packfile 压缩原理

    [→ 内部原理](internals/index.md)

- :lucide-badge-check: `提交规范与最佳实践`

    Conventional Commits 格式、分支命名约定、原子性提交、GUI 客户端、进阶配置技巧

    [→ 提交规范与最佳实践](best-practices/index.md)

</div>
