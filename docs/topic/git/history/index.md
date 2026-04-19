---
title: 查看历史
---

# 查看与搜索历史

**本文你会学到：**

- `git log` 的各种展示形式（图形化、格式化、过滤）
- 用 `git grep` 在整个历史中搜索代码
- 用 `git blame` 追踪"这行代码是谁写的、为什么这么写"
- 用 `git bisect` 二分法定位引入 bug 的提交（进阶，先了解）

## 🔍 为什么需要"看历史"？

代码库就像一本连载小说——每次提交都是新章节。当你遇到"这功能怎么突然坏了？""这行代码为什么这么写？"这类问题时，查历史就是你的侦探工具。

## 📜 git log：项目的提交年鉴

### 基本用法

```bash
git log
# 每条提交显示：SHA、作者、日期、提交信息
# 按 q 退出翻页模式

git log --oneline
# 精简显示，每行一个提交：
# a1b2c3d feat: 添加用户登录
# e4f5g6h fix: 修复空指针

git log -5             # 只看最近 5 条
git log -p             # 显示每次提交的详细 diff
git log --stat         # 显示每次提交修改了哪些文件（增删行数）
```

### 图形化查看分支结构

```bash title="最好看的 git log 命令"
git log --oneline --graph --all --decorate

# 输出示例：
# * a1b2c3d (HEAD -> main) feat: 用户系统重构
# * b2c3d4e fix: 登录 bug
# | * c3d4e5f (feature/pay) feat: 支付接口
# | * d4e5f6g feat: 购物车
# |/
# * e5f6g7h 项目初始化

# 建议设置别名，以后直接用 git lg
git config --global alias.lg "log --oneline --graph --all --decorate"
```

### 按条件过滤历史

```bash title="git log 过滤器大全"
# 按时间范围
git log --since="2024-01-01"
git log --until="2024-12-31"
git log --since="2 weeks ago"
git log --since="3 days ago" --until="yesterday"

# 按作者
git log --author="张三"
git log --author="zhangsan@company.com"

# 按提交信息关键词
git log --grep="登录"           # 提交信息含"登录"
git log --grep="fix" -i         # 不区分大小写

# 按文件（只看影响了某个文件的提交）
git log -- src/app.py
git log -- "*.js"               # 任何 js 文件

# 按代码内容（找某个字符串的增删历史）
git log -S "def login"          # 代码中出现/消失了这个字符串
git log -G "user.*=.*admin"     # 正则匹配

# 组合使用
git log --author="张三" --since="1 month ago" --oneline
```

### 自定义输出格式

```bash title="格式化输出，适合脚本处理"
git log --pretty=format:"%h - %an, %ar : %s"
# 输出：
# a1b2c3d - 张三, 2 hours ago : feat: 添加登录

# 常用格式占位符：
# %H  完整 SHA   %h  短 SHA
# %an 作者名     %ae 作者邮箱
# %ar 相对时间   %ad 绝对时间
# %s  提交标题   %b  提交正文
```

## 🔎 git grep：在代码中搜索

`git grep` 比普通的 `grep` 更强，因为它了解 Git 的历史：

```bash title="git grep vs 普通搜索"
# 在当前工作区搜索
git grep "def authenticate"
git grep -n "TODO"               # 显示行号
git grep -l "password"           # 只显示文件名
git grep -i "error"              # 不区分大小写
git grep -c "import"             # 每个文件匹配次数

# 在特定提交/分支中搜索（无需切换分支！）
git grep "def login" HEAD~5      # 5个提交之前的版本
git grep "def login" v1.0.0      # 某个标签的版本
git grep "def login" feature/auth # 某个分支中

# 搜索特定文件类型
git grep "useState" -- "*.tsx"
git grep "SELECT" -- "*.sql"

# 显示上下文
git grep -A 3 "def login"       # 匹配行后3行
git grep -B 2 "def login"       # 匹配行前2行
git grep -C 2 "def login"       # 前后各2行
```

**对比**：为什么用 `git grep` 而不是系统的 `grep`？

