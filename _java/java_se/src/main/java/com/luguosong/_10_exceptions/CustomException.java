package com.luguosong._10_exceptions;

/**
 * 自定义异常
 *
 * @author luguosong
 */
public class CustomException {


    public static void main(String[] args) {
        //捕获编译时异常，并将其转为运行时异常抛出
        try {
            testCheckedException();
        } catch (MyCheckedException e) {
            throw new RuntimeException(e);
        }

        //运行时异常，不需要显示处理
        testRuntimeException();
    }

    /**
     * @throws MyCheckedException 抛出异常
     */
    public static void testCheckedException() throws MyCheckedException {
        throw new MyCheckedException("test CheckedException");
    }

    /**
     * 抛出自定义运行时异常
     */
    public static void testRuntimeException() {
        throw new MyRuntimeException("test RuntimeException");
    }

    /**
     * 自定义编译时异常
     */
    static class MyCheckedException extends Exception {

        public MyCheckedException(String message) {
            super(message);
        }
    }

    /**
     * 自定义运行时异常
     */
    static class MyRuntimeException extends RuntimeException {

        public MyRuntimeException(String message) {
            super(message);
        }
    }
}
