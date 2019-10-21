package com.alibaba.csp.ahas.sentinel.cluster;

import java.util.List;

import com.alibaba.csp.ahas.sentinel.cluster.entity.ClusterGroupEntity;
import com.alibaba.csp.ahas.sentinel.util.MachineUtils;
import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.acm.DataAcmFormat;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.taobao.csp.third.com.alibaba.fastjson.JSON;
import com.taobao.csp.third.com.alibaba.fastjson.TypeReference;

/**
 * @author Eric Zhao
 */
public class ClusterServerTransportConfigParser implements Converter<String, ServerTransportConfig> {

    @Override
    public ServerTransportConfig convert(String source) {
        if (source == null) {
            return null;
        }
        String data = new DataAcmFormat(source).getData();
        RecordLog.info("[ClusterServerTransportConfigParser] Get data: " + data);
        List<ClusterGroupEntity> groupList = JSON.parseObject(data, new TypeReference<List<ClusterGroupEntity>>() {});
        if (groupList == null || groupList.isEmpty()) {
            return null;
        }
        return extractServerTransportConfig(groupList);
    }

    private ServerTransportConfig extractServerTransportConfig(List<ClusterGroupEntity> groupList) {
        for (ClusterGroupEntity group : groupList) {
            if (MachineUtils.isCurrentMachineEqual(group)) {
                return new ServerTransportConfig().setPort(group.getServerPort()).setIdleSeconds(600);
            }
        }
        return null;
    }
}
