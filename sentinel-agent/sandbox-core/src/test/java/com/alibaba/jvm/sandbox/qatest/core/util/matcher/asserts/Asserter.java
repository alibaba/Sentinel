package com.alibaba.jvm.sandbox.qatest.core.util.matcher.asserts;

public interface Asserter<T> {

    void assertThat(String message, T target);

}
