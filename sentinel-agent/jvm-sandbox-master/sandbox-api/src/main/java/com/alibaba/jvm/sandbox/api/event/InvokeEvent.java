package com.alibaba.jvm.sandbox.api.event;

/**
 * 方法调用事件
 *
 * @author luanjia@taobao.com
 */
public abstract class InvokeEvent extends Event {

    /**
     * 调用过程ID
     */
    public final int processId;

    /**
     * 调用ID
     */
    public final int invokeId;

    /**
     * 构造调用事件
     *
     * @param processId 调用过程ID
     * @param invokeId  调用ID
     * @param type      事件类型
     */
    protected InvokeEvent(int processId, int invokeId, Type type) {
        super(type);
        this.processId = processId;
        this.invokeId = invokeId;
    }

}
