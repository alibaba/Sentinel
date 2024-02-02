package com.alibaba.csp.sentinel.demo.spring.webmvc.exception;

/**
 * @author by lfy
 * @Date 2022/8/3 20:40
 */
public class WebMvcException extends RuntimeException {

    public WebMvcException() {

    }

    public WebMvcException(String msg) {
        super(msg);
    }

    public WebMvcException(Throwable t) {
        super(t);
    }
}
