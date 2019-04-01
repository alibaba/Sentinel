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
package com.alibaba.csp.sentinel.transport.init;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.transport.HeartbeatSender;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;

/**
 * Global init function for heartbeat sender.
 *
 * @author Eric Zhao
 */
public class HeartbeatSenderInitFunc implements InitFunc {

    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    private static ScheduledExecutorService pool = Executors.newScheduledThreadPool(2,
        new NamedThreadFactory("sentinel-heartbeat-send-task", true));

    private boolean validHeartbeatInterval(Long interval) {
        return interval != null && interval > 0;
    }

    @Override
    public void init() {
        ServiceLoader<HeartbeatSender> loader = ServiceLoader.load(HeartbeatSender.class);
        Iterator<HeartbeatSender> iterator = loader.iterator();
        if (iterator.hasNext()) {
            final HeartbeatSender sender = iterator.next();
            if (iterator.hasNext()) {
                throw new IllegalStateException("Only single heartbeat sender can be scheduled");
            } else {
                long interval = retrieveInterval(sender);
                setIntervalIfNotExists(interval);
                scheduleHeartbeatTask(sender, interval);
            }
        }
    }

    private void setIntervalIfNotExists(long interval) {
        SentinelConfig.setConfig(TransportConfig.HEARTBEAT_INTERVAL_MS, String.valueOf(interval));
    }

    long retrieveInterval(/*@NonNull*/ HeartbeatSender sender) {
        Long intervalInConfig = TransportConfig.getHeartbeatIntervalMs();
        if (validHeartbeatInterval(intervalInConfig)) {
            RecordLog.info("[HeartbeatSenderInit] Using heartbeat interval in Sentinel config property: " + intervalInConfig);
            return intervalInConfig;
        } else {
            long senderInterval = sender.intervalMs();
            RecordLog.info("[HeartbeatSenderInit] Heartbeat interval not configured in config property or invalid, "
                + "using sender default: " + senderInterval);
            return senderInterval;
        }
    }

    private void scheduleHeartbeatTask(/*@NonNull*/ final HeartbeatSender sender, /*@Valid*/ long interval) {
        pool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    sender.sendHeartbeat();
                } catch (Throwable e) {
                    RecordLog.warn("[HeartbeatSender] Send heartbeat error", e);
                }
            }
        }, 5000, interval, TimeUnit.MILLISECONDS);
        RecordLog.info("[HeartbeatSenderInit] HeartbeatSender started: "
            + sender.getClass().getCanonicalName());
    }
}
