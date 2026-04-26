package com.example.creational.abstract_factory;

/**
 * 抽象工厂模式 - 反例
 * 问题：直接 new 具体组件，切换 UI 平台需要修改大量代码
 */
public class AbstractFactoryBadExample {
    public static void main(String[] args) {
        // ❌ 硬编码了具体平台，切换到 Mac 要改所有 new 语句
        ApplicationBad app = new ApplicationBad("windows");
        app.render();
    }
}

// ❌ Application 直接 new 具体组件
class ApplicationBad {
    private final String platform;

    public ApplicationBad(String platform) {
        this.platform = platform;
    }

    public void render() {
        ButtonBad  button;
        InputBad   input;
        DialogBad  dialog;

        if ("windows".equals(platform)) {
            button = new WindowsButton();
            input  = new WindowsInput();
            dialog = new WindowsDialog();
        } else if ("mac".equals(platform)) {
            button = new MacButton();        // ❌ 每次都要在这里判断
            input  = new MacInput();
            dialog = new MacDialog();
        } else {
            throw new IllegalArgumentException("未知平台：" + platform);
        }

        button.render();
        input.render();
        dialog.render();
    }
}

// 抽象 UI 组件
interface ButtonBad { void render(); }
interface InputBad  { void render(); }
interface DialogBad { void render(); }

// Windows 组件
class WindowsButton implements ButtonBad { public void render() { System.out.println("[Windows] 按钮"); } }
class WindowsInput  implements InputBad  { public void render() { System.out.println("[Windows] 输入框"); } }
class WindowsDialog implements DialogBad { public void render() { System.out.println("[Windows] 对话框"); } }

// Mac 组件
class MacButton implements ButtonBad { public void render() { System.out.println("[Mac] 按钮"); } }
class MacInput  implements InputBad  { public void render() { System.out.println("[Mac] 输入框"); } }
class MacDialog implements DialogBad { public void render() { System.out.println("[Mac] 对话框"); } }
