package com.alibaba.csp.sentinel.adapter.gateway.common.rule;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;


import java.util.ArrayList;
import java.util.List;


public class GatewayCompositeParamFlowItem extends GatewayParamFlowItem {

    private final List<GatewayParamFlowItem> paramItems = new ArrayList<>(10);

    public GatewayCompositeParamFlowItem() {
        super.setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_COMPOSITE_PARAM);
    }

    public GatewayCompositeParamFlowItem addParamItem(GatewayParamFlowItem item) {
        if (item instanceof GatewayCompositeParamFlowItem) {
            // just log and skip or throw exception?
            throw new IllegalArgumentException("GatewayCompositeParamFlowItem can not be added again");
        }
        paramItems.add(item);
        return this;
    }

    public List<GatewayParamFlowItem> getParamItems() {
        return paramItems;
    }

    @Override
    public GatewayParamFlowItem setParseStrategy(int parseStrategy) {
        return this;
    }

    @Override
    public String toString() {
        return "GatewayCompositeParamFlowItem{" +
                "paramItems=" + paramItems +
                '}';
    }
}
