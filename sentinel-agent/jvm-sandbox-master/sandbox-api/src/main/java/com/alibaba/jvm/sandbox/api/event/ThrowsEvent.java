package com.alibaba.jvm.sandbox.api.event;

/**
 * 异常/错误抛出事件
 *
 * @author luanjia@taobao.com
 */
public class ThrowsEvent extends InvokeEvent {

    /**
     * 抛出的异常/错误信息
     */
    public final Throwable throwable;

    /**
     * 构造异常/错误抛出调用事件
     *
     * @param processId 调用过程ID
     * @param invokeId  调用ID
     * @param throwable 抛出的异常/错误信息
     */
    public ThrowsEvent(final int processId,
                       final int invokeId,
                       final Throwable throwable) {
        super(processId, invokeId, Type.THROWS);
        this.throwable = throwable;
    }

    /**
     * 构造异常/错误抛出调用事件
     * 主要开放给{@link ImmediatelyThrowsEvent}构造所使用
     *
     * @param type      必须是{@link Type#THROWS}或{@link Type#IMMEDIATELY_THROWS}两者之一的值
     * @param processId 调用过程ID
     * @param invokeId  调用ID
     * @param throwable 抛出的异常/错误信息
     */
    ThrowsEvent(final Type type,
                final int processId,
                final int invokeId,
                final Throwable throwable) {
        super(processId, invokeId, type);
        this.throwable = throwable;

        // 对入参进行校验
        if (type != Type.THROWS
                && type != Type.IMMEDIATELY_THROWS) {
            throw new IllegalArgumentException(String.format("type must be %s or %s", Type.THROWS, Type.IMMEDIATELY_THROWS));
        }

    }

}
