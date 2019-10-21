package com.alibaba.jvm.sandbox.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 模块信息
 *
 * @author luanjia@taobao.com
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Information {

    /**
     * 未知版本
     */
    String UNKNOW_VERSION = "UNKNOW_VERSION";

    /**
     * 未知作者
     */
    String UNKNOW_AUTHOR = "UNKNOW_AUTHOR";

    /**
     * 模块ID
     * 全JVM唯一标记了一个模块，所有基于模块的操作都将基于此ID来完成
     *
     * @return 模块ID
     */
    String id();

    /**
     * 模块期待沙箱的加载模式
     * <p>
     * 一些模块对沙箱的启动方式有特殊要求，比如必须要求沙箱以{@code AGENT}的方式启动才能正常工作
     * 通过设置这个标记位可以让沙箱加载模块的时候做一个判断，不符合模块期待的，当前模块将不会被加载
     * </p>
     *
     * @return 期待沙箱的加载模式
     */
    Mode[] mode() default {Mode.AGENT, Mode.ATTACH};

    /**
     * 是否在加载时候就激活模块。
     *
     * @return 当值为TRUE时，模块加载完成后会主动激活模块；
     * 当值为FALSE时，模块加载完成后状态为冻结
     */
    boolean isActiveOnLoad() default true;

    /**
     * 定义模块版本号
     *
     * @return 模块版本号
     */
    String version() default UNKNOW_VERSION;

    /**
     * 定义模块作者
     *
     * @return 模块作者
     */
    String author() default UNKNOW_AUTHOR;

    /**
     * 加载方式
     */
    enum Mode {

        /**
         * 通过agent方式加载
         */
        AGENT,

        /**
         * 通过attach方式加载
         */
        ATTACH,
    }

}
