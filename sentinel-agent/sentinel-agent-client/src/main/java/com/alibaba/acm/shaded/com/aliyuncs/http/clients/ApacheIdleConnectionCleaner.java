/*
 * Copyright 2017 Alibaba Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.acm.shaded.com.aliyuncs.http.clients;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.HttpClientConnectionManager;

public class ApacheIdleConnectionCleaner extends Thread {

    private static final Log LOG = LogFactory.getLog(ApacheIdleConnectionCleaner.class);
    private static final int PERIOD_SEC = 60;

    private static volatile ApacheIdleConnectionCleaner instance;
    private static final Map<HttpClientConnectionManager, Long> connMgrMap = new ConcurrentHashMap<HttpClientConnectionManager, Long>();

    private volatile boolean isShuttingDown;

    private ApacheIdleConnectionCleaner() {
        super("sdk-apache-idle-connection-cleaner");
        setDaemon(true);
    }

    public static void registerConnectionManager(HttpClientConnectionManager connMgr, Long idleTimeMills){
        if (instance == null) {
            synchronized (ApacheIdleConnectionCleaner.class) {
                if (instance == null) {
                    instance = new ApacheIdleConnectionCleaner();
                    instance.start();
                }
            }
        }
        connMgrMap.put(connMgr, idleTimeMills);
    }

    public static void removeConnectionManager(HttpClientConnectionManager connectionManager) {
        connMgrMap.remove(connectionManager);
        if (connMgrMap.isEmpty()) {
            shutdown();
        }
    }

    public static void shutdown(){
        if (instance != null) {
            instance.isShuttingDown = true;
            instance.interrupt();
            connMgrMap.clear();
            instance = null;
        }
    }

    @Override
    public void run() {
        while (true) {
            if (isShuttingDown) {
                LOG.debug("Shutting down.");
                return;
            }
            try {
                Thread.sleep(PERIOD_SEC * 1000);

                for (Entry<HttpClientConnectionManager, Long> entry : connMgrMap.entrySet()) {
                    try {
                        entry.getKey().closeIdleConnections(entry.getValue(), TimeUnit.MILLISECONDS);
                    } catch (Exception t) {
                        LOG.warn("close idle connections failed", t);
                    }
                }
            } catch (InterruptedException e){
                LOG.debug("interrupted.", e);
            } catch (Throwable t) {
                LOG.warn("fatal error", t);
            }
        }
    }

}