package com.alibaba.jvm.sandbox.qatest.core.enhance;

import com.alibaba.jvm.sandbox.api.event.BeforeEvent;
import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.qatest.core.enhance.listener.LineNumTracingEventListener;
import com.alibaba.jvm.sandbox.qatest.core.enhance.listener.TracingEventListener;
import com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator;
import com.alibaba.jvm.sandbox.qatest.core.util.JvmHelper;
import org.junit.Ignore;
import org.junit.Test;

import static com.alibaba.jvm.sandbox.api.ProcessController.returnImmediately;
import static com.alibaba.jvm.sandbox.api.ProcessController.throwsImmediately;
import static com.alibaba.jvm.sandbox.api.event.Event.Type.*;
import static com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.ERROR_EXCEPTION_MESSAGE;
import static com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.TestCase.ADD$EXCEPTION;
import static com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator.TestCase.SUM$EXCEPTION;
import static com.alibaba.jvm.sandbox.qatest.core.util.CalculatorHelper.*;
import static org.junit.Assert.assertEquals;

public class CalculatorImplByEventListenerTestCase implements ICalculatorTestCase {

//    @BeforeClass
//    public static void initSpy() {
//        Spy.isSpyThrowException = true;
//    }

    @Test
    @Override
    public void cal$sum$around() throws Throwable {

        final TracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_SUM_FILTER,
                        listener = new TracingEventListener(),
                        BEFORE, RETURN, THROWS
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        assertEquals(30, sum(newInstance(calculatorClass), 10,20));
        listener.assertEventTracing(
                BEFORE,
                RETURN
        );

    }

    @Test
    @Override
    public void cal$sum$line() throws Throwable {

        final LineNumTracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_SUM_FILTER,
                        listener = new LineNumTracingEventListener(),
                        LINE
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        assertEquals(30, sum(newInstance(calculatorClass), 10,20));
        listener.assertLIneTracing(
                91,
                94,
                95,
                96,
                95,
                96,
                95,
                98
        );
    }

    @Test
    @Override
    public void cal$sum$call() throws Throwable {

        final TracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_SUM_FILTER,
                        listener = new TracingEventListener(),
                        CALL_BEFORE, CALL_RETURN, CALL_THROWS
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        assertEquals(30, sum(newInstance(calculatorClass), 10,20));
        listener.assertEventTracing(
                CALL_BEFORE,
                CALL_RETURN,
                CALL_BEFORE,
                CALL_RETURN
        );

    }

    @Test
    @Override
    public void cal$sum$before$changeParameters() throws Throwable {
        final TracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_SUM_FILTER,
                        listener = new TracingEventListener(){
                            @Override
                            public void onEvent(Event event) throws Throwable {
                                super.onEvent(event);
                                ((int[]) ((BeforeEvent) event).argumentArray[0])[0]=40;
                                ((int[]) ((BeforeEvent) event).argumentArray[0])[1]=60;
                            }
                        },
                        BEFORE
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        assertEquals(100, sum(newInstance(calculatorClass), 10,20));
        listener.assertEventTracing(
                BEFORE
        );
    }

    @Test
    @Override
    public void cal$sum$before$returnImmediately() throws Throwable {
        final TracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_SUM_FILTER,
                        listener = new TracingEventListener(){
                            @Override
                            public void onEvent(Event event) throws Throwable {
                                super.onEvent(event);
                                returnImmediately(100);
                            }
                        },
                        BEFORE
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        assertEquals(100, sum(newInstance(calculatorClass), 10,20));
        listener.assertEventTracing(
                BEFORE
        );
    }

