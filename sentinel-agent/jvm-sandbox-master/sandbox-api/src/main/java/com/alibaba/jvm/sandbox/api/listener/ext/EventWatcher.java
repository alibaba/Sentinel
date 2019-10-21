package com.alibaba.jvm.sandbox.api.listener.ext;

/**
 * 事件观察者
 *
 * @author luanjia@taobao.com
 * @since {@code sandbox-api:1.0.10}
 */
public interface EventWatcher extends EventWatchBuilder.IBuildingForUnWatching {

    /**
     * 获取本次观察事件ID
     *
     * @return 本次观察事件ID
     */
    int getWatchId();

}
