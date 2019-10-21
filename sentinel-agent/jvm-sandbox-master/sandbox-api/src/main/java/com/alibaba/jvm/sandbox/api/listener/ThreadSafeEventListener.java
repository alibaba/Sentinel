package com.alibaba.jvm.sandbox.api.listener;

import com.alibaba.jvm.sandbox.api.event.Event;

/**
 * 线程安全的事件监听器
 * <p>
 * 每一个线程都会开启一个独立的{@link EventListener}进行处理
 * </p>
 *
 * @author luanjia@taobao.com
 * @since {@code sandbox-api:1.0.10}
 * @deprecated
 */
@Deprecated
public class ThreadSafeEventListener implements EventListener {

    private final EventListenerFactory factory;
    private final ThreadLocal<EventListener> eventListenerRef = new ThreadLocal<EventListener>() {
        @Override
        protected EventListener initialValue() {
            return factory.createEventListener();
        }
    };

    /**
     * 构造线程安全的事件监听器
     *
     * @param factory 事件监听器工厂
     */
    public ThreadSafeEventListener(EventListenerFactory factory) {
        this.factory = factory;
    }

    @Override
    public void onEvent(Event event) throws Throwable {
        eventListenerRef.get().onEvent(event);
    }
}
