package com.luguosong.ioc.dependency_injection;

/**
 * @author luguosong
 */
public class Jdbc {
    private String url;

    private String username;

    private String password;

    private String driver;

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "Jdbc{" +
                "driver='" + driver + '\'' +
                ", url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
