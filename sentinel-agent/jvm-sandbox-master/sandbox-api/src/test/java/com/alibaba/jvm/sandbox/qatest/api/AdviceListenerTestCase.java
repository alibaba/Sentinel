package com.alibaba.jvm.sandbox.qatest.api;

import com.alibaba.jvm.sandbox.api.event.BeforeEvent;
import com.alibaba.jvm.sandbox.api.event.ReturnEvent;
import com.alibaba.jvm.sandbox.api.event.ThrowsEvent;
import com.alibaba.jvm.sandbox.api.listener.ext.Advice;
import com.alibaba.jvm.sandbox.api.listener.ext.AdviceListener;
import com.alibaba.jvm.sandbox.api.listener.ext.EventWatchBuilder;
import com.alibaba.jvm.sandbox.qatest.api.mock.MockForBuilderModuleEventWatcher;
import org.junit.Assert;
import org.junit.Test;

public class AdviceListenerTestCase {


    @Test
    public void test$$AdviceListener$$onBefore$onReturn$onThrows() throws Throwable {

        final MockForBuilderModuleEventWatcher mockForBuilderModuleEventWatcher
                = new MockForBuilderModuleEventWatcher();

        final StringBuilder traceSB = new StringBuilder();
        new EventWatchBuilder(mockForBuilderModuleEventWatcher)
                .onClass(String.class)
                .onBehavior("toString")
                .onWatch(new AdviceListener(){
                    @Override
                    protected void before(Advice advice) throws Throwable {
                        traceSB.append("before;");
                    }

                    @Override
                    protected void afterReturning(Advice advice) throws Throwable {
                        traceSB.append("afterReturning;");
                    }

                    @Override
                    protected void afterThrowing(Advice advice) throws Throwable {
                        traceSB.append("afterThrowing;");
                    }
                });

        {
            mockForBuilderModuleEventWatcher.getEventListener().onEvent(
                    new BeforeEvent(
                            10001,
                            10001,
                            getClass().getClassLoader(),
                            "java.lang.String",
                            "toString",
                            "()Ljava/lang/String;",
                            this,
                            new Object[]{}
                    )
            );

            mockForBuilderModuleEventWatcher.getEventListener().onEvent(
                    new ReturnEvent(
                            10001,
                            10001,
                            this
                    )
            );

            Assert.assertEquals("before;afterReturning;", traceSB.toString());
            traceSB.delete(0, traceSB.length());

        }

        {
            mockForBuilderModuleEventWatcher.getEventListener().onEvent(
                    new BeforeEvent(
                            10001,
                            10001,
                            getClass().getClassLoader(),
                            "java.lang.String",
                            "toString",
                            "()Ljava/lang/String;",
                            this,
                            new Object[]{}
                    )
            );

            mockForBuilderModuleEventWatcher.getEventListener().onEvent(
                    new ThrowsEvent(
                            10001,
                            10001,
                            new RuntimeException()
                    )
            );

            Assert.assertEquals("before;afterThrowing;", traceSB.toString());
            traceSB.delete(0, traceSB.length());

        }

    }

}
