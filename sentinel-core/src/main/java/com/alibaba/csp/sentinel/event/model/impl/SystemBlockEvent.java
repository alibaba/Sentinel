/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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


package com.alibaba.csp.sentinel.event.model.impl;


import com.alibaba.csp.sentinel.slots.block.AbstractRule;

/**
 * Event published when flow blocked.
 *
 * @author Daydreamer-ia
 */
public class SystemBlockEvent extends SentinelRuleEvent {

    /**
     * sys metric.
     */
    private String sysMetricKey;

    public SystemBlockEvent(AbstractRule rule, String sysMetricKey) {
        super(rule);
        this.sysMetricKey = sysMetricKey;
    }

    public String getSysMetricKey() {
        return sysMetricKey;
    }

    public void setSysMetricKey(String sysMetricKey) {
        this.sysMetricKey = sysMetricKey;
    }
}