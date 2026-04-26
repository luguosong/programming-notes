package com.example.creational.builder;

import java.util.List;

/**
 * 建造者模式 - 反例
 * 问题1：构造器参数过多，调用方不知道每个位置的含义
 * 问题2：使用 JavaBean 设值，对象在构建完成前处于不一致状态
 */
public class BuilderBadExample {
    public static void main(String[] args) {
        // ❌ 方式1：重叠构造器——参数太多，顺序容易搞错
        HttpRequestBad request = new HttpRequestBad(
            "https://api.example.com/users",
            "GET",
            null,
            30,
            3,
            true
        );
        System.out.println(request);

        // ❌ 方式2：JavaBean 风格——对象构建中途可被使用，状态不一致
        HttpRequestBad javaBeanStyle = new HttpRequestBad();
        javaBeanStyle.setUrl("https://api.example.com/users");
        // 忘记调用 setMethod，对象已经可以被传递出去了 ❌
        System.out.println(javaBeanStyle);
    }
}

class HttpRequestBad {
    private String  url;
    private String  method;
    private String  body;
    private int     timeout;
    private int     retryCount;
    private boolean followRedirects;

    // ❌ 重叠构造器：参数太多，极易传错位置
    public HttpRequestBad(String url, String method, String body,
                          int timeout, int retryCount, boolean followRedirects) {
        this.url             = url;
        this.method          = method;
        this.body            = body;
        this.timeout         = timeout;
        this.retryCount      = retryCount;
        this.followRedirects = followRedirects;
    }

    // ❌ JavaBean 默认构造器：允许创建"半成品"对象
    public HttpRequestBad() {}

    public void setUrl(String url)                      { this.url             = url;             }
    public void setMethod(String method)                { this.method          = method;          }
    public void setTimeout(int timeout)                 { this.timeout         = timeout;         }
    public void setRetryCount(int retryCount)           { this.retryCount      = retryCount;      }
    public void setFollowRedirects(boolean f)           { this.followRedirects = f;               }

    @Override
    public String toString() {
        return "HttpRequest{url=" + url + ", method=" + method
             + ", timeout=" + timeout + ", retry=" + retryCount + "}";
    }
}
