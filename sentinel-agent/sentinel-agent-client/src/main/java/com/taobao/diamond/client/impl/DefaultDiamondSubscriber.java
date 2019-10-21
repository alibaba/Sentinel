package com.taobao.diamond.client.impl;

import static com.taobao.diamond.client.impl.DiamondEnv.log;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.taobao.diamond.client.DiamondConfigure;
import com.taobao.diamond.client.DiamondSubscriber;

/**
 * ȱʡ��DiamondSubscriber
 * 
 */
@Deprecated
class DefaultDiamondSubscriber implements DiamondSubscriber {

    @Override
    public String getAvailableConfigureInfomation(String dataId, String group, long timeout) {
        try {
            return DiamondEnvRepo.getDefaultEnv().getConfig(dataId, group, timeout);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    @Override
    public String getConfigureInfomation(String dataId, String group, long timeout) {
        return getAvailableConfigureInfomation(dataId, group, timeout);
    }

    @Override
    public List<String> getServerList() {
        return DiamondEnvRepo.getDefaultEnv().getServerUrls();
    }
    
    @Override
    public Set<String> getDataIds() {
        return DiamondEnvRepo.getDefaultEnv().getSubscribeDataIds();
    }

    @Override
    public DiamondConfigure getDiamondConfigure() {
        return DiamondConfigure.singleton;
    }

    @Override
    public void setDiamondConfigure(DiamondConfigure diamondConfigure) {
        log.info("DefaultDiamondSubscriber.setDiamondConfigure() not supported.");
    }

    @Override
    public void start() {
        log.info("DefaultDiamondSubscriber.start() not supported.");
    }

    @Override
    public void close() {
        log.info("DefaultDiamondSubscriber.close() not supported.");
    }

    private DefaultDiamondSubscriber() {
    }

    // ============================

    static final DefaultDiamondSubscriber singleton = new DefaultDiamondSubscriber();

}
