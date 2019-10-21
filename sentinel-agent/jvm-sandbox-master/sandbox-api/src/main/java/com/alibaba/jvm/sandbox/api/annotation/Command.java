package com.alibaba.jvm.sandbox.api.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 模块命令注解，拥有此注解的模块方法将能接收到从{@code sandbox.sh -d} 发出的命令
 * <p>
 * 有这个注解的方法只能接收四类型的参数:
 * <ul>
 * <li>命令参数: {@code Map<String,String>}</li>
 * <li>命令参数: {@code Map<String,String[]>}</li>
 * <li>命令参数: {@code String}</li>
 * <li>文本输出: {@code PrintWriter}</li>
 * </ul>
 *
 * @author luanjia@taobao.com
 * @since {@code sandbox-api:1.2.0}
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface Command {

    /**
     * 命令名称
     *
     * @return 命令名称
     */
    String value();

}
