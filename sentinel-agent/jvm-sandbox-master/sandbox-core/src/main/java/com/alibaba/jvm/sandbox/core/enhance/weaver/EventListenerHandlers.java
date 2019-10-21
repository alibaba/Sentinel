package com.alibaba.jvm.sandbox.core.enhance.weaver;

import com.alibaba.jvm.sandbox.api.ProcessControlException;
import com.alibaba.jvm.sandbox.api.event.BeforeEvent;
import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.event.ImmediatelyReturnEvent;
import com.alibaba.jvm.sandbox.api.event.ImmediatelyThrowsEvent;
import com.alibaba.jvm.sandbox.api.listener.EventListener;
import com.alibaba.jvm.sandbox.core.util.ObjectIDs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.com.alibaba.jvm.sandbox.spy.Spy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.alibaba.jvm.sandbox.api.event.Event.Type.IMMEDIATELY_RETURN;
import static com.alibaba.jvm.sandbox.api.event.Event.Type.IMMEDIATELY_THROWS;
import static com.alibaba.jvm.sandbox.core.util.SandboxReflectUtils.isInterruptEventHandler;
import static java.com.alibaba.jvm.sandbox.spy.Spy.Ret.newInstanceForNone;
import static org.apache.commons.lang3.ArrayUtils.contains;
import static org.apache.commons.lang3.StringUtils.join;

/**
 * 事件处理
 *
 * @author luanjia@taobao.com
 */
public class EventListenerHandlers {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // 调用序列生成器
    // private final Sequencer invokeIdSequencer = new Sequencer();
    private final AtomicInteger invokeIdSequencer = new AtomicInteger(1000);

    // 全局处理器ID:处理器映射集合
    private final Map<Integer/*LISTENER_ID*/, EventProcessor> mappingOfEventProcessor
            = new ConcurrentHashMap<Integer, EventProcessor>();

    /**
     * 注册事件处理器
     *
     * @param listenerId 事件监听器ID
     * @param listener   事件监听器
     * @param eventTypes 监听事件集合
     */
    public void active(final int listenerId,
                       final EventListener listener,
                       final Event.Type[] eventTypes) {
        mappingOfEventProcessor.put(listenerId, new EventProcessor(listenerId, listener, eventTypes));
        logger.info("activated listener[id={};target={};] event={}",
                listenerId,
                listener,
                join(eventTypes, ",")
        );
    }

    /**
     * 取消事件处理器
     *
     * @param listenerId 事件处理器ID
     */
    public void frozen(int listenerId) {
        final EventProcessor processor = mappingOfEventProcessor.remove(listenerId);
        if (null == processor) {
            logger.debug("ignore frozen listener={}, because not found.", listenerId);
            return;
        }

        logger.info("frozen listener[id={};target={};]",
                listenerId,
                processor.listener
        );

        // processor.clean();
    }

