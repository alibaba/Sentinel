package com.alibaba.csp.sentinel.spi;

/**
 * @author cdfive
 */
@Spi(order = -300, isSingleton = false)
public class TestThreeProvider implements TestInterface {

}
