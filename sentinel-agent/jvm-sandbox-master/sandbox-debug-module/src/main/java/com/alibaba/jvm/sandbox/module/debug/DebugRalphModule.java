package com.alibaba.jvm.sandbox.module.debug;

import com.alibaba.jvm.sandbox.api.Information;
import com.alibaba.jvm.sandbox.api.Module;
import com.alibaba.jvm.sandbox.api.annotation.Command;
import com.alibaba.jvm.sandbox.api.event.BeforeEvent;
import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.event.InvokeEvent;
import com.alibaba.jvm.sandbox.api.http.printer.ConcurrentLinkedQueuePrinter;
import com.alibaba.jvm.sandbox.api.http.printer.Printer;
import com.alibaba.jvm.sandbox.api.listener.EventListener;
import com.alibaba.jvm.sandbox.api.listener.ext.EventWatchBuilder;
import com.alibaba.jvm.sandbox.api.listener.ext.EventWatcher;
import com.alibaba.jvm.sandbox.api.resource.ModuleEventWatcher;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.lang3.EnumUtils;
import org.kohsuke.MetaInfServices;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static com.alibaba.jvm.sandbox.api.ProcessController.throwsImmediately;
import static com.alibaba.jvm.sandbox.api.event.Event.Type.*;

/**
 * 故障模拟
 * <p>
 * 模块名字取自我喜欢的一部动画《Wreck-It Ralph》（无敌破坏王），
 * 能对任意方法模拟指定的故障类型：
 * <ul>
 * <li>方法延时：delay</li>
 * <li>方法异常：exception</li>
 * <li>速率控制：r-limit</li>
 * <li>并发控制：c-limit</li>
 * </ul>
 */
@MetaInfServices(Module.class)
@Information(id = "debug-ralph", version = "0.0.2", author = "luanjia@taobao.com")
public class DebugRalphModule extends ParamSupported implements Module {

    @Resource
    private ModuleEventWatcher moduleEventWatcher;

    /*
     * 并发控制
     * -d 'debug-ralph/c-limit?class=<CLASS>&method=<METHOD>&c=<CONCURRENT>'
     */
    @Command("c-limit")
    public void concurrentLimit(final Map<String, String> param, final PrintWriter writer) {

        final Printer printer = new ConcurrentLinkedQueuePrinter(writer);

        // --- 解析参数 ---

        final String cnPattern = getParameter(param, "class");
        final String mnPattern = getParameter(param, "method");
        final int concurrent = getParameter(param, "c", int.class);

        final EventWatcher watcher = new EventWatchBuilder(moduleEventWatcher)
                .onClass(cnPattern)
                .includeSubClasses()
                .includeBootstrap()
                .onBehavior(mnPattern)
                .onWatching()
                .withProgress(new ProgressPrinter(printer))
                .onWatch(new EventListener() {

                    // 设定一个本次拦截共享的并发限制器，所有被匹配上的类的入口
                    // 将会共同被同一个并发限制！
                    final Semaphore sph = new Semaphore(concurrent);

                    // 是否一次拦截调用链的入口
                    private boolean isProcessTop(InvokeEvent event) {
                        return event.processId == event.invokeId;
                    }

                    @Override
                    public void onEvent(Event event) throws Throwable {

                        final InvokeEvent iEvent = (InvokeEvent) event;
                        // 不是顶层调用，说明之前已经通过并发控制的闸门，可以不受到并发的制约
                        if (!isProcessTop(iEvent)) {
                            return;
                        }

                        switch (event.type) {
                            case BEFORE: {
                                final BeforeEvent bEvent = (BeforeEvent) event;
                                // 如果是顶层的调用，必须通过流控获取继续调用的门票
                                // 没有拿到门票的就让他快速失败掉
                                if (!sph.tryAcquire()) {
                                    printer.println(String.format(
                                            "%s.%s will be limit by concurrent: %s on %s",
                                            bEvent.javaClassName,
                                            bEvent.javaMethodName,
                                            concurrent,
                                            Thread.currentThread().getName()
                                    ));
                                    throwsImmediately(new RuntimeException("concurrent-limit by Ralph!!!"));
                                }
                                break;
                            }
                            case RETURN:
                            case THROWS: {
                                sph.release();
                                break;
                            }

                        }

                    }//onEvent

                }, BEFORE, RETURN, THROWS);

        // --- 等待结束 ---

        try {
            printer.println(String.format(
                    "concurrent-limit on [%s#%s] concurrent:%s.\nPress CTRL_C abort it!",
                    cnPattern,
                    mnPattern,
                    concurrent
            ));
            printer.waitingForBroken();
        } finally {
            watcher.onUnWatched();
        }

    }