    /**
     * 调用出发事件处理&调用执行流程控制
     *
     * @param listenerId 处理器ID
     * @param processId  调用过程ID
     * @param invokeId   调用ID
     * @param event      调用事件
     * @param processor  事件处理器
     * @return 处理返回结果
     * @throws Throwable 当出现未知异常时,且事件处理器为中断流程事件时抛出
     */
    private Spy.Ret handleEvent(final int listenerId,
                                final int processId,
                                final int invokeId,
                                final Event event,
                                final EventProcessor processor) throws Throwable {
        final EventListener listener = processor.listener;

        try {

            if (contains(processor.eventTypes, event.type)) {
                // 调用事件处理
                listener.onEvent(event);
                if (logger.isDebugEnabled()) {
                    logger.debug("on-event: event|{}|{}|{}@listener|{}",
                            event.type,
                            processId,
                            invokeId,
                            listenerId
                    );
                }
            }

        }

        // 代码执行流程变更
        catch (ProcessControlException pce) {

            final EventProcessor.Process process = processor.processRef.get();

            final ProcessControlException.State state = pce.getState();
            logger.debug("on-event: event|{}|{}|{};listener|{}, process-changed: {}. isIgnoreProcessEvent={};",
                    event.type,
                    processId,
                    invokeId,
                    listenerId,
                    state,
                    pce.isIgnoreProcessEvent()
            );

            // 如果流程控制要求忽略后续处理所有事件，则需要在此处进行标记
            // 标记当前线程中、当前EventListener中需要主动忽略的ProcessId
            if (pce.isIgnoreProcessEvent()) {
                process.markIgnoreProcessId(processId);
            }

            switch (state) {

                // 立即返回对象
                case RETURN_IMMEDIATELY: {

                    // 如果在BeforeEvent处理过程中发生ProcessControl行为，将会造成堆栈错位
                    // 所以这里需要将错位的堆栈进行补齐
                    if (event instanceof BeforeEvent) {
                        process.popInvokeId();
                    }

                    // 如果已经禁止后续返回任何事件了，则不进行后续的操作
                    if (pce.isIgnoreProcessEvent()) {
                        logger.debug("on-event: event|{}|{}|{};listener|{}, ignore immediately-return-event, isIgnored.",
                                event.type,
                                processId,
                                invokeId,
                                listenerId
                        );
                        return Spy.Ret.newInstanceForReturn(pce.getRespond());
                    }

                    // 如果没有注册监听ImmediatelyEvent事件，则不进行后续的操作
                    if (!contains(processor.eventTypes, IMMEDIATELY_RETURN)) {
                        logger.debug("on-event: event|{}|{}|{};listener|{}, ignore immediately-return-event, not contains.",
                                event.type,
                                processId,
                                invokeId,
                                listenerId
                        );
                        return Spy.Ret.newInstanceForReturn(pce.getRespond());
                    }

                    // 这里需要补偿ImmediatelyEvent
                    final ImmediatelyReturnEvent immediatelyReturnEvent
                            = process
                            .getEventFactory()
                            .makeImmediatelyReturnEvent(processId, invokeId, pce.getRespond());

                    final Spy.Ret ret;
                    try {
                        ret = handleEvent(
                                listenerId,
                                processId,
                                invokeId,
                                immediatelyReturnEvent,
                                processor
                        );
                    } finally {
                        process.getEventFactory().returnEvent(immediatelyReturnEvent);
                    }

                    if (ret.state == Spy.Ret.RET_STATE_NONE) {
                        return Spy.Ret.newInstanceForReturn(pce.getRespond());
                    } else {
                        // 如果不是,则返回最新的处理结果
                        return ret;
                    }

                }

                // 立即抛出异常
                case THROWS_IMMEDIATELY: {

                    final Throwable throwable = (Throwable) pce.getRespond();

                    // 如果在BeforeEvent处理过程中发生ProcessControl行为，将会造成堆栈错位
                    // 所以这里需要将错位的堆栈进行补齐
                    if (event instanceof BeforeEvent) {
                        process.popInvokeId();
                    }

                    // 如果已经禁止后续返回任何事件了，则不进行后续的操作
                    if (pce.isIgnoreProcessEvent()) {
                        logger.debug("on-event: event|{}|{}|{};listener|{}, ignore immediately-throws-event, isIgnored.",
                                event.type,
                                processId,
                                invokeId,
                                listenerId
                        );
                        return Spy.Ret.newInstanceForThrows(throwable);
                    }

                    // 如果没有注册监听ImmediatelyEvent事件，则不进行后续的操作
                    if (!contains(processor.eventTypes, IMMEDIATELY_THROWS)) {
                        logger.debug("on-event: event|{}|{}|{};listener|{}, ignore immediately-throws-event, not contains.",
                                event.type,
                                processId,
                                invokeId,
                                listenerId
                        );
                        return Spy.Ret.newInstanceForThrows(throwable);
                    }

                    // 如果已经禁止后续返回任何事件了，则不进行后续的操作
                    if (pce.isIgnoreProcessEvent()) {
                        return Spy.Ret.newInstanceForThrows(throwable);
                    }

                    final ImmediatelyThrowsEvent immediatelyThrowsEvent
                            = process
                            .getEventFactory()
                            .makeImmediatelyThrowsEvent(processId, invokeId, throwable);


                    final Spy.Ret ret;
                    try {
                        ret = handleEvent(
                                listenerId,
                                processId,
                                invokeId,
                                immediatelyThrowsEvent,
                                processor
                        );
                    } finally {
                        process.getEventFactory().returnEvent(immediatelyThrowsEvent);
                    }

                    if (ret.state == Spy.Ret.RET_STATE_NONE) {
                        return Spy.Ret.newInstanceForThrows(throwable);
                    } else {
                        // 如果不是,则返回最新的处理结果
                        return ret;
                    }

                }

                // 什么都不操作，立即返回
                case NONE_IMMEDIATELY:
                default: {
                    return newInstanceForNone();
                }
            }

        }

        // BEFORE处理异常,打日志,并通知下游不需要进行处理
        catch (Throwable throwable) {

            // 如果当前事件处理器是可中断的事件处理器,则对外抛出UnCaughtException
            // 中断当前方法
            if (isInterruptEventHandler(listener.getClass())) {
                throw throwable;
            }

            // 普通事件处理器则可以打个日志后,直接放行
            else {
                logger.warn("on-event: event|{}|{}|{};listener|{} occur an error.",
                        event.type,
                        processId,
                        invokeId,
                        listenerId,
                        throwable
                );
            }
        }

        // 默认返回不进行任何流程变更
        return newInstanceForNone();
    }

