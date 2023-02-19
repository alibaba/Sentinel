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

import java.io.UnsupportedEncodingException;
import java.nio.channels.AsynchronousFileChannel;
import java.security.Permission;
import java.util.logging.*;

import static java.lang.System.getProperty;
import static jdk.nashorn.internal.runtime.options.Options.getStringProperty;

public class AsyncStreamHandler extends Handler {

    private AsynchronousFileChannel fileChannel;
    private boolean doneHeader;
    private volatile AsyncFileWriter writer;


    // Private method to configure a StreamHandler from LogManager
    // properties and/or default values as specified in the class
    // javadoc.
    private void configure() {
        LogManager manager = LogManager.getLogManager();
        String cname = getClass().getName();

        setLevel(getLevelProperty(cname +".level", Level.INFO));
        setFilter(getFilterProperty(cname +".filter", null));
        setFormatter(getFormatterProperty(cname +".formatter", new SimpleFormatter()));
        try {
            setEncoding(getStringProperty(cname +".encoding", null));
        } catch (Exception ex) {
            try {
                setEncoding(null);
            } catch (Exception ex2) {
                // doing a setEncoding with null should always work.
                // assert false;
            }
        }
    }

    /**
     * Create a <tt>StreamHandler</tt>, with no current output stream.
     */
    public AsyncStreamHandler() {
        configure();
    }

    /**
     * Create a <tt>StreamHandler</tt> with a given <tt>Formatter</tt>
     * and output stream.
     * <p>
     * @param fileChannel         the target output stream
     * @param formatter   Formatter to be used to format output
     */
    public AsyncStreamHandler(AsynchronousFileChannel fileChannel, Formatter formatter) {
        configure();
        setFormatter(formatter);
        setAsyncChannel(fileChannel);
    }

    /**
     * Change the output stream.
     * <P>
     * If there is a current output stream then the <tt>Formatter</tt>'s
     * tail string is written and the stream is flushed and closed.
     * Then the output stream is replaced with the new output stream.
     *
     * @param channel   New AsynchronousFileChannel.  May not be null.
     * @exception  SecurityException  if a security manager exists and if
     *             the caller does not have <tt>LoggingPermission("control")</tt>.
     */
    protected synchronized void setAsyncChannel(AsynchronousFileChannel channel) throws SecurityException {
        if (channel == null) {
            throw new NullPointerException();
        }
        flushAndClose();
        fileChannel = channel;
        doneHeader = false;
        writer = new AsyncFileWriter(channel);


    }

    /**
     * Set (or change) the character encoding used by this <tt>Handler</tt>.
     * <p>
     * The encoding should be set before any <tt>LogRecords</tt> are written
     * to the <tt>Handler</tt>.
     *
     * @param encoding  The name of a supported character encoding.
     *        May be null, to indicate the default platform encoding.
     * @exception  SecurityException  if a security manager exists and if
     *             the caller does not have <tt>LoggingPermission("control")</tt>.
     * @exception  UnsupportedEncodingException if the named encoding is
     *          not supported.
     */
    @Override
    public synchronized void setEncoding(String encoding)
            throws SecurityException, java.io.UnsupportedEncodingException {
        super.setEncoding(encoding);
        if (fileChannel == null) {
            return;
        }
        // Replace the current writer with a writer for the new encoding.
        flush();
        writer = new AsyncFileWriter(fileChannel);

    }

    /**
     * Format and publish a <tt>LogRecord</tt>.
     * <p>
     * The <tt>StreamHandler</tt> first checks if there is an <tt>OutputStream</tt>
     * and if the given <tt>LogRecord</tt> has at least the required log level.
     * If not it silently returns.  If so, it calls any associated
     * <tt>Filter</tt> to check if the record should be published.  If so,
     * it calls its <tt>Formatter</tt> to format the record and then writes
     * the result to the current output stream.
     * <p>
     * If this is the first <tt>LogRecord</tt> to be written to a given
     * <tt>OutputStream</tt>, the <tt>Formatter</tt>'s "head" string is
     * written to the stream before the <tt>LogRecord</tt> is written.
     *
     * @param  record  description of the log event. A null record is
     *                 silently ignored and is not published
     */
    @Override
    public synchronized void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        String msg;
        try {
            msg = getFormatter().format(record);
        } catch (Exception ex) {
            // We don't want to throw an exception here, but we
            // report the exception to any registered ErrorManager.
            reportError(null, ex, ErrorManager.FORMAT_FAILURE);
            return;
        }

