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

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

interface StatEntryFunc {

    void appendTo(StringBuilder appender, char delimiter);

    int getStatType();

    Object[] getValues();

    void count(long count);

    void countAndSum(long count, long value);

    void arrayAdd(long... values);

    void arraySet(long... values);

    void minMax(long candidate, String ref);

    void batchAdd(long... values);

    void strArray(String... values);
}

enum StatEntryFuncFactory {
    COUNT_SUM {
        @Override
        StatEntryFunc create() {
            return new StatEntryFuncCountAndSum();
        }
    },
    MIN_MAX {
        @Override
        StatEntryFunc create() {
            return new StatEntryFuncMinMax();
        }
    };

    abstract StatEntryFunc create();
}

class StatEntryFuncCountAndSum implements StatEntryFunc {

    private LongAdder count = new LongAdder();
    private LongAdder value = new LongAdder();

    @Override
    public void appendTo(StringBuilder appender, char delimiter) {
        appender.append(count.sum()).append(delimiter).append(value.sum());
    }

    @Override
    public Object[] getValues() {
        return new Object[] {count.sum(), value.sum()};
    }

    @Override
    public int getStatType() {
        return 1;
    }

    @Override
    public void count(long count) {
        this.count.add(count);
    }

    @Override
    public void countAndSum(long count, long value) {
        this.count.add(count);
        this.value.add(value);
    }

    @Override
    public void arrayAdd(long... values) {
        throw new IllegalStateException("arrayAdd() is unavailable if countAndSum() has been called");
    }

    @Override
    public void arraySet(long... values) {
        throw new IllegalStateException("arraySet() is unavailable if countAndSum() has been called");
    }

    @Override
    public void minMax(long candidate, String ref) {
        throw new IllegalStateException("minMax() is unavailable if countAndSum() has been called");
    }

    @Override
    public void batchAdd(long... values) {
        throw new IllegalStateException("batchAdd() is unavailable if countAndSum() has been called");
    }

    @Override
    public void strArray(String... values) {
        throw new IllegalStateException("strArray() is unavailable if countAndSum() has been called");
    }
}

class StatEntryFuncMinMax implements StatEntryFunc {

    private AtomicReference<ValueRef> max = new AtomicReference<ValueRef>(new ValueRef(Long.MIN_VALUE, null));
    private AtomicReference<ValueRef> min = new AtomicReference<ValueRef>(new ValueRef(Long.MAX_VALUE, null));

    @Override
    public void appendTo(StringBuilder appender, char delimiter) {
        ValueRef lmax = max.get();
        ValueRef lmin = min.get();

        appender.append(lmax.value).append(delimiter);
        if (lmax.ref != null) {
            appender.append(lmax.ref);
        }
        appender.append(delimiter);

        appender.append(lmin.value).append(delimiter);
        if (lmin.ref != null) {
            appender.append(lmin.ref);
        }
    }

    @Override
    public Object[] getValues() {
        ValueRef lmax = max.get();
        ValueRef lmin = min.get();
        return new Object[] {lmax.value, lmax.ref, lmin.value, lmin.ref};
    }

    @Override
    public int getStatType() {
        return 4;
    }

    @Override
    public void count(long count) {
        throw new IllegalStateException("count() is unavailable if minMax() has been called");
    }

    @Override
    public void countAndSum(long count, long value) {
        throw new IllegalStateException("countAndSum() is unavailable if minMax() has been called");
    }

    @Override
    public void arrayAdd(long... values) {
        throw new IllegalStateException("arrayAdd() is unavailable if minMax() has been called");
    }

    @Override
    public void arraySet(long... values) {
        throw new IllegalStateException("arraySet() is unavailable if minMax() has been called");
    }

    @Override
    public void batchAdd(long... values) {
        throw new IllegalStateException("batchAdd() is unavailable if minMax() has been called");
    }

    @Override
    public void minMax(long candidate, String ref) {
        ValueRef lmax = max.get();
        if (lmax.value <= candidate) {
            final ValueRef cmax = new ValueRef(candidate, ref);
            while (!max.compareAndSet(lmax, cmax) && (lmax = max.get()).value <= candidate) { ; }
        }
        ValueRef lmin = min.get();
        if (lmin.value >= candidate) {
            final ValueRef cmin = new ValueRef(candidate, ref);
            while (!min.compareAndSet(lmin, cmin) && (lmin = min.get()).value >= candidate) { ; }
        }
    }

    @Override
    public void strArray(String... values) {
        throw new IllegalStateException("strArray() is unavailable if minMax() has been called");
    }

    private static final class ValueRef {
        final long value;
        final String ref;

        ValueRef(long value, String ref) {
            this.value = value;
            this.ref = ref;
        }
    }
}