    /*
     * 速率控制
     * -d 'debug-ralph/r-limit?class=<CLASS>&method=<METHOD>&c=<RATE>'
     */
    @Command("r-limit")
    public void rateLimit(final Map<String, String> param, final PrintWriter writer) {

        final Printer printer = new ConcurrentLinkedQueuePrinter(writer);

        // --- 解析参数 ---

        final String cnPattern = getParameter(param, "class");
        final String mnPattern = getParameter(param, "method");
        final double rate = getParameter(param, "r", double.class);

        final EventWatcher watcher = new EventWatchBuilder(moduleEventWatcher)
                .onClass(cnPattern)
                .includeSubClasses()
                .includeBootstrap()
                .onBehavior(mnPattern)
                .onWatching()
                .withProgress(new ProgressPrinter(printer))
                .onWatch(new EventListener() {

                    // 设定一个本次拦截共享的速率限制器，所有被匹配上的类的入口
                    // 将会共同被同一个速率限速！
                    final RateLimiter limiter = RateLimiter.create(rate);

                    // 是否一次拦截调用链的入口
                    private boolean isProcessTop(InvokeEvent event) {
                        return event.processId == event.invokeId;
                    }

                    @Override
                    public void onEvent(Event event) throws Throwable {
                        final BeforeEvent bEvent = (BeforeEvent) event;

                        // 不是顶层调用，说明之前已经通过流控的闸门，可以不受到流控的制约
                        if (!isProcessTop(bEvent)) {
                            return;
                        }

                        // 如果是顶层的调用，必须通过流控获取继续调用的门票
                        // 没有拿到门票的就让他快速失败掉
                        if (!limiter.tryAcquire()) {
                            printer.println(String.format(
                                    "%s.%s will be limit by rate: %s on %s",
                                    bEvent.javaClassName,
                                    bEvent.javaMethodName,
                                    rate,
                                    Thread.currentThread().getName()
                            ));
                            throwsImmediately(new RuntimeException("rate-limit by Ralph!!!"));
                        }

                    }

                }, BEFORE);

        // --- 等待结束 ---

        try {
            printer.println(String.format(
                    "rate-limit on [%s#%s] rate:%.2f(TPS).\nPress CTRL_C abort it!",
                    cnPattern,
                    mnPattern,
                    rate
            ));
            printer.waitingForBroken();
        } finally {
            watcher.onUnWatched();
        }

    }


    /**
     * 异常工厂
     */
    interface ExceptionFactory {
        Exception newInstance(String message);
    }

    /**
     * 异常类型
     */
    enum ExceptionType {
        IOException(new ExceptionFactory() {
            @Override
            public Exception newInstance(String message) {
                return new IOException(message);
            }
        }),
        NullPointException(new ExceptionFactory() {
            @Override
            public Exception newInstance(String message) {
                return new NullPointerException(message);
            }
        }),
        RuntimeException(new ExceptionFactory() {
            @Override
            public Exception newInstance(String message) {
                return new RuntimeException(message);
            }
        }),
        TimeoutException(new ExceptionFactory() {
            @Override
            public Exception newInstance(String message) {
                return new TimeoutException(message);
            }
        });

        private final ExceptionFactory factory;

        ExceptionType(final ExceptionFactory factory) {
            this.factory = factory;
        }

        public Exception throwIt(final String message) throws Exception {
            return factory.newInstance(message);
        }

    }

