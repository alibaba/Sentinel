package com.alibaba.jvm.sandbox.api.event;

/**
 * 调用事件
 * JVM方法调用事件
 *
 * @author luanjia@taobao.com
 */
public abstract class Event {

    /**
     * 事件类型
     */
    public final Type type;

    /**
     * 构造调用事件
     *
     * @param type 事件类型
     */
    protected Event(Type type) {
        this.type = type;
    }

    /**
     * 事件枚举类型
     */
    public enum Type {

        /**
         * 调用:BEFORE
         */
        BEFORE,

        /**
         * 调用:RETURN
         */
        RETURN,

        /**
         * 调用:THROWS
         */
        THROWS,

        /**
         * 调用:LINE
         * 一行被调用了
         */
        LINE,


        //
        // CALL事件系列是从GREYS中衍生过来的事件，它描述了一个方法内部，调用其他方法的过程。整个过程可以被描述成为三个阶段
        //
        // void test() {
        //     # CALL_BEFORE
        //     try {
        //         logger.info("TEST");
        //         # CALL_RETURN
        //     } catch(Throwable cause) {
        //         # CALL_THROWS
        //     }
        // }
        //

        /**
         * 调用:CALL_BEFORE
         * 一个方法被调用之前
         */
        CALL_BEFORE,

        /**
         * 调用:CALL_RETURN
         * 一个方法被调用正常返回之后
         */
        CALL_RETURN,

        /**
         * 调用:CALL_THROWS
         * 一个方法被调用抛出异常之后
         */
        CALL_THROWS,


        /**
         * 立即调用:RETURN
         * 由{@link com.alibaba.jvm.sandbox.api.ProcessControlException#throwReturnImmediately(Object)}触发
         */
        IMMEDIATELY_RETURN,

        /**
         * 立即调用:THROWS
         * 由{@link com.alibaba.jvm.sandbox.api.ProcessControlException#throwThrowsImmediately(Throwable)}触发
         */
        IMMEDIATELY_THROWS,

    }

}
