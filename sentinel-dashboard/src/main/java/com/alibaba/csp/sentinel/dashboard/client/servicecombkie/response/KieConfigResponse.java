package com.alibaba.csp.sentinel.dashboard.client.servicecombkie.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

@Data
public class KieConfigResponse {
    @JSONField(name = "total")
    private int total;

    @JSONField(name = "data")
    private List<KieConfigItem> data;
}
