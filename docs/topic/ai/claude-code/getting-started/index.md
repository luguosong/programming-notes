---
title: 起步
---

## 安装和更新

### npm install-安装与更新 Claude Code

**安装**：需要 Node.js 18 或更高版本。

```bash
npm install -g @anthropic-ai/claude-code
```

**查看当前版本：**

```bash
claude --version
```

**更新：**

```bash
claude update
```

> Windows 用户推荐安装 [Git for Windows](https://gitforwindows.org/)，以便 Claude Code 可以使用 Bash 工具。

---

## 初始配置

### hasCompletedOnboarding-跳过强制登录引导

初次启动 Claude Code 会要求登录 Anthropic 账号。若需接入第三方 LLM（如通过代理），可在配置文件中设置 `hasCompletedOnboarding` 为 `true` 跳过引导流程。

配置文件位置：`~/.claude.json`

```json
{
  "hasCompletedOnboarding": true
}
```

### settings.json env-配置环境变量

在项目根目录或用户主目录的 `.claude/` 目录下创建 `settings.json`，通过 `env` 字段配置环境变量（如 API Key、代理地址等）：

```json
{
  "env": {
    "ANTHROPIC_BASE_URL": "http://localhost:4141",
    "ANTHROPIC_API_KEY": "your-api-key"
  }
}
```

---

## 开启 Copilot API 代理

> ❗ 如果已订阅 Copilot 按量付费（Pay-as-you-go）套餐，此代理意义不大，可跳过。

通过 `copilot-api` 将 Copilot 转为兼容 Anthropic API 的本地代理，供 Claude Code 使用：

```bash
# 推荐版本（@jeffreycao/copilot-api）
npx @jeffreycao/copilot-api@latest start --proxy-env --rate-limit 3 --wait
```

```bash
# 官方原版
npx copilot-api@latest start --proxy-env --rate-limit 3 --wait
```

代理启动后，可查看 Copilot 配额与用量统计：

- 本地接口：`http://localhost:4141/usage`
- 可视化页面：`https://ericc-ch.github.io/copilot-api/?endpoint=http://localhost:4141/usage`

---

## claude -c / -r-启动与恢复会话

```bash
# 新建会话
claude

# 直接续上最近的会话
claude -c

# 恢复指定会话（交互式选择）
claude -r
```
