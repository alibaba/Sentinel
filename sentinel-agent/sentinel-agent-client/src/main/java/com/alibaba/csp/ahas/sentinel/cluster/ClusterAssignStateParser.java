package com.alibaba.csp.ahas.sentinel.cluster;

import java.util.List;

import com.alibaba.csp.ahas.sentinel.cluster.entity.ClusterGroupEntity;
import com.alibaba.csp.ahas.sentinel.util.MachineUtils;
import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.acm.DataAcmFormat;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.taobao.csp.third.com.alibaba.fastjson.JSON;
import com.taobao.csp.third.com.alibaba.fastjson.TypeReference;

/**
 * @author Eric Zhao
 */
public class ClusterAssignStateParser implements Converter<String, Integer> {

    @Override
    public Integer convert(String source) {
        if (source == null) {
            return null;
        }
        String data = new DataAcmFormat(source).getData();
        RecordLog.info("[ClusterAssignStateParser] Get data: " + data);
        List<ClusterGroupEntity> groupList = JSON.parseObject(data, new TypeReference<List<ClusterGroupEntity>>() {});
        if (groupList == null || groupList.isEmpty()) {
            return ClusterStateManager.CLUSTER_NOT_STARTED;
        }
        return extractMode(groupList);
    }

    private int extractMode(List<ClusterGroupEntity> groupList) {
        // If any server group machine matches current, then it's token server.
        for (ClusterGroupEntity group : groupList) {
            if (MachineUtils.isCurrentMachineEqual(group)) {
                return ClusterStateManager.CLUSTER_SERVER;
            }
            if (group.getClientSet() != null) {
                for (String client : group.getClientSet()) {
                    if (client != null && client.equals(MachineUtils.getCurrentProcessConfigurationId())) {
                        return ClusterStateManager.CLUSTER_CLIENT;
                    }
                }
            }
        }
        return ClusterStateManager.CLUSTER_NOT_STARTED;
    }
}
