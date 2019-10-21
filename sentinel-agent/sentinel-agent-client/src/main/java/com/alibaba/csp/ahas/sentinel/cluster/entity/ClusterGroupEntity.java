package com.alibaba.csp.ahas.sentinel.cluster.entity;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Eric Zhao
 */
public class ClusterGroupEntity {

    /**
     * Currently we use `processConfigurationId` as machineId.
     */
    private String machineId;

    private String serverIp;
    private String vpcId;
    private Integer pid;

    private Boolean belongToApp;

    private Integer serverPort;
    private Set<String> clientSet = new HashSet<>();

    private Double maxAllowedQps;

    public String getMachineId() {
        return machineId;
    }

    public ClusterGroupEntity setMachineId(String machineId) {
        this.machineId = machineId;
        return this;
    }

    public String getServerIp() {
        return serverIp;
    }

    public ClusterGroupEntity setServerIp(String serverIp) {
        this.serverIp = serverIp;
        return this;
    }

    public String getVpcId() {
        return vpcId;
    }

    public ClusterGroupEntity setVpcId(String vpcId) {
        this.vpcId = vpcId;
        return this;
    }

    public Integer getPid() {
        return pid;
    }

    public ClusterGroupEntity setPid(Integer pid) {
        this.pid = pid;
        return this;
    }

    public Boolean getBelongToApp() {
        return belongToApp;
    }

    public ClusterGroupEntity setBelongToApp(Boolean belongToApp) {
        this.belongToApp = belongToApp;
        return this;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public ClusterGroupEntity setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
        return this;
    }

    public Set<String> getClientSet() {
        return clientSet;
    }

    public ClusterGroupEntity setClientSet(Set<String> clientSet) {
        this.clientSet = clientSet;
        return this;
    }

    public Double getMaxAllowedQps() {
        return maxAllowedQps;
    }

    public ClusterGroupEntity setMaxAllowedQps(Double maxAllowedQps) {
        this.maxAllowedQps = maxAllowedQps;
        return this;
    }

    @Override
    public String toString() {
        return "ClusterGroupEntity{" +
            "machineId='" + machineId + '\'' +
            ", serverIp='" + serverIp + '\'' +
            ", vpcId='" + vpcId + '\'' +
            ", pid=" + pid +
            ", belongToApp=" + belongToApp +
            ", serverPort=" + serverPort +
            ", clientSet=" + clientSet +
            ", maxAllowedQps=" + maxAllowedQps +
            '}';
    }
}
