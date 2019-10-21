package com.alibaba.jvm.sandbox.qatest.core.issues;

import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.listener.EventListener;
import com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator;
import com.alibaba.jvm.sandbox.qatest.core.util.JvmHelper;
import org.junit.Test;

import static com.alibaba.jvm.sandbox.api.event.Event.Type.CALL_BEFORE;
import static com.alibaba.jvm.sandbox.api.event.Event.Type.LINE;
import static com.alibaba.jvm.sandbox.qatest.core.util.CalculatorHelper.*;
import static org.junit.Assert.assertEquals;

/**
 * 修复<a href="https://github.com/alibaba/jvm-sandbox/issues/130">#130</a>
 */
public class TestIssues130 {

    @Test
    public void cal$sum_add$call_before() throws Throwable {
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_SUM_and_ADD_FILTER,
                        new EventListener() {

                            @Override
                            public void onEvent(Event event) throws Throwable {

                            }
                        },
                        CALL_BEFORE
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        final Object objectOfCal = newInstance(calculatorClass);
        for (int i = 0; i < 1000000; i++) {
            assertEquals(30, sum(objectOfCal, 10, 20));
        }
    }

    @Test
    public void cal$sum_add$line() throws Throwable {
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_SUM_and_ADD_FILTER,
                        new EventListener() {

                            @Override
                            public void onEvent(Event event) throws Throwable {

                            }
                        },
                        LINE
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        final Object objectOfCal = newInstance(calculatorClass);
        for (int i = 0; i < 1000000; i++) {
            assertEquals(30, sum(objectOfCal, 10, 20));
        }
    }

    @Test
    public void cal$sum_add$all() throws Throwable {
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_SUM_and_ADD_FILTER,
                        new EventListener() {

                            @Override
                            public void onEvent(Event event) throws Throwable {

                            }
                        },
                        Event.Type.values()
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        final Object objectOfCal = newInstance(calculatorClass);
        for (int i = 0; i < 1000000; i++) {
            assertEquals(30, sum(objectOfCal, 10, 20));
        }
    }

}