    private Spy.Ret handleOnBefore(final int listenerId,
                                   final int targetClassLoaderObjectID,
                                   final String javaClassName,
                                   final String javaMethodName,
                                   final String javaMethodDesc,
                                   final Object target,
                                   final Object[] argumentArray) throws Throwable {

        // 获取事件处理器
        final EventProcessor processor = mappingOfEventProcessor.get(listenerId);

        // 如果尚未注册,则直接返回,不做任何处理
        if (null == processor) {
            logger.debug("listener={} is not activated, ignore processing before-event.", listenerId);
            return newInstanceForNone();
        }

        // 获取调用跟踪信息
        final EventProcessor.Process process = processor.processRef.get();

        // 调用ID
        final int invokeId = invokeIdSequencer.getAndIncrement();
        process.pushInvokeId(invokeId);

        // 调用过程ID
        final int processId = process.getProcessId();

        // 如果当前处理ID被忽略，则立即返回
        if (process.touchIsIgnoreProcess(processId)) {
            process.popInvokeId();
            return newInstanceForNone();
        }

        final ClassLoader javaClassLoader = ObjectIDs.instance.getObject(targetClassLoaderObjectID);
        final BeforeEvent event = process.getEventFactory().makeBeforeEvent(
                processId,
                invokeId,
                javaClassLoader,
                javaClassName,
                javaMethodName,
                javaMethodDesc,
                target,
                argumentArray
        );
        try {
            return handleEvent(listenerId, processId, invokeId, event, processor);
        } finally {
            process.getEventFactory().returnEvent(event);
        }
    }

    /*
     * 判断堆栈是否错位
     */
    private boolean checkProcessStack(final int processId,
                                      final int invokeId,
                                      final boolean isEmptyStack) {
        return (processId == invokeId && !isEmptyStack)
                || (processId != invokeId && isEmptyStack);
    }

