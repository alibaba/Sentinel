package com.alibaba.jvm.sandbox.module.debug;

import com.alibaba.jvm.sandbox.api.Information;
import com.alibaba.jvm.sandbox.api.Module;
import com.alibaba.jvm.sandbox.api.annotation.Command;
import com.alibaba.jvm.sandbox.api.http.printer.ConcurrentLinkedQueuePrinter;
import com.alibaba.jvm.sandbox.api.http.printer.Printer;
import com.alibaba.jvm.sandbox.api.listener.ext.Advice;
import com.alibaba.jvm.sandbox.api.listener.ext.AdviceListener;
import com.alibaba.jvm.sandbox.api.listener.ext.EventWatchBuilder;
import com.alibaba.jvm.sandbox.api.listener.ext.EventWatcher;
import com.alibaba.jvm.sandbox.api.resource.ModuleEventWatcher;
import com.alibaba.jvm.sandbox.module.debug.textui.TTree;
import org.kohsuke.MetaInfServices;

import javax.annotation.Resource;
import java.io.PrintWriter;
import java.util.Map;

/**
 * 模仿Greys的trace命令
 * <p>测试用模块</p>
 */
@MetaInfServices(Module.class)
@Information(id = "debug-trace", version = "0.0.2", author = "luanjia@taobao.com")
public class DebugTraceModule extends ParamSupported implements Module {

    @Resource
    private ModuleEventWatcher moduleEventWatcher;


    @Command("trace")
    public void trace(final Map<String, String> param, final PrintWriter writer) {

        final String cnPattern = getParameter(param, "class");
        final String mnPattern = getParameter(param, "method");
        final Printer printer = new ConcurrentLinkedQueuePrinter(writer);

        final EventWatcher watcher = new EventWatchBuilder(moduleEventWatcher)
                .onClass(cnPattern).includeSubClasses()
                .onBehavior(mnPattern)
                .onWatching()
                .withCall()
                .withProgress(new ProgressPrinter(printer))
                .onWatch(new AdviceListener() {

                    private String getTracingTitle(final Advice advice) {
                        return "Tracing for : "
                                + advice.getBehavior().getDeclaringClass().getName()
                                + "."
                                + advice.getBehavior().getName()
                                + " by "
                                + Thread.currentThread().getName()
                                ;
                    }

                    private String getEnterTitle(final Advice advice) {
                        return "Enter : "
                                + advice.getBehavior().getDeclaringClass().getName()
                                + "."
                                + advice.getBehavior().getName()
                                + "(...);"
                                ;
                    }

                    @Override
                    protected void before(Advice advice) throws Throwable {
                        final TTree tTree;
                        if (advice.isProcessTop()) {
                            advice.attach(tTree = new TTree(true, getTracingTitle(advice)));
                        } else {
                            tTree = advice.getProcessTop().attachment();
                        }
                        tTree.begin(getEnterTitle(advice));
                    }

                    @Override
                    protected void afterReturning(Advice advice) throws Throwable {
                        final TTree tTree = advice.getProcessTop().attachment();
                        tTree.end();
                        finish(advice);
                    }

                    @Override
                    protected void afterThrowing(Advice advice) throws Throwable {
                        final TTree tTree = advice.getProcessTop().attachment();
                        tTree.begin("throw:" + advice.getThrowable().getClass().getName() + "()").end();
                        tTree.end();
                        finish(advice);
                    }

                    private void finish(Advice advice) {
                        if (advice.isProcessTop()) {
                            final TTree tTree = advice.attachment();
                            printer.println(tTree.rendering());
                        }
                    }

                    @Override
                    protected void beforeCall(final Advice advice,
                                              final int callLineNum,
                                              final String callJavaClassName,
                                              final String callJavaMethodName,
                                              final String callJavaMethodDesc) {
                        final TTree tTree = advice.getProcessTop().attachment();
                        tTree.begin(callJavaClassName + ":" + callJavaMethodName + "(@" + callLineNum + ")");
                    }

                    @Override
                    protected void afterCallReturning(final Advice advice,
                                                      final int callLineNum,
                                                      final String callJavaClassName,
                                                      final String callJavaMethodName,
                                                      final String callJavaMethodDesc) {
                        final TTree tTree = advice.getProcessTop().attachment();
                        tTree.end();
                    }

                    @Override
                    protected void afterCallThrowing(final Advice advice,
                                                     final int callLineNum,
                                                     final String callJavaClassName,
                                                     final String callJavaMethodName,
                                                     final String callJavaMethodDesc,
                                                     final String callThrowJavaClassName) {
                        final TTree tTree = advice.getProcessTop().attachment();
                        tTree.set(tTree.get() + "[throw " + callThrowJavaClassName + "]").end();
                    }

                });

        try {
            printer.println(String.format(
                    "tracing on [%s#%s].\nPress CTRL_C abort it!",
                    cnPattern,
                    mnPattern
            ));
            printer.waitingForBroken();
        } finally {
            watcher.onUnWatched();
        }

    }

}