| 特性 | `git grep` | 系统 `grep` |
|------|-----------|------------|
| 自动跳过 `.git/` 目录 | ✅ | ❌（需手动排除） |
| 自动跳过 `.gitignore` 的文件 | ✅ | ❌ |
| 可搜索历史版本 | ✅ | ❌ |
| 速度 | 通常更快 | 视情况 |

## 👉 git blame：追责神器

当你看到一行代码，困惑地想"这谁写的？为什么这么写？"——`git blame` 帮你找到答案：

```bash title="git blame 基本用法"
git blame src/auth.py
# 每行显示：SHA | 作者 | 时间 | 行号 | 代码内容
# a1b2c3d4 (张三    2024-03-15 09:21:08 +0800 42) def authenticate(user, pwd):
# e5f6g7h8 (李四    2024-04-01 14:33:00 +0800 43)     if not user:
# a1b2c3d4 (张三    2024-03-15 09:21:08 +0800 44)         raise ValueError("user required")

# 只看某几行
git blame -L 40,55 src/auth.py     # 只看第40-55行
git blame -L "/def authenticate/,+10" src/auth.py  # 从函数开头起10行

# 忽略空白符变更（代码格式化引入的变更不影响溯源）
git blame -w src/auth.py

# 查看某个历史版本的 blame（不是当前代码）
git blame v1.2.0 -- src/auth.py
```

!!! tip "blame 不是"甩锅""

    `git blame` 的真正用途是**找到提交该行代码的 commit**，从而：
    - 读那次提交的 commit message，了解**当时的背景**
    - 看那次提交的完整 diff，了解**上下文**
    - 用 `git show <sha>` 查看完整提交详情

```bash title="从 blame 深入查看某次提交"
git blame src/auth.py            # 找到某行的 SHA：a1b2c3d4
git show a1b2c3d4                # 查看这次提交的完整内容
git log a1b2c3d4 -1 --stat      # 这次提交改了哪些文件
```

## 📊 实战：用历史分析代码演变

```bash title="综合运用——找出「谁、什么时候、为什么」删除了某功能"
# 第一步：用 git log -S 找到某个函数被删除的提交
git log -S "def send_email" --oneline

# 第二步：查看那次提交的详情（含完整 diff）
git show abc1234

# 第三步：如果删除发生在很多次提交中，用 git log -p 过滤
git log -p -S "def send_email" -- src/notification.py
```

## ↩️ HEAD 引用：如何定位历史提交

理解 HEAD 引用符号，是能灵活使用 `git log`、`git diff`、`git reset` 等命令的前提。

```bash title="HEAD 引用速查"
HEAD        # 当前所在提交
HEAD~       # 上一个提交（等同于 HEAD~1）
HEAD~3      # 往前第3个提交（沿第一父链走3步）
HEAD^       # 上一个提交（与 HEAD~ 相同）
HEAD^2      # 合并提交的第2个父提交（仅对合并提交有效）
abc1234     # 直接用 SHA（可简写为前7位）
v1.2.0      # 通过 tag 名称引用
main        # 通过分支名称引用
```

**`~` vs `^` 的区别**：对普通提交，二者等价；对**合并提交**（有两个父提交），`~` 沿第一父链，`^2` 指向第二个父提交：

``` mermaid
graph TD
    M["🔀 合并提交 M\n(HEAD)"]
    A["A（main 父提交）\nM^ = M^1 = M~"]
    B["B（feature 父提交）\nM^2"]
    A2["A2\nM~2 = M^^"]
    B2["B2\nM^2~"]
    A2 --> A --> M
    B2 --> B --> M
    classDef head fill:transparent,stroke:#f57c00,color:#adbac7,stroke-width:2px
    classDef first fill:transparent,stroke:#539bf5,color:#adbac7,stroke-width:2px
    classDef second fill:transparent,stroke:#388e3c,color:#adbac7,stroke-width:2px
    classDef older fill:transparent,stroke:#768390,color:#adbac7,stroke-width:1px
    class M head
    class A first
    class B second
    class A2,B2 older
```

