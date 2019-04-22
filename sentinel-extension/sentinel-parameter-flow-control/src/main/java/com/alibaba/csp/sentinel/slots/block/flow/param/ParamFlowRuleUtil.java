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
package com.alibaba.csp.sentinel.slots.block.flow.param;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * @author Eric Zhao
 */
public final class ParamFlowRuleUtil {

    public static boolean isValidRule(ParamFlowRule rule) {
        return rule != null && !StringUtil.isBlank(rule.getResource()) && rule.getCount() >= 0
            && rule.getGrade() >= 0 && rule.getParamIdx() != null
            && rule.getBurstCount() >= 0 && rule.getControlBehavior() >= 0
            && rule.getDurationInSec() > 0 && rule.getMaxQueueingTimeMs() >= 0
            && checkCluster(rule);
    }

    private static boolean checkCluster(/*@PreChecked*/ ParamFlowRule rule) {
        if (!rule.isClusterMode()) {
            return true;
        }
        ParamFlowClusterConfig clusterConfig = rule.getClusterConfig();
        if (clusterConfig == null) {
            return false;
        }
        if (!FlowRuleUtil.isWindowConfigValid(clusterConfig.getSampleCount(), clusterConfig.getWindowIntervalMs())) {
            return false;
        }
        return validClusterRuleId(clusterConfig.getFlowId());
    }

    public static boolean validClusterRuleId(Long id) {
        return id != null && id > 0;
    }

    public static void fillExceptionFlowItems(ParamFlowRule rule) {
        if (rule != null) {
            if (rule.getParamFlowItemList() == null) {
                rule.setParamFlowItemList(new ArrayList<ParamFlowItem>());
            }

            Map<Object, Integer> itemMap = parseHotItems(rule.getParamFlowItemList());
            rule.setParsedHotItems(itemMap);
        }
    }

    static Map<Object, Integer> parseHotItems(List<ParamFlowItem> items) {
        Map<Object, Integer> itemMap = new HashMap<Object, Integer>();
        if (items == null || items.isEmpty()) {
            return itemMap;
        }
        for (ParamFlowItem item : items) {
            // Value should not be null.
            Object value;
            try {
                value = parseItemValue(item.getObject(), item.getClassType());
            } catch (Exception ex) {
                RecordLog.warn("[ParamFlowRuleUtil] Failed to parse value for item: " + item, ex);
                continue;
            }
            if (item.getCount() == null || item.getCount() < 0 || value == null) {
                RecordLog.warn("[ParamFlowRuleUtil] Ignoring invalid exclusion parameter item: " + item);
                continue;
            }
            itemMap.put(value, item.getCount());
        }
        return itemMap;
    }

    static Object parseItemValue(String value, String classType) {
        if (value == null) {
            throw new IllegalArgumentException("Null value");
        }
        if (StringUtil.isBlank(classType)) {
            // If the class type is not provided, then treat it as string.
            return value;
        }
        // Handle primitive type.
        if (int.class.toString().equals(classType) || Integer.class.getName().equals(classType)) {
            return Integer.parseInt(value);
        } else if (boolean.class.toString().equals(classType) || Boolean.class.getName().equals(classType)) {
            return Boolean.parseBoolean(value);
        } else if (long.class.toString().equals(classType) || Long.class.getName().equals(classType)) {
            return Long.parseLong(value);
        } else if (double.class.toString().equals(classType) || Double.class.getName().equals(classType)) {
            return Double.parseDouble(value);
        } else if (float.class.toString().equals(classType) || Float.class.getName().equals(classType)) {
            return Float.parseFloat(value);
        } else if (byte.class.toString().equals(classType) || Byte.class.getName().equals(classType)) {
            return Byte.parseByte(value);
        } else if (short.class.toString().equals(classType) || Short.class.getName().equals(classType)) {
            return Short.parseShort(value);
        } else if (char.class.toString().equals(classType)) {
            char[] array = value.toCharArray();
            return array.length > 0 ? array[0] : null;
        }

        return value;
    }

    private ParamFlowRuleUtil() {}
}
