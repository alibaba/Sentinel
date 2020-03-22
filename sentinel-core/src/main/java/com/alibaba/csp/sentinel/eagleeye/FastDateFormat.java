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
package com.alibaba.csp.sentinel.eagleeye;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

class FastDateFormat {

    private final SimpleDateFormat fmt = createSimpleDateFormat();

    private char[] buffer = new char[23];

    private long lastSecond = -1;
    private long lastMillis = -1;

    public String format(long timestamp) {
        formatToBuffer(timestamp);
        return new String(buffer, 0, 23);
    }

    public String format(Date date) {
        return format(date.getTime());
    }

    public void formatAndAppendTo(long timestamp, StringBuilder appender) {
        formatToBuffer(timestamp);
        appender.append(buffer, 0, 23);
    }

    private void formatToBuffer(long timestamp) {
        if (timestamp == lastMillis) {
            return;
        }
        long diff = timestamp - lastSecond;
        if (diff >= 0 && diff < 1000) {
            int ms = (int)(timestamp % 1000);
            buffer[22] = (char)(ms % 10 + '0');
            ms /= 10;
            buffer[21] = (char)(ms % 10 + '0');
            buffer[20] = (char)(ms / 10 + '0');
            lastMillis = timestamp;
        } else {
            String result = fmt.format(new Date(timestamp));
            result.getChars(0, result.length(), buffer, 0);
            lastSecond = timestamp / 1000 * 1000;
            lastMillis = timestamp;
        }
    }

    String formatWithoutMs(long timestamp) {
        long diff = timestamp - lastSecond;
        if (diff < 0 || diff >= 1000) {
            String result = fmt.format(new Date(timestamp));
            result.getChars(0, result.length(), buffer, 0);
            lastSecond = timestamp / 1000 * 1000;
            lastMillis = timestamp;
        }
        return new String(buffer, 0, 19);
    }

    private SimpleDateFormat createSimpleDateFormat() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        fmt.setTimeZone(TimeZone.getDefault());
        return fmt;
    }
}
