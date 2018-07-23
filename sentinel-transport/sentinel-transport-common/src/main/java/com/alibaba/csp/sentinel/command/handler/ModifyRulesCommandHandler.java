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
package com.alibaba.csp.sentinel.command.handler;

import java.net.URLDecoder;
import java.util.List;

import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.command.annotation.CommandMapping;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.datasource.DataSource;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.fastjson.JSONArray;

/**
 * @author jialiang.linjl
 */
@CommandMapping(name = "setRules")
public class ModifyRulesCommandHandler implements CommandHandler<String> {

    static DataSource<?, List<FlowRule>> flowDataSource = null;
    static DataSource<?, List<AuthorityRule>> authorityDataSource = null;
    static DataSource<?, List<DegradeRule>> degradeDataSource = null;
    static DataSource<?, List<SystemRule>> systemSource = null;

    public static synchronized void registerFlowDataSource(DataSource<?, List<FlowRule>> datasource) {
        flowDataSource = datasource;
    }

    public static synchronized void registerAuthorityDataSource(DataSource<?, List<AuthorityRule>> dataSource) {
        authorityDataSource = dataSource;
    }

    public static synchronized void registerDegradeDataSource(DataSource<?, List<DegradeRule>> dataSource) {
        degradeDataSource = dataSource;
    }

    public static synchronized void registerSystemDataSource(DataSource<?, List<SystemRule>> dataSource) {
        systemSource = dataSource;
    }

    @Override
    public CommandResponse<String> handle(CommandRequest request) {
        String type = request.getParam("type");
        // rule data in get parameter
        String data = request.getParam("data");
        if (StringUtil.isNotEmpty(data)) {
            try {
                data = URLDecoder.decode(data, "utf-8");
            } catch (Exception e) {
                RecordLog.info("decode rule data error", e);
                return CommandResponse.ofFailure(e, "decode rule data error");
            }
        }

        RecordLog.info("receive rule change:" + type);
        RecordLog.info(data);

        String result = "success";

        if ("flow".equalsIgnoreCase(type)) {
            List<FlowRule> flowRules = JSONArray.parseArray(data, FlowRule.class);
            FlowRuleManager.loadRules(flowRules);
            if (flowDataSource != null) {
                try {
                    flowDataSource.writeDataSource(flowRules);
                } catch (Exception e) {
                    result = "partial success";
                    RecordLog.info(e.getMessage(), e);
                }
            }
            return CommandResponse.ofSuccess(result);
        } else if ("authority".equalsIgnoreCase(type)) {
            List<AuthorityRule> rules = JSONArray.parseArray(data, AuthorityRule.class);
            AuthorityRuleManager.loadRules(rules);
            if (authorityDataSource != null) {
                try {
                    authorityDataSource.writeDataSource(rules);
                } catch (Exception e) {
                    result = "partial success";
                    RecordLog.info(e.getMessage(), e);
                }
            }
            return CommandResponse.ofSuccess(result);
        } else if ("degrade".equalsIgnoreCase(type)) {
            List<DegradeRule> rules = JSONArray.parseArray(data, DegradeRule.class);
            DegradeRuleManager.loadRules(rules);
            if (degradeDataSource != null) {
                try {
                    degradeDataSource.writeDataSource(rules);
                } catch (Exception e) {
                    result = "partial success";
                    RecordLog.info(e.getMessage(), e);
                }
            }
            return CommandResponse.ofSuccess(result);
        } else if ("system".equalsIgnoreCase(type)) {
            List<SystemRule> rules = JSONArray.parseArray(data, SystemRule.class);
            SystemRuleManager.loadRules(rules);
            if (systemSource != null) {
                try {
                    systemSource.writeDataSource(rules);
                } catch (Exception e) {
                    result = "partial success";
                    RecordLog.info(e.getMessage(), e);
                }
            }
            return CommandResponse.ofSuccess(result);
        }
        return CommandResponse.ofFailure(new IllegalArgumentException("invalid type"));

    }

}