```bash title="实际使用场景"
# 查看上3个提交的变化
git log HEAD~3..HEAD --oneline

# 比较当前和3个提交之前的差异
git diff HEAD~3 HEAD -- src/app.py

# 查看合并提交的两个父提交分别是什么
git log --merges --oneline -5
git show <merge-sha>^   # 第1个父提交（主线）
git show <merge-sha>^2  # 第2个父提交（功能分支）
```

## 🔍 bisect：二分法精确定位引入 bug 的提交

当你知道"某个版本好用，现在坏了"，但中间有几十上百个提交，手动排查太慢——`git bisect` 帮你用**二分查找**把范围快速缩小到个位数：

``` mermaid
graph LR
    A["v1.0（正常）"] --> B["?"] --> C["?"] --> D["?"] --> E["?"] --> F["HEAD（有bug）"]
    B -.->|"git bisect: 测这个"| GOOD["✅ 正常 → 向右缩"]
    D -.->|"git bisect: 测这个"| BAD["❌ 有bug → 向左缩"]
    classDef commit fill:transparent,stroke:#768390,color:#adbac7,stroke-width:1px
    classDef ok fill:transparent,stroke:#388e3c,color:#adbac7,stroke-width:2px
    classDef bad fill:transparent,stroke:#d32f2f,color:#adbac7,stroke-width:2px
    class A,B,C,D,E,F commit
    class GOOD ok
    class BAD bad
```

### 手动 bisect 步骤演练

```bash title="git bisect 完整工作流"
# 第1步：开始 bisect
git bisect start

# 第2步：标记当前版本（HEAD）有 bug
git bisect bad

# 第3步：标记某个已知正常的版本（tag、SHA 均可）
git bisect good v2.0.0
# Git 输出：Bisecting: 23 revisions left to test after this (roughly 5 steps)
# Git 自动检出中间那个提交

# 第4步：测试当前检出的版本（运行测试或手动验证）
# 有 bug → 告诉 Git：
git bisect bad
# 没 bug → 告诉 Git：
git bisect good
# Git 再次缩短范围并自动检出下一个

# ... 重复第4步约 5-6 次 ...

# 第5步：Git 找到了！输出类似：
# abc1234 is the first bad commit
# commit abc1234 Author: 张三 ...
# feat: 修改用户查询逻辑

# 第6步：结束 bisect（恢复到原来的 HEAD）
git bisect reset
```

### 自动 bisect：用脚本测试

如果有自动化测试脚本，可以让 Git 完全自动完成二分过程（脚本返回 0 表示正常，非 0 表示有 bug）：

```bash title="全自动 bisect"
git bisect start
git bisect bad HEAD
git bisect good v2.0.0

# 提供自动测试脚本（Git 自动循环直到找到）
git bisect run python tests/test_login.py
# 或
git bisect run ./scripts/check-bug.sh

# 结束
git bisect reset
```

!!! tip "bisect 日志"

    ```bash
    git bisect log          # 查看本次 bisect 的所有标记历史
    git bisect log > bisect.log && git bisect replay bisect.log  # 重放 bisect 过程
    ```



| 命令 | 用途 |
|------|------|
| `git log --oneline --graph --all` | 图形化查看分支历史 |
| `git log --author="..." --since="..."` | 按条件过滤提交 |
| `git log -S "关键词"` | 找包含某段代码变更的提交 |
| `git log -p` | 显示每次提交的详细 diff |
| `git grep "关键词"` | 在当前或历史版本中搜索代码 |
| `git blame <file>` | 每行代码的最后修改者 |
| `git show <sha>` | 查看某次提交的详情 |
| `HEAD~N` / `HEAD^` | 引用相对历史提交 |
| `git bisect start/good/bad` | 二分法定位引入 bug 的提交 |
| `git bisect run <script>` | 自动化二分法 |

下一篇「撤销时光机」将教你如何"反悔"——从小改动的撤销到"抹掉历史"，以及每种方式的风险边界。
