package com.alibaba.jvm.sandbox.api.listener.ext;

import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.event.InvokeEvent;
import com.alibaba.jvm.sandbox.api.util.LazyGet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 行为通知
 *
 * @author luanjia@taobao.com
 * @since {@code sandbox-api:1.0.10}
 */
public class Advice implements Attachment {

    private final int processId;
    private final int invokeId;

    private final ClassLoader loader;
    private final LazyGet<Behavior> behaviorLazyGet;
    private final Object[] parameterArray;
    private final Object target;

    private Object returnObj;
    private Throwable throwable;

    private Object attachment;
    private Set<String> marks = new HashSet<String>();

    private Advice top = this;
    private Advice parent = this;
    private Event.Type state = Event.Type.BEFORE;

    /**
     * 构造通知
     *
     * @param processId       {@link InvokeEvent#processId}
     * @param invokeId        {@link InvokeEvent#invokeId}
     * @param behaviorLazyGet 触发事件的行为(懒加载)
     * @param loader          触发事件的行为所在ClassLoader
     * @param parameterArray  触发事件的行为入参
     * @param target          触发事件所归属的对象实例
     */
    Advice(final int processId,
           final int invokeId,
           final LazyGet<Behavior> behaviorLazyGet,
           final ClassLoader loader,
           final Object[] parameterArray,
           final Object target) {
        this.processId = processId;
        this.invokeId = invokeId;
        this.behaviorLazyGet = behaviorLazyGet;
        this.loader = loader;
        this.parameterArray = parameterArray;
        this.target = target;
    }

    /**
     * 应用BEFORE
     *
     * @param top    集联顶层调用的通知
     * @param parent 集联上一个调用的通知
     * @return this
     */
    Advice applyBefore(final Advice top,
                       final Advice parent) {
        this.top = top;
        this.parent = parent;
        return this;
    }

    /**
     * 应用返回结果，应用返回结果之后，通知将变为返回通知
     *
     * @param returnObj 行为返回的对象
     * @return this
     */
    Advice applyReturn(final Object returnObj) {
        this.returnObj = returnObj;
        this.state = Event.Type.RETURN;
        return this;
    }

    /**
     * 应用行为抛出的异常，应用异常之后，通知将变为异常通知
     *
     * @param throwable 行为抛出的异常
     * @return this
     */
    Advice applyThrows(final Throwable throwable) {
        this.throwable = throwable;
        this.state = Event.Type.THROWS;
        return this;
    }

    public boolean isReturn() {
        return this.state == Event.Type.RETURN;
    }

    public boolean isThrows() {
        return this.state == Event.Type.THROWS;
    }

    /**
     * 改变方法入参
     *
     * @param index       方法入参编号(从0开始)
     * @param changeValue 改变的值
     * @return this
     */
    public Advice changeParameter(final int index,
                                  final Object changeValue) {
        parameterArray[index] = changeValue;
        return this;
    }

    /**
     * @return InvokeEvent#processId
     */
    public int getProcessId() {
        return processId;
    }

    /**
     * @return InvokeEvent#invokeId
     */
    public int getInvokeId() {
        return invokeId;
    }

    /**
     * 获取触发事件的行为
     * <p>
     * 一般而言能触发事件的行为是：普通方法和构造函数
     * </p>
     *
     * @return 触发事件的行为
     */
    public Behavior getBehavior() {
        return behaviorLazyGet.get();
    }

    /**
     * 获取触发事件的行为所在的ClassLoader
     *
     * @return 触发事件的行为所在的ClassLoader
     * @since {@code sandbox-api:1.2.2}
     */
    public ClassLoader getLoader() {
        return loader;
    }

    /**
     * 获取触发事件的行为入参
     *
     * @return 触发事件的行为入参
     */
    public Object[] getParameterArray() {
        return parameterArray;
    }

    /**
     * 获取触发事件所归属的对象实例
     *
     * @return 触发事件所归属的对象实例
     */
    public Object getTarget() {
        return target;
    }

    /**
     * 获取行为的返回结果
     * <ul>
     * <li>如果是构造函数，返回当前对象的实例</li>
     * <li>如果是普通方法，返回方法的返回值</li>
     * </ul>
     *
     * @return 行为的返回结果
     */
    public Object getReturnObj() {
        return returnObj;
    }

    /**
     * 获取行为抛出的异常
     *
     * @return 行为抛出的异常
     */
    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public void attach(final Object attachment) {
        this.attachment = attachment;
    }

    @Override
    public <E> E attachment() {
        return (E) attachment;
    }

    @Override
    public int hashCode() {
        return processId + invokeId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Advice) {
            final Advice advice = (Advice) obj;
            return processId == advice.processId
                    && invokeId == advice.invokeId;
        } else {
            return false;
        }
    }

    /**
     * 对通知进行标记
     *
     * @param mark 标记
     */
    public void mark(final String mark) {
        marks.add(mark);
    }

    /**
     * 通知是否拥有期待的标记
     *
     * @param exceptMark 期待删除的标记
     * @return TRUE:拥有;FALSE:不拥有
     */
    public boolean hasMark(final String exceptMark) {
        return marks.contains(exceptMark);
    }

    /**
     * 删除标记
     *
     * @param mark 要删除的标记
     * @return TRUE:标记曾经存在，现已删；FALSE：标记从未存在，现已删；
     */
    public boolean unMark(final String mark) {
        return marks.remove(mark);
    }

    /**
     * 添加附件并打上标记
     *
     * @param attachment 附件
     * @param mark       标记
     */
    public void attach(final Object attachment,
                       final String mark) {
        attach(attachment);
        mark(mark);
    }

    /**
     * 是否整个递进调用过程中的顶层通知
     *
     * @return TRUE:是;FALSE:否
     */
    public boolean isProcessTop() {
        return parent == this;
    }

    /**
     * 获取调用顶层通知
     *
     * @return 调用顶层通知
     */
    public Advice getProcessTop() {
        return top;
    }

    /**
     * 列出调用链路上所有拥有期待标记的调用通知
     *
     * @param exceptMark 期待标记
     * @return 调用链路上所有拥有期待标记的调用通知
     */
    public List<Advice> listHasMarkOnChain(final String exceptMark) {
        final List<Advice> advices = new ArrayList<Advice>();
        if (hasMark(exceptMark)) {
            advices.add(this);
        }
        if (!isProcessTop()) {
            advices.addAll(parent.listHasMarkOnChain(exceptMark));
        }
        return advices;
    }

}

