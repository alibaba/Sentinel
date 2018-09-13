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
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * @author jialiang.linjl
 * @since 0.2.0
 */
public class HotParamRuleManager {

    private static volatile Map<String, List<HotParamRule>> flowRules
        = new ConcurrentHashMap<String, List<HotParamRule>>();

    private static volatile Map<String, HotParamRule> hotParamRule = new ConcurrentHashMap<String, HotParamRule>();

    static {
        //if (appName != null) {
        //    RuleProperyListener listener = new RuleProperyListener();
        //    HotParamRulesProperty.getInstance(appName).registerListener(appName, listener);
        //}
    }

    public static boolean hasConfig(String resource) {
        return flowRules.containsKey(resource);
    }

    public static Map<String, List<HotParamRule>> getFlowRules() {
        return flowRules;
    }

    public static void setFlowRules(Map<String, List<HotParamRule>> flowRules) {
        HotParamRuleManager.flowRules = flowRules;
    }

    public static Map<String, HotParamRule> getHotParamRule() {
        return hotParamRule;
    }

    public static void setHotParamRule(Map<String, HotParamRule> hotParamRule) {
        HotParamRuleManager.hotParamRule = hotParamRule;
    }

    private static Object parseValue(String value, String classType) {
        /* handle primitive type */

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
            return array.length > 0 ? array[1] : null;
        }

