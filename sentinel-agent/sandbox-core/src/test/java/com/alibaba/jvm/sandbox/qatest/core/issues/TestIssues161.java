package com.alibaba.jvm.sandbox.qatest.core.issues;

import com.alibaba.jvm.sandbox.api.listener.ext.Advice;
import com.alibaba.jvm.sandbox.api.listener.ext.AdviceListener;
import com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator;
import com.alibaba.jvm.sandbox.qatest.core.util.JvmHelper;
import org.junit.Assert;
import org.junit.Test;

import static com.alibaba.jvm.sandbox.qatest.core.util.CalculatorHelper.*;
import static org.junit.Assert.assertEquals;

public class TestIssues161 {

    @Test
    public void cal$sum$around() throws Throwable {
        final Class<?> calculatorClass = JvmHelper
                .createJvm()
                .defineClass(
                        Calculator.class,
                        new JvmHelper.Transformer(
                                CALCULATOR_SUM_FILTER,
                                new AdviceListener(){
                                    @Override
                                    protected void before(Advice advice) {
                                        Assert.assertEquals(
                                                "com.alibaba.jvm.sandbox.qatest.core.util.JvmHelper.PrivateClassLoader",
                                                advice.getLoader().getClass().getCanonicalName()
                                        );
                                    }
                                }
                        )
                ).loadClass(CALCULATOR_CLASS_NAME);
        assertEquals(30, sum(newInstance(calculatorClass), 10, 20));
    }

}
