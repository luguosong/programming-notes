---
title: 自带模型（BYOK）
description: 配置 Copilot CLI 使用自有 LLM 提供商的模型
---

**本文你会学到**：

- 🔑 BYOK 模式的概念与适用场景
- 🏢 三种支持的提供商类型及兼容服务
- ⚙️ 通过环境变量配置模型提供商
- 🌐 连接 OpenAI 兼容端点、Azure OpenAI、Anthropic 的具体步骤
- 📡 离线模式的配置与注意事项
- ⚠️ BYOK 模式下的功能限制

BYOK（Bring Your Own Key）就像自带厨师——GitHub 提供厨房（CLI 框架），你自带厨师（模型）。这样你可以使用自己熟悉的模型提供商，而不必依赖 GitHub 托管模型。

---

## 概念

BYOK 全称 Bring Your Own Key，允许你使用自有模型提供商替代 GitHub 托管模型。通过设置环境变量，Copilot CLI 可以连接到外部 LLM 服务，包括 OpenAI 兼容端点、Azure OpenAI、Anthropic，以及本地运行的模型（如 Ollama）。

### 前置条件

- 已安装 Copilot CLI
- 拥有支持的 LLM 提供商的 API Key，或已启动本地模型（如 Ollama）

## 支持的提供商

| 提供商类型 | 兼容服务 |
|-----------|---------|
| `openai`（默认） | OpenAI、Ollama、vLLM、Foundry Local 及任何 OpenAI Chat Completions API 兼容端点 |
| `azure` | Azure OpenAI Service |
| `anthropic` | Anthropic（Claude 模型） |

!!! tip "查看更多示例"
    运行 `copilot help providers` 查看所有提供商的配置示例。

## 模型要求

并非所有模型都能与 Copilot CLI 配合使用。选用的模型必须满足以下要求：

- **工具调用**（Tool Calling / Function Calling）：模型必须能理解并执行工具调用，这是 Copilot CLI 实现文件操作、命令执行等能力的基础
- **流式传输**（Streaming）：模型必须支持流式返回结果，确保交互过程中实时展示响应
- **上下文窗口**：推荐 ≥128k tokens，以获得最佳效果

!!! warning "不支持的工具调用或流式传输"
    如果模型不支持工具调用或流式传输，Copilot CLI 会返回错误，无法正常工作。

## 环境变量配置

通过环境变量配置模型提供商。以下变量需要在启动 Copilot CLI **之前**设置。

| 环境变量 | 必需 | 说明 |
|---------|------|------|
| `COPILOT_PROVIDER_BASE_URL` | 是 | 模型提供商 API 端点的 Base URL |
| `COPILOT_PROVIDER_TYPE` | 否 | 提供商类型：`openai`（默认）/ `azure` / `anthropic` |
| `COPILOT_PROVIDER_API_KEY` | 否 | API Key。本地 Ollama 等无需认证的提供商不需要此项 |
| `COPILOT_MODEL` | 是 | 模型标识符。也可通过 `--model` 命令行参数设置 |

## 连接 OpenAI 兼容端点

适用于 OpenAI、Ollama、vLLM、Foundry Local 以及任何兼容 OpenAI Chat Completions API 的端点。

### 本地 Ollama

如果已在本地运行 Ollama，只需设置两个变量：

```bash
export COPILOT_PROVIDER_BASE_URL=http://localhost:11434
export COPILOT_MODEL=llama3.2
```

将 `llama3.2` 替换为你在 Ollama 中拉取的模型名称。

### 远程 OpenAI

连接 OpenAI 官方 API 时，还需要提供 API Key：

```bash
export COPILOT_PROVIDER_BASE_URL=https://api.openai.com/v1
export COPILOT_PROVIDER_API_KEY=YOUR-OPENAI-API-KEY
export COPILOT_MODEL=gpt-4o
```

将 `YOUR-OPENAI-API-KEY` 替换为你的 OpenAI API Key，`gpt-4o` 替换为你要使用的模型。

## 连接 Azure OpenAI

使用 Azure OpenAI Service 时，需要指定资源名称和部署名称：

```bash
export COPILOT_PROVIDER_BASE_URL=https://YOUR-RESOURCE-NAME.openai.azure.com/openai/deployments/YOUR-DEPLOYMENT-NAME
export COPILOT_PROVIDER_TYPE=azure
export COPILOT_PROVIDER_API_KEY=YOUR-AZURE-API-KEY
export COPILOT_MODEL=YOUR-DEPLOYMENT-NAME
```

需要替换的占位符：

- `YOUR-RESOURCE-NAME`：你的 Azure OpenAI 资源名称
- `YOUR-DEPLOYMENT-NAME`：你的模型部署名称
- `YOUR-AZURE-API-KEY`：你的 Azure OpenAI API Key

注意 `COPILOT_MODEL` 的值需要与部署名称保持一致。

## 连接 Anthropic

使用 Anthropic 的 Claude 模型：

```bash
export COPILOT_PROVIDER_TYPE=anthropic
export COPILOT_PROVIDER_BASE_URL=https://api.anthropic.com
export COPILOT_PROVIDER_API_KEY=YOUR-ANTHROPIC-API-KEY
export COPILOT_MODEL=claude-opus-4-5
```

将 `YOUR-ANTHROPIC-API-KEY` 替换为你的 Anthropic API Key，`claude-opus-4-5` 替换为你要使用的 Claude 模型。

## 离线模式

离线模式阻止 Copilot CLI 联系 GitHub 服务器，适用于网络隔离环境：

```bash
export COPILOT_OFFLINE=true
```

使用场景：企业内网、气隙环境、仅与本地模型交互等。

!!! warning "离线模式不等于完全隔离"
    离线模式仅阻止 CLI 联系 GitHub 服务器。如果 `COPILOT_PROVIDER_BASE_URL` 指向远程端点，你的提示词和代码上下文仍然会通过网络发送到该提供商。要实现完全隔离，请确保模型提供商也是本地部署的。

## 启动 Copilot CLI

配置好环境变量后，正常启动即可：

```bash
copilot
```

Copilot CLI 会自动读取环境变量，使用你配置的模型提供商。

## 认证与功能限制

BYOK 模式下有以下特点：

**不需要 GitHub 认证**：使用自有 API Key，无需登录 GitHub 账号即可使用 Copilot CLI 的核心功能。

**不可用功能**：以下依赖 GitHub 后端的功能在 BYOK 模式下不可用：

- `/delegate`：将任务委派给 GitHub 托管的 Copilot
- GitHub MCP Server：与 GitHub 仓库交互的 MCP 服务
- GitHub Code Search：GitHub 代码搜索功能

这些限制是因为上述功能需要 GitHub 后端服务配合，而 BYOK 模式绕过了 GitHub 托管模型。

## 与配置体系的关系

BYOK 相关环境变量属于 Copilot CLI **运行时配置**的一部分，与信任目录、工具权限等配置文件各司其职：

- 环境变量控制**模型连接**
- `config.json` 控制**工具权限与信任目录**
- `copilot-instructions.md` 控制**行为定制**

三者可以独立配置，互不影响。例如你可以同时使用 BYOK 模型 + 自定义工具权限 + 项目级指令文件。

!!! tip "持久化环境变量"
    建议将 BYOK 环境变量写入 shell 配置文件（如 `~/.bashrc` 或 `~/.zshrc`），避免每次手动设置。Windows 用户可通过「系统环境变量」或 PowerShell Profile 持久化。
