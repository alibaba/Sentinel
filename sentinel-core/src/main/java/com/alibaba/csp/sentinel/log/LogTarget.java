package com.alibaba.csp.sentinel.log;

import java.lang.annotation.*;

/**
 * @author xue8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface LogTarget {
    LogType value() default LogType.RECORD_LOG;
}
