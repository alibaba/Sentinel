package com.alibaba.csp.sentinel.spi;

/**
 * @author cdfive
 */
@Spi(value = "two", isSingleton = true, order = -200)
public class TestTwoProvider implements TestInterface {

}
