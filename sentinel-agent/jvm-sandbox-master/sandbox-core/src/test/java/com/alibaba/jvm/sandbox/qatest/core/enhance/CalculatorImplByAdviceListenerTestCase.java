package com.alibaba.jvm.sandbox.qatest.core.enhance;

import com.alibaba.jvm.sandbox.api.listener.ext.Advice;
import com.alibaba.jvm.sandbox.qatest.core.enhance.listener.TracingAdviceListener;
import com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator;
import com.alibaba.jvm.sandbox.qatest.core.util.JvmHelper;
import org.junit.Test;

import static com.alibaba.jvm.sandbox.api.ProcessController.returnImmediately;
import static com.alibaba.jvm.sandbox.api.ProcessController.throwsImmediately;
import static com.alibaba.jvm.sandbox.api.event.Event.Type.*;
import static com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.ERROR_EXCEPTION_MESSAGE;
import static com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.TestCase.ADD$EXCEPTION;
import static com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.TestCase.SUM$EXCEPTION;
import static com.alibaba.jvm.sandbox.qatest.core.util.CalculatorHelper.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * AdviceListener相关测试用例
 */
public class CalculatorImplByAdviceListenerTestCase implements ICalculatorTestCase {

//    @BeforeClass
//    public static void initSpy() {
//        Spy.isSpyThrowException = true;
//    }

