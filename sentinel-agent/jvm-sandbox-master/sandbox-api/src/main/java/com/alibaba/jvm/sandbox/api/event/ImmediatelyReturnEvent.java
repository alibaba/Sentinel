package com.alibaba.jvm.sandbox.api.event;

/**
 * 立即返回事件
 * 该事件并非原生的方法返回事件，而是由{@link com.alibaba.jvm.sandbox.api.ProcessControlException#throwReturnImmediately(Object)}
 * 所引起的返回事件
 *
 * @author luanjia@taobao.com
 */
public class ImmediatelyReturnEvent extends ReturnEvent {

    /**
     * 构造立即返回事件
     *
     * @param processId 调用过程ID
     * @param invokeId  调用ID
     * @param object    调用返回值(void方法返回值为null)
     */
    public ImmediatelyReturnEvent(final int processId,
                                  final int invokeId,
                                  final Object object) {
        super(Type.IMMEDIATELY_RETURN, processId, invokeId, object);
    }

}