    @Test
    @Override
    public void cal$sum$before$throwsImmediately() throws Throwable {
        final TracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_SUM_FILTER,
                        listener = new TracingEventListener(){
                            @Override
                            public void onEvent(Event event) throws Throwable {
                                super.onEvent(event);
                                throwsImmediately(new Throwable(ERROR_EXCEPTION_MESSAGE));
                            }
                        },
                        THROWS
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        try {
            sum(newInstance(calculatorClass, SUM$EXCEPTION), 10,20);
        }catch (Throwable e){
            assertEquals(ERROR_EXCEPTION_MESSAGE, e.getMessage());
        }
        listener.assertEventTracing(
                THROWS
        );
    }

    @Test
    @Override
    public void cal$sum$return$changeParameters() throws Throwable {
        final TracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_SUM_FILTER,
                        listener = new TracingEventListener(){
                            @Override
                            public void onEvent(Event event) throws Throwable {
                                super.onEvent(event);
                                returnImmediately(100);
                            }
                        },
                        RETURN
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        assertEquals(100, sum(newInstance(calculatorClass), 10,20));
        listener.assertEventTracing(
                RETURN
        );
    }

    @Test
    @Override
    public void cal$sum$return$returnImmediately() throws Throwable {
        final TracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_SUM_FILTER,
                        listener = new TracingEventListener(){
                            @Override
                            public void onEvent(Event event) throws Throwable {
                                super.onEvent(event);
                                returnImmediately(100);
                            }
                        },
                        RETURN
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        assertEquals(100, sum(newInstance(calculatorClass), 10,20));
        listener.assertEventTracing(
                RETURN
        );
    }

    @Test
    @Override
    public void cal$sum$return$throwsImmediately() throws Throwable {
        final TracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_SUM_FILTER,
                        listener = new TracingEventListener(){
                            @Override
                            public void onEvent(Event event) throws Throwable {
                                super.onEvent(event);
                                throwsImmediately(new Throwable(ERROR_EXCEPTION_MESSAGE));
                            }
                        },
                        RETURN
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        try {
            sum(newInstance(calculatorClass), 10,20);
        }catch (Throwable throwable){
            assertEquals(ERROR_EXCEPTION_MESSAGE, throwable.getMessage());
        }
        listener.assertEventTracing(
                RETURN
        );
    }

    @Test
    @Override
    public void cal$sum$throws$changeParameters() throws Throwable {
        final TracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_SUM_FILTER,
                        listener = new TracingEventListener(){
                            @Override
                            public void onEvent(Event event) throws Throwable {
                                super.onEvent(event);
                                returnImmediately(100);
                            }
                        },
                        THROWS
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        assertEquals(100, sum(newInstance(calculatorClass, SUM$EXCEPTION), 10,20));
        listener.assertEventTracing(
                THROWS
        );
    }

    @Override
    public void cal$sum$throws$returnImmediately() throws Throwable {

    }

    @Test
    @Override
    public void cal$sum$throws$throwsImmediately() throws Throwable {
        final TracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_SUM_FILTER,
                        listener = new TracingEventListener(){
                            @Override
                            public void onEvent(Event event) throws Throwable {
                                super.onEvent(event);
                                throwsImmediately(new Throwable(ERROR_EXCEPTION_MESSAGE));
                            }
                        },
                        THROWS
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        try {
            sum(newInstance(calculatorClass, SUM$EXCEPTION), 10,20);
        }catch (Throwable throwable){
            assertEquals(ERROR_EXCEPTION_MESSAGE, throwable.getMessage());
        }
        listener.assertEventTracing(
                THROWS
        );
    }

    @Test
    @Override
    public void cal$sum_add$around() throws Throwable {

        final TracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_SUM_and_ADD_FILTER,
                        listener = new TracingEventListener(),
                        BEFORE, RETURN, THROWS
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        assertEquals(30, sum(newInstance(calculatorClass), 10,20));
        listener.assertEventTracing(
                BEFORE,
                BEFORE,
                RETURN,
                BEFORE,
                RETURN,
                RETURN
        );

    }