    @Test
    @Override
    public void cal$sum$around() throws Throwable {
        final TracingAdviceListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        new JvmHelper.Transformer(
                                CALCULATOR_SUM_FILTER,
                                listener = new TracingAdviceListener()
                        )
                ).loadClass(CALCULATOR_CLASS_NAME);
        assertEquals(30, sum(newInstance(calculatorClass), 10, 20));
        listener.assertTracing(
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE"
        );
    }

    @Override
    public void cal$sum$line() throws Throwable {

    }

    @Test
    @Override
    public void cal$sum$call() throws Throwable {
        final TracingAdviceListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        new JvmHelper.Transformer(
                                CALCULATOR_SUM_FILTER,
                                listener = new TracingAdviceListener(),
                                CALL_BEFORE, CALL_RETURN, CALL_THROWS
                        )
                ).loadClass(CALCULATOR_CLASS_NAME);
        assertEquals(30, sum(newInstance(calculatorClass), 10, 20));
        listener.assertTracing(
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "CALL-BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE|96|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)",
                "CALL-RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE|96|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)",
                "CALL-AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE|96|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)",
                "CALL-BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE|96|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)",
                "CALL-RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE|96|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)",
                "CALL-AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE|96|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE"
        );
    }

    @Test
    @Override
    public void cal$sum$before$changeParameters() throws Throwable {
        final TracingAdviceListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        new JvmHelper.Transformer(
                                CALCULATOR_SUM_FILTER,
                                listener = new TracingAdviceListener() {
                                    @Override
                                    protected void before(Advice advice) throws Throwable {
                                        super.before(advice);
                                        advice.changeParameter(0, new int[]{40, 60});
                                    }
                                }
                        )
                ).loadClass(CALCULATOR_CLASS_NAME);
        assertEquals(100, sum(newInstance(calculatorClass), 10, 20));
        listener.assertTracing(
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE"
        );
    }

    @Test
    @Override
    public void cal$sum$before$returnImmediately() throws Throwable {
        final TracingAdviceListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        new JvmHelper.Transformer(
                                CALCULATOR_SUM_FILTER,
                                listener = new TracingAdviceListener() {
                                    @Override
                                    protected void before(Advice advice) throws Throwable {
                                        super.before(advice);
                                        returnImmediately(100);
                                    }
                                }
                        )
                ).loadClass(CALCULATOR_CLASS_NAME);
        assertEquals(100, sum(newInstance(calculatorClass), 10, 20));
        listener.assertTracing(
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE"
        );
    }

    @Test
    @Override
    public void cal$sum$before$throwsImmediately() throws Throwable {
        final TracingAdviceListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        new JvmHelper.Transformer(
                                CALCULATOR_SUM_FILTER,
                                listener = new TracingAdviceListener() {
                                    @Override
                                    protected void before(Advice advice) throws Throwable {
                                        super.before(advice);
                                        throwsImmediately(new Throwable(ERROR_EXCEPTION_MESSAGE));
                                    }
                                }
                        )
                ).loadClass(CALCULATOR_CLASS_NAME);
        try {
            sum(newInstance(calculatorClass), 10, 20);
            assertTrue(false);
        } catch (Throwable throwable) {
            assertEquals(ERROR_EXCEPTION_MESSAGE, throwable.getMessage());
        }
        listener.assertTracing(
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE"
        );
    }

    @Test
    @Override
    public void cal$sum$return$changeParameters() throws Throwable {
        final TracingAdviceListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        new JvmHelper.Transformer(
                                CALCULATOR_SUM_FILTER,
                                listener = new TracingAdviceListener() {
                                    @Override
                                    protected void afterReturning(Advice advice) throws Throwable {
                                        super.afterReturning(advice);
                                        advice.changeParameter(0, new int[]{40, 60});
                                    }
                                }
                        )
                ).loadClass(CALCULATOR_CLASS_NAME);
        assertEquals(30, sum(newInstance(calculatorClass), 10, 20));
        listener.assertTracing(
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE"
        );
    }

    @Test
    @Override
    public void cal$sum$return$returnImmediately() throws Throwable {
        final TracingAdviceListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        new JvmHelper.Transformer(
                                CALCULATOR_SUM_FILTER,
                                listener = new TracingAdviceListener() {
                                    @Override
                                    protected void afterReturning(Advice advice) throws Throwable {
                                        super.afterReturning(advice);
                                        returnImmediately(100);
                                    }
                                }
                        )
                ).loadClass(CALCULATOR_CLASS_NAME);
        assertEquals(100, sum(newInstance(calculatorClass), 10, 20));
        listener.assertTracing(
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE"
        );
    }

    @Test
    @Override
    public void cal$sum$return$throwsImmediately() throws Throwable {
        final TracingAdviceListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        new JvmHelper.Transformer(
                                CALCULATOR_SUM_FILTER,
                                listener = new TracingAdviceListener() {
                                    @Override
                                    protected void afterReturning(Advice advice) throws Throwable {
                                        super.afterReturning(advice);
                                        throwsImmediately(new Throwable(ERROR_EXCEPTION_MESSAGE));
                                    }
                                }
                        )
                ).loadClass(CALCULATOR_CLASS_NAME);
        try {
            sum(newInstance(calculatorClass), 10, 20);
            assertTrue(false);
        } catch (Throwable throwable) {
            assertEquals(ERROR_EXCEPTION_MESSAGE, throwable.getMessage());
        }
        listener.assertTracing(
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE"
        );
    }

    @Test
    @Override
    public void cal$sum$throws$changeParameters() throws Throwable {
        final TracingAdviceListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        new JvmHelper.Transformer(
                                CALCULATOR_SUM_FILTER,
                                listener = new TracingAdviceListener() {
                                    @Override
                                    protected void afterThrowing(Advice advice) throws Throwable {
                                        super.afterThrowing(advice);
                                        advice.changeParameter(0, new int[]{40, 60});
                                    }
                                }
                        )
                ).loadClass(CALCULATOR_CLASS_NAME);
        try {
            sum(newInstance(calculatorClass, SUM$EXCEPTION), 10, 20);
            assertTrue(false);
        } catch (Throwable throwable) {
            assertEquals(ERROR_EXCEPTION_MESSAGE, throwable.getMessage());
        }
        listener.assertTracing(
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "THROWING|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE"
        );
    }

    @Test
    @Override
    public void cal$sum$throws$returnImmediately() throws Throwable {
        final TracingAdviceListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        new JvmHelper.Transformer(
                                CALCULATOR_SUM_FILTER,
                                listener = new TracingAdviceListener() {
                                    @Override
                                    protected void afterThrowing(Advice advice) throws Throwable {
                                        super.afterThrowing(advice);
                                        returnImmediately(100);
                                    }
                                }
                        )
                ).loadClass(CALCULATOR_CLASS_NAME);
        assertEquals(100, sum(newInstance(calculatorClass, SUM$EXCEPTION), 10, 20));
        listener.assertTracing(
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "THROWING|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE"
        );
    }

    @Test
    @Override
    public void cal$sum$throws$throwsImmediately() throws Throwable {
        final TracingAdviceListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        new JvmHelper.Transformer(
                                CALCULATOR_SUM_FILTER,
                                listener = new TracingAdviceListener() {
                                    @Override
                                    protected void afterThrowing(Advice advice) throws Throwable {
                                        super.afterThrowing(advice);
                                        throwsImmediately(new Throwable(ERROR_EXCEPTION_MESSAGE));
                                    }
                                }
                        )
                ).loadClass(CALCULATOR_CLASS_NAME);
        try {
            sum(newInstance(calculatorClass, SUM$EXCEPTION), 10, 20);
            assertTrue(false);
        } catch (Throwable throwable) {
            assertEquals(ERROR_EXCEPTION_MESSAGE, throwable.getMessage());
        }
        listener.assertTracing(
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "THROWING|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE"
        );
    }

    @Test
    @Override
    public void cal$sum_add$around() throws Throwable {
        final TracingAdviceListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        new JvmHelper.Transformer(
                                CALCULATOR_SUM_and_ADD_FILTER,
                                listener = new TracingAdviceListener()
                        )
                ).loadClass(CALCULATOR_CLASS_NAME);
        assertEquals(30, sum(newInstance(calculatorClass), 10, 20));
        listener.assertTracing(
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE"
        );
    }

    @Override
    public void cal$sum_add$line() throws Throwable {

    }

    @Test
    @Override
    public void cal$sum_add$call() throws Throwable {
        final TracingAdviceListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        new JvmHelper.Transformer(
                                CALCULATOR_SUM_and_ADD_FILTER,
                                listener = new TracingAdviceListener(),
                                CALL_BEFORE, CALL_RETURN, CALL_THROWS
                        )
                ).loadClass(CALCULATOR_CLASS_NAME);
        assertEquals(30, sum(newInstance(calculatorClass), 10, 20));
        listener.assertTracing(
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "CALL-BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE|96|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)",
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "CALL-RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE|96|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)",
                "CALL-AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE|96|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)",
                "CALL-BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE|96|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)",
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "CALL-RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE|96|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)",
                "CALL-AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE|96|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE"
        );
    }

    @Test
    @Override
    public void cal$sum_add$before$changeParameters_at_add() throws Throwable {
        final TracingAdviceListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        new JvmHelper.Transformer(
                                CALCULATOR_SUM_and_ADD_FILTER,
                                listener = new TracingAdviceListener() {
                                    @Override
                                    protected void before(Advice advice) throws Throwable {
                                        super.before(advice);
                                        if (advice.getBehavior().getName().equalsIgnoreCase("add")) {
                                            advice.changeParameter(0, 40);
                                            advice.changeParameter(1, 60);
                                        }
                                    }
                                }
                        )
                ).loadClass(CALCULATOR_CLASS_NAME);
        assertEquals(100, sum(newInstance(calculatorClass), 10, 20));
        listener.assertTracing(
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE"
        );
    }

    @Test
    @Override
    public void cal$sum_add$before$returnImmediately_at_add() throws Throwable {
        final TracingAdviceListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        new JvmHelper.Transformer(
                                CALCULATOR_SUM_and_ADD_FILTER,
                                listener = new TracingAdviceListener() {
                                    @Override
                                    protected void before(Advice advice) throws Throwable {
                                        super.before(advice);
                                        if (advice.getBehavior().getName().equalsIgnoreCase("add")) {
                                            returnImmediately(100);
                                        }
                                    }
                                }
                        )
                ).loadClass(CALCULATOR_CLASS_NAME);
        assertEquals(100, sum(newInstance(calculatorClass), 10, 20));
        listener.assertTracing(
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE"
        );
    }

    @Override
    public void cal$sum_add$before$throwsImmediately_at_add() throws Throwable {
        final TracingAdviceListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        new JvmHelper.Transformer(
                                CALCULATOR_SUM_and_ADD_FILTER,
                                listener = new TracingAdviceListener() {
                                    @Override
                                    protected void before(Advice advice) throws Throwable {
                                        super.before(advice);
                                        if (advice.getBehavior().getName().equalsIgnoreCase("add")) {
                                            throwsImmediately(new Throwable(ERROR_EXCEPTION_MESSAGE));
                                        }
                                    }
                                }
                        )
                ).loadClass(CALCULATOR_CLASS_NAME);
        try {
            sum(newInstance(calculatorClass), 10, 20);
            assertTrue(false);
        } catch (Throwable throwable) {
            assertEquals(ERROR_EXCEPTION_MESSAGE, throwable.getMessage());
        }
        listener.assertTracing(
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "THROWING|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE"
        );
    }

    @Override
    public void cal$sum_add$return$changeParameters_at_add() throws Throwable {
        final TracingAdviceListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        new JvmHelper.Transformer(
                                CALCULATOR_SUM_and_ADD_FILTER,
                                listener = new TracingAdviceListener() {
                                    @Override
                                    protected void afterReturning(Advice advice) throws Throwable {
                                        super.afterReturning(advice);
                                        if (advice.getBehavior().getName().equalsIgnoreCase("add")) {
                                            advice.changeParameter(0, 40);
                                            advice.changeParameter(0, 60);
                                        }
                                    }
                                }
                        )
                ).loadClass(CALCULATOR_CLASS_NAME);
        assertEquals(30, sum(newInstance(calculatorClass), 10, 20));
        listener.assertTracing(
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE"
        );
    }

    @Test
    @Override
    public void cal$sum_add$return$returnImmediately_at_add() throws Throwable {
        final TracingAdviceListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        new JvmHelper.Transformer(
                                CALCULATOR_SUM_and_ADD_FILTER,
                                listener = new TracingAdviceListener() {
                                    @Override
                                    protected void afterReturning(Advice advice) throws Throwable {
                                        super.afterReturning(advice);
                                        if (advice.getBehavior().getName().equalsIgnoreCase("add")) {
                                            returnImmediately(100);
                                        }
                                    }
                                }
                        )
                ).loadClass(CALCULATOR_CLASS_NAME);
        assertEquals(100, sum(newInstance(calculatorClass), 10, 20));
        listener.assertTracing(
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE"
        );
    }

    @Test
    @Override
    public void cal$sum_add$return$throwsImmediately_at_add() throws Throwable {
        final TracingAdviceListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        new JvmHelper.Transformer(
                                CALCULATOR_SUM_and_ADD_FILTER,
                                listener = new TracingAdviceListener() {
                                    @Override
                                    protected void afterReturning(Advice advice) throws Throwable {
                                        super.afterReturning(advice);
                                        if (advice.getBehavior().getName().equalsIgnoreCase("add")) {
                                            throwsImmediately(new Throwable(ERROR_EXCEPTION_MESSAGE));
                                        }
                                    }
                                }
                        )
                ).loadClass(CALCULATOR_CLASS_NAME);
        try {
            sum(newInstance(calculatorClass), 10, 20);
            assertTrue(false);
        } catch (Throwable throwable) {
            assertEquals(ERROR_EXCEPTION_MESSAGE, throwable.getMessage());
        }
        listener.assertTracing(
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "THROWING|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE"
        );
    }

    @Test
    @Override
    public void cal$sum_add$throws$changeParameters_at_add() throws Throwable {
        final TracingAdviceListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        new JvmHelper.Transformer(
                                CALCULATOR_SUM_and_ADD_FILTER,
                                listener = new TracingAdviceListener() {
                                    @Override
                                    protected void afterThrowing(Advice advice) throws Throwable {
                                        super.afterThrowing(advice);
                                        if (advice.getBehavior().getName().equalsIgnoreCase("add")) {
                                            advice.changeParameter(0, 40);
                                            advice.changeParameter(1, 60);
                                        }
                                    }
                                }
                        )
                ).loadClass(CALCULATOR_CLASS_NAME);
        try {
            sum(newInstance(calculatorClass, ADD$EXCEPTION), 10, 20);
            assertTrue(false);
        } catch (Throwable throwable) {
            assertEquals(ERROR_EXCEPTION_MESSAGE, throwable.getMessage());
        }
        listener.assertTracing(
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "THROWING|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "THROWING|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE"
        );
    }

    @Override
    public void cal$sum_add$throws$returnImmediately_at_add() throws Throwable {
        final TracingAdviceListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        new JvmHelper.Transformer(
                                CALCULATOR_SUM_and_ADD_FILTER,
                                listener = new TracingAdviceListener() {
                                    @Override
                                    protected void afterThrowing(Advice advice) throws Throwable {
                                        super.afterThrowing(advice);
                                        if (advice.getBehavior().getName().equalsIgnoreCase("add")) {
                                            returnImmediately(100);
                                        }
                                    }
                                }
                        )
                ).loadClass(CALCULATOR_CLASS_NAME);
        assertEquals(100, sum(newInstance(calculatorClass), 10, 20));
        listener.assertTracing(
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "THROWING|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "THROWING|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE"
        );
    }

    @Override
    public void cal$sum_add$throws$throwsImmediately_at_add() throws Throwable {
        final TracingAdviceListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        new JvmHelper.Transformer(
                                CALCULATOR_SUM_and_ADD_FILTER,
                                listener = new TracingAdviceListener() {
                                    @Override
                                    protected void afterThrowing(Advice advice) throws Throwable {
                                        super.afterThrowing(advice);
                                        if (advice.getBehavior().getName().equalsIgnoreCase("add")) {
                                            throwsImmediately(new Throwable(ERROR_EXCEPTION_MESSAGE));
                                        }
                                    }
                                }
                        )
                ).loadClass(CALCULATOR_CLASS_NAME);
        try {
            sum(newInstance(calculatorClass, ADD$EXCEPTION), 10, 20);
            assertTrue(false);
        } catch (Throwable throwable) {
            assertEquals(ERROR_EXCEPTION_MESSAGE, throwable.getMessage());
        }
        listener.assertTracing(
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE",
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "THROWING|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.add(int,int)|FALSE",
                "THROWING|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.sum(int[])|TRUE"
        );
    }

    @Test
    @Override
    public void cal$pow$around() throws Throwable {
        final TracingAdviceListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        new JvmHelper.Transformer(
                                CALCULATOR_POW_FILTER,
                                listener = new TracingAdviceListener()
                        )
                ).loadClass(CALCULATOR_CLASS_NAME);
        assertEquals(25, pow(newInstance(calculatorClass), 5, 2));
        listener.assertTracing(
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)|TRUE",
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)|FALSE",
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)|FALSE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)|FALSE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)|FALSE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)|FALSE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)|FALSE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)|TRUE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)|TRUE"
        );
    }

    @Override
    public void cal$pow$line() throws Throwable {

    }

    @Test
    @Override
    public void cal$pow$call() throws Throwable {
        final TracingAdviceListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        new JvmHelper.Transformer(
                                CALCULATOR_POW_FILTER,
                                listener = new TracingAdviceListener(),
                                CALL_BEFORE, CALL_RETURN, CALL_THROWS
                        )
                ).loadClass(CALCULATOR_CLASS_NAME);
        assertEquals(25, pow(newInstance(calculatorClass), 5, 2));
        listener.assertTracing(
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)|TRUE",
                "CALL-BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)|TRUE|115|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)",
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)|FALSE",
                "CALL-BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)|FALSE|115|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)",
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)|FALSE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)|FALSE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)|FALSE",
                "CALL-RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)|FALSE|115|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)",
                "CALL-AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)|FALSE|115|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)|FALSE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)|FALSE",
                "CALL-RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)|TRUE|115|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)",
                "CALL-AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)|TRUE|115|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)|TRUE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.pow(int,int)|TRUE"
        );
    }

    @Test
    @Override
    public void cal$init_with_TestCase$around() throws Throwable {
        final TracingAdviceListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        new JvmHelper.Transformer(
                                CALCULATOR_INIT_FILTER_WITH_TEST_CASE,
                                listener = new TracingAdviceListener()
                        )
                ).loadClass(CALCULATOR_CLASS_NAME);
        assertEquals(30, sum(newInstance(calculatorClass), 10, 20));
        listener.assertTracing(
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.<init>(com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator$TestCase)|TRUE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.<init>(com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator$TestCase)|TRUE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.<init>(com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator$TestCase)|TRUE"
        );
    }

    @Override
    public void cal$init_with_TestCase$line() throws Throwable {

    }

    @Test
    @Override
    public void cal$init_with_TestCase$call() throws Throwable {
        final TracingAdviceListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        new JvmHelper.Transformer(
                                CALCULATOR_INIT_FILTER_WITH_TEST_CASE,
                                listener = new TracingAdviceListener(),
                                CALL_BEFORE, CALL_RETURN, CALL_THROWS
                        )
                ).loadClass(CALCULATOR_CLASS_NAME);
        assertEquals(30, sum(newInstance(calculatorClass), 10, 20));
        listener.assertTracing(
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.<init>(com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator$TestCase)|TRUE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.<init>(com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator$TestCase)|TRUE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.<init>(com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator$TestCase)|TRUE"
        );
    }

    @Test
    @Override
    public void cal$init_with_TestCase$before$changeParameters() throws Throwable {
        final TracingAdviceListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        new JvmHelper.Transformer(
                                CALCULATOR_INIT_FILTER_WITH_TEST_CASE,
                                listener = new TracingAdviceListener() {
                                    @Override
                                    protected void before(Advice advice) throws Throwable {
                                        super.before(advice);
                                        advice.changeParameter(0, Calculator.TestCase.SUM$EXCEPTION);
                                    }
                                }
                        )
                ).loadClass(CALCULATOR_CLASS_NAME);
        try {
            sum(newInstance(calculatorClass), 10, 20);
        } catch (RuntimeException e) {
            assertEquals(ERROR_EXCEPTION_MESSAGE, e.getMessage());
        }
        listener.assertTracing(
                "BEFORE|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.<init>(com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator$TestCase)|TRUE",
                "RETURN|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.<init>(com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator$TestCase)|TRUE",
                "AFTER|com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.<init>(com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator$TestCase)|TRUE"
        );
    }
}
