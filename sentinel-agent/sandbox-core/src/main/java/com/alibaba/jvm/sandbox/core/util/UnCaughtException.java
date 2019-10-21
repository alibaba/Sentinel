package com.alibaba.jvm.sandbox.core.util;

/**
 * 未捕获异常
 * 用来封装不希望抛出的异常
 * Created by luanjia@taobao.com on 16/5/21.
 */
public class UnCaughtException extends RuntimeException {

    public UnCaughtException(Throwable cause) {
        super(cause);
    }
}