    @Test
    @Override
    public void cal$sum_add$line() throws Throwable {
        final LineNumTracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_SUM_and_ADD_FILTER,
                        listener = new LineNumTracingEventListener(),
                        LINE
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        assertEquals(30, sum(newInstance(calculatorClass), 10,20));
        listener.assertLIneTracing(
                91,
                94,
                95,
                96,
                78,
                81,
                95,
                96,
                78,
                81,
                95,
                98
        );
    }

    @Test
    @Override
    public void cal$sum_add$call() throws Throwable {
        final TracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_SUM_and_ADD_FILTER,
                        listener = new TracingEventListener(),
                        CALL_BEFORE, CALL_RETURN, CALL_THROWS
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        assertEquals(30, sum(newInstance(calculatorClass), 10,20));
        listener.assertEventTracing(
                CALL_BEFORE,
                CALL_RETURN,
                CALL_BEFORE,
                CALL_RETURN
        );
    }

    @Test
    @Override
    public void cal$sum_add$before$changeParameters_at_add() throws Throwable {
        final TracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_SUM_and_ADD_FILTER,
                        listener = new TracingEventListener(){
                            @Override
                            public void onEvent(Event event) throws Throwable {
                                super.onEvent(event);
                                BeforeEvent beforeEvent = (BeforeEvent) event;
                                if (beforeEvent.javaMethodName.equalsIgnoreCase("add")){
                                    beforeEvent.argumentArray[0]=20;
                                }
                            }
                        },
                        BEFORE
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        assertEquals(40, sum(newInstance(calculatorClass), 10,20));
        listener.assertEventTracing(
                BEFORE,
                BEFORE,
                BEFORE
        );
    }

    @Test
    @Override
    public void cal$sum_add$before$returnImmediately_at_add() throws Throwable {
        final TracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_SUM_and_ADD_FILTER,
                        listener = new TracingEventListener(){
                            @Override
                            public void onEvent(Event event) throws Throwable {
                                super.onEvent(event);
                                BeforeEvent beforeEvent = (BeforeEvent) event;
                                if (beforeEvent.javaMethodName.equalsIgnoreCase("add")){
                                    returnImmediately(100);
                                }
                            }
                        },
                        BEFORE
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        assertEquals(100, sum(newInstance(calculatorClass), 10,20));
        listener.assertEventTracing(
                BEFORE,
                BEFORE,
                BEFORE
        );
    }

    @Test
    @Override
    public void cal$sum_add$before$throwsImmediately_at_add() throws Throwable {
        final TracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_SUM_and_ADD_FILTER,
                        listener = new TracingEventListener(){
                            @Override
                            public void onEvent(Event event) throws Throwable {
                                super.onEvent(event);
                                BeforeEvent beforeEvent = (BeforeEvent) event;
                                if (beforeEvent.javaMethodName.equalsIgnoreCase("add")){
                                    throwsImmediately(new Throwable(ERROR_EXCEPTION_MESSAGE));
                                }
                            }
                        },
                        BEFORE
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        try{
            sum(newInstance(calculatorClass), 10,20);
        }catch (Throwable throwable){
            assertEquals(ERROR_EXCEPTION_MESSAGE, throwable.getMessage());
        }
        listener.assertEventTracing(
                BEFORE,
                BEFORE
        );
    }

    @Test
    @Override
    public void cal$sum_add$return$changeParameters_at_add() throws Throwable {
        final TracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_SUM_and_ADD_FILTER,
                        listener = new TracingEventListener(){
                            @Override
                            public void onEvent(Event event) throws Throwable {
                                super.onEvent(event);
                                if (isSpecialMethodEvent(event, "add")){
                                    returnImmediately(100);
                                }
                            }
                        },
                        BEFORE,RETURN
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        assertEquals(100, sum(newInstance(calculatorClass), 10,20));
        assertEquals(true, stack.isEmpty());
        listener.assertEventTracing(
                BEFORE,
                BEFORE,
                RETURN,
                BEFORE,
                RETURN,
                RETURN
        );
    }

