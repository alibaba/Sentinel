package com.alibaba.csp.sentinel.dashboard;

/**
 * @author longqiang
 */
public class Constants {

    private Constants() {
        throw new IllegalStateException("Constants class");
    }

    public static final String PARAM_FLOW_RULE_FETCHER = "ParamFlowRuleFetcher";

    public static final String SYSTEM_RULE_FETCHER = "SystemRuleFetcher";

    public static final String AUTHORITY_RULE_FETCHER = "AuthorityRuleFetcher";

    public static final String DEGRADE_RULE_FETCHER = "DegradeRuleFetcher";

    public static final String FLOW_RULE_FETCHER = "FlowRuleFetcher";

    public static final String PARAM_FLOW_RULE_PUBLISHER = "ParamFlowRulePublisher";

    public static final String SYSTEM_RULE_PUBLISHER = "SystemRulePublisher";

    public static final String AUTHORITY_RULE_PUBLISHER = "AuthorityRulePublisher";

    public static final String DEGRADE_RULE_PUBLISHER = "DegradeRulePublisher";

    public static final String FLOW_RULE_PUBLISHER = "FlowRulePublisher";

    public static final String PARAM_FLOW_RULE_STORE = "ParamFlowRuleStore";

    public static final String SYSTEM_RULE_STORE = "SystemRuleStore";

    public static final String AUTHORITY_RULE_STORE = "AuthorityRuleStore";

    public static final String DEGRADE_RULE_STORE = "DegradeRuleStore";

    public static final String FLOW_RULE_STORE = "FlowRuleStore";

    public static final String CONFIG_CHANGE_CHECKER = "ConfigChangeChecker";

    public static final String MANAGEMENT = "Management";

    public static final String DATASOURCE_APOLLO = "Apollo";

    public static final String DATASOURCE_NACOS = "Nacos";

    public static final String APOLLO_MANAGEMENT = DATASOURCE_APOLLO + MANAGEMENT;

    public static final String NACOS_MANAGEMENT = DATASOURCE_NACOS + MANAGEMENT;
}
