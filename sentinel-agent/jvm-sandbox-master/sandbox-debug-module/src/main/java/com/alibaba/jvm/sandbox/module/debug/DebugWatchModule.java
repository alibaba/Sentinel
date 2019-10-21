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
import com.alibaba.jvm.sandbox.module.debug.util.Express;
import org.apache.commons.lang3.EnumUtils;
import org.kohsuke.MetaInfServices;

import javax.annotation.Resource;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.jvm.sandbox.module.debug.DebugWatchModule.Trigger.*;

/**
 * 模仿Greys的watch命令
 * <p>测试用模块</p>
 *
 * @author luanjia@taobao.com
 */
@MetaInfServices(Module.class)
@Information(id = "debug-watch", version = "0.0.2", author = "luanjia@taobao.com")
public class DebugWatchModule extends ParamSupported implements Module {

    @Resource
    private ModuleEventWatcher moduleEventWatcher;

    @Command("watch")
    public void watch(final Map<String, String> param,
                      final Map<String, String[]> params,
                      final PrintWriter writer) {

        final String cnPattern = getParameter(param, "class");
        final String mnPattern = getParameter(param, "method");
        final String watchExpress = getParameter(param, "watch");
        final List<Trigger> triggers = getParameters(
                params,
                "at",
                new Converter<Trigger>() {
                    @Override
                    public Trigger convert(String string) {
                        return EnumUtils.getEnum(Trigger.class, string);
                    }
                },
                Trigger.BEFORE);
        final Printer printer = new ConcurrentLinkedQueuePrinter(writer);


        final EventWatcher watcher = new EventWatchBuilder(moduleEventWatcher)
                .onClass(cnPattern)
                .includeSubClasses()
                .includeBootstrap()
                .onBehavior(mnPattern)
                .onWatching()
                .withProgress(new ProgressPrinter(printer))
                .onWatch(new AdviceListener() {

                    @Override
                    public void before(final Advice advice) {
                        if (!triggers.contains(BEFORE)) {
                            return;
                        }
                        printlnByExpress(binding(advice));
                    }

                    @Override
                    public void afterReturning(final Advice advice) {
                        if (!triggers.contains(RETURN)) {
                            return;
                        }
                        printlnByExpress(
                                binding(advice)
                                        .bind("return", advice.getReturnObj())
                        );
                    }

                    @Override
                    public void afterThrowing(final Advice advice) {
                        if (!triggers.contains(THROWS)) {
                            return;
                        }
                        printlnByExpress(
                                binding(advice)
                                        .bind("throws", advice.getThrowable())
                        );
                    }

                    private Bind binding(Advice advice) {
                        return new Bind()
                                .bind("class", advice.getBehavior().getDeclaringClass())
                                .bind("method", advice.getBehavior())
                                .bind("params", advice.getParameterArray())
                                .bind("target", advice.getTarget());
                    }

                    private void printlnByExpress(final Bind bind) {
                        try {
                            final Object watchObject = Express.ExpressFactory.newExpress(bind).get(watchExpress);
                            printer.println(DebugWatchModule.toString(watchObject));
                        } catch (Express.ExpressException e) {
                            printer.println(String.format("express: %s was wrong! msg:%s.", watchExpress, e.getMessage()));
                        }

                    }

                });
        try {
            printer.println(String.format(
                    "watching on [%s#%s], at %s, watch:%s.\nPress CTRL_C abort it!",
                    cnPattern,
                    mnPattern,
                    triggers,
                    watchExpress
            ));
            printer.waitingForBroken();
        } finally {
            watcher.onUnWatched();
        }

    }


    private static String toString(final Object object) {
        return null == object
                ? "null"
                : object.toString();
    }

    /**
     * 观察触点
     */
    enum Trigger {
        BEFORE,
        RETURN,
        THROWS
    }

    static class Bind extends HashMap<String, Object> {
        Bind bind(final String name,
                  final Object value) {
            put(name, value);
            return this;
        }
    }

}
