package com.alibaba.csp.ahas.sentinel.cluster;

import java.util.List;

import com.alibaba.csp.ahas.sentinel.cluster.entity.ClusterGroupEntity;
import com.alibaba.csp.ahas.sentinel.util.MachineUtils;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientAssignConfig;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.acm.DataAcmFormat;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.taobao.csp.third.com.alibaba.fastjson.JSON;
import com.taobao.csp.third.com.alibaba.fastjson.TypeReference;

/**
 * @author Eric Zhao
 */
public class ClusterClientAssignConfigParser implements Converter<String, ClusterClientAssignConfig> {

    @Override
    public ClusterClientAssignConfig convert(String source) {
        if (source == null) {
            return null;
        }
        String data = new DataAcmFormat(source).getData();
        RecordLog.info("[ClusterClientAssignConfigParser] Get data: " + data);
        List<ClusterGroupEntity> groupList = JSON.parseObject(data, new TypeReference<List<ClusterGroupEntity>>() {});
        if (groupList == null || groupList.isEmpty()) {
            return null;
        }
        return extractClientAssignment(groupList);
    }

    private ClusterClientAssignConfig extractClientAssignment(List<ClusterGroupEntity> groupList) {
        // Skip the token servers.
        for (ClusterGroupEntity group : groupList) {
            if (MachineUtils.isCurrentMachineEqual(group)) {
                return null;
            }
        }
        // Build client assign config from the client set of target server group.
        for (ClusterGroupEntity group : groupList) {
            if (group.getClientSet().contains(MachineUtils.getCurrentProcessConfigurationId())) {
                String ip = group.getServerIp();
                Integer port = group.getServerPort();
                return new ClusterClientAssignConfig(ip, port);
            }
        }
        // Not assigned.
        return null;
    }
}
