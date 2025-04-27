---
icon: material/microsoft-windows
---

# windows命令

## 查看端口占用

```shell
# 查看所有端口
netstat -ano

# 查看指定端口
netstat -ano|findstr "8080"
```

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405151732766.png){ loading=lazy }
  <figcaption>观察结果中的PID</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405151733757.png){ loading=lazy }
  <figcaption>在任务栏管理器中找到对应进程</figcaption>
</figure>
