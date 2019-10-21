package com.alibaba.csp.ahas.sentinel.cluster;

import java.util.List;

import com.alibaba.csp.ahas.sentinel.cluster.entity.ClusterGroupEntity;
import com.alibaba.csp.ahas.sentinel.util.MachineUtils;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerFlowConfig;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.acm.DataAcmFormat;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.taobao.csp.third.com.alibaba.fastjson.JSON;
import com.taobao.csp.third.com.alibaba.fastjson.TypeReference;

/**
 * @author Eric Zhao
 */
public class ClusterServerFlowConfigParser implements Converter<String, ServerFlowConfig> {

    @Override
    public ServerFlowConfig convert(String source) {
        if (source == null) {
            return null;
        }
        String data = new DataAcmFormat(source).getData();
        RecordLog.info("[ClusterServerFlowConfigParser] Get data: " + data);
        List<ClusterGroupEntity> groupList = JSON.parseObject(data, new TypeReference<List<ClusterGroupEntity>>() {});
        if (groupList == null || groupList.isEmpty()) {
            return null;
        }
        return extractServerFlowConfig(groupList);
    }

    private ServerFlowConfig extractServerFlowConfig(List<ClusterGroupEntity> groupList) {
        for (ClusterGroupEntity group : groupList) {
            if (MachineUtils.isCurrentMachineEqual(group)) {
                return new ServerFlowConfig()
                    .setExceedCount(ClusterServerConfigManager.getExceedCount())
                    .setIntervalMs(ClusterServerConfigManager.getIntervalMs())
                    .setMaxAllowedQps(group.getMaxAllowedQps())
                    .setMaxOccupyRatio(ClusterServerConfigManager.getMaxOccupyRatio())
                    .setSampleCount(ClusterServerConfigManager.getSampleCount());
            }
        }
        return null;
    }
}
