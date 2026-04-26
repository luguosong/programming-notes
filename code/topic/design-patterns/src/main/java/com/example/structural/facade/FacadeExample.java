package com.example.structural.facade;

/**
 * 外观模式 - 正例
 * HomeTheaterFacade 封装所有子系统调用，客户端只需一行命令
 */
public class FacadeExample {
    public static void main(String[] args) {
        // ✅ 客户端只依赖外观，不需要了解子系统细节
        HomeTheaterFacade theater = new HomeTheaterFacade(
                new Lights(), new Projector(), new Amplifier(), new DVDPlayer()
        );

        theater.watchMovie("星际穿越"); // 一行完成所有设备联动 ✅
        System.out.println("--- 结束 ---");
        theater.endMovie();             // 一行完成所有关闭 ✅
    }
}

// 子系统（单独保持不变）
class Lights {
    public void on()           { System.out.println("灯光：打开");          }
    public void off()          { System.out.println("灯光：关闭");          }
    public void dim(int level) { System.out.println("灯光：调暗到 " + level + "%"); }
}

class Projector {
    public void on()               { System.out.println("投影仪：开启");     }
    public void off()              { System.out.println("投影仪：关闭");     }
    public void setInput(String s) { System.out.println("投影仪：输入 " + s); }
}

class Amplifier {
    public void on()            { System.out.println("功放：开启");          }
    public void off()           { System.out.println("功放：关闭");          }
    public void setVolume(int v){ System.out.println("功放：音量 " + v);     }
}

class DVDPlayer {
    public void on()           { System.out.println("DVD：开机");            }
    public void off()          { System.out.println("DVD：关机");            }
    public void play(String m) { System.out.println("DVD：播放《" + m + "》"); }
    public void stop()         { System.out.println("DVD：停止");            }
}

// ✅ 外观：封装"观影模式"和"结束模式"两组复杂操作
class HomeTheaterFacade {
    private final Lights    lights;
    private final Projector projector;
    private final Amplifier amplifier;
    private final DVDPlayer dvdPlayer;

    public HomeTheaterFacade(Lights lights, Projector projector,
                             Amplifier amplifier, DVDPlayer dvdPlayer) {
        this.lights    = lights;
        this.projector = projector;
        this.amplifier = amplifier;
        this.dvdPlayer = dvdPlayer;
    }

    // 一键开启观影模式
    public void watchMovie(String movie) {
        System.out.println("=== 准备观影模式 ===");
        lights.dim(10);
        projector.on();
        projector.setInput("HDMI");
        amplifier.on();
        amplifier.setVolume(50);
        dvdPlayer.on();
        dvdPlayer.play(movie);
    }

    // 一键关闭所有设备
    public void endMovie() {
        System.out.println("=== 关闭影院模式 ===");
        dvdPlayer.stop();
        dvdPlayer.off();
        amplifier.off();
        projector.off();
        lights.on();
    }
}
