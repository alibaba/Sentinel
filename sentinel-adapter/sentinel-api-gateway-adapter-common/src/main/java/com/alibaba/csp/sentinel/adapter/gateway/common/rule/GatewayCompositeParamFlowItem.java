/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.gateway.common.rule;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;


import java.util.ArrayList;
import java.util.List;

/**
 * @author wavesZh
 */
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
