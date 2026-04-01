# 插件与 Hook

插件（Plugins）和 Hook 是 Copilot CLI 的两种扩展机制。插件为 Copilot 添加新的 Agent 和 Skill，Hook 在 Agent 执行过程中的关键节点注入自定义逻辑。

---

## 插件系统

### 安装插件

=== "从应用市场安装"

    ``` text
    /plugin install PLUGIN-NAME@MARKETPLACE-NAME
    ```

    例如：

    ``` text
    /plugin install database-data-management@awesome-copilot
    ```

=== "从 GitHub 仓库安装"

    ``` text
    # 从 GitHub.com 上的仓库
    /plugin install OWNER/REPO

    # 从任意在线 Git 仓库
    /plugin install URL-OF-GIT-REPO
    ```

=== "从本地路径安装"

    ``` text
    /plugin install ./PATH/TO/PLUGIN
    ```

### 管理插件

| 命令 | 功能 |
|------|------|
| `/plugin list` | 显示已安装的所有插件及其状态 |
| `/plugin uninstall <name>` | 卸载指定插件 |
| `/plugin enable <name>` | 启用插件 |
| `/plugin disable <name>` | 禁用插件 |

### 插件市场

Copilot CLI 自带两个默认插件市场：

| 市场 | 说明 |
|------|------|
| `copilot-plugins` | 官方插件市场 |
| `awesome-copilot` | 社区插件市场 |

| 命令 | 功能 |
|------|------|
| `/plugin marketplace list` | 列出可用的市场 |
| `/plugin marketplace browse <name>` | 浏览指定市场中的插件 |

### 插件提供的内容

插件可以为 Copilot CLI 添加：

- `Agent`：新的专业角色（如数据库管理 Agent）
- `Skills`：新的自动触发技能
- `MCP 服务器`：新的外部数据源连接
- `Slash 命令`：新的斜杠命令

---

## Hook 扩展

Hook 允许你在 Copilot Agent 执行过程中的关键节点注入自定义 shell 命令，实现自动化和质量控制。

### 生命周期事件

| 事件 | 触发时机 | 典型用途 |
|------|---------|---------|
| `sessionStart` | 会话启动时 | 日志记录、环境初始化 |
| `sessionEnd` | 会话结束时 | 清理临时文件、发送统计 |
| `userPromptSubmitted` | 用户提交 prompt 后 | 输入验证、日志审计 |
| `preToolUse` | 工具调用前 | 权限检查、参数验证 |
| `postToolUse` | 工具调用后 | 结果检查、格式化 |
| `errorOccurred` | 发生错误时 | 错误通知、自动恢复 |

### 创建 Hook

在仓库的 `.github/hooks/` 文件夹中创建 JSON 文件（文件名可自由选择）：

``` json title=".github/hooks/session-log.json - 记录会话"
[
  {
    "event": "sessionStart",
    "steps": [
      {
        "command": "echo \"Session started at $(date)\" >> /tmp/copilot-sessions.log"
      }
    ]
  },
  {
    "event": "sessionEnd",
    "steps": [
      {
        "command": "echo \"Session ended at $(date)\" >> /tmp/copilot-sessions.log"
      }
    ]
  }
]
```

### Hook 配置字段

| 字段 | 说明 |
|------|------|
| `event` | 触发事件名称 |
| `steps` | 步骤数组，每步包含一个 `command` |
| `steps[].command` | 要执行的 shell 命令 |

### 实战示例

#### 工具调用后运行脚本

``` json title=".github/hooks/post-tool.json"
[
  {
    "event": "postToolUse",
    "steps": [
      {
        "command": "bash .github/hooks/scripts/post-tool.sh"
      }
    ]
  }
]
```

#### 自动格式化代码

``` json title=".github/hooks/format.json"
[
  {
    "event": "postToolUse",
    "steps": [
      {
        "command": "npx prettier --write '**/*.{ts,tsx,js,jsx}' 2>/dev/null || true"
      }
    ]
  }
]
```

#### 错误时发送通知

``` json title=".github/hooks/notify.json"
[
  {
    "event": "errorOccurred",
    "steps": [
      {
        "command": "echo '[Copilot Error]' | mail -s 'Copilot Error Alert' dev@example.com"
      }
    ]
  }
]
```

---

## Hook vs 手动 Git Hook

| 维度 | Copilot Hook | Git Hook |
|------|-------------|----------|
| `触发时机` | Copilot Agent 生命周期事件 | Git 操作事件（commit、push 等） |
| `配置方式` | `.github/hooks/*.json` | `.git/hooks/*`（或 Husky 等工具） |
| `提交到 Git` | ✅ | ❌（`.git/hooks/` 不跟踪） |
| `用途` | 扩展 Copilot 行为 | 代码质量门禁 |

两者可以互补：Git Hook 确保提交质量，Copilot Hook 增强 AI 辅助体验。
