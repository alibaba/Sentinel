package com.alibaba.jvm.sandbox.module.debug;

import com.alibaba.jvm.sandbox.api.Information;
import com.alibaba.jvm.sandbox.api.LoadCompleted;
import com.alibaba.jvm.sandbox.api.Module;
import com.alibaba.jvm.sandbox.api.event.BeforeEvent;
import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.listener.EventListener;
import com.alibaba.jvm.sandbox.api.listener.ext.EventWatchBuilder;
import com.alibaba.jvm.sandbox.api.resource.ModuleEventWatcher;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

import static com.alibaba.jvm.sandbox.api.event.Event.Type.BEFORE;
import static com.alibaba.jvm.sandbox.api.util.GaStringUtils.getJavaClassName;

/**
 * 异常类创建日志
 *
 * @author luanjia@taobao.com
 */
@MetaInfServices(Module.class)
@Information(id = "debug-exception-logger", version = "0.0.2", author = "luanjia@taobao.com")
public class LogExceptionModule implements Module, LoadCompleted {

    private final Logger exLogger = LoggerFactory.getLogger("DEBUG-EXCEPTION-LOGGER");

    @Resource
    private ModuleEventWatcher moduleEventWatcher;

    @Override
    public void loadCompleted() {
        new EventWatchBuilder(moduleEventWatcher)
                .onClass(Exception.class)
                .includeBootstrap()
                .onBehavior("<init>")
                .onWatch(new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Throwable {
                        final BeforeEvent bEvent = (BeforeEvent) event;
                        exLogger.info("{} occur an exception: {}",
                                getJavaClassName(bEvent.target.getClass()),
                                bEvent.target
                        );
                    }
                }, BEFORE);
    }

}