    /*
     * 注入异常
     * -d 'debug-ralph/wreck?class=<CLASS>&method=<METHOD>&type=<EXCEPTION-TYPE>'
     */
    @Command("wreck")
    public void exception(final Map<String, String> param, final PrintWriter writer) {

        final Printer printer = new ConcurrentLinkedQueuePrinter(writer);

        // --- 解析参数 ---

        final String cnPattern = getParameter(param, "class");
        final String mnPattern = getParameter(param, "method");
        final ExceptionType exType = getParameter(
                param,
                "type",
                new Converter<ExceptionType>() {
                    @Override
                    public ExceptionType convert(String string) {
                        return EnumUtils.getEnum(ExceptionType.class, string);
                    }
                },
                ExceptionType.RuntimeException
        );

        // --- 开始增强 ---

        final EventWatcher watcher = new EventWatchBuilder(moduleEventWatcher)
                .onClass(cnPattern)
                .includeSubClasses()
                .includeBootstrap()
                .onBehavior(mnPattern)
                .onWatching()
                .withProgress(new ProgressPrinter(printer))
                .onWatch(new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Throwable {

                        final BeforeEvent bEvent = (BeforeEvent) event;
                        printer.println(String.format(
                                "%s.%s will be wreck by exception: %s on %s",
                                bEvent.javaClassName,
                                bEvent.javaMethodName,
                                exType.name(),
                                Thread.currentThread().getName()
                        ));

                        throwsImmediately(exType.throwIt("wreck-it by Ralph!!!"));
                    }
                }, BEFORE);

        // --- 等待结束 ---

        try {
            printer.println(String.format(
                    "exception on [%s#%s] exception: %s.\nPress CTRL_C abort it!",
                    cnPattern,
                    mnPattern,
                    exType.name()
            ));
            printer.waitingForBroken();
        } finally {
            watcher.onUnWatched();
        }

    }

    /*
     * 注入延时
     * -d 'debug-ralph/delay?class=<CLASS>&method=<METHOD>&delay=<DELAY(ms)>'
     */
    @Command("delay")
    public void delay(final Map<String, String> param, final PrintWriter writer) {

        final ReentrantLock delayLock = new ReentrantLock();
        final Condition delayCondition = delayLock.newCondition();
        final Printer printer = new ConcurrentLinkedQueuePrinter(writer);
        final AtomicBoolean isFinishRef = new AtomicBoolean(false);

        // --- 解析参数 ---

        final String cnPattern = getParameter(param, "class");
        final String mnPattern = getParameter(param, "method");
        final long delayMs = getParameter(param, "delay", long.class);

        // --- 开始增强 ---

        final EventWatcher watcher = new EventWatchBuilder(moduleEventWatcher)
                .onClass(cnPattern)
                .includeSubClasses()
                .includeBootstrap()
                .onBehavior(mnPattern)
                .onWatching()
                .withProgress(new ProgressPrinter(printer))
                .onWatch(new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Throwable {

                        final BeforeEvent bEvent = (BeforeEvent) event;
                        printer.println(String.format(
                                "%s.%s will be delay %s(ms) on %s",
                                bEvent.javaClassName,
                                bEvent.javaMethodName,
                                delayMs,
                                Thread.currentThread().getName()
                        ));

                        delayLock.lock();
                        try {
                            // 如果已经结束，则放弃本次请求
                            if (isFinishRef.get()) {
                                return;
                            }
                            delayCondition.await(delayMs, TimeUnit.MILLISECONDS);
                        } finally {
                            delayLock.unlock();
                        }
                    }
                }, BEFORE);

        // --- 等待结束 ---

        try {
            printer.println(String.format(
                    "delay on [%s#%s] %s(ms).\nPress CTRL_C abort it!",
                    cnPattern,
                    mnPattern,
                    delayMs
            ));
            printer.waitingForBroken();
        } finally {

            // 释放锁
            delayLock.lock();
            try {
                isFinishRef.set(true);
                delayCondition.signalAll();
            } finally {
                delayLock.unlock();
            }

            watcher.onUnWatched();
        }
    }

}
