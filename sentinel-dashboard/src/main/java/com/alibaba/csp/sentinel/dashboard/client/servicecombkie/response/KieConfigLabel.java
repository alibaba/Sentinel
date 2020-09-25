package com.alibaba.csp.sentinel.dashboard.client.servicecombkie.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KieConfigLabel {
    @JSONField(name = "app")
    private String app;

    @JSONField(name = "service")
    private String service;

    @JSONField(name = "version")
    private String version;

    @JSONField(name = "environment")
    private String environment;

    @JSONField(name = "resource")
    private String resource;
}
