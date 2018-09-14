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
package com.alibaba.csp.sentinel.slots.statistic.metric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.alibaba.csp.sentinel.node.IntervalProperty;
import com.alibaba.csp.sentinel.slots.hotspot.RollingParamEvent;
import com.alibaba.csp.sentinel.slots.statistic.base.LeapArray;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;
import com.alibaba.csp.sentinel.slots.statistic.data.LruMapBucket;

/**
 * The fundamental data structure for hot parameters statistics in a time window.
 *
 * @author Eric Zhao
 */
public class HotParameterLeapArray extends LeapArray<LruMapBucket> {

    public HotParameterLeapArray(int windowLength, int intervalInSec) {
        super(windowLength, intervalInSec);
    }

    @Override
    public LruMapBucket newEmptyBucket() {
        return new LruMapBucket();
    }

    @Override
    protected WindowWrap<LruMapBucket> resetWindowTo(WindowWrap<LruMapBucket> w, long startTime) {
        w.resetTo(startTime);
        w.value().reset();
        return w;
    }

    public void addValue(RollingParamEvent event, Object value) {
        currentWindow().value().add(event, value);
    }

    public Map<Object, Double> getTopValues(RollingParamEvent event, int number) {
        currentWindow();
        List<LruMapBucket> buckets = this.values();

        Map<Object, Integer> result = new HashMap<Object, Integer>();

        for (LruMapBucket b : buckets) {
            Set<Object> subSet = b.ascendingKeySet(event);
            for (Object o : subSet) {
                Integer count = result.get(o);
                if (count == null) {
                    count = b.get(event, o);

                } else {
                    count += b.get(event, o);
                }
                result.put(o, count);
            }
        }

        // After merge, get the top set one.
        Set<Entry<Object, Integer>> set = result.entrySet();
        List<Entry<Object, Integer>> list = new ArrayList<Entry<Object, Integer>>(set);
        Collections.sort(list, new Comparator<Entry<Object, Integer>>() {
            @Override
            public int compare(Entry<Object, Integer> a,
                               Entry<Object, Integer> b) {
                return (b.getValue() == null ? 0 : b.getValue()) - (a.getValue() == null ? 0 : a.getValue());
            }
        });

        Map<Object, Double> doubleResult = new HashMap<Object, Double>();

        int size = list.size() > number ? number : list.size();
        for (int i = 0; i < size; i++) {
            Map.Entry<Object, Integer> x = list.get(i);
            if (x.getValue() == 0) {
                break;
            }
            doubleResult.put(x.getKey(), ((double)x.getValue()) / IntervalProperty.INTERVAL);
        }

        return doubleResult;
    }

    public long getCount(RollingParamEvent event, Object value) {
        currentWindow();

        long sum = 0;

        List<LruMapBucket> buckets = this.values();
        for (LruMapBucket b : buckets) {
            sum += b.get(event, value);
        }

        return sum;
    }
}
