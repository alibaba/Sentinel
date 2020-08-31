package com.alibaba.csp.sentinel.dashboard.datasource;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayParamFlowItemEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.*;

/**
 * keys of rule seetings stored in config center
 *
 * @Author Jiajiangnan
 * @E-mail jiajiangnan.office@foxmail.com
 * @Date 2020/8/30
 * @since 1.8.0
 */
public enum RuleConfigTypeEnum {

    FLOW("sentinel-flow-rules", FlowRuleEntity.class),
    PARAM_FLOW("sentinel-param-rules", ParamFlowRuleEntity.class),
    AUTHORITY("sentinel-authority-rules", AuthorityRuleEntity.class),
    DEGRADE("sentinel-degrade-rules", DegradeRuleEntity.class),
    SYSTEM("sentinel-system-rules", SystemRuleEntity.class),
    GATEWAY_FLOW("sentinel-gateway-flow-rules", GatewayFlowRuleEntity.class),
    GATEWAY_PARAM_FLOW("sentinel-gateway-param-flow-rules", GatewayParamFlowItemEntity.class);

//    CLUSTER("sentinel-cluster-map"),
//    CLIENT_CONFIG("sentinel-cluster-client-config"),

//    SERVER_TRANSPORT_CONFIG("sentinel-cluster-server-transport-config"),
//    SERVER_FLOW_CONFIG("sentinel-cluster-server-flow-config"),
//    SERVER_NAMESPACE_SET("sentinel-cluster-server-namespace-set");

    private String value;
    private Class clazz;

    RuleConfigTypeEnum(String value, Class clazz){
        this.value = value;
        this.clazz = clazz;
    }

    public String getValue(){
        return this.value;
    }

    public Class getClazz() {
        return this.clazz;
    }

}
