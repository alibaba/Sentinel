package com.alibaba.jvm.sandbox.module.debug.textui;

/**
 * 命令行控件
 *
 * @author oldmanpushcart@gmail.com
 */
public interface TComponent {

    /**
     * 渲染组件内容
     *
     * @return 组件内容(TEXT)
     */
    String rendering();

}