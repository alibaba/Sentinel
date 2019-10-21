package com.alibaba.jvm.sandbox.api.annotation;

import com.alibaba.jvm.sandbox.api.filter.Filter;

import java.lang.annotation.*;

/**
 * 拥有此标注的{@link Filter}将能匹配到目标类的子类
 *
 * @author luanjia@taobao.com
 * @since {@code sandbox-api:1.0.10}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface IncludeSubClasses {
}
