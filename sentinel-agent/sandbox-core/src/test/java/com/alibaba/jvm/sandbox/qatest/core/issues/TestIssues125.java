package com.alibaba.jvm.sandbox.qatest.core.issues;

import org.junit.Test;

/**
 * 修复<a href="https://github.com/alibaba/jvm-sandbox/issues/125">#125</a>
 */
public class TestIssues125 {

    /**
     * 怀疑Spy被重新加载后EventListenerHandlers的计数器被重置
     */
    @Test
    public void test$001() {

//        final TracingEventListener listener;
//        final Class<?> calculatorClass = JvmHelper
//                .createJvm()
//                .defineClass(
//                        Calculator.class,
//                        CALCULATOR_SUM_FILTER,
//                        listener = new TracingEventListener(),
//                        BEFORE, RETURN, THROWS
//                )
//                .loadClass(getJavaClassName(Calculator.class));
//
//        assertEquals(20, calculatorSum(calculatorClass.newInstance(), 10, 10));
//        listener.assertEventTracing(
//                BEFORE,
//                RETURN
//        );

    }

}
