package com.alibaba.jvm.sandbox.api.event;

/**
 * 方法调用BEFORE事件
 *
 * @author luanjia@taobao.com
 */
public class BeforeEvent extends InvokeEvent {

    /**
     * 触发调用事件的ClassLoader
     */
    public final ClassLoader javaClassLoader;

    /**
     * 获取触发调用事件的类名称
     */
    public final String javaClassName;

    /**
     * 获取触发调用事件的方法名称
     */
    public final String javaMethodName;

    /**
     * 获取触发调用事件的方法签名
     */
    public final String javaMethodDesc;

    /**
     * 获取触发调用事件的对象
     */
    public final Object target;

    /**
     * 获取触发调用事件的方法参数
     */
    public final Object[] argumentArray;

    /**
     * 构造调用BEFORE事件
     *
     * @param processId       调用过程ID
     * @param invokeId        调用ID
     * @param javaClassLoader 触发调用事件的ClassLoader
     * @param javaClassName   触发调用事件的类名称
     * @param javaMethodName  触发调用事件的方法名称
     * @param javaMethodDesc  触发调用事件的方法签名
     * @param target          触发调用事件的对象(静态方法为null)
     * @param argumentArray   触发调用事件的方法参数
     */
    public BeforeEvent(final int processId,
                       final int invokeId,
                       final ClassLoader javaClassLoader,
                       final String javaClassName,
                       final String javaMethodName,
                       final String javaMethodDesc,
                       final Object target,
                       final Object[] argumentArray) {
        super(processId, invokeId, Type.BEFORE);
        this.javaClassLoader = javaClassLoader;
        this.javaClassName = javaClassName;
        this.javaMethodName = javaMethodName;
        this.javaMethodDesc = javaMethodDesc;
        this.target = target;
        this.argumentArray = argumentArray;
    }

    /**
     * 改变方法入参
     *
     * @param index       方法入参编号(从0开始)
     * @param changeValue 改变的值
     * @return this
     * @since {@code sandbox-api:1.0.10}
     */
    public BeforeEvent changeParameter(final int index,
                                       final Object changeValue) {
        argumentArray[index] = changeValue;
        return this;
    }

}
