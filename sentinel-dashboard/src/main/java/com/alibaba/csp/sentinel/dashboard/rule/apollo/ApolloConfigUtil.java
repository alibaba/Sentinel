package com.alibaba.csp.sentinel.dashboard.rule.apollo;

/***
 * 配置中心常量 
 * @author Fx_demon
 */
public final class ApolloConfigUtil {
	
	/***
	 * Apollo 对应Sentinel应用ID
	 */
    public static final String SENTINEL_ID = "sentinel_ds";
    
    public static final boolean releaseEnabled = System.getProperty("csp.sentinel.dashboard.dynamic-rule.apollo.releaseEnabled").equals("0")? true:false;
    
	public static final String env = System.getProperty("csp.sentinel.dashboard.dynamic-rule.apollo.env");
	public static final String clusterName = System.getProperty("csp.sentinel.dashboard.dynamic-rule.apollo.cluster-name");
	public static final String namespaceName = System.getProperty("csp.sentinel.dashboard.dynamic-rule.apollo.namespace-name");
	
	//Apollo KEY
	/***
	 * 限流
	 */
	public static final String KEY_FLOW_RULES = "flowRules"; 
	public static final String KEY_DEGRADE_RULES = "degradeRules"; 
	public static final String KEY_AUTHORITY_RULES = "authorityRules";  
	public static final String KEY_PARAM_FLOW_RULES = "paramFlowRules";
	public static final String KEY_SYSTEM_FLOW_RULES = "systemRules";
	 
}
