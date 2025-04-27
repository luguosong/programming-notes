---
icon: octicons/play-16
---

# PotPlayer设置

## 按住→倍速播放

- 右键->选项->播放->速度调整单位

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202501041817866.png){ loading=lazy }
  <figcaption>将速度调整单位设置为1</figcaption>
</figure>

- 下载`AutoHotkey`。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202501041155370.png){ loading=lazy }
  <figcaption>AutoHotkey</figcaption>
</figure>

- 创建脚本

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202501041157283.png){ loading=lazy }
  <figcaption>创建脚本</figcaption>
</figure>

- 编写`potplayer加速.ahk`脚本

``` ahk

#Requires AutoHotkey v2.0
; Potplayer 长按倍速播放
#HotIf WinActive("ahk_class PotPlayer64 ahk_exe PotPlayerMini64.exe")
Right::     ; 模仿B站长按快进功能：长按0.3秒方向右键进行倍速播放，松开时恢复
{
    if !(KeyWait("Right", "T0.3")) { ; 按下按键持续0.3s
        Send("c") ; 加速x3播放，每个c表示+0.1x，可以自行修改c的数量
        ToolTip(">>>")  ; 脚本执行的提示符
        KeyWait("Right") ;松开按键
        ; Send("z") ; 播放速度复原, 旧版代码
        Send("x") ;输入等量的x可以恢复为原速。而不是直接回1x
        ToolTip("")
    }
    else {
        Send("{Right}")  ; 如果按键按下未持续0.3s，则触发Potplayer原始快捷键
    }
    return
}

```

- `Win+R` 输入 `shell:startup`,将脚本文件放入启动文件夹，开机自动启动。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202501041812296.png){ loading=lazy }
  <figcaption>打开开机启动目录</figcaption>
</figure>


## 关闭自动更新

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202501041839807.png){ loading=lazy }
  <figcaption>关闭自动更新</figcaption>
</figure>

- 手动检测更新

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202501041840080.png){ loading=lazy }
  <figcaption>右键->关于</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202501041841937.png){ loading=lazy }
  <figcaption>手动检测更新</figcaption>
</figure>

## 打开视频时将同级目录视频加入播放列表

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202501041912704.png){ loading=lazy }
  <figcaption>打开视频时将同级目录视频加入播放列表</figcaption>
</figure>

## 启动时窗口的位置

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202501041918863.png){ loading=lazy }
  <figcaption>启动时窗口的位置</figcaption>
</figure>

## 单击暂停

PotPlayer默认为双击暂停

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202501041843877.png){ loading=lazy }
  <figcaption>单击暂停</figcaption>
</figure>

## 双击全屏

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202501041851271.png){ loading=lazy }
  <figcaption>双击全屏</figcaption>
</figure>

## 显示视频播放进度

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202501041900247.png){ loading=lazy }
  <figcaption>显示播放信息和进度</figcaption>
</figure>

## 记住上次播放位置

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202501041902081.png){ loading=lazy }
  <figcaption>记住播放位置</figcaption>
</figure>

## 鼠标进度缩略图

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202501041903654.png){ loading=lazy }
  <figcaption>鼠标进度缩略图</figcaption>
</figure>

## 设置右键快进时间

默认为5秒，这里改为1秒

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202501041905608.png){ loading=lazy }
  <figcaption>设置时间跨度</figcaption>
</figure>

## 缩放时保持宽高比

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202501042056038.png){ loading=lazy }
  <figcaption>缩放时保持宽高比</figcaption>
</figure>