    @Test
    @Override
    public void cal$sum_add$return$returnImmediately_at_add() throws Throwable {
        final TracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_SUM_and_ADD_FILTER,
                        listener = new TracingEventListener(){
                            @Override
                            public void onEvent(Event event) throws Throwable {
                                super.onEvent(event);
                                if (isSpecialMethodEvent(event, "add")){
                                    returnImmediately(100);
                                }
                            }
                        },
                        BEFORE,RETURN
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        assertEquals(100, sum(newInstance(calculatorClass), 10,20));
        assertEquals(true, stack.isEmpty());
        listener.assertEventTracing(
                BEFORE,
                BEFORE,
                RETURN,
                BEFORE,
                RETURN,
                RETURN
        );
    }

    @Test
    @Override
    public void cal$sum_add$return$throwsImmediately_at_add() throws Throwable {
        final TracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_SUM_and_ADD_FILTER,
                        listener = new TracingEventListener(){
                            @Override
                            public void onEvent(Event event) throws Throwable {
                                super.onEvent(event);
                                if (isSpecialMethodEvent(event, "add")){
                                    throwsImmediately(new Throwable(ERROR_EXCEPTION_MESSAGE));
                                }
                            }
                        },
                        BEFORE,RETURN,THROWS
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        try {
            sum(newInstance(calculatorClass), 10,20);
        }catch (Throwable throwable){
            assertEquals(ERROR_EXCEPTION_MESSAGE, throwable.getMessage());
        }

        assertEquals(true, stack.isEmpty());
        listener.assertEventTracing(
                BEFORE,
                BEFORE,
                RETURN,
                THROWS
        );
    }

    @Test
    @Override
    public void cal$sum_add$throws$changeParameters_at_add() throws Throwable {
        final TracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_SUM_and_ADD_FILTER,
                        listener = new TracingEventListener(){
                            @Override
                            public void onEvent(Event event) throws Throwable {
                                super.onEvent(event);
                                if (isSpecialMethodEvent(event, "add")){
                                    ((BeforeEvent)stack.peek()).argumentArray[0] = 100;
                                }
                            }
                        },
                        BEFORE,RETURN,THROWS
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        try {
            sum(newInstance(calculatorClass, ADD$EXCEPTION), 10,20);
        }catch (RuntimeException throwable){
            assertEquals(ERROR_EXCEPTION_MESSAGE, throwable.getMessage());
        }

        assertEquals(true, stack.isEmpty());
        listener.assertEventTracing(
                BEFORE,
                BEFORE,
                THROWS,
                THROWS
        );
    }

    @Test
    @Override
    public void cal$sum_add$throws$returnImmediately_at_add() throws Throwable {
        final TracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_SUM_and_ADD_FILTER,
                        listener = new TracingEventListener(){
                            @Override
                            public void onEvent(Event event) throws Throwable {
                                super.onEvent(event);
                                if (isSpecialMethodEvent(event, "add")){
                                    returnImmediately(100);
                                }
                            }
                        },
                        BEFORE,RETURN,THROWS
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        assertEquals(100, sum(newInstance(calculatorClass, ADD$EXCEPTION), 10,20));
        assertEquals(true, stack.isEmpty());
        listener.assertEventTracing(
                BEFORE,
                BEFORE,
                THROWS,
                BEFORE,
                THROWS,
                RETURN
        );
    }

    @Test
    @Override
    public void cal$sum_add$throws$throwsImmediately_at_add() throws Throwable {
        final TracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_SUM_and_ADD_FILTER,
                        listener = new TracingEventListener(){
                            @Override
                            public void onEvent(Event event) throws Throwable {
                                super.onEvent(event);
                                if (isSpecialMethodEvent(event, "add")){
                                    throwsImmediately(new Throwable(ERROR_EXCEPTION_MESSAGE));
                                }
                            }
                        },
                        BEFORE,RETURN,THROWS
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        try {
            sum(newInstance(calculatorClass, ADD$EXCEPTION), 10,20);
        }catch (Throwable throwable){
            assertEquals(ERROR_EXCEPTION_MESSAGE, throwable.getMessage());
        }

        assertEquals(true, stack.isEmpty());
        listener.assertEventTracing(
                BEFORE,
                BEFORE,
                THROWS,
                THROWS
        );
    }

