package com.alibaba.csp.ahas.sentinel.cluster;

import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.acm.DataAcmFormat;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.taobao.csp.third.com.alibaba.fastjson.JSON;

/**
 * @author Eric Zhao
 */
public class ClusterClientCommonConfigParser implements Converter<String, ClusterClientConfig> {

    @Override
    public ClusterClientConfig convert(String source) {
        if (source == null) {
            return null;
        }
        String data = new DataAcmFormat(source).getData();
        RecordLog.info("[ClusterClientCommonConfigParser] Get data: " + data);
        if (data == null) {
            return null;
        }
        return JSON.parseObject(data, ClusterClientConfig.class);
    }
}