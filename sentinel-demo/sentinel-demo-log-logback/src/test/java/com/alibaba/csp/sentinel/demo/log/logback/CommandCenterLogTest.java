/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.demo.log.logback;

import com.alibaba.csp.sentinel.transport.log.CommandCenterLog;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

/**
 * @author xue8
 */
public class CommandCenterLogTest {
    @Rule
    public SystemOutRule log = new SystemOutRule().enableLog();

    @Test
    public void testLog() {
        CommandCenterLog.info("init");
        log.clearLog();
        int count = 0;

        // info test
        while (count++ < 1000) {
            log.clearLog();
            CommandCenterLog.info("Count {}", count);
            String str = String.format("INFO  sentinelCommandCenterLogger - Count %d" + System.lineSeparator(), count);
            Assert.assertEquals(str, log.getLog());
        }

        // warn test
        while (count++ < 2000) {
            log.clearLog();
            CommandCenterLog.warn("Count {}", count);
            String str = String.format("WARN  sentinelCommandCenterLogger - Count %d" + System.lineSeparator(), count);
            Assert.assertEquals(str, log.getLog());
        }

        // trace test
        while (count++ < 3000) {
            log.clearLog();
            CommandCenterLog.trace("Count {}", count);
            String str = String.format("TRACE sentinelCommandCenterLogger - Count %d" + System.lineSeparator(), count);
            Assert.assertEquals(str, log.getLog());
        }

        // debug test
        while (count++ < 4000) {
            log.clearLog();
            CommandCenterLog.debug("Count {}", count);
            String str = String.format("DEBUG sentinelCommandCenterLogger - Count %d" + System.lineSeparator(), count);
            Assert.assertEquals(str, log.getLog());
        }

        // test error
        while (count++ < 5000) {
            log.clearLog();
            CommandCenterLog.error("Count {}", count);
            String str = String.format("ERROR sentinelCommandCenterLogger - Count %d" + System.lineSeparator(), count);
            Assert.assertEquals(str, log.getLog());
        }
    }

    @Test
    public void testLogException() {
        CommandCenterLog.info("init");
        log.clearLog();
        Exception e = new Exception("ex");

        // info test
        CommandCenterLog.info("Error", e);
        // split the log for test
        String[] logSplit = log.getLog().split(System.lineSeparator());
        Assert.assertEquals("INFO  sentinelCommandCenterLogger - Error", logSplit[0]);

        // warn test
        log.clearLog();
        CommandCenterLog.warn("Error", e);
        logSplit = log.getLog().split(System.lineSeparator());
        Assert.assertEquals("WARN  sentinelCommandCenterLogger - Error", logSplit[0]);

        // trace test
        log.clearLog();
        CommandCenterLog.trace("Error", e);
        logSplit = log.getLog().split(System.lineSeparator());
        Assert.assertEquals("TRACE sentinelCommandCenterLogger - Error", logSplit[0]);

        // debug test
        log.clearLog();
        CommandCenterLog.debug("Error", e);
        logSplit = log.getLog().split(System.lineSeparator());
        Assert.assertEquals("DEBUG sentinelCommandCenterLogger - Error", logSplit[0]);

        // error test
        log.clearLog();
        CommandCenterLog.error("Error", e);
        logSplit = log.getLog().split(System.lineSeparator());
        Assert.assertEquals("ERROR sentinelCommandCenterLogger - Error", logSplit[0]);
    }


}
