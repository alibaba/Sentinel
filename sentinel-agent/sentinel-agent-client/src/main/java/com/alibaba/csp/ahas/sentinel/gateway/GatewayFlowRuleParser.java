package com.alibaba.csp.ahas.sentinel.gateway;

import java.util.Set;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.acm.RulesAcmFormat;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.taobao.csp.third.com.alibaba.fastjson.JSON;
import com.taobao.csp.third.com.alibaba.fastjson.TypeReference;

/**
 * @author Eric Zhao
 * @since 1.3.0
 */
public class GatewayFlowRuleParser implements Converter<String, Set<GatewayFlowRule>> {

    @Override
    public Set<GatewayFlowRule> convert(String source) {
        if (StringUtil.isBlank(source)) {
            return null;
        }
        String data = new RulesAcmFormat(source).getData();
        RecordLog.info("[GatewayFlowRuleParser] Get data: " + data);
        return JSON.parseObject(data, new TypeReference<Set<GatewayFlowRule>>() {});
    }
}
