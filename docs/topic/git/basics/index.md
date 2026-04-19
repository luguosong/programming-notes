---
title: 基础操作
---

# Git 基础操作：记录每次变化

**本文你会学到：**

- 如何创建或克隆一个 Git 仓库
- `git add` / `git commit` 的工作机制
- 用 `git status` 和 `git diff` 观察仓库状态
- `.gitignore` 的写法与最佳实践

## 🏁 获取 Git 仓库

### 方式一：在已有项目中初始化

```bash title="在现有目录中初始化 Git 仓库"
cd my-project          # 进入项目目录
git init               # 创建 .git 目录，仓库骨架就此建立

# .git/ 目录包含了所有历史记录，删掉它 = 仓库彻底消失
# 此时项目文件还没有被跟踪，需要手动 add

git add .              # 跟踪所有文件
git commit -m "初始提交"  # 第一个快照
```

### 方式二：克隆已有仓库

```bash title="克隆远程仓库"
# 基本用法（在当前目录下创建 libgit2/ 目录）
git clone https://github.com/libgit2/libgit2

# 自定义本地目录名
git clone https://github.com/libgit2/libgit2 my-libgit2

# 支持多种协议
git clone git@github.com:user/repo.git    # SSH（推荐，免密码）
git clone https://github.com/user/repo    # HTTPS
```

!!! info "clone 和 checkout 的区别"

    SVN 用 `checkout` 只取某个版本；Git 用 `clone` 会把**整个仓库**（含所有历史）复制到本地。这就是为什么 Git 可以完全离线工作。

## 📝 记录每次变化

### git status：观察仓库"心跳"

```bash title="随时用 git status 了解当前状态"
git status

# 典型输出：
# On branch main
# Changes to be committed:    ← 已暂存（绿色）
#   new file:   README.md
# Changes not staged for commit:  ← 已修改但未暂存（红色）
#   modified:   app.py
# Untracked files:             ← 未跟踪（红色）
#   config.local

# 简洁模式（只看 2 列标志）
git status -s
# M  README.md   ← 左M=暂存已改，右M=工作区已改，??=未跟踪
```

`git status -s` 的两列标志含义：

| 标志 | 含义 |
|------|------|
| `??` | 未跟踪文件 |
| `A ` | 新文件已暂存 |
| ` M` | 已修改但未暂存 |
| `M ` | 修改已暂存 |
| `MM` | 暂存后又修改了（两个版本都存在！） |

### git add：把改动放进"候车室"

`git add` 是个多功能命令，正确理解它的语义是**「将内容精确地放入下一次提交中」**，而不是「添加文件」。

```bash title="git add 的各种用法"
git add README.md          # 添加单个文件
git add src/               # 添加整个目录（递归）
git add *.py               # 添加所有 .py 文件
git add .                  # 添加当前目录所有变化
git add -p                 # 交互式选择要暂存的代码块（精细控制）
```

!!! warning "add 暂存的是「那一刻的内容」"

    ```bash
    vim app.py         # 第一次修改
    git add app.py     # 暂存了 v1 的内容

    vim app.py         # 继续修改（v2）
    git commit         # ❌ 提交的是 v1，不是 v2！

    # 正确做法：修改后重新 add
    git add app.py     # 重新暂存 v2
    git commit         # ✅ 提交 v2
    ```

### git commit：创建一个快照

```bash title="提交的几种姿势"
# 基本提交（会打开编辑器写提交信息）
git commit

# 内联提交信息（日常最常用）
git commit -m "feat: 添加用户登录功能"

# 跳过暂存区，直接提交所有已跟踪文件的修改（慎用）
git commit -a -m "fix: 修复空指针异常"

# 查看将要提交的内容（在编辑器里显示 diff）
git commit -v
```

**好的提交信息是写给未来自己的情书**：

```
# ✅ 好的提交信息格式（Conventional Commits）
feat: 添加邮件验证功能
fix: 修复登录时 token 过期未刷新的问题
docs: 更新 API 文档中的认证章节
refactor: 将用户服务拆分为独立模块

# ❌ 坏的提交信息
update
修了个bug
aaa
改代码
```

### git diff：看清楚改了什么

很多人只用 `git status` 知道"哪些文件变了"，但不知道"具体改了哪几行"。`git diff` 就是干这个的：

```bash title="git diff 常用场景"
git diff               # 工作区 vs 暂存区（未暂存的修改）
git diff --staged      # 暂存区 vs 最新提交（即将提交的内容）
git diff HEAD          # 工作区 vs 最新提交（所有未提交修改）
git diff main feature  # 两个分支之间的差异
git diff abc123 def456 # 两个提交之间的差异
```

**`git diff` 比较的是哪两个区域：**

``` mermaid
graph TD
    WD["🗂️ 工作区\n（正在编辑）"]
    SA["📦 暂存区\n（已 git add）"]
    Repo["🗃️ 最新提交\n（HEAD）"]
    WD <-->|"git diff\n未暂存的改动"| SA
    SA <-->|"git diff --staged\n将要提交的内容"| Repo
    WD <-->|"git diff HEAD\n所有未提交的改动"| Repo
    classDef area fill:transparent,stroke:#768390,color:#adbac7,stroke-width:1px
    class WD,SA,Repo area
```

读懂 diff 输出：

