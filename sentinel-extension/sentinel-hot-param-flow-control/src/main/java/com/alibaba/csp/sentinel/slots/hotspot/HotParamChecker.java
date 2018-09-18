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
import java.util.Set;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;

/**
 * @author Eric Zhao
 */
final class HotParamChecker {

    static boolean passCheck(ResourceWrapper resourceWrapper, /*@Valid*/ HotParamRule rule, /*@Valid*/ int count, Object... args) {
        if (args == null) {
            return true;
        }

        int paramIdx = rule.getParamIdx();
        if (args.length <= paramIdx) {
            return true;
        }

        Object value = args[paramIdx];

        return passLocalCheck(resourceWrapper, rule, count, value);
    }

    private static HotParameterMetric getHotParameters(ResourceWrapper resourceWrapper) {
        // Should not be null.
        return HotParamSlot.getHotParamMetric(resourceWrapper);
    }

    private static boolean passLocalCheck(ResourceWrapper resourceWrapper, HotParamRule rule, int count, Object value) {
        try {
            if (Collection.class.isAssignableFrom(value.getClass())) {
                for (Object param : ((Collection)value)) {
                    if (!passSingleValueCheck(resourceWrapper, rule, count, param)) {
                        return false;
                    }
                }
            } else if (value.getClass().isArray()) {
                int length = Array.getLength(value);
                for (int i = 0; i < length; i++) {
                    Object param = Array.get(value, i);
                    if (!passSingleValueCheck(resourceWrapper, rule, count, param)) {
                        return false;
                    }
                }
            } else {
                return passSingleValueCheck(resourceWrapper, rule, count, value);
            }
        } catch (Throwable e) {
            RecordLog.info("[HotParamChecker] Unexpected error", e);
        }

        return true;
    }

    static boolean passSingleValueCheck(ResourceWrapper resourceWrapper, HotParamRule rule, int count, Object value) {
        Set<Object> exclusionItems = rule.getParsedHotItems().keySet();
        if (rule.getBlockGrade() == RuleConstant.FLOW_GRADE_QPS) {

            int curCount = (int) getHotParameters(resourceWrapper).getPassParamQps(rule.getParamIdx(), value);

            if (exclusionItems.contains(value)) {
                int qps = rule.getParsedHotItems().get(value);
                return curCount + count <= qps;
            } else if (curCount + count > rule.getCount()) {
                if ((curCount - rule.getCount()) < 1 && (curCount - rule.getCount()) > 0) {
                    return true;
                }
                return false;
            }
        } else {
            long threadCount = getHotParameters(resourceWrapper).getThreadCount(rule.getParamIdx(), value);
            if (exclusionItems.contains(value)) {
                int itemThreshold = rule.getParsedHotItems().get(value);
                return ++threadCount <= itemThreshold;
            }
            // TODO: check here (double type)
            if (++threadCount > rule.getCount()) {
                return false;
            }
        }

        return true;
    }

    private HotParamChecker() {}
}
