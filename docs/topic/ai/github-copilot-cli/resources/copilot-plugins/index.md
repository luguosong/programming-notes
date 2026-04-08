---
title: 官方市场 copilot-plugins
description: GitHub 官方维护的 Copilot CLI 插件集合，经过验证，质量有保障
---

# 官方市场 copilot-plugins

📦 仓库地址：[github/copilot-plugins](https://github.com/github/copilot-plugins)

`copilot-plugins` 是 GitHub 官方维护的插件集合——可以理解为 Copilot CLI 的"官方应用商店"。这里的每个插件都经过 GitHub 团队审核，质量有保障，适合作为首选安装来源。

## 📥 安装方式

``` text
/plugin install <插件名>@copilot-plugins
```

由于 `copilot-plugins` 是默认注册的市场，无需额外配置即可直接安装。

## 🔌 可用插件

目前官方市场包含 3 个插件：

### Advanced Security — 安全扫描

``` text
/plugin install advanced-security@copilot-plugins
```

将 GitHub Advanced Security 的能力引入 AI 编码工作流。主要功能：

- 🔍 扫描代码片段、文件和 Git 变更中的潜在密钥泄露
- 使用 GitHub 密钥检测模式（通过 MCP 工具）
- 支持 `pre-commit` 检查，在代码提交前捕获泄露的凭证

**包含**：`secret-scanning` Skill — 当你让 Agent 检查代码中的凭证暴露时自动激活

### Spark — Web 应用脚手架

``` text
/plugin install spark@copilot-plugins
```

为构建现代 Web 应用提供全方位的技术栈指导和脚手架生成。主要功能：

- ⚡ 预选技术栈：Vite + React 19+ + TypeScript + Tailwind CSS v4+ + shadcn/ui + TanStack Router & Query
- 🎨 设计系统：字体搭配、OKLCH 调色板、空间布局、微交互
- 📐 性能目标：Core Web Vitals、React Compiler 配置、优化清单

根据应用复杂度提供 4 种预配置变体：

| 变体 | 适用场景 |
|------|---------|
| Default Web App | 通用工具、CRUD、MVP、原型 |
| Content Showcase | 营销站点、作品集、博客、文档 |
| Data Dashboard | 数据分析面板、管理后台、监控系统 |
| Complex Application | SaaS 平台、企业工具、多视图应用 |

**包含**：`spark-app-template` Skill — 当你让 Agent 创建新的 Web 应用时自动激活

### WorkIQ — Microsoft 365 集成

``` text
/plugin install workiq@copilot-plugins
```

!!! warning "公开预览"
    WorkIQ 目前处于公开预览阶段，功能和 API 可能变更。

将 AI Agent 连接到 Microsoft 365 Copilot，提供对组织数据的访问能力。支持查询的数据类型：

| 数据类型 | 示例问题 |
|---------|---------|
| 邮件 | "John 关于提案说了什么？" |
| 会议 | "我明天的日程是什么？" |
| 文档 | "找到我最近的 PPT 演示文稿" |
| Teams | "总结一下工程频道今天的消息" |
| 人员 | "谁在参与 Alpha 项目？" |

**包含**：`workiq` Skill + MCP 服务器（基于 [`@microsoft/workiq`](https://github.com/microsoft/work-iq-mcp)）

**前置条件**：Node.js 18+、具备 Copilot 访问权限的 Microsoft 365 账户、租户管理员授权

---

## 🆚 与社区市场的区别

| 维度 | copilot-plugins（官方） | awesome-copilot（社区） |
|------|----------------------|----------------------|
| 维护者 | GitHub 团队 | 社区贡献者 |
| 审核标准 | 严格审核 | 社区评审 |
| 插件数量 | 少而精（3 个） | 种类丰富（57+） |
| 适用场景 | 通用、安全相关 | 特定语言、框架、工作流 |

想浏览更多插件？查看 → [社区市场 awesome-copilot](../awesome-copilot/index.md)
