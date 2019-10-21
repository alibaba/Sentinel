package com.alibaba.jvm.sandbox.api.event;

import static com.alibaba.jvm.sandbox.api.event.Event.Type.LINE;

/**
 * 方法调用行事件
 *
 * @author luanjia@taobao.com
 */
public class LineEvent extends InvokeEvent {

    public final int lineNumber;

    /**
     * 构造调用事件
     *
     * @param processId  调用过程ID
     * @param invokeId   调用ID
     * @param lineNumber 代码行号
     */
    public LineEvent(int processId, int invokeId, int lineNumber) {
        super(processId, invokeId, LINE);
        this.lineNumber = lineNumber;
    }

}
