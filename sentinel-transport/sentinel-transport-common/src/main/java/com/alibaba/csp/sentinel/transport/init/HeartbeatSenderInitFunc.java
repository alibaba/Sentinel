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

    private static ScheduledExecutorService pool = Executors.newScheduledThreadPool(2,
        new NamedThreadFactory("sentinel-heartbeat-send-task", true));

    @Override
    public void init() throws Exception {
        long heartBeatInterval = -1;
        try {
            heartBeatInterval = TransportConfig.getHeartbeatIntervalMs();
            RecordLog.info("system property heartbeat interval set: " + heartBeatInterval);
        } catch (Exception ex) {
            RecordLog.info("Parse heartbeat interval failed, use that in code, " + ex.getMessage());
        }
        ServiceLoader<HeartbeatSender> loader = ServiceLoader.load(HeartbeatSender.class);
        Iterator<HeartbeatSender> iterator = loader.iterator();
        if (iterator.hasNext()) {
            final HeartbeatSender sender = iterator.next();
            if (iterator.hasNext()) {
                throw new IllegalStateException("Only single heartbeat sender can be scheduled");
            } else {
                long interval = sender.intervalMs();
                if (heartBeatInterval != -1) {
                    interval = heartBeatInterval;
                }
                pool.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sender.sendHeartbeat();
                        } catch (Exception e) {
                            e.printStackTrace();
                            RecordLog.info("[HeartbeatSender] Send heartbeat error", e);
                        }
                    }
                }, 10000, interval, TimeUnit.MILLISECONDS);
                RecordLog.info("[HeartbeatSenderInit] HeartbeatSender started: "
                    + sender.getClass().getCanonicalName());
            }
        }
    }
}
