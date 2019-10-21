package com.alibaba.jvm.sandbox.api;

/**
 * 模块生命周期适配器，用于简化接口实现
 *
 * @author dadiyang
 * @since {@code sandbox-api:1.2.2}
 */
public class ModuleLifecycleAdapter implements ModuleLifecycle {
    @Override
    public void onLoad() throws Throwable {
    }

    @Override
    public void onUnload() throws Throwable {

    }

    @Override
    public void onActive() throws Throwable {

    }

    @Override
    public void onFrozen() throws Throwable {

    }

    @Override
    public void loadCompleted() {

    }
}
