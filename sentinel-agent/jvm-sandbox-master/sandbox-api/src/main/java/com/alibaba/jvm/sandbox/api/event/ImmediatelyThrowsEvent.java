package com.alibaba.jvm.sandbox.api.event;

/**
 * 立即异常抛出事件
 * 该事件并非原生的方法异常抛出事件，而是由{@link com.alibaba.jvm.sandbox.api.ProcessControlException#throwThrowsImmediately(Throwable)}
 * 所引起的异常抛出事件
 *
 * @author luanjia@taobao.com
 */
public class ImmediatelyThrowsEvent extends ThrowsEvent {

    /**
     * 构造立即异常抛出事件
     *
     * @param processId 调用过程ID
     * @param invokeId  调用ID
     * @param throwable 抛出的异常/错误信息
     */
    public ImmediatelyThrowsEvent(final int processId,
                                  final int invokeId,
                                  final Throwable throwable) {
        super(Type.IMMEDIATELY_THROWS, processId, invokeId, throwable);
    }

}
