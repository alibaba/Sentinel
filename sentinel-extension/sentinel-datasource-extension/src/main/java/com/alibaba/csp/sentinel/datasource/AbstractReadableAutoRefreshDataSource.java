/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.datasource;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.log.RecordLog;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * The abstract readable data source provides a {@link ReadableDataSource} automatically fetches the backend data.
 *
 * @param <D> data type for DataSource
 * @param <S> data type for Sentinel
 *
 * @author Carpenter Lee
 * @author Jiajiangnan
 */
public abstract class AbstractReadableAutoRefreshDataSource<D, S> extends AbstractReadableDataSource<D, S> {

    private static final long DEFAULT_REFRESH_MS = 3000;

    private ScheduledExecutorService service;
    protected long refreshMs;

    public AbstractReadableAutoRefreshDataSource(final AbstractDataSourceContext<D, S> context) {
        this(context, DEFAULT_REFRESH_MS);
    }

    public AbstractReadableAutoRefreshDataSource(final AbstractDataSourceContext<D, S> context, final long refreshMs) {
        super(context);
        if (refreshMs <= 0) {
            throw new IllegalArgumentException("recommendRefreshMs must > 0, but " + refreshMs + " get");
        }
        this.refreshMs = refreshMs;
        startTimerService();
    }


    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    private void startTimerService() {
        service = Executors.newScheduledThreadPool(1, new NamedThreadFactory("sentinel-datasource-auto-refresh-task", true));
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!isModified()) {
                        return;
                    }
                    S newValue = loadConfig();
                    getProperty().updateValue(newValue);
                } catch (Throwable e) {
                    RecordLog.info("loadConfig exception", e);
                }
            }
        }, refreshMs, refreshMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() throws Exception {
        if (service != null) {
            service.shutdownNow();
            service = null;
        }
    }

    protected boolean isModified() {
        return true;
    }

}
