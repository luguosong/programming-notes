package com.example.creational.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 建造者模式 - 正例
 * 内部 Builder 类负责组装，build() 时做校验，构造出不可变的完整对象
 */
public class BuilderExample {
    public static void main(String[] args) {
        // ✅ 链式调用，每个参数含义清晰，且 build() 保证校验
        EmailMessage email = new EmailMessage.Builder("hello@example.com", "重置密码通知")
                .from("noreply@example.com")
                .cc(Arrays.asList("admin@example.com", "log@example.com"))
                .body("<p>请在24小时内完成密码重置</p>")
                .htmlEnabled(true)
                .priority(1)
                .build();

        System.out.println(email);

        // 使用 Director 封装常用构建流程
        EmailMessage welcome = EmailDirector.buildWelcomeEmail("zhangsan@example.com", "张三");
        System.out.println(welcome);
    }
}

// 产品类：不可变的邮件消息
class EmailMessage {
    private final String       to;
    private final String       subject;
    private final String       from;
    private final List<String> cc;
    private final String       body;
    private final boolean      htmlEnabled;
    private final int          priority;

    // 私有构造器：只能通过 Builder 创建
    private EmailMessage(Builder builder) {
        this.to          = builder.to;
        this.subject     = builder.subject;
        this.from        = builder.from;
        this.cc          = Collections.unmodifiableList(builder.cc);
        this.body        = builder.body;
        this.htmlEnabled = builder.htmlEnabled;
        this.priority    = builder.priority;
    }

    @Override
    public String toString() {
        return "EmailMessage{to=" + to + ", subject=" + subject
             + ", from=" + from + ", cc=" + cc
             + ", html=" + htmlEnabled + ", priority=" + priority + "}";
    }

    // ✅ Builder：内部类，链式设置参数
    public static class Builder {
        // 必填参数
        private final String to;
        private final String subject;
        // 可选参数（带默认值）
        private String       from        = "no-reply@example.com";
        private List<String> cc          = new ArrayList<>();
        private String       body        = "";
        private boolean      htmlEnabled = false;
        private int          priority    = 3;

        public Builder(String to, String subject) {
            if (to == null || to.isBlank())      throw new IllegalArgumentException("收件人不能为空");
            if (subject == null || subject.isBlank()) throw new IllegalArgumentException("主题不能为空");
            this.to      = to;
            this.subject = subject;
        }

        public Builder from(String from)            { this.from        = from;        return this; }
        public Builder cc(List<String> cc)          { this.cc          = cc;          return this; }
        public Builder body(String body)            { this.body        = body;        return this; }
        public Builder htmlEnabled(boolean html)    { this.htmlEnabled = html;        return this; }
        public Builder priority(int priority)       { this.priority    = priority;    return this; }

        public EmailMessage build() {
            // 构建时做业务校验，保证产品完整性
            if (htmlEnabled && (body == null || body.isBlank())) {
                throw new IllegalStateException("HTML 邮件必须提供正文");
            }
            return new EmailMessage(this);
        }
    }
}

// Director：封装常用构建配方，进一步简化调用方
class EmailDirector {
    public static EmailMessage buildWelcomeEmail(String toEmail, String userName) {
        return new EmailMessage.Builder(toEmail, "欢迎加入 " + userName + "！")
                .from("welcome@example.com")
                .body("<h1>欢迎 " + userName + "</h1>")
                .htmlEnabled(true)
                .priority(2)
                .build();
    }
}
