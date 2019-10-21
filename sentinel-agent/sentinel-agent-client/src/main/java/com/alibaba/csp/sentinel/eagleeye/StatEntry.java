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

import java.util.Arrays;
import java.util.List;

public final class StatEntry {

    private final StatLogger statLogger;

    private final String[] keys;
    private transient int hash;

    public StatEntry(StatLogger statLogger, String key) {
        this.statLogger = statLogger;
        this.keys = new String[] {key};
    }

    public StatEntry(StatLogger statLogger, String key1, String key2) {
        this.statLogger = statLogger;
        this.keys = new String[] {key1, key2};
    }

    public StatEntry(StatLogger statLogger, String key1, String key2, String key3) {
        this.statLogger = statLogger;
        this.keys = new String[] {key1, key2, key3};
    }

    public StatEntry(StatLogger statLogger, String key1, String key2, String key3, String key4) {
        this.statLogger = statLogger;
        this.keys = new String[] {key1, key2, key3, key4};
    }

    public StatEntry(StatLogger statLogger, String key1, String key2, String key3, String key4, String key5) {
        this.statLogger = statLogger;
        this.keys = new String[] {key1, key2, key3, key4, key5};
    }

    public StatEntry(StatLogger statLogger, String key1, String key2, String key3, String key4, String key5,
                     String key6) {
        this.statLogger = statLogger;
        this.keys = new String[] {key1, key2, key3, key4, key5, key6};
    }

    public StatEntry(StatLogger statLogger, String key1, String key2, String key3, String key4, String key5,
                     String key6, String key7) {
        this.statLogger = statLogger;
        this.keys = new String[] {key1, key2, key3, key4, key5, key6, key7};
    }

    public StatEntry(StatLogger statLogger, String key1, String key2, String key3, String key4, String key5,
                     String key6, String key7, String key8) {
        this.statLogger = statLogger;
        this.keys = new String[] {key1, key2, key3, key4, key5, key6, key7, key8};
    }

    public StatEntry(StatLogger statLogger, String key1, String... moreKeys) {
        String[] keys = new String[1 + moreKeys.length];
        keys[0] = key1;
        for (int i = 0; i < moreKeys.length; ++i) {
            keys[i + 1] = moreKeys[i];
        }
        this.statLogger = statLogger;
        this.keys = keys;
    }

    public StatEntry(StatLogger statLogger, List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            throw new IllegalArgumentException("keys empty or null: " + keys);
        }
        this.statLogger = statLogger;
        this.keys = keys.toArray(new String[keys.size()]);
    }

    public StatEntry(StatLogger statLogger, String[] keys) {
        if (keys == null || keys.length == 0) {
            throw new IllegalArgumentException("keys empty or null");
        }
        this.statLogger = statLogger;
        this.keys = Arrays.copyOf(keys, keys.length);
    }

    public String[] getKeys() {
        return keys;
    }

    void appendTo(StringBuilder appender, char delimiter) {
        final int len = keys.length;
        if (len > 0) {
            appender.append(keys[0]);
            for (int i = 1; i < len; ++i) {
                appender.append(delimiter).append(keys[i]);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(64);
        sb.append("StatKeys [");
        appendTo(sb, ',');
        sb.append("]");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            int result = 1;
            result = 31 * result + Arrays.hashCode(keys);
            hash = result;
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StatEntry other = (StatEntry)obj;
        if (hash != 0 && other.hash != 0 && hash != other.hash) {
            return false;
        }
        if (!Arrays.equals(keys, other.keys)) {
            return false;
        }

        return true;
    }

    StatEntryFunc getFunc(final StatEntryFuncFactory factory) {
        return this.statLogger.getRollingData().getStatEntryFunc(this, factory);
    }

    public void count() {
        count(1);
    }

    public void count(long count) {
        getFunc(StatEntryFuncFactory.COUNT_SUM).count(count);
    }

    public void countAndSum(long valueToSum) {
        countAndSum(1, valueToSum);
    }

    public void countAndSum(long count, long valueToSum) {
        getFunc(StatEntryFuncFactory.COUNT_SUM).countAndSum(count, valueToSum);
    }

    public void minMax(long candidate) {
        minMax(candidate, null);
    }

    public void minMax(long candidate, String ref) {
        getFunc(StatEntryFuncFactory.MIN_MAX).minMax(candidate, ref);
    }
}
