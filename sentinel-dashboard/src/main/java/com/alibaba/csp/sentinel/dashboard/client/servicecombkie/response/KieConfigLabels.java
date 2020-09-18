package com.alibaba.csp.sentinel.dashboard.client.servicecombkie.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class KieConfigLabels {
    @JSONField(name = "app")
    private String app;

    @JSONField(name = "service")
    private String service;

    @JSONField(name = "version")
    private String version;

    @JSONField(name = "environment")
    private String environment;
}
