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

package com.alibaba.csp.sentinel.logging.slf4j;

import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

import java.io.PrintStream;

/**
 * @author xue8
 * @author jason
 */
public abstract class AbstraceSlf4jLogTest {
    private static final class TestException extends Exception {
        private static final long serialVersionUID = 1L;
        
    }
    PrintStream mockStream;
    PrintStream oldOutStream;
    PrintStream oldErrStream;
    
    protected abstract String getLoggerName();
    protected abstract void debug(String msg, Object... args);
    protected abstract void trace(String msg, Object... args);
    protected abstract void info(String msg, Object... args);
    protected abstract void warn(String msg, Object... args);
    protected abstract void error(String msg, Object... args);
    
    @Before
    public void mockOutput() {
        System.out.println("Try to mock System.out and System.err");
        mockStream = mock(PrintStream.class);
        oldOutStream = System.out;
        oldOutStream = System.out;
        System.setOut(mockStream);
        System.setErr(mockStream);
        TestLoggerFactory.getInstance().setPrintLevel(Level.TRACE);
    }
    
    @After
    public void restore() {
        if (oldOutStream != null) {
            System.setOut(oldOutStream);
            System.setErr(oldErrStream);
            oldOutStream = null;
            oldErrStream = null;
            System.out.println("Restore System.out and System.err");
        }
    }

    @Test
    public void testRecordLog() {
        info("init");
        verify(mockStream).println(contains("init"));
        clearInvocations(mockStream);
        int count = 0;

        // info test
        while (count < 1000) { // 0~999
            info("Count {}.", count);
            count ++;
        }
        verify(mockStream).println(contains("Count 0."));
        verify(mockStream).println(contains("Count 1."));
        verify(mockStream).println(contains("Count 99."));
        verify(mockStream).println(contains("Count 123."));
        verify(mockStream).println(contains("Count 888."));
        verify(mockStream).println(contains("Count 999."));
        verify(mockStream, times(1000)).println(contains(" INFO "));
        verify(mockStream, times(1000)).println(contains(getLoggerName()));
        clearInvocations(mockStream);

        // warn test
        while (count < 2000) { // 1000~1999
            warn("Count {}.", count);
            count ++;
        }
        verify(mockStream).println(contains("Count 1000."));
        verify(mockStream).println(contains("Count 1223."));
        verify(mockStream).println(contains("Count 1888."));
        verify(mockStream).println(contains("Count 1999."));
        verify(mockStream, times(1000)).println(contains(" WARN "));
        verify(mockStream, times(1000)).println(contains(getLoggerName()));
        clearInvocations(mockStream);

        // trace test
        while (count < 3000) { // 2000~2999
            trace("Count {}.", count);
            count ++;
        }
        verify(mockStream).println(contains("Count 2000."));
        verify(mockStream).println(contains("Count 2999."));
        verify(mockStream, times(1000)).println(contains(" TRACE "));
        verify(mockStream, times(1000)).println(contains(getLoggerName()));
        clearInvocations(mockStream);

        // debug test
        while (count < 4000) { // 3000~3999
            debug("Count {}.", count);
            count ++;
        }
        verify(mockStream).println(contains("Count 3000."));
        verify(mockStream).println(contains("Count 3999."));
        verify(mockStream, times(1000)).println(contains(" DEBUG "));
        verify(mockStream, times(1000)).println(contains(getLoggerName()));
        clearInvocations(mockStream);

        // test error
        while (count < 5000) { // 4000~4999
            error("Count {}.", count);
            count ++;
        }
        verify(mockStream).println(contains("Count 4000."));
        verify(mockStream).println(contains("Count 4999."));
        verify(mockStream, times(1000)).println(contains(" ERROR "));
        verify(mockStream, times(1000)).println(contains(getLoggerName()));
        clearInvocations(mockStream);
    }


    @Test
    public void testLogException() {
        info("init");
        verify(mockStream).println(contains("init"));
        verify(mockStream, atLeastOnce()).println(contains(getLoggerName()));
        clearInvocations(mockStream);
        
        Exception e = new TestException();

        // info test
        info("some log", e);
        verify(mockStream).println(contains("INFO"));
        verify(mockStream).println(isA(TestException.class));
        verify(mockStream).println(contains(getLoggerName()));
        clearInvocations(mockStream);

        // warn test
        warn("some log", e);
        verify(mockStream).println(contains("WARN"));
        verify(mockStream).println(isA(TestException.class));
        verify(mockStream).println(contains(getLoggerName()));
        clearInvocations(mockStream);

        // trace test
        trace("some log", e);
        verify(mockStream).println(contains("TRACE"));
        verify(mockStream).println(isA(TestException.class));
        verify(mockStream).println(contains(getLoggerName()));
        clearInvocations(mockStream);

        // debug test
        debug("some log", e);
        verify(mockStream).println(contains("DEBUG"));
        verify(mockStream).println(isA(TestException.class));
        verify(mockStream).println(contains(getLoggerName()));
        clearInvocations(mockStream);

        // error test
        error("some log", e);
        verify(mockStream).println(contains("ERROR"));
        verify(mockStream).println(isA(TestException.class));
        verify(mockStream).println(contains(getLoggerName()));
        clearInvocations(mockStream);
    }
}
