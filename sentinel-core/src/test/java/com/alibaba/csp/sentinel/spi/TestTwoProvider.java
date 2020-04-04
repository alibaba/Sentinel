package com.alibaba.csp.sentinel.spi;

/**
 * @author cdfive
 */
@Spi(value = "two", order = -200, singleton = true)
public class TestTwoProvider implements TestInterface {

}