    private Spy.Ret handleOnEnd(final int listenerId,
                                final Object object,
                                final boolean isReturn) throws Throwable {

        final EventProcessor wrap = mappingOfEventProcessor.get(listenerId);

        // 如果尚未注册,则直接返回,不做任何处理
        if (null == wrap) {
            logger.debug("listener={} is not activated, ignore processing return-event|throws-event.", listenerId);
            return newInstanceForNone();
        }

        final EventProcessor.Process process = wrap.processRef.get();

        // 如果当前调用过程信息堆栈是空的,说明
        // 1. BEFORE/RETURN错位
        // 2. super.<init>
        // 处理方式是直接返回,不做任何事件的处理和代码流程的改变,放弃对super.<init>的观察，可惜了
        if (process.isEmptyStack()) {
            return newInstanceForNone();
        }

        final int processId = process.getProcessId();
        final int invokeId = process.popInvokeId();

        // 如果PID==IID说明已经到栈顶，此时需要核对堆栈是否为空
        // 如果不为空需要输出日志进行告警
        if (checkProcessStack(processId, invokeId, process.isEmptyStack())) {
            logger.warn("ERROR process-stack. pid={};iid={};listener={};",
                    processId,
                    invokeId,
                    listenerId
            );
        }

        // 忽略事件处理
        // 放在stack.pop()后边是为了对齐执行栈
        if (process.touchIsIgnoreProcess(processId)) {
            return newInstanceForNone();
        }

        final Event event = isReturn
                ? process.getEventFactory().makeReturnEvent(processId, invokeId, object)
                : process.getEventFactory().makeThrowsEvent(processId, invokeId, (Throwable) object);

        try {
            return handleEvent(listenerId, processId, invokeId, event, wrap);
        } finally {
            process.getEventFactory().returnEvent(event);
        }

    }

    private void handleOnLine(final int listenerId,
                              final int lineNumber) throws Throwable {
        final EventProcessor wrap = mappingOfEventProcessor.get(listenerId);
        if (null == wrap) {
            logger.debug("listener={} is not activated, ignore processing line-event.", listenerId);
            return;
        }

        final EventProcessor.Process process = wrap.processRef.get();

        // 如果当前调用过程信息堆栈是空的,说明BEFORE/LINE错位
        // 处理方式是直接返回,不做任何事件的处理和代码流程的改变
        if (process.isEmptyStack()) {
            return;
        }

        final int processId = process.getProcessId();
        final int invokeId = process.getInvokeId();

        // 如果事件处理流被忽略，则直接返回，不产生后续事件
        if (process.touchIsIgnoreProcess(processId)) {
            return;
        }

        final Event event = process.getEventFactory().makeLineEvent(processId, invokeId, lineNumber);
        try {
            handleEvent(listenerId, processId, invokeId, event, wrap);
        } finally {
            process.getEventFactory().returnEvent(event);
        }

    }

    private void handleOnCallBefore(final int listenerId,
                                    final int lineNumber,
                                    final String owner,
                                    final String name,
                                    final String desc) throws Throwable {
        final EventProcessor wrap = mappingOfEventProcessor.get(listenerId);
        if (null == wrap) {
            logger.debug("listener={} is not activated, ignore processing call-before-event.", listenerId);
            return;
        }

        final EventProcessor.Process process = wrap.processRef.get();

        // 如果当前调用过程信息堆栈是空的,有两种情况
        // 1. CALL_BEFORE事件和BEFORE事件错位
        // 2. 当前方法是<init>，而CALL_BEFORE事件触发是当前方法在调用父类的<init>
        //    super.<init>会导致CALL_BEFORE事件优先于BEFORE事件
        // 但如果按照现在的架构要兼容这种情况，比较麻烦，所以暂时先放弃了这部分的消息，可惜可惜
        if (process.isEmptyStack()) {
            return;
        }

        final int processId = process.getProcessId();
        final int invokeId = process.getInvokeId();

        // 如果事件处理流被忽略，则直接返回，不产生后续事件
        if (process.touchIsIgnoreProcess(processId)) {
            return;
        }

        final Event event = process
                .getEventFactory()
                .makeCallBeforeEvent(processId, invokeId, lineNumber, owner, name, desc);
        try {
            handleEvent(listenerId, processId, invokeId, event, wrap);
        } finally {
            process.getEventFactory().returnEvent(event);
        }

    }

