package com.taobao.diamond.client.impl;

import com.taobao.diamond.client.ClusterType;
import com.taobao.diamond.client.ContentIdentityPattern;
import com.taobao.diamond.client.Diamond;
import com.taobao.diamond.client.DiamondConfigure;
import com.taobao.diamond.client.DiamondPublisher;


/**
 * �ɵķ�����ݽӿڣ�ת���µ�ʵ�֡�
 */
@Deprecated
class DefaultDiamondPublisher implements DiamondPublisher {

    static public final DefaultDiamondPublisher singleton = new DefaultDiamondPublisher();

    @Override
    public boolean addDatum(String dataId, String group, String datumId, String configInfo,
            long timeout) {
        return Diamond.publishAggr(dataId, group, datumId, configInfo);
    }

    @Override
    public boolean deleteDatum(String dataId, String group, String datumId, long timeout) {
        return Diamond.removeAggr(dataId, group, datumId);
    }

    @Override
    public boolean deleteAllDatums(String dataId, String group, long timeout) {
        return Diamond.remove(dataId, group);
    }

    @Override
    public void publish(String dataId, String group, String configInfo,
            ContentIdentityPattern pattern) {
        Diamond.publishAggr(dataId, group, pattern.getContentIdentity(configInfo), configInfo);
    }

    @Override
    public boolean syncPublish(String dataId, String group, String configInfo, long timeout,
            ContentIdentityPattern pattern) {
        return Diamond.publishAggr(dataId, group, pattern.getContentIdentity(configInfo), configInfo);
    }

    @Override
    public void publishAll(String dataId, String group, String configInfo) {
        Diamond.publishSingle(dataId, group, configInfo);
    }

    @Override
    public boolean syncPublishAll(String dataId, String group, String configInfo, long timeout) {
        return Diamond.publishSingle(dataId, group, configInfo);
    }

    @Override
    public void unpublishAll(String dataId, String group) {
        Diamond.remove(dataId, group);
    }

    @Override
    public boolean syncUnpublishAll(String dataId, String group, long timeout) {
        return Diamond.remove(dataId, group);
    }

    @Override
    public DiamondConfigure getDiamondConfigure() {
        return DiamondConfigure.singleton;
    }

    @Override
    public void setDiamondConfigure(DiamondConfigure diamondConfigure) {
        // do nothing
    }

    @Override
    public void start() {
        // do nothing
    }

    @Override
    public void close() {
        // do nothing
    }

    @Override
    public void setClusterType(ClusterType clusterType) {
        // do nothing
    }
    
}
