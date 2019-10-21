package com.alibaba.jvm.sandbox.api.listener;

import com.alibaba.jvm.sandbox.api.event.Event;

/**
 * 事件监听器
 *
 * @author luanjia@taobao.com
 */
public interface EventListener {

    /**
     * 触发事件处理
     * <p>
     * 事件处理是整个sandbox最重要的一环,也是整个系统设计的精髓所在
     * </p>
     * <pre>
     * {@code
     * 事件流转流程
     *
     *                                        +-------+
     *                                        |       |
     * +========+  <return>             +========+    | <return immediately>
     * |        |  <return immediately> |        |    |
     * | BEFORE |---------------------->| RETURN |<---+
     * |        |                       |        |
     * +========+                       +========+
     *     |                              |    ^
     *     |         <throws immediately> |    |
     *     |                              |    | <return immediately>
     *     |                              v    |
     *     |                            +========+
     *     |                            |        |
     *     +--------------------------->| THROWS |<---+
     *                    <throws>      |        |    |
     *        <throws immediately>      +========+    | <throws immediately>
     *                                        |       |
     *                                        +-------+
     * }
     * </pre>
     *
     * @param event 触发事件
     * @throws Throwable 处理异常
     */
    void onEvent(Event event) throws Throwable;

}
