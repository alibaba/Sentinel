package com.alibaba.jvm.sandbox.api.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * HTTP服务请求
 *
 * @author luanjia@taobao.com
 * @deprecated 请使用 {@link com.alibaba.jvm.sandbox.api.annotation.Command}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface Http {

    /**
     * HTTP请求路径
     * 路径是基于<span>/sandbox/module/<b><i>{MODULE_UNIQUE_ID}</i></b>/http</span>作为请求前缀
     *
     * @return 请求路径
     */
    String value();

    /**
     * 期待的请求方法
     * 目前只支持HTTP的GET方法和POST方法，默认是两者都支持
     *
     * @return 请求方法
     */
    Method[] method() default {Method.GET, Method.POST};

    /**
     * HTTP请求方法
     */
    enum Method {

        /**
         * HTTP's GET method
         */
        GET,

        /**
         * HTTP's POST method
         */
        POST
    }

}
