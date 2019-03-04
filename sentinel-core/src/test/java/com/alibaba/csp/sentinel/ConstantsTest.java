package com.alibaba.csp.sentinel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for {@link Constants}.
 *
 * @author cdfive
 */
public class ConstantsTest {

    @Test
    public void testDefaultTimeDropValue() {
        assertEquals(4900, Constants.TIME_DROP_VALVE);
    }

//    add JVM parameter
//    -Dcsp.sentinel.statistic.max.rt=10000
//    @Test
    public void testCustomTimeDropValue() {
        assertEquals(10000, Constants.TIME_DROP_VALVE);
    }
}