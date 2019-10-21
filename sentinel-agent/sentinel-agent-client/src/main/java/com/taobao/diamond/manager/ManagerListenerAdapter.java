package com.taobao.diamond.manager;

import java.util.concurrent.Executor;


public abstract class ManagerListenerAdapter implements ManagerListener {

    public Executor getExecutor() {
        return null;
    }

}
