---
layout: note
title: JavaScript工具和扩展
nav_order: 170
parent: JavaScript
create_time: 2023/5/12
---

# npm

## 镜像源管理工具

```shell
# 安装nrm
npm install -g nrm

# 查看可用的镜像源和当前使用的镜像源
nrm ls

# 切换镜像源
nrm use taobao

# 测试镜像源的响应时间
nrm test taobao

# 添加镜像源
nrm add taobao https://npm.taobao.org/mirrors/node/

# 删除镜像源
nrm del taobao
```
