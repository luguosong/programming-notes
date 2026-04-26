package com.example.structural.facade;

/**
 * 外观模式 - 反例
 * 问题：客户端直接调用所有子系统，依赖细节，顺序容易出错
 */
public class FacadeBadExample {
    public static void main(String[] args) {
        // ❌ 客户端需要知道所有子系统的调用细节和顺序
        LightsBad  lights   = new LightsBad();
        ProjectorBad projector = new ProjectorBad();
        AmplifierBad amp    = new AmplifierBad();
        DVDPlayerBad dvd    = new DVDPlayerBad();

        // 开启电影模式（客户端必须记住每个步骤）
        lights.dim(10);
        projector.on();
        projector.setInput("HDMI");
        amp.on();
        amp.setVolume(50);
        dvd.on();
        dvd.play("星际穿越");

        System.out.println("--- 停止 ---");

        // 关闭时顺序也要正确
        dvd.stop();
        dvd.off();
        amp.off();
        projector.off();
        lights.on(); // 每次都要记顺序 ❌
    }
}

class LightsBad   {
    public void on()          { System.out.println("灯光：打开");      }
    public void off()         { System.out.println("灯光：关闭");      }
    public void dim(int level){ System.out.println("灯光：调暗到 " + level + "%"); }
}
class ProjectorBad {
    public void on()          { System.out.println("投影仪：开启");    }
    public void off()         { System.out.println("投影仪：关闭");    }
    public void setInput(String src) { System.out.println("投影仪：输入源 " + src); }
}
class AmplifierBad {
    public void on()          { System.out.println("功放：开启");      }
    public void off()         { System.out.println("功放：关闭");      }
    public void setVolume(int v) { System.out.println("功放：音量 " + v); }
}
class DVDPlayerBad {
    public void on()          { System.out.println("DVD：开机");       }
    public void off()         { System.out.println("DVD：关机");       }
    public void play(String m){ System.out.println("DVD：播放 " + m);  }
    public void stop()        { System.out.println("DVD：停止");       }
}