    @Test
    @Override
    public void cal$pow$around() throws Throwable {
        final TracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_POW_FILTER,
                        listener = new TracingEventListener(),
                        BEFORE, RETURN, THROWS
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        assertEquals(25, pow(newInstance(calculatorClass), 5,2));
        listener.assertEventTracing(
                BEFORE,
                BEFORE,
                BEFORE,
                RETURN,
                RETURN,
                RETURN
        );
    }

    @Test
    @Override
    public void cal$pow$line() throws Throwable {
        final LineNumTracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_POW_FILTER,
                        listener = new LineNumTracingEventListener(),
                        LINE
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        assertEquals(25, pow(newInstance(calculatorClass), 5,2));
        listener.assertLIneTracing(
                109,
                115,
                109,
                115,
                109,
                110,
                113
        );
    }

    @Test
    @Override
    public void cal$pow$call() throws Throwable {
        final TracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_POW_FILTER,
                        listener = new TracingEventListener(),
                        CALL_BEFORE, CALL_RETURN, CALL_THROWS
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        assertEquals(25, pow(newInstance(calculatorClass), 5,2));
        listener.assertEventTracing(
                CALL_BEFORE,
                CALL_BEFORE,
                CALL_RETURN,
                CALL_RETURN
        );
    }

    @Test
    @Override
    public void cal$init_with_TestCase$around() throws Throwable {
        final TracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_INIT_FILTER,
                        listener = new TracingEventListener(),
                        BEFORE, RETURN, THROWS
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        assertEquals(30, sum(newInstance(calculatorClass), 10,20));
        listener.assertEventTracing(
                BEFORE,
                RETURN,
                BEFORE,
                RETURN
        );
    }

    @Test
    @Override
    public void cal$init_with_TestCase$line() throws Throwable {
        final LineNumTracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_INIT_FILTER,
                        listener = new LineNumTracingEventListener(),
                        LINE
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        assertEquals(30, sum(newInstance(calculatorClass), 10,20));
        listener.assertLIneTracing(
                64,
                65,
                68,
                57
        );
    }

    @Ignore
    @Override
    public void cal$init_with_TestCase$call() throws Throwable {
        final TracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_INIT_FILTER,
                        listener = new TracingEventListener(),
                        CALL_BEFORE, CALL_RETURN, CALL_THROWS
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        assertEquals(25, pow(newInstance(calculatorClass), 5,2));
        listener.assertEventTracing(
                CALL_BEFORE,
                CALL_RETURN
        );
    }

    @Test
    @Override
    public void cal$init_with_TestCase$before$changeParameters() throws Throwable {
        final TracingEventListener listener;
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        CALCULATOR_INIT_FILTER_WITH_TEST_CASE,
                        listener = new TracingEventListener(){
                            @Override
                            public void onEvent(Event event) throws Throwable {
                                super.onEvent(event);
                                if (event instanceof BeforeEvent){
                                    ((BeforeEvent) event).argumentArray[0] = Calculator.TestCase.ADD$EXCEPTION;
                                }

                            }
                        },
                        BEFORE, RETURN, THROWS
                )
                .loadClass(CALCULATOR_CLASS_NAME);

        try {
            assertEquals(-1, sum(newInstance(calculatorClass), 10,20));
        }catch (RuntimeException e){
            assertEquals(ERROR_EXCEPTION_MESSAGE, e.getMessage());
        }

        listener.assertEventTracing(
                BEFORE,
                RETURN
        );
    }
}
