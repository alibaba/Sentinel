package com.alibaba.csp.sentinel.spi;

/**
 * @author cdfive
 */
@Spi(value = "four", order = -400, singleton = true)
public class TestFourProvider implements TestInterface {

}
