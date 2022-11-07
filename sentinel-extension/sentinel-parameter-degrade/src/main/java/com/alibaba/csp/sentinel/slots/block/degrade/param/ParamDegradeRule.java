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
package com.alibaba.csp.sentinel.slots.block.degrade.param;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Rules for "hot-spot" frequent parameter degrade control.
 *
 * @author furao
 * @since 1.9.0
 */
public class ParamDegradeRule extends DegradeRule {

    public ParamDegradeRule() {
    }

    public ParamDegradeRule(String resourceName) {
        setResource(resourceName);
    }

    /**
     * Parameter index.
     */
    private int paramIdx;

    /**
     * Original exclusion items of parameters.
     */
    private List<ParamDegradeItem> paramDegradeItemList = new ArrayList<>();

    public Integer getParamIdx() {
        return paramIdx;
    }

    public ParamDegradeRule setParamIdx(Integer paramIdx) {
        this.paramIdx = paramIdx;
        return this;
    }

    public List<ParamDegradeItem> getParamDegradeItemList() {
        return paramDegradeItemList;
    }

    public ParamDegradeRule setParamDegradeItemList(List<ParamDegradeItem> paramDegradeItemList) {
        this.paramDegradeItemList = paramDegradeItemList;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ParamDegradeRule that = (ParamDegradeRule) o;

        if (getGrade() != that.getGrade()) {
            return false;
        }
        if (Double.compare(getCount(), that.getCount()) != 0) {
            return false;
        }
        if (getTimeWindow() != that.getTimeWindow()) {
            return false;
        }
        if (getMinRequestAmount() != that.getMinRequestAmount()) {
            return false;
        }
        if (Double.compare(getSlowRatioThreshold(), that.getSlowRatioThreshold()) != 0) {
            return false;
        }
        if (getStatIntervalMs() != that.getStatIntervalMs()) {
            return false;
        }
        if (!Objects.equals(getParamIdx(), that.getParamIdx())) {
            return false;
        }

        if (!Objects.equals(this.getParamDegradeItemList().size(), that.getParamDegradeItemList().size())) {
            return false;
        }

        for (int index = 0; index < this.getParamDegradeItemList().size(); index++) {
            if (!Objects.equals(this.getParamDegradeItemList().get(index), that.getParamDegradeItemList().get(index))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getTimeWindow(), getGrade(), getMinRequestAmount(),
                getSlowRatioThreshold(), getStatIntervalMs(), getCount(), getParamIdx(), getParamDegradeItemList());
    }

    @Override
    public String toString() {
        return "ParamDegradeRule{" +
                "grade=" + getGrade() +
                ", paramIdx=" + paramIdx +
                ", timeWindow=" + getTimeWindow() +
                ", minRequestAmount=" + getMinRequestAmount() +
                ", slowRatioThreshold=" + getSlowRatioThreshold() +
                ", statIntervalMs=" + getStatIntervalMs() +
                ", paramDegradeItemList=" + paramDegradeItemList +
                '}';
    }

    public ParamDegradeRule cloneWithoutItem() {
        ParamDegradeRule paramDegradeRule = new ParamDegradeRule();
        paramDegradeRule.setResource(this.getResource());
        paramDegradeRule.setLimitApp(this.getLimitApp());
        paramDegradeRule.setGrade(this.getGrade());
        paramDegradeRule.setCount(this.getCount());
        paramDegradeRule.setTimeWindow(this.getTimeWindow());
        paramDegradeRule.setMinRequestAmount(this.getMinRequestAmount());
        paramDegradeRule.setSlowRatioThreshold(this.getSlowRatioThreshold());
        paramDegradeRule.setStatIntervalMs(this.getStatIntervalMs());

        return paramDegradeRule;
    }
}
