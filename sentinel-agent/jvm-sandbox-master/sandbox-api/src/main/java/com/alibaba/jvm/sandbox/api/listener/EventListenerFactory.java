package com.alibaba.jvm.sandbox.api.listener;

/**
 * 事件监听器工厂类
 *
 * @author luanjia@taobao.com
 * @since {@code sandbox-api:1.0.10}
 * @deprecated
 */
@Deprecated
public interface EventListenerFactory {

    /**
     * 生产事件监听器
     *
     * @return 事件监听器
     */
    EventListener createEventListener();

}
