package com.alibaba.jvm.sandbox.api.event;

/**
 * 方法调用追踪事件:BEFORE
 *
 * @author luanjia@taobao.com
 */
public class CallBeforeEvent extends InvokeEvent {

    /**
     * 代码行号
     */
    public final int lineNumber;

    /**
     * 调用类名
     */
    public final String owner;

    /**
     * 调用方法名
     */
    public final String name;

    /**
     * 调用方法描述
     */
    public final String desc;

    /**
     * 构造调用事件
     *
     * @param processId  调用过程ID
     * @param invokeId   调用ID
     * @param lineNumber 代码行号
     * @param owner      调用类名
     * @param name       调用方法名
     * @param desc       调用方法描述
     */
    public CallBeforeEvent(final int processId,
                           final int invokeId,
                           final int lineNumber,
                           final String owner,
                           final String name,
                           final String desc) {
        super(processId, invokeId, Type.CALL_BEFORE);
        this.lineNumber = lineNumber;
        this.owner = owner;
        this.name = name;
        this.desc = desc;
    }

}
