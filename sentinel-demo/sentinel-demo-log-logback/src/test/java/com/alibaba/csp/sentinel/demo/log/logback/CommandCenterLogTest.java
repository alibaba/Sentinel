package com.alibaba.csp.sentinel.demo.log.logback;

import com.alibaba.csp.sentinel.log.CommandCenterLog;
import org.junit.Test;

/**
 * @author xue8
 */
public class CommandCenterLogTest {
    @Test
    public void testLogException() {
        Exception e = new Exception("ex");
        CommandCenterLog.info("Error", e);
    }

    @Test
    public void testLogRolling() {
        int count = 1000;
        while (--count > 0) {
            CommandCenterLog.info("Count " + count);
        }
    }
}
