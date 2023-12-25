package com.luguosong._06_io;

import java.nio.charset.Charset;

/**
 * 打印系统默认字符集
 *
 * @author luguosong
 */
public class CharSetExample {
    public static void main(String[] args) {
        System.out.println(Charset.defaultCharset()); // UTF-8
    }
}
