package com.taobao.diamond.manager.impl;

import com.taobao.diamond.client.ContentIdentityPattern;
import com.taobao.diamond.client.Diamond;
import com.taobao.diamond.client.DiamondConfigure;
import com.taobao.diamond.manager.BaseStonePubManager;


/**
 * �ɵķ�����ݽӿڣ�ת���µ�ʵ��Diamond��
 */
@Deprecated
public class DefaultBaseStonePubManager implements BaseStonePubManager {

	/**
	 * ʹ���½ӿ�Diamond.publishAggr
	 */
    @Override
    public void publish(String dataId, String group, String configInfo,
            ContentIdentityPattern pattern) {
        Diamond.publishAggr(dataId, group, pattern.getContentIdentity(configInfo), configInfo);
    }

    /**
	 * ʹ���½ӿ�Diamond.publishAggr
	 */
    @Override
    public boolean syncPublish(String dataId, String group, String configInfo, long timeout,
            ContentIdentityPattern pattern) {
        return Diamond.publishAggr(dataId, group, pattern.getContentIdentity(configInfo),
                configInfo);
    }

    /**
	 * ʹ���½ӿ�Diamond.publishSingle
	 */
    @Override
    public void publishAll(String dataId, String group, String configInfo) {
        Diamond.publishSingle(dataId, group, configInfo);
    }

    /**
	 * ʹ���½ӿ�Diamond.publishSingle
	 */
    @Override
    public boolean syncPublishAll(String dataId, String group, String configInfo, long timeout) {
        return Diamond.publishSingle(dataId, group, configInfo);
    }

    @Override
    public void removeAll(String dataId, String group) {
        Diamond.remove(dataId, group);
    }

    @Override
    public boolean syncRemoveAll(String dataId, String group, long timeout) {
        return Diamond.remove(dataId, group);
    }

    @Override
    public boolean publish(String dataId, String group, String datumId, String configInfo,
            long timeout) {
        return Diamond.publishAggr(dataId, group, datumId, configInfo);
    }

    @Override
    public boolean unPublish(String dataId, String group, String datumId, long timeout) {
        return Diamond.removeAggr(dataId, group, datumId);
    }

    @Override
    public boolean unPublishAll(String dataId, String group, long timeout) {
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
    public void close() {
        // do nothing
    }

}
