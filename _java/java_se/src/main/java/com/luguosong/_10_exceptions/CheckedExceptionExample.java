package com.luguosong._10_exceptions;

/**
 * 编译时异常
 *
 * @author luguosong
 */
public class CheckedExceptionExample {
    public static void main(String[] args) {
        //catchException捕获编译时异常，并将其转为运行时异常抛出
        catchException();

        //对于抛出的编译时异常，调用方必须进行处理
        try {
            throwsException();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            /*
             * 不管是否发生异常，finally中的代码都会执行
             * finally中的代码一般用于资源的释放，比如IO流的关闭、数据库连接的关闭等
             * */
            System.out.println("throwsException finally");
        }
    }

    /**
     * 捕获异常，抛出运行时异常
     */
    public static void catchException() {
        try {
            Class<?> aClass = Class.forName("java.lang.String");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            /*
             * 不管是否发生异常，finally中的代码都会执行
             * finally中的代码一般用于资源的释放，比如IO流的关闭、数据库连接的关闭等
             * */
            System.out.println("catchException finally");
        }
    }

    /**
     * @throws ClassNotFoundException 抛出异常
     */
    public static void throwsException() throws ClassNotFoundException {
        Class<?> aClass = Class.forName("lang.Hello");
    }
}