        try {
            if (!doneHeader) {
                writer.write(getFormatter().getHead(this));
                doneHeader = true;
            }
            writer.write(msg);
        } catch (Exception ex) {
            // We don't want to throw an exception here, but we
            // report the exception to any registered ErrorManager.
            reportError(null, ex, ErrorManager.WRITE_FAILURE);
        }
    }


    /**
     * Check if this <tt>Handler</tt> would actually log a given <tt>LogRecord</tt>.
     * <p>
     * This method checks if the <tt>LogRecord</tt> has an appropriate level and
     * whether it satisfies any <tt>Filter</tt>.  It will also return false if
     * no output stream has been assigned yet or the LogRecord is null.
     * <p>
     * @param record  a <tt>LogRecord</tt>
     * @return true if the <tt>LogRecord</tt> would be logged.
     *
     */
    @Override
    public boolean isLoggable(LogRecord record) {
        if (writer == null || record == null) {
            return false;
        }
        return super.isLoggable(record);
    }

    /**
     * Flush any buffered messages.
     */
    @Override
    public synchronized void flush() {
        if (writer != null) {
            try {
                writer.flush();
            } catch (Exception ex) {
                // We don't want to throw an exception here, but we
                // report the exception to any registered ErrorManager.
                reportError(null, ex, ErrorManager.FLUSH_FAILURE);
            }
        }
    }

    private synchronized void flushAndClose() throws SecurityException {
        checkPermission();
        if (writer != null) {
            try {
                if (!doneHeader) {
                    writer.write(getFormatter().getHead(this));
                    doneHeader = true;
                }
                writer.write(getFormatter().getTail(this));
                writer.flush();
                writer.close();
            } catch (Exception ex) {
                // We don't want to throw an exception here, but we
                // report the exception to any registered ErrorManager.
                reportError(null, ex, ErrorManager.CLOSE_FAILURE);
            }
            writer = null;
            fileChannel = null;
        }
    }

    /**
     * Close the current output stream.
     * <p>
     * The <tt>Formatter</tt>'s "tail" string is written to the stream before it
     * is closed.  In addition, if the <tt>Formatter</tt>'s "head" string has not
     * yet been written to the stream, it will be written before the
     * "tail" string.
     *
     * @exception  SecurityException  if a security manager exists and if
     *             the caller does not have LoggingPermission("control").
     */
    @Override
    public synchronized void close() throws SecurityException {
        flushAndClose();
    }


    // Package private method to get a Level property.
    // If the property is not defined or cannot be parsed
    // we return the given default value.
    Level getLevelProperty(String name, Level defaultValue) {
        String val = getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        Level l = Level.parse(val.trim());
        return l != null ? l : defaultValue;
    }

    // Package private method to get a filter property.
    // We return an instance of the class named by the "name"
    // property. If the property is not defined or has problems
    // we return the defaultValue.
    Filter getFilterProperty(String name, Filter defaultValue) {
        String val = getProperty(name);
        try {
            if (val != null) {
                Class<?> clz = ClassLoader.getSystemClassLoader().loadClass(val);
                return (Filter) clz.newInstance();
            }
        } catch (Exception ex) {
            // We got one of a variety of exceptions in creating the
            // class or creating an instance.
            // Drop through.
        }
        // We got an exception.  Return the defaultValue.
        return defaultValue;
    }


    // Package private method to get a formatter property.
    // We return an instance of the class named by the "name"
    // property. If the property is not defined or has problems
    // we return the defaultValue.
    Formatter getFormatterProperty(String name, Formatter defaultValue) {
        String val = getProperty(name);
        try {
            if (val != null) {
                Class<?> clz = ClassLoader.getSystemClassLoader().loadClass(val);
                return (Formatter) clz.newInstance();
            }
        } catch (Exception ex) {
            // We got one of a variety of exceptions in creating the
            // class or creating an instance.
            // Drop through.
        }
        // We got an exception.  Return the defaultValue.
        return defaultValue;
    }

    private final Permission controlPermission = new LoggingPermission("control", null);

    void checkPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(controlPermission);
    }
}
