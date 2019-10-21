package com.alibaba.jvm.sandbox.qatest.api.mock;

import com.alibaba.jvm.sandbox.api.resource.ModuleEventWatcher;

import java.util.concurrent.atomic.AtomicInteger;

public class MockForBuilderProgress implements ModuleEventWatcher.Progress {

    private final AtomicInteger onSuccessCntRef = new AtomicInteger();
    private final AtomicInteger onFailedCntRef = new AtomicInteger();

    @Override
    public void begin(int total) {

    }

    @Override
    public void progressOnSuccess(Class clazz, int index) {
        onSuccessCntRef.incrementAndGet();
    }

    @Override
    public void progressOnFailed(Class clazz, int index, Throwable cause) {
        onFailedCntRef.incrementAndGet();
    }

    @Override
    public void finish(int cCnt, int mCnt) {

    }

    public int cntOnSuccess() {
        return onSuccessCntRef.get();
    }

    public int cntOnFailed() {
        return onFailedCntRef.get();
    }

}