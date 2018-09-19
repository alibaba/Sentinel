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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;

/**
 * Rules for "hot-spot" frequent parameter flow control.
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 * @since 0.2.0
 */
public class HotParamRule extends AbstractRule {

    public HotParamRule() {}

    public HotParamRule(String resourceName) {
        setResource(resourceName);
    }

    /**
     * The threshold type of flow control (0: thread count, 1: QPS).
     */
    private int blockGrade = RuleConstant.FLOW_GRADE_QPS;

    /**
     * Parameter index.
     */
    private Integer paramIdx;

    /**
     * The threshold count.
     */
    private double count;

    /**
     * Original exception items of hot parameters.
     */
    private List<HotItem> hotItemList = new ArrayList<HotItem>();

    /**
     * Parsed exception items of hot parameters. Only for internal use.
     */
    private Map<Object, Integer> hotItems = new HashMap<Object, Integer>();

    public int getBlockGrade() {
        return blockGrade;
    }

    public HotParamRule setBlockGrade(int blockGrade) {
        this.blockGrade = blockGrade;
        return this;
    }

    public Integer getParamIdx() {
        return paramIdx;
    }

    public HotParamRule setParamIdx(Integer paramIdx) {
        this.paramIdx = paramIdx;
        return this;
    }

    public double getCount() {
        return count;
    }

    public HotParamRule setCount(double count) {
        this.count = count;
        return this;
    }

    public List<HotItem> getHotItemList() {
        return hotItemList;
    }

    public HotParamRule setHotItemList(List<HotItem> hotItemList) {
        this.hotItemList = hotItemList;
        return this;
    }

    Map<Object, Integer> getParsedHotItems() {
        return hotItems;
    }

    HotParamRule setParsedHotItems(Map<Object, Integer> hotItems) {
        this.hotItems = hotItems;
        return this;
    }

    @Override
    @Deprecated
    public boolean passCheck(Context context, DefaultNode node, int count, Object... args) {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        if (!super.equals(o)) { return false; }

        HotParamRule rule = (HotParamRule)o;

        if (blockGrade != rule.blockGrade) { return false; }
        if (Double.compare(rule.count, count) != 0) { return false; }
        if (paramIdx != null ? !paramIdx.equals(rule.paramIdx) : rule.paramIdx != null) { return false; }
        return hotItemList != null ? hotItemList.equals(rule.hotItemList) : rule.hotItemList == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        result = 31 * result + blockGrade;
        result = 31 * result + (paramIdx != null ? paramIdx.hashCode() : 0);
        temp = Double.doubleToLongBits(count);
        result = 31 * result + (int)(temp ^ (temp >>> 32));
        result = 31 * result + (hotItemList != null ? hotItemList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HotParamRule{" +
            "resource=" + getResource() +
            ", limitApp=" + getLimitApp() +
            ", blockGrade=" + blockGrade +
            ", paramIdx=" + paramIdx +
            ", count=" + count +
            ", hotItemList=" + hotItemList +
            '}';
    }
}
