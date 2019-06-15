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
package com.alibaba.csp.sentinel.log;

import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link ConsoleHandler}.
 *
 * @author cdfive
 */
public class ConsoleHandlerTest {

    @Test
    public void testPublish() {
        ConsoleHandler consoleHandler = new ConsoleHandler();

        LogRecord logRecord = mock(LogRecord.class);
        when(logRecord.getMillis()).thenReturn(System.currentTimeMillis());

        when(logRecord.getLevel()).thenReturn(Level.INFO);
        when(logRecord.getMessage()).thenReturn("test info message");
        consoleHandler.publish(logRecord);
        assertEquals(consoleHandler.getStdoutHandler(), consoleHandler.getCurrentHandler());

        when(logRecord.getLevel()).thenReturn(Level.WARNING);
        when(logRecord.getMessage()).thenReturn("test warning message");
        consoleHandler.publish(logRecord);
        assertEquals(consoleHandler.getStderrHandler(), consoleHandler.getCurrentHandler());

        // Default log level is INFO, to view FINE log message, add following config in $JAVA_HOME/jre/lib/logging.properties
        // java.util.logging.StreamHandler.level=FINE
        when(logRecord.getLevel()).thenReturn(Level.FINE);
        when(logRecord.getMessage()).thenReturn("test fine message");
        consoleHandler.publish(logRecord);
        assertEquals(consoleHandler.getStdoutHandler(), consoleHandler.getCurrentHandler());

        when(logRecord.getLevel()).thenReturn(Level.SEVERE);
        when(logRecord.getMessage()).thenReturn("test severe message");
        consoleHandler.publish(logRecord);
        assertEquals(consoleHandler.getStderrHandler(), consoleHandler.getCurrentHandler());
    }
}
