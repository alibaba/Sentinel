package com.alibaba.jvm.sandbox.api.event;

/**
 * 方法调用RETURN事件
 *
 * @author luanjia@taobao.com
 */
public class ReturnEvent extends InvokeEvent {

    /**
     * 调用返回值
     */
    public final Object object;

    /**
     * 构造调用RETURN事件
     *
     * @param processId 调用过程ID
     * @param invokeId  调用ID
     * @param object    调用返回值(void方法返回值为null)
     */
    public ReturnEvent(final int processId,
                       final int invokeId,
                       final Object object) {
        super(processId, invokeId, Type.RETURN);
        this.object = object;
    }

    /**
     * 构造调用RETURN事件，
     * 主要开放给{@link ImmediatelyReturnEvent}构造所使用
     *
     * @param type      必须是{@link Type#RETURN}或{@link Type#IMMEDIATELY_RETURN}两者之一的值
     * @param processId 调用过程ID
     * @param invokeId  调用ID
     * @param object    调用返回值(void方法返回值为null)
     */
    ReturnEvent(final Type type,
                final int processId,
                final int invokeId,
                final Object object) {
        super(processId, invokeId, type);
        this.object = object;

        // 对入参进行校验
        if (type != Type.IMMEDIATELY_RETURN
                && type != Type.RETURN) {
            throw new IllegalArgumentException(String.format("type must be %s or %s", Type.RETURN, Type.IMMEDIATELY_RETURN));
        }

    }

}
