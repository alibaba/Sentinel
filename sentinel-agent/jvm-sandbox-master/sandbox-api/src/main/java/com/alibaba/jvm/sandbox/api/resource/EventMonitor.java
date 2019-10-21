package com.alibaba.jvm.sandbox.api.resource;

import com.alibaba.jvm.sandbox.api.event.Event;

/**
 * 事件监控器
 *
 * @author luanjia@taobao.com
 * @since {@code sandbox-api:1.0.9}
 * @deprecated 后续不再支持事件池
 */
@Deprecated
public interface EventMonitor {

    /**
     * 事件池信息
     *
     * @author luanjia@taobao.com
     * @since {@code sandbox-api:1.0.9}
     * @deprecated 后续不再支持事件池
     */
    interface EventPoolInfo {

        /**
         * 获取已分配事件对象总数量
         *
         * @return 已分配事件对象总数量
         */
        int getNumActive();

        /**
         * 获取指定事件类型已分配对象数量
         *
         * @param type 指定事件类型
         * @return 指定事件类型已分配数量
         */
        int getNumActive(Event.Type type);

        /**
         * 获取事件对象总空闲数量
         *
         * @return 事件对象总空闲数量
         */
        int getNumIdle();

        /**
         * 获取指定事件类型总空闲对象数量
         *
         * @param type 指定事件类型
         * @return 指定事件类型总空闲对象数量
         */
        int getNumIdle(Event.Type type);

    }

    /**
     * 获取事件池信息
     *
     * @return 获取事件池信息
     */
    EventPoolInfo getEventPoolInfo();

}
