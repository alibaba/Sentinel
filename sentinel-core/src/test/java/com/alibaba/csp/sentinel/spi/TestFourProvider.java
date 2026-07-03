package com.alibaba.csp.sentinel.spi;

/**
 * This Provider class isn't configured in SPI file.
 *
 * @author cdfive
 */
@Spi(value = "four", isSingleton = true, order = -400)
public class TestFourProvider implements TestInterface {

}