```diff
diff --git a/app.py b/app.py
index 1234567..abcdef0 100644
--- a/app.py   ← 改之前
+++ b/app.py   ← 改之后

@@ -10,7 +10,8 @@   ← 改动位置（从第10行开始，前后7行/8行）
 def login(user, password):
-    if user == admin:          # ← 删除的行（红色）
+    if user == "admin":        # ← 新增的行（绿色）
+    # 修复：字符串要加引号
     return authenticate(user)
```

## 🗑️ 删除与重命名文件

很多初学者犯的错误：直接在文件管理器或终端用 `rm` 删除文件，然后发现 Git 状态混乱。正确做法是告诉 Git 参与这个过程。

### git rm：从 Git 中删除文件

```bash title="git rm 常用场景"
# 从工作区和暂存区同时删除（最常用）
git rm old-file.py
git commit -m "删除废弃的 old-file.py"

# 只从暂存区移出（工作区文件保留），相当于"取消跟踪"
# 常用场景：忘记加到 .gitignore 就提交了，现在补救
git rm --cached secrets.env
echo "secrets.env" >> .gitignore
git commit -m "停止跟踪敏感配置文件"

# 递归删除目录
git rm -r old-module/

# 强制删除（有未提交修改时需要加 -f）
git rm -f temp.py
```

!!! warning "直接 `rm` 和 `git rm` 的区别"

    ```bash
    # ❌ 错误做法：只用系统 rm 删除
    rm old-file.py
    git status
    # deleted: old-file.py   ← 显示为"已修改"，还需要再 git add 才能暂存删除操作

    # ✅ 正确做法：用 git rm 一步完成
    git rm old-file.py
    git status
    # deleted: old-file.py   ← 已暂存删除，直接可以 commit
    ```

### git mv：重命名或移动文件

```bash title="git mv 基本用法"
# 重命名文件
git mv old-name.py new-name.py

# 移动文件到另一个目录
git mv src/utils.py lib/utils.py

# 等效的手动做法（git mv 就是这三步的简写）
mv old-name.py new-name.py
git rm old-name.py
git add new-name.py
```

!!! tip "Git 如何追踪重命名？"

    Git 没有专门的"重命名"记录，但会通过内容相似度检测来推断——如果新旧文件内容超过 50% 相同，`git log --follow` 就能跨越重命名追踪文件历史：

    ```bash
    git log --follow -- lib/utils.py    # 即使文件原来叫 src/utils.py，也能看到完整历史
    ```

## 📂 .gitignore：不跟踪哪些文件

有些文件不应该进入版本控制：编译产物、日志文件、本地配置、密钥等。`.gitignore` 告诉 Git 忽略它们。

```bash title=".gitignore 示例（Java + Python 混合项目）"
# 注释行（# 开头）

# ── Java 编译产物 ──────────────
*.class
*.jar
target/                 # Maven 构建目录

# ── Python ────────────────────
__pycache__/
*.pyc
*.pyo
.venv/
venv/

# ── IDE 配置（个人喜好，不强制加入）──
.idea/
.vscode/
*.iml

# ── 系统文件 ──────────────────
.DS_Store               # macOS 缩略图数据库
Thumbs.db               # Windows 缩略图

# ── 敏感配置 ──────────────────
.env                    # 环境变量（含密码！绝对不要提交）
*.key
*.pem
config.local.*

# ── 日志 ──────────────────────
*.log
logs/

# 例外：即使目录被忽略，也跟踪这个特定文件
!logs/.gitkeep          # 保留空目录占位文件
```

**.gitignore 匹配规则速查：**

| 模式 | 含义 |
|------|------|
| `*.log` | 所有 .log 文件 |
| `build/` | 名为 build 的目录 |
| `!important.log` | 例外，不忽略这个文件 |
| `**/temp` | 任何位置的 temp 目录 |
| `doc/*.txt` | doc 根目录下的 .txt（不含子目录）|
| `doc/**/*.txt` | doc 下所有 .txt（含子目录）|

!!! tip "快速生成 .gitignore"

    访问 [gitignore.io](https://www.toptal.com/developers/gitignore) 选择语言/框架，自动生成 `.gitignore` 内容。或使用 GitHub 官方模板库：`https://github.com/github/gitignore`

## 🔄 典型工作流示例

```bash title="一次完整的开发迭代"
# 1. 查看当前状态
git status

# 2. 编写代码……（修改了 app.py，新建了 test_app.py）

# 3. 查看具体改了什么
git diff

# 4. 选择性暂存
git add app.py test_app.py

# 5. 确认即将提交的内容
git diff --staged

# 6. 提交
git commit -m "feat: 添加用户注册接口及单元测试"

# 7. 查看提交是否成功
git log --oneline
```

## 小结

| 命令 | 用途 |
|------|------|
| `git init` | 在当前目录初始化仓库 |
| `git clone <url>` | 克隆远程仓库到本地 |
| `git status` / `git status -s` | 查看文件状态 |
| `git add <file>` | 暂存指定文件 |
| `git add .` | 暂存所有变化 |
| `git commit -m "msg"` | 创建提交 |
| `git diff` | 工作区 vs 暂存区 |
| `git diff --staged` | 暂存区 vs 最新提交 |
| `git rm <file>` | 从仓库和工作区删除文件 |
| `git rm --cached <file>` | 只从暂存区移出（停止跟踪） |
| `git mv <old> <new>` | 重命名或移动文件 |

下一篇「查看历史」将深入讲解 `git log`，教你用图形化方式看懂项目的演进脉络。
