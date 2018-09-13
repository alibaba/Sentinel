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
package com.alibaba.csp.sentinel.slots.hotspot;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;

/**
 * @author jialiang.linjl
 * @since 0.2.0
 */
public class HotParamRule extends AbstractRule {

    public final static int INTEGER = 0;
    public final static int LONG = 1;
    public final static int BYTE = 2;
    public final static int DOUBLE = 3;
    public final static int FLOAT = 4;
    public final static int SHORT = 5;
    public final static int BOOLEAN = 6;
    public final static int STRING = 7;

    Integer idx;

    // parameter index
    Integer paramIdx;

    // 阀值
    double count;

    Set<Object> keys = new ConcurrentSkipListSet<Object>();

    Map<Object, Integer> hotItems = new ConcurrentHashMap<Object, Integer>();

    Integer machineCount;

    String id;

    // /0线程数;1 qps
    Integer blockGrade = 1;

    Integer blockCount = 0;

    // 0 为单个，1为 热点的总量
    Integer blockStrategy = 0;

    public Integer getIdx() {
        return idx;
    }

    public void setIdx(Integer idx) {
        this.idx = idx;
    }

    final Map<Object, Integer> clusterExclusionItems = new ConcurrentHashMap<Object, Integer>();

    public Integer getBlockStrategy() {
        return blockStrategy;
    }

    public void setBlockStrategy(Integer blockStrategy) {
        this.blockStrategy = blockStrategy;
    }

    public Integer getBlockGrade() {
        return blockGrade;
    }

    public void setBlockGrade(Integer blockGrade) {
        this.blockGrade = blockGrade;
    }

    public Integer getBlockCount() {
        return blockCount;
    }

    public void setBlockCount(Integer blockCount) {
        this.blockCount = blockCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setHotItems(Map<Object, Integer> hotItems) {
        this.hotItems = hotItems;
    }

    public Integer getMachineCount() {
        return machineCount;
    }

    public void setMachineCount(Integer machineCount) {
        this.machineCount = machineCount;
    }

    public Set<Object> getKeys() {
        return keys;
    }

    public Map<Object, Integer> getHotItems() {
        return hotItems;
    }

    public Integer getParamIdx() {
        return paramIdx;
    }

    public void setParamIdx(Integer paramIdx) {
        this.paramIdx = paramIdx;
    }

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }

    private boolean passSingleValueCheck(ClusterNode node, Object value) {
        // and then check its real value
        Set<Object> exclusionItems = this.getKeys();
        if (this.getBlockGrade() == RuleConstant.FLOW_GRADE_QPS) {

            int curCount = (int)node.getReqPassParamAvg(paramIdx, value);

            if (exclusionItems.contains(value)) {
                int qps = getHotItems().get(value);
                if (++curCount > qps) {
                    return false;
                }
            } else if (++curCount > count) {

                //要考虑小数点,否则就会抖
                if ((curCount - count) < 1 && (curCount - count) > 0) {
                    return true;
                }
                return false;
            }
        } else {
            long threadCount = node.getThreadCount(paramIdx, value);
            if (exclusionItems.contains(value)) {
                int thread = getHotItems().get(value);
                if (++threadCount > thread) {
                    return false;
                }
            }
            if (++threadCount > this.blockCount) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean passCheck(Context context, DefaultNode node, int count, Object... args) {

        if (args == null) {
            return true;
        }

        // first get the index of the parameters
        if (args.length <= this.getParamIdx()) {
            return true;
        }

        Object value = args[paramIdx];

        return passLocalCheck(node.getClusterNode(), value);

    }

    private boolean passLocalCheck(ClusterNode node, Object value) {
        // 单个的非cluster的逻辑
        try {
            if (Collection.class.isAssignableFrom(value.getClass())) {
                for (Object param : ((Collection)value)) {
                    if (!passSingleValueCheck(node, param)) {
                        return false;
                    }
                }
            } else if (value.getClass().isArray()) {
                int length = Array.getLength(value);
                for (int i = 0; i < length; i++) {
                    Object param = Array.get(value, i);
                    if (!passSingleValueCheck(node, param)) {
                        return false;
                    }
                }
            } else {
                return passSingleValueCheck(node, value);
            }
        } catch (Throwable e) {
            RecordLog.info(e.getMessage(), e);
        }

        return true;
    }
}
