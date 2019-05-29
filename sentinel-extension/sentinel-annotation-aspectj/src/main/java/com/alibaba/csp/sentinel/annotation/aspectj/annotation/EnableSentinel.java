package com.alibaba.csp.sentinel.annotation.aspectj.annotation;

import com.alibaba.csp.sentinel.annotation.aspectj.configuration.SentinelConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Documented
@Import({SentinelConfiguration.class})
public @interface EnableSentinel {
}
