package com.alibaba.jvm.sandbox.qatest.core.manager;

import com.alibaba.jvm.sandbox.api.Module;
import com.alibaba.jvm.sandbox.api.ModuleLifecycle;

import java.util.ArrayList;
import java.util.List;

import static com.alibaba.jvm.sandbox.qatest.core.util.AssertUtils.assertArrayEquals;

/**
 * 追踪生命周期管理模块
 */
public abstract class TracingLifeCycleModule implements ModuleLifecycle, Module {

    private final List<LifeCycleType> lifeCycleTypeTracings = new ArrayList<LifeCycleType>();

    @Override
    public void onLoad() throws Throwable {
        lifeCycleTypeTracings.add(LifeCycleType.LOAD);
    }

    @Override
    public void onUnload() {
        lifeCycleTypeTracings.add(LifeCycleType.UNLOAD);
    }

    @Override
    public void onActive() {
        lifeCycleTypeTracings.add(LifeCycleType.ACTIVE);
    }

    @Override
    public void onFrozen() {
        lifeCycleTypeTracings.add(LifeCycleType.FROZEN);
    }

    @Override
    public void loadCompleted() {
        lifeCycleTypeTracings.add(LifeCycleType.LOAD_COMPLETED);
    }

    public void assertTracing(final LifeCycleType... exceptLifeCycleTypes) {
        assertArrayEquals(
                exceptLifeCycleTypes,
                lifeCycleTypeTracings.toArray(new LifeCycleType[]{})
        );
    }


    /**
     * 生命周期类型
     */
    public enum LifeCycleType {
        LOAD,
        UNLOAD,
        ACTIVE,
        FROZEN,
        LOAD_COMPLETED
    }

}
