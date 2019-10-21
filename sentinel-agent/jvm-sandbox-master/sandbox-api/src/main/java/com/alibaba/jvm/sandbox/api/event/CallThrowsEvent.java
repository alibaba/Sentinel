package com.alibaba.jvm.sandbox.api.event;

/**
 * 方法调用追踪事件:THROWS
 *
 * @author luanjia@taobao.com
 */
public class CallThrowsEvent extends InvokeEvent {

    /**
     * 抛出的异常类名称
     */
    public final String throwException;

    /**
     * 构造调用事件
     *
     * @param processId      调用过程ID
     * @param invokeId       调用ID
     * @param throwException 抛出的异常类名称
     */
    public CallThrowsEvent(int processId, int invokeId, String throwException) {
        super(processId, invokeId, Type.CALL_THROWS);
        this.throwException = throwException;
    }

}
