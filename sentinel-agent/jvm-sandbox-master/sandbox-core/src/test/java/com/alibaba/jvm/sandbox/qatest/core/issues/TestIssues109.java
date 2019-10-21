package com.alibaba.jvm.sandbox.qatest.core.issues;

import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.listener.EventListener;
import com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator;
import com.alibaba.jvm.sandbox.qatest.core.util.JvmHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.alibaba.jvm.sandbox.api.event.Event.Type.*;
import static com.alibaba.jvm.sandbox.qatest.core.util.CalculatorHelper.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TestIssues109 {

    @Test
    public void cal$sum_add$all$multi_thread() throws Throwable {
        final ThreadLocal<List<Event.Type>> tracingRef = new ThreadLocal<List<Event.Type>>() {
            @Override
            protected List<Event.Type> initialValue() {
                return new ArrayList<Event.Type>();
            }
        };
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_SUM_and_ADD_FILTER,
                        new EventListener() {

                            @Override
                            public void onEvent(Event event) {
                                tracingRef.get().add(event.type);
                            }

                        },
                        Event.Type.values()
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        final Object objectOfCal = newInstance(calculatorClass);

        final int total = 10000;
        final int concurrent = 700;
        final AtomicInteger successCntRef = new AtomicInteger();
        final CountDownLatch countDownLatch = new CountDownLatch(total);
        final ExecutorService executors = Executors.newFixedThreadPool(concurrent);
        try {
            for (int index = 0; index < total; index++) {
                executors.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            assertEquals(30, sum(objectOfCal, 10, 20));
                            assertArrayEquals(
                                    ArrayUtils.toArray(BEFORE, LINE, LINE, LINE, LINE, CALL_BEFORE, BEFORE, LINE, LINE, RETURN, CALL_RETURN, LINE, LINE, CALL_BEFORE, BEFORE, LINE, LINE, RETURN, CALL_RETURN, LINE, LINE, RETURN),
                                    tracingRef.get().toArray(new Event.Type[]{})
                            );
                            successCntRef.getAndIncrement();
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        } finally {
                            tracingRef.remove();
                            countDownLatch.countDown();
                        }
                    }
                });
            }
            countDownLatch.await();
            Assert.assertEquals(total, successCntRef.get());
        } finally {
            executors.shutdown();
        }

    }

}
