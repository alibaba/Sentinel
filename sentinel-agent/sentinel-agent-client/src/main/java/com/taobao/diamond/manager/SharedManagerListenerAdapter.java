package com.taobao.diamond.manager;

import java.util.concurrent.Executor;

public abstract class SharedManagerListenerAdapter implements ManagerListener {
    private volatile String dataId;
    private volatile String group;

    final public void fillContext(String dataId, String group) {
        this.dataId = dataId;
        this.group = group;
    }

    @Override
    final public void receiveConfigInfo(String configInfo) {
        innerReceive(dataId, group, configInfo);
    }

    @Override
    public Executor getExecutor() {
        return null;
    }

    public abstract void innerReceive(String dataId, String group, String configInfo);
}