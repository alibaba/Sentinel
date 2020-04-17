/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.demo.log.logback;

import com.alibaba.csp.sentinel.log.RecordLog;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

/**
 * @author xue8
 */
public class RecordLogTest {
    @Rule
    public SystemOutRule log = new SystemOutRule().enableLog();

    @Test
    public void testLog() {
        RecordLog.info("init");
        log.clearLog();
        int count = 0;

        // info test
        while (count++ < 1000) {
            log.clearLog();
            RecordLog.info("Count {}", count);
            String str = String.format("INFO  sentinelRecordLogger - Count %d" + System.lineSeparator(), count);
            Assert.assertEquals(str, log.getLog());
        }

        // warn test
        while (count++ < 2000) {
            log.clearLog();
            RecordLog.warn("Count {}", count);
            String str = String.format("WARN  sentinelRecordLogger - Count %d" + System.lineSeparator(), count);
            Assert.assertEquals(str, log.getLog());
        }

        // trace test
        while (count++ < 3000) {
            log.clearLog();
            RecordLog.trace("Count {}", count);
            String str = String.format("TRACE sentinelRecordLogger - Count %d" + System.lineSeparator(), count);
            Assert.assertEquals(str, log.getLog());
        }

        // debug test
        while (count++ < 4000) {
            log.clearLog();
            RecordLog.debug("Count {}", count);
            String str = String.format("DEBUG sentinelRecordLogger - Count %d" + System.lineSeparator(), count);
            Assert.assertEquals(str, log.getLog());
        }

        // test error
        while (count++ < 5000) {
            log.clearLog();
            RecordLog.error("Count {}", count);
            String str = String.format("ERROR sentinelRecordLogger - Count %d" + System.lineSeparator(), count);
            Assert.assertEquals(str, log.getLog());
        }
    }

    @Test
    public void testLogException() {
        RecordLog.info("init");
        log.clearLog();
        Exception e = new Exception("ex");

        // info test
        RecordLog.info("Error", e);
        // split the log for test
        String[] logSplit = log.getLog().split(System.lineSeparator());
        Assert.assertEquals("INFO  sentinelRecordLogger - Error", logSplit[0]);

        // warn test
        log.clearLog();
        RecordLog.warn("Error", e);
        logSplit = log.getLog().split(System.lineSeparator());
        Assert.assertEquals("WARN  sentinelRecordLogger - Error", logSplit[0]);

        // trace test
        log.clearLog();
        RecordLog.trace("Error", e);
        logSplit = log.getLog().split(System.lineSeparator());
        Assert.assertEquals("TRACE sentinelRecordLogger - Error", logSplit[0]);

        // debug test
        log.clearLog();
        RecordLog.debug("Error", e);
        logSplit = log.getLog().split(System.lineSeparator());
        Assert.assertEquals("DEBUG sentinelRecordLogger - Error", logSplit[0]);

        // error test
        log.clearLog();
        RecordLog.error("Error", e);
        logSplit = log.getLog().split(System.lineSeparator());
        Assert.assertEquals("ERROR sentinelRecordLogger - Error", logSplit[0]);
    }

}