    private void handleOnCallReturn(final int listenerId) throws Throwable {

        final EventProcessor wrap = mappingOfEventProcessor.get(listenerId);
        if (null == wrap) {
            logger.debug("listener={} is not activated, ignore processing call-return-event.", listenerId);
            return;
        }

        final EventProcessor.Process process = wrap.processRef.get();
        if (process.isEmptyStack()) {
            return;
        }

        final int processId = process.getProcessId();
        final int invokeId = process.getInvokeId();

        // 如果事件处理流被忽略，则直接返回，不产生后续事件
        if (process.touchIsIgnoreProcess(processId)) {
            return;
        }

        final Event event = process
                .getEventFactory()
                .makeCallReturnEvent(processId, invokeId);
        try {
            handleEvent(listenerId, processId, invokeId, event, wrap);
        } finally {
            process.getEventFactory().returnEvent(event);
        }

    }

    private void handleOnCallThrows(final int listenerId,
                                    final String throwException) throws Throwable {
        final EventProcessor wrap = mappingOfEventProcessor.get(listenerId);
        if (null == wrap) {
            logger.debug("listener={} is not activated, ignore processing call-throws-event.", listenerId);
            return;
        }

        final EventProcessor.Process process = wrap.processRef.get();
        if (process.isEmptyStack()) {
            return;
        }

        final int processId = process.getProcessId();
        final int invokeId = process.getInvokeId();

        // 如果事件处理流被忽略，则直接返回，不产生后续事件
        if (process.touchIsIgnoreProcess(processId)) {
            return;
        }

        final Event event = process
                .getEventFactory()
                .makeCallThrowsEvent(processId, invokeId, throwException);
        try {
            handleEvent(listenerId, processId, invokeId, event, wrap);
        } finally {
            process.getEventFactory().returnEvent(event);
        }
    }


    // ----------------------------------- 从这里开始就是提供给Spy的static方法 -----------------------------------

    private static EventListenerHandlers singleton = new EventListenerHandlers();

    public static EventListenerHandlers getSingleton() {
        return singleton;
    }

    public static Object onBefore(final int listenerId,
                                  final int targetClassLoaderObjectID,
                                  final Class<?> spyRetClassInTargetClassLoader,
                                  final String javaClassName,
                                  final String javaMethodName,
                                  final String javaMethodDesc,
                                  final Object target,
                                  final Object[] argumentArray) throws Throwable {
        return singleton.handleOnBefore(
                listenerId,
                targetClassLoaderObjectID,
                javaClassName,
                javaMethodName,
                javaMethodDesc,
                target,
                argumentArray
        );
    }

    public static Object onReturn(final int listenerId,
                                  final Class<?> spyRetClassInTargetClassLoader,
                                  final Object object) throws Throwable {
        return singleton.handleOnEnd(listenerId, object, true);
    }

    public static Object onThrows(final int listenerId,
                                  final Class<?> spyRetClassInTargetClassLoader,
                                  final Throwable throwable) throws Throwable {
        return singleton.handleOnEnd(listenerId, throwable, false);
    }

    public static void onLine(final int listenerId,
                              final int lineNumber) throws Throwable {
        singleton.handleOnLine(listenerId, lineNumber);
    }

    public static void onCallBefore(final int listenerId,
                                    final int lineNumber,
                                    final String owner,
                                    final String name,
                                    final String desc) throws Throwable {
        singleton.handleOnCallBefore(listenerId, lineNumber, owner, name, desc);
    }

    public static void onCallReturn(final int listenerId) throws Throwable {
        singleton.handleOnCallReturn(listenerId);
    }

    public static void onCallThrows(final int listenerId,
                                    final String throwException) throws Throwable {
        singleton.handleOnCallThrows(listenerId, throwException);
    }

    // ---- 自检查
    public void checkEventProcessor(final int... listenerIds) {
        for (int listenerId : listenerIds) {
            final EventProcessor processor = mappingOfEventProcessor.get(listenerId);
            if (null == processor) {
                throw new IllegalStateException(String.format("listener=%s not existed.", listenerId));
            }
            processor.check();
        }
    }

}
