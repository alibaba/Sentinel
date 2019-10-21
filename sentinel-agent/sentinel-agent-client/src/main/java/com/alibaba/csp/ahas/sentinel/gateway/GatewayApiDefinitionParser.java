package com.alibaba.csp.ahas.sentinel.gateway;

import java.util.HashSet;
import java.util.Set;

import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.acm.RulesAcmFormat;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.taobao.csp.third.com.alibaba.fastjson.JSON;
import com.taobao.csp.third.com.alibaba.fastjson.JSONArray;
import com.taobao.csp.third.com.alibaba.fastjson.JSONObject;

/**
 * @author Eric Zhao
 * @since 1.3.0
 */
public class GatewayApiDefinitionParser implements Converter<String, Set<ApiDefinition>> {

    @Override
    public Set<ApiDefinition> convert(String source) {
        if (StringUtil.isBlank(source)) {
            return null;
        }
        String data = new RulesAcmFormat(source).getData();
        RecordLog.info("[GatewayApiDefinitionParser] Get data: " + data);
        Set<ApiDefinition> set = new HashSet<>();
        JSONArray array = JSON.parseArray(data);
        for (Object obj : array) {
            JSONObject o = (JSONObject)obj;
            ApiDefinition apiGroup = new ApiDefinition((o.getString("apiName")));
            Set<ApiPredicateItem> predicateItems = new HashSet<>();
            JSONArray itemArray = o.getJSONArray("predicateItems");
            if (itemArray != null) {
                predicateItems.addAll(itemArray.toJavaList(ApiPathPredicateItem.class));
            }
            apiGroup.setPredicateItems(predicateItems);
            set.add(apiGroup);
        }
        return set;
    }
}
