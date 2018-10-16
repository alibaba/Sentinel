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
package com.taobao.csp.sentinel.dashboard.util;

import java.util.List;

import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Eric Zhao
 * @since 0.2.1
 */
public final class RuleUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleUtils.class);

    public static List<FlowRule> parseFlowRule(String body) {
        try {
            return JSON.parseArray(body, FlowRule.class);
        } catch (Exception e) {
            LOGGER.error("parser FlowRule error: ", e);
            return null;
        }
    }

    public static List<DegradeRule> parseDegradeRule(String body) {
        try {
            return JSON.parseArray(body, DegradeRule.class);
        } catch (Exception e) {
            LOGGER.error("parser DegradeRule error: ", e);
            return null;
        }
    }

    public static List<AuthorityRule> parseAuthorityRule(String body) {
        if (StringUtil.isBlank(body)) {
            return null;
        }
        try {
            return JSON.parseArray(body, AuthorityRule.class);
        } catch (Exception e) {
            LOGGER.error("Error when parsing authority rules", e);
            return null;
        }
    }

    /**
     * Parse parameter flow rules.
     *
     * @param body raw string content
     * @return parsed rule list; null if error occurs or empty content
     */
    public static List<ParamFlowRule> parseParamFlowRule(String body) {
        if (StringUtil.isBlank(body)) {
            return null;
        }
        try {
            return JSON.parseArray(body, ParamFlowRule.class);
        } catch (Exception e) {
            LOGGER.error("Error when parsing parameter flow rules", e);
            return null;
        }
    }

    public static List<SystemRule> parseSystemRule(String body) {
        try {
            return JSON.parseArray(body, SystemRule.class);
        } catch (Exception e) {
            LOGGER.info("parser SystemRule error: ", e);
            return null;
        }
    }

    private RuleUtils() {}
}
