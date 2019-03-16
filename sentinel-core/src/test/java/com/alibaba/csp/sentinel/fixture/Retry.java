package com.alibaba.csp.sentinel.fixture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a test case should be retried, e.g. for flakiness control.
 * If a test case fails, it will be retried unless it reaches {@link #maxCount()} or succeeds.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Retry {
    /**
     * How many times the test case should be retried before a success.
     *
     * @return the max retry count.
     */
    int maxCount() default 2;
}
