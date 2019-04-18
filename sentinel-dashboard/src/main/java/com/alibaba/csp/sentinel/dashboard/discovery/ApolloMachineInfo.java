package com.alibaba.csp.sentinel.dashboard.discovery;

/**
 * the machine info of apollo
 *
 * @author longqiang
 */
public class ApolloMachineInfo extends DataSourceMachineInfo {

    private String appId;
    private String env;
    private String namespace;
    private String clusterName;
    private String portalUrl;
    private String token;
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

    @Override
    public String toString() {
        return new StringBuilder("ApolloMachineInfo{")
                .append(super.toString())
                .append(", appId='").append(appId).append('\'')
                .append(", env='").append(env).append('\'')
                .append(", namespace='").append(namespace).append('\'')
                .append(", clusterName='").append(clusterName).append('\'')
                .append(", portalUrl='").append(portalUrl).append('\'')
                .append(", token='").append(token).append('\'')
                .append(", connectTimeout=").append(connectTimeout)
                .append(", readTimeout=").append(readTimeout)
                .append(", operator='").append(operator).append('\'')
                .append('}').toString();
    }

}
