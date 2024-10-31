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
package com.alibaba.csp.sentinel.log.jul;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.alibaba.csp.sentinel.log.jul.ConsoleHandler;
import com.alibaba.csp.sentinel.log.jul.CspFormatter;

import static org.junit.Assert.*;

/**
 * Test cases for {@link ConsoleHandler}.
 *
 * @author cdfive
 */
public class ConsoleHandlerTest {

    @Test
    public void testInfoPublish() {
        ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baosOut));

        ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
        System.setErr(new PrintStream(baosErr));

        CspFormatter cspFormatter = new CspFormatter();
        ConsoleHandler consoleHandler = new ConsoleHandler();

        LogRecord logRecord;

        // Test INFO level, should log to stdout
        logRecord = new LogRecord(Level.INFO, "test info message");
        consoleHandler.publish(logRecord);

        consoleHandler.close();
        assertEquals(cspFormatter.format(logRecord), baosOut.toString());
        assertEquals("", baosErr.toString());
        baosOut.reset();
        baosErr.reset();
    }

    @Test
    public void testWarnPublish() {
        ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baosOut));

        ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
        System.setErr(new PrintStream(baosErr));

        CspFormatter cspFormatter = new CspFormatter();
        ConsoleHandler consoleHandler = new ConsoleHandler();

        LogRecord logRecord;

        // Test INFO level, should log to stderr
        logRecord = new LogRecord(Level.WARNING, "test warning message");
        consoleHandler.publish(logRecord);

        consoleHandler.close();
        assertEquals(cspFormatter.format(logRecord), baosErr.toString());
        assertEquals("", baosOut.toString());
        baosOut.reset();
        baosErr.reset();
    }

    @Test
    public void testFinePublish() {
        ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baosOut));

        ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
        System.setErr(new PrintStream(baosErr));

        CspFormatter cspFormatter = new CspFormatter();
        ConsoleHandler consoleHandler = new ConsoleHandler();

        LogRecord logRecord;

        // Test FINE level, no log by default
        // Default log level is INFO, to log FINE message to stdout, add following config in $JAVA_HOME/jre/lib/logging.properties
        // java.util.logging.StreamHandler.level=FINE
        logRecord = new LogRecord(Level.FINE, "test fine message");
        consoleHandler.publish(logRecord);

        consoleHandler.close();
        assertEquals("", baosOut.toString());
        assertEquals("", baosErr.toString());
        baosOut.reset();
        baosErr.reset();
    }

    @Test
    public void testSeverePublish() {
        ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baosOut));

        ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
        System.setErr(new PrintStream(baosErr));

        CspFormatter cspFormatter = new CspFormatter();
        ConsoleHandler consoleHandler = new ConsoleHandler();

        LogRecord logRecord;
        // Test SEVERE level, should log to stderr
        logRecord = new LogRecord(Level.SEVERE, "test severe message");
        consoleHandler.publish(logRecord);

        consoleHandler.close();
        assertEquals(cspFormatter.format(logRecord), baosErr.toString());
        assertEquals("", baosOut.toString());
        baosOut.reset();
        baosErr.reset();
    }

}
