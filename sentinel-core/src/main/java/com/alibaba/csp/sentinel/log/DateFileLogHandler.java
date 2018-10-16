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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

class DateFileLogHandler extends Handler {

    private final ThreadLocal<SimpleDateFormat> dateFormatThreadLocal = new ThreadLocal<SimpleDateFormat>() {
        @Override
        public SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };

    private volatile FileHandler handler;

    private final String pattern;
    private final int limit;
    private final int count;
    private final boolean append;

    private volatile boolean initialized = false;

    private volatile long startDate = System.currentTimeMillis();
    private volatile long endDate;

    private final Object monitor = new Object();

    DateFileLogHandler(String pattern, int limit, int count, boolean append) throws SecurityException {
        this.pattern = pattern;
        this.limit = limit;
        this.count = count;
        this.append = append;
        rotateDate();
        this.initialized = true;
    }

    @Override
    public void close() throws SecurityException {
        handler.close();
    }

    @Override
    public void flush() {
        handler.flush();
    }

    @Override
    public void publish(LogRecord record) {
        if (shouldRotate(record)) {
            synchronized (monitor) {
                if (shouldRotate(record)) {
                    rotateDate();
                }
            }
        }
        if (System.currentTimeMillis() - startDate > 25 * 60 * 60 * 1000) {
            String msg = record.getMessage();
            record.setMessage("missed file rolling at: " + new Date(endDate) + "\n" + msg);
        }
        handler.publish(record);
    }

    private boolean shouldRotate(LogRecord record) {
        if (endDate <= record.getMillis() || !logFileExits()) {
            return true;
        }
        return false;
    }

    @Override
    public void setFormatter(Formatter newFormatter) {
        super.setFormatter(newFormatter);
        if (handler != null) { handler.setFormatter(newFormatter); }
    }

    private boolean logFileExits() {
        try {
            SimpleDateFormat format = dateFormatThreadLocal.get();
            String fileName = pattern.replace("%d", format.format(new Date()));
            // When file count is not 1, the first log file name will end with ".0"
            if (count != 1) {
                fileName += ".0";
            }
            File logFile = new File(fileName);
            return logFile.exists();
        } catch (Throwable e) {

        }
        return false;
    }

    private void rotateDate() {
        this.startDate = System.currentTimeMillis();
        if (handler != null) {
            handler.close();
        }
        SimpleDateFormat format = dateFormatThreadLocal.get();
        String newPattern = pattern.replace("%d", format.format(new Date()));
        // Get current date.
        Calendar next = Calendar.getInstance();
        // Begin of next date.
        next.set(Calendar.HOUR_OF_DAY, 0);
        next.set(Calendar.MINUTE, 0);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        next.add(Calendar.DATE, 1);
        this.endDate = next.getTimeInMillis();

        try {
            this.handler = new FileHandler(newPattern, limit, count, append);
            if (initialized) {
                handler.setEncoding(this.getEncoding());
                handler.setErrorManager(this.getErrorManager());
                handler.setFilter(this.getFilter());
                handler.setFormatter(this.getFormatter());
                handler.setLevel(this.getLevel());
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
