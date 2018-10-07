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

import com.alibaba.csp.sentinel.slots.block.flow.param.RollingParamEvent;
import com.alibaba.csp.sentinel.slots.statistic.base.LeapArray;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;
import com.alibaba.csp.sentinel.slots.statistic.data.ParamMapBucket;

/**
 * The fundamental data structure for frequent parameters statistics in a time window.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class HotParameterLeapArray extends LeapArray<ParamMapBucket> {

    private int intervalInSec;

    public HotParameterLeapArray(int windowLengthInMs, int intervalInSec) {
        super(windowLengthInMs, intervalInSec);
        this.intervalInSec = intervalInSec;
    }

    public int getIntervalInSec() {
        return intervalInSec;
    }

    @Override
    public ParamMapBucket newEmptyBucket() {
        return new ParamMapBucket();
    }

    @Override
    protected WindowWrap<ParamMapBucket> resetWindowTo(WindowWrap<ParamMapBucket> w, long startTime) {
        w.resetTo(startTime);
        w.value().reset();
        return w;
    }

    public void addValue(RollingParamEvent event, int count, Object value) {
        currentWindow().value().add(event, count, value);
    }

    public Map<Object, Double> getTopValues(RollingParamEvent event, int number) {
        currentWindow();
        List<ParamMapBucket> buckets = this.values();

        Map<Object, Integer> result = new HashMap<Object, Integer>();

        for (ParamMapBucket b : buckets) {
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
            doubleResult.put(x.getKey(), ((double)x.getValue()) / getIntervalInSec());
        }

        return doubleResult;
    }

    public long getRollingSum(RollingParamEvent event, Object value) {
        currentWindow();

        long sum = 0;

        List<ParamMapBucket> buckets = this.values();
        for (ParamMapBucket b : buckets) {
            sum += b.get(event, value);
        }

        return sum;
    }

    public double getRollingAvg(RollingParamEvent event, Object value) {
        return ((double) getRollingSum(event, value)) / getIntervalInSec();
    }
}
