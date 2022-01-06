
package com.alibaba.csp.sentinel.dashboard.repository.nacos;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 */

@ConfigurationProperties(prefix = "nacos.server", ignoreInvalidFields = true)
public class NacosConfigProperties
{
	/**
	 *  默认组名
	 */
	public final static String DEFAULT_GROUP_ID = "SENTINEL_GROUP";
	
	/**
	 *  默认流控规则，在nacos中的dataid后缀值：
	 */
	public static final String FLOW_DATA_ID_SUFFIX = "-flow-rules";
	
	/**
	 * 默认降级规则，在nacos中的dataid后缀值：
	 */
	public static final String DEGRADE_ID_SUFFIX = "-degrade-rules";
	
	/**
	 * 默认热点参数降级规则，在nacos中的dataid后缀值：
	 */
	public static final String PARAM_ID_SUFFIX = "-params-rules";
	
	/**
	 * 默认系统规则，在nacos中的dataid后缀值：
	 */
	public static final String SYSTEM_ID_SUFFIX = "-system-rules";
	
	/**
	 * 默认授权规则，在nacos中的dataid后缀值：
	 */
	public static final String AUTHORITY_ID_SUFFIX = "-authority-rules";
	
	/**
	 * 默认gateway规则，在nacos中的dataid后缀值：
	 */
	public static final String GATEWAY_ID_SUFFIX = "-gateway-rules";
	
	/**
	 * 默认gateway规则，在nacos中的dataid后缀值：
	 */
	public static final String GATEWAY_API_ID_SUFFIX = "-gateway-api-rules";
	
	/**
	 * 默认集群规则，在nacos中的dataid后缀值：
	 */
	public static final String CLUSTER_ID_SUFFIX = "-cluster-rules";
	
	private String ip;

	private String port;

	private String namespace;

	private String groupId = DEFAULT_GROUP_ID;
	
	private String flowSuffix = FLOW_DATA_ID_SUFFIX;	
	private String degradeSuffix = DEGRADE_ID_SUFFIX;
	private String paramSuffix = PARAM_ID_SUFFIX;
	private String systemSuffix = SYSTEM_ID_SUFFIX;
	private String authoritySuffix = AUTHORITY_ID_SUFFIX;
	private String gatewaySuffix = GATEWAY_ID_SUFFIX;
	private String gatewayApiSuffix = GATEWAY_API_ID_SUFFIX;
	private String clusterSuffix = CLUSTER_ID_SUFFIX;

	public String getIp()
	{
		return ip;
	}

	public void setIp(String ip)
	{
		this.ip = ip;
	}

	public String getPort()
	{
		return port;
	}

	public void setPort(String port)
	{
		this.port = port;
	}

	public String getNamespace()
	{
		return namespace;
	}

	public void setNamespace(String namespace)
	{
		this.namespace = namespace;
	}

	public String getGroupId()
	{
		return groupId;
	}

	public void setGroupId(String groupId)
	{
		this.groupId = groupId;
	}

	public String getServerAddr()
	{
		return this.getIp() + ":" + this.getPort();
	}	

	public String getFlowSuffix()
	{
		return flowSuffix;
	}

	public void setFlowSuffix(String flowSuffix)
	{
		this.flowSuffix = flowSuffix;
	}	

	public String getDegradeSuffix()
	{
		return degradeSuffix;
	}

	public void setDegradeSuffix(String degradeSuffix)
	{
		this.degradeSuffix = degradeSuffix;
	}

	public String getParamSuffix()
	{
		return paramSuffix;
	}

	public void setParamSuffix(String paramSuffix)
	{
		this.paramSuffix = paramSuffix;
	}

	public String getSystemSuffix()
	{
		return systemSuffix;
	}

	public void setSystemSuffix(String systemSuffix)
	{
		this.systemSuffix = systemSuffix;
	}

	public String getAuthoritySuffix()
	{
		return authoritySuffix;
	}

	public void setAuthoritySuffix(String authoritySuffix)
	{
		this.authoritySuffix = authoritySuffix;
	}

	public String getGatewaySuffix()
	{
		return gatewaySuffix;
	}

	public void setGatewaySuffix(String gatewaySuffix)
	{
		this.gatewaySuffix = gatewaySuffix;
	}

	public String getClusterSuffix()
	{
		return clusterSuffix;
	}

	public void setClusterSuffix(String clusterSuffix)
	{
		this.clusterSuffix = clusterSuffix;
	}

	public String getGatewayApiSuffix()
	{
		return gatewayApiSuffix;
	}

	public void setGatewayApiSuffix(String gatewayApiSuffix)
	{
		this.gatewayApiSuffix = gatewayApiSuffix;
	}

	@Override
	public String toString()
	{
		return "NacosConfigProperties [ip=" + ip + ", port=" + port + ", namespace=" + namespace + ", groupId="
				+ groupId + ", flowSuffix=" + flowSuffix + ", degradeSuffix=" + degradeSuffix + ", paramSuffix="
				+ paramSuffix + ", systemSuffix=" + systemSuffix + ", authoritySuffix=" + authoritySuffix
				+ ", gatewaySuffix=" + gatewaySuffix + ", gatewayApiSuffix=" + gatewayApiSuffix + ", clusterSuffix="
				+ clusterSuffix + "]";
	}	

}