        return value;

    }

    public static void fillHotItems(JSONObject rule) {

        Integer machineCount = rule.getInteger("machineCount") == null ? 0 : rule.getInteger("machineCount");
        if (machineCount <= 0) {
            RecordLog.info("invalid machine count for hot item");
            return;
        }

        String ruleId = rule.getString("ruleId");
        if (StringUtil.isEmpty(ruleId)) {
            RecordLog.info("invalid  rule id for hot item rule");
            return;
        }

        JSONArray items = rule.getJSONArray("items");

        HotParamRule existedRule = hotParamRule.get(ruleId);
        if (existedRule == null) {
            return;
        }

        Map<Object, Integer> hotItems = existedRule.getHotItems();

        int itemIdx = 0;
        for (itemIdx = 0; itemIdx < items.size(); itemIdx++) {
            JSONObject jsonItem = items.getJSONObject(itemIdx);
            HotItem hotItem = new HotItem();

            String jsonObject = jsonItem.getString("object");
            String classType = jsonItem.getString("classType");
            Integer count = jsonItem.getInteger("count");
            Object object = parseValue(jsonObject, classType);

            hotItem.setCount(count);
            hotItem.setObject(object);
            hotItems.put(hotItem.getObject(), hotItem.getCount());

            Integer thredshold = hotItem.getCount();
            if (thredshold == null) {
                continue;
            }

            double qps = thredshold / machineCount * 1.0;

            double avgCount = thredshold % machineCount * 1.0;

            double value = 1 - avgCount * 1.0 / machineCount;

            // TODO: Replace with `ThreadLocalRandom` in next version (requires JDK 1.7 or later).
            Random random = new Random();
            double percentage = random.nextDouble();

            if (percentage < value) {
                hotItems.put(object, (int)qps);
            } else {
                hotItems.put(object, (int)(qps + 1));
            }
            existedRule.getKeys().add(object);

        }
    }

    public static void fillHotItems(String conf, HotParamRule rule) {

        if (StringUtil.isEmpty(conf)) {
            return;
        }

        JSONObject jsonContent = JSONObject.parseObject(conf);

        JSONObject hotItemsObject = jsonContent.getJSONObject("data");
        Integer machineCount = hotItemsObject.getInteger("machineCount") == null ? 0
            : hotItemsObject.getInteger("machineCount");
        if (machineCount <= 0) {
            RecordLog.info("invalid object");
            return;
        }

        JSONArray items = hotItemsObject.getJSONArray("items");

        Map<Object, Integer> hotItems = rule.getHotItems();
        int itemIdx = 0;
        for (itemIdx = 0; itemIdx < items.size(); itemIdx++) {
            JSONObject jsonItem = items.getJSONObject(itemIdx);
            HotItem hotItem = new HotItem();

            String jsonObject = jsonItem.getString("object");
            String classType = jsonItem.getString("classType");
            Integer count = jsonItem.getInteger("count");
            Object object = parseValue(jsonObject, classType);

            hotItem.setCount(count);
            hotItem.setObject(object);
            hotItems.put(hotItem.getObject(), hotItem.getCount());

            Integer thredshold = hotItem.getCount();
            if (thredshold == null) {
                continue;
            }

            double qps = thredshold / machineCount * 1.0;

            double avgCount = thredshold % machineCount * 1.0;

            double value = 1 - avgCount * 1.0 / machineCount;

            Random random = new Random();
            double percentage = random.nextDouble();

            if (percentage < value) {
                rule.getHotItems().put(object, (int)qps);
            } else {
                rule.getHotItems().put(object, (int)(qps + 1));
            }
            rule.getKeys().add(object);

        }
    }

    public static List<Integer> loadIdentityIndex(String identity) {
        Set<Integer> indexes = new HashSet<Integer>();

        List<HotParamRule> rules = flowRules.get(identity);
        if (rules == null || rules.isEmpty()) {
            return null;
        }

        for (HotParamRule rule : rules) {
            indexes.add(rule.getParamIdx());
        }

        return new ArrayList<Integer>(indexes);
    }

    public static boolean hasRules(String identity) {
        List<HotParamRule> rules = flowRules.get(identity);
        if (rules == null || rules.isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * @param resourceWraper
     * @param context
     * @param node
     * @param args
     */
    public static void checkFlow(ResourceWrapper resourceWraper, Context context, DefaultNode node, int count,
                                 Object... args) throws BlockException {

        // initialize its cluster node parameter node
        if (hasRules(resourceWraper.getName())) {

            List<HotParamRule> rules = flowRules.get(resourceWraper.getName());

            if (rules != null) {
                for (HotParamRule rule : rules) {
                    // first make sure that this counter exists
                    node.getClusterNode().checkCounterExits(rule.getParamIdx());

                    if (!rule.passCheck(context, node, count, false, args)) {
                        String message = "";
                        // first get the index of the parameters
                        if (args.length > rule.getParamIdx()) {
                            Object value = args[rule.getParamIdx()];
                            message = String.valueOf(value);
                        }
                        // and then check its real value
                        throw new HotParamException(resourceWraper.getName(), message);
                    }

                }
            }

        }
    }

    static class RuleProperyListener implements PropertyListener<Map<String, List<HotParamRule>>> {

        @Override
        public void configSourceLoaded(Map<String, List<HotParamRule>> rules) {
            flowRules.putAll(rules);

            Set<Entry<String, List<HotParamRule>>> entries = flowRules.entrySet();

            hasCluster = false;
            for (Entry<String, List<HotParamRule>> entry : entries) {
                for (HotParamRule paramRule : entry.getValue()) {
                    HotItemsProperty.getInstance(paramRule.getId()).clear();
                    hotParamRule.put(paramRule.getId(), paramRule);
                    HotItemsProperty.getInstance(paramRule.getId()).registerListener(paramRule.getId(),
                        new HotItemProperyListener());
                    if (paramRule.isCluster) {
                        hasCluster = true;
                    }
                }
            }
            RecordLog.info("init hot param config: " + JSONArray.toJSONString(flowRules));

            RecordLog.info("going to " + hasCluster);
            if (hasCluster || FlowRuleManager.hasCluster) {
                StateManager.sendStartCommand();
            } else if (!hasCluster && !FlowRuleManager.hasCluster) {
                StateManager.sendStopCommand();
            }
        }

        @Override
        public void updateSource(Map<String, List<HotParamRule>> rules) {
            // before change do some laundry
            // clean item Listeners first
            Set<Entry<String, List<HotParamRule>>> entries = flowRules.entrySet();

            for (Entry<String, List<HotParamRule>> entry : entries) {
                for (HotParamRule paramRule : entry.getValue()) {
                    HotItemsProperty.getInstance(paramRule.getId()).clear();
                }
            }
            hotParamRule.clear();
            flowRules.clear();
            flowRules.putAll(rules);

            entries = flowRules.entrySet();

            hasCluster = false;
            for (Entry<String, List<HotParamRule>> entry : entries) {
                for (HotParamRule paramRule : entry.getValue()) {

                    hotParamRule.put(paramRule.getId(), paramRule);

                    HotItemsProperty.getInstance(paramRule.getId()).registerListener(paramRule.getId(),
                        new HotItemProperyListener());

                    if (paramRule.isCluster) {
                        hasCluster = true;
                    }
                }
            }

            RecordLog.info("receive hot param change: " + JSONArray.toJSONString(flowRules));

            RecordLog.info("going to start client: " + hasCluster + " FlowRuleManager:" + FlowRuleManager.hasCluster);

            if (hasCluster || FlowRuleManager.hasCluster) {
                StateManager.sendStartCommand();
            } else if (!hasCluster && !FlowRuleManager.hasCluster) {
                StateManager.sendStopCommand();
            }
        }

        @Override
        public void clear(Map<String, List<HotParamRule>> rules) {
            flowRules.clear();
            RecordLog.info("clear hot param rules");
        }

    }

    static class HotItemProperyListener implements PropertyListener<JSONObject> {

        @Override
        public void configSourceLoaded(JSONObject rule) {

            fillHotItems(rule);

            RecordLog.info("current hot param values: " + JSONArray.toJSONString(flowRules));
        }

        @Override
        public void updateSource(JSONObject rules) {
            fillHotItems(rules);
            // mean while, add listeners

            RecordLog.info("current hot param values: " + JSONObject.toJSONString(flowRules));
        }

        @Override
        public void clear(JSONObject rules) {

            RecordLog.info("clear hot item rules");
        }

    }

    /**
     * @param rules
     */
    public static void initHotParamRules(List<HotParamRule> rules) {
        hotParamRule.clear();
        flowRules.clear();

        for (HotParamRule paramRule : rules) {
            String resource = paramRule.getResource();

            List<HotParamRule> paramRules = flowRules.get(resource);
            if (paramRules == null) {
                paramRules = new ArrayList<HotParamRule>();
                flowRules.put(resource, paramRules);
            }

            paramRules.add(paramRule);
            hotParamRule.put(paramRule.getId(), paramRule);
        }
    }

}

