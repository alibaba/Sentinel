package com.alibaba.csp.sentinel.dashboard.discovery.kie.common;

import lombok.Builder;
import lombok.Data;

import java.util.Objects;

@Data
@Builder
public class KieServerLabel {
    private String project;
    private String app;
    private String service;
    private String environment;
    private String serverVersion;

    @Override
    public boolean equals(Object o){
        if (this == o) { return true; }
        if (!(o instanceof KieServerLabel)) { return false; }
        KieServerLabel that = (KieServerLabel)o;

        return Objects.equals(project, that.project) &&
                Objects.equals(app, that.app) &&
                Objects.equals(service, that.service)&&
                Objects.equals(environment, that.environment)&&
                Objects.equals(serverVersion, that.serverVersion);
    }

    @Override
    public int hashCode(){
        return Objects.hash(project, app, service, environment, serverVersion);
    }
}
