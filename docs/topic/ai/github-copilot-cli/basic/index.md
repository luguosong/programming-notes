# 安装与快速入门

## 安装

=== "npm（推荐）"

    ``` bash
    # 安装稳定版
    npm install -g @github/copilot

    # 安装预发布版本（获取最新功能）
    npm install -g @github/copilot@prerelease
    ```

=== "Homebrew（macOS/Linux）"

    ``` bash
    brew install gh-copilot
    ```

=== "WinGet（Windows）"

    ``` bash
    winget install GitHub.CopilotCLI
    ```

=== "一键安装脚本"

    ``` bash
    # macOS / Linux
    curl -fsSL https://cli.github.com/copilot/install.sh | sh

    # Windows（PowerShell）
    irm https://cli.github.com/copilot/install.ps1 | iex
    ```

### 更新

``` bash
# npm 更新
npm update -g @github/copilot

# 检查当前版本
copilot --version
```

---

## 认证

首次使用需要通过 GitHub 账号认证：

``` bash
# 启动认证流程
copilot auth

# 验证认证状态
copilot auth status
```

!!! tip "前提条件"

    需要一个启用了 GitHub Copilot 的 GitHub 账号（个人、Business 或 Enterprise 订阅均可）。

---

## 启动与退出

``` bash
# 启动交互式会话
copilot

# 启动并恢复上次会话
copilot --resume

# 非交互式执行单条指令（Programmatic 模式）
copilot -p "解释这段代码的作用"

# 退出会话
/exit
```

---

## 快捷键

| 快捷键 | 功能 |
|--------|------|
| ++shift+tab++ | 循环切换交互模式（Interactive → Plan → Autopilot） |
| ++esc++ | 取消当前操作 |
| ++ctrl+c++ | 若在思考中：清空输入；否则退出 |
| ++ctrl+l++ | 清屏 |
| ++ctrl+y++ | 在编辑器中打开计划文件（Plan 模式） |
| ++up++ / ++down++ | 导航命令历史 |

---

## 输入前缀

| 前缀 | 功能 | 示例 |
|------|------|------|
| `@` | 引用文件或目录作为上下文 | `@src/main.py 解释这段代码` |
| `/` | 执行斜杠命令 | `/help` |
| `?` | 显示分类帮助 | `?commands` |
| `&` | 快速委派任务到云端 Agent | `& 完成集成测试` |

---

## 常用斜杠命令速查

### 通用命令

| 命令 | 功能 |
|------|------|
| `/help` | 查看所有快捷方式和可用命令 |
| `/clear` | 清除当前会话的对话历史 |
| `/exit` | 退出 Copilot CLI |
| `/compact` | 压缩上下文，释放 token 空间 |
| `/session` | 查看当前会话信息 |

### 模式与权限

| 命令 | 功能 |
|------|------|
| `/plan` | 进入计划模式 |
| `/allow-all` | 启用所有权限（工具、路径和 URL） |
| `/experimental` | 启用实验性功能（如 Autopilot） |

### 开发相关

| 命令 | 功能 |
|------|------|
| `/review` | 审查代码变更 |
| `/diff` | 查看当前工作区的 diff |
| `/pr` | 创建或查看 Pull Request |
| `/research` | 对主题进行深入研究 |

### 扩展管理

| 命令 | 功能 |
|------|------|
| `/agent` | 选择并激活 Agent |
| `/skills list` | 列出可用的 Skills |
| `/mcp show` | 列出已配置的 MCP 服务器 |
| `/plugin list` | 列出已安装的插件 |

---

## 验证安装

安装完成后，运行以下命令验证一切正常：

``` bash
# 检查版本
copilot --version

# 检查认证状态
copilot auth status

# 启动一次简单对话测试
copilot -p "你好，请用一句话介绍你自己"
```

如果都能正确输出，说明安装和认证成功。
