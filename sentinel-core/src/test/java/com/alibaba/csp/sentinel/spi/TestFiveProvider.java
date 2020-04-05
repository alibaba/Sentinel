package com.alibaba.csp.sentinel.spi;

/**
 * @author cdfive
 */
@Spi(value = "five", isDefault = true, order = -270)
public class TestFiveProvider implements TestInterface {

}
