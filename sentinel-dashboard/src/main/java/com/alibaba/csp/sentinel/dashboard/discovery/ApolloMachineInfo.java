package com.alibaba.csp.sentinel.dashboard.discovery;

/**
 * the machine info of apollo
 *
 * @author longqiang
 */
public class ApolloMachineInfo extends MachineInfo {

    private String appId;
    private String env;
    private String namespace;
    private String clusterName;
    private String portalUrl;
    private String token;
    private String degradeRulesKey;
    private String flowRulesKey;
    private String authorityRulesKey;
    private String systemRulesKey;
    private String paramFlowRulesKey;
    private String operator;
    private int connectTimeout;
    private int readTimeout;

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getPortalUrl() {
        return portalUrl;
    }

    public void setPortalUrl(String portalUrl) {
        this.portalUrl = portalUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getDegradeRulesKey() {
        return degradeRulesKey;
    }

    public void setDegradeRulesKey(String degradeRulesKey) {
        this.degradeRulesKey = degradeRulesKey;
    }

    public String getFlowRulesKey() {
        return flowRulesKey;
    }

    public void setFlowRulesKey(String flowRulesKey) {
        this.flowRulesKey = flowRulesKey;
    }

    public String getAuthorityRulesKey() {
        return authorityRulesKey;
    }

    public void setAuthorityRulesKey(String authorityRulesKey) {
        this.authorityRulesKey = authorityRulesKey;
    }

    public String getSystemRulesKey() {
        return systemRulesKey;
    }

    public void setSystemRulesKey(String systemRulesKey) {
        this.systemRulesKey = systemRulesKey;
    }

    public String getParamFlowRulesKey() {
        return paramFlowRulesKey;
    }

    public void setParamFlowRulesKey(String paramFlowRulesKey) {
        this.paramFlowRulesKey = paramFlowRulesKey;
    }

    @Override
    public String toString() {
        return "ApolloMachineInfo{" + "MachineInfo{" +
                "app='" + getApp() + '\'' +
                ", hostname='" + getHostname() + '\'' +
                ", ip='" + getIp() + '\'' +
                ", port=" + getPort() +
                ", timestamp=" + getTimestamp() +
                ", version='" + getVersion() + '\'' +
                '}'+
                "appId='" + appId + '\'' +
                ", env='" + env + '\'' +
                ", namespace='" + namespace + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", portalUrl='" + portalUrl + '\'' +
                ", token='" + token + '\'' +
                ", degradeRulesKey='" + degradeRulesKey + '\'' +
                ", flowRulesKey='" + flowRulesKey + '\'' +
                ", authorityRulesKey='" + authorityRulesKey + '\'' +
                ", systemRulesKey='" + systemRulesKey + '\'' +
                ", paramFlowRulesKey='" + paramFlowRulesKey + '\'' +
                ", connectTimeout=" + connectTimeout +
                ", readTimeout=" + readTimeout +
                ", operator=" + operator +
                '}';
    }

}
