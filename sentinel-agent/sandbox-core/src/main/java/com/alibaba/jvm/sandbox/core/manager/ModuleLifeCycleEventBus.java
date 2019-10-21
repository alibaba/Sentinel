package com.alibaba.jvm.sandbox.core.manager;

import com.alibaba.jvm.sandbox.core.domain.CoreModule;

/**
 * 模块生命周期事件总线
 * Created by luanjia@taobao.com on 2017/2/3.
 */
public interface ModuleLifeCycleEventBus {

    /**
     * 添加模块生命周期监听器
     * <p>被添加的监听器将按顺序完成生命周期的通知</p>
     *
     * @param lifeCycleEventListener 模块生命周期监听
     */
    void append(ModuleLifeCycleEventListener lifeCycleEventListener);

    /**
     * 通知事件
     *
     * @param coreModule 被通知的沙箱模块
     * @param event      通知事件类型
     */
    void fire(CoreModule coreModule, Event event);

    /**
     * 模块事件类型
     */
    enum Event {

        /**
         * 模块加载
         */
        LOAD,

        /**
         * 模块加载完成
         */
        LOAD_COMPLETED,

        /**
         * 模块卸载
         */
        UNLOAD,

        /**
         * 模块激活
         */
        ACTIVE,

        /**
         * 模块冻结
         */
        FROZE
    }

    /**
     * 模块生命周期事件监听器
     */
    interface ModuleLifeCycleEventListener {

        /**
         * 模块生命周期事件到达
         *
         * @param coreModule 被通知的沙箱模块
         * @param event      通知事件类型
         * @return TRUE  : 继续保持监听，下次有事件通知时继续接收消息
         * FALSE : 放弃后续的监听
         */
        boolean onFire(CoreModule coreModule, Event event);

    }

}