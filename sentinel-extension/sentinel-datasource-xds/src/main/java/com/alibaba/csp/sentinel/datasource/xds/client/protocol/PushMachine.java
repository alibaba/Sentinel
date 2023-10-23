/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.datasource.xds.client.protocol;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alibaba.csp.sentinel.datasource.xds.config.XdsConfigProperties;
import com.alibaba.csp.sentinel.datasource.xds.expection.XdsException;
import com.alibaba.csp.sentinel.log.RecordLog;

/**
 * @author lwj
 * @since 2.0.0
 */
public class PushMachine {
    private final List<AbstractXdsProtocol> protocols;
    private final XdsConfigProperties xdsConfigProperties;
    private final AtomicBoolean initialize;
    private CountDownLatch initCountDownLatch;

    public PushMachine(List<AbstractXdsProtocol> protocols, XdsConfigProperties xdsConfigProperties) {
        this.protocols = protocols;
        this.xdsConfigProperties = xdsConfigProperties;
        this.initialize = new AtomicBoolean(false);
    }

    public void start() throws XdsException {
        if (initialize.get()) {
            RecordLog.error("[XdsDataSource] Unable to continue initialization while initializing");
            throw new XdsException("PushMachine Error");
        }
        start0(xdsConfigProperties.getInitAwaitTimeS());

    }

    /**
     * If the wait time is 0, no wait is required
     *
     * @param waitTimeS
     */
    public void start0(int waitTimeS) {
        //Initially, we only observe ourselves
        for (AbstractXdsProtocol protocol : protocols) {
            protocol.observeResource();
        }

        //The restart case no longer waits
        if (0 == waitTimeS) {
            return;
        }
        initCountDownLatch = new CountDownLatch(1);
        try {
            if (!initCountDownLatch.await(waitTimeS, TimeUnit.SECONDS)) {
                throw new XdsException("Xds Client Init Timeout");
            }
        } catch (Exception e) {
            throw new XdsException("Xds Client Init Error");
        }

    }

    public void restart() throws XdsException {
        //The reconnection must be initialized before the next reconnection
        start0(0);
    }

    /**
     * Promote the use of another protocol or change the status after receiving the corresponding lds
     *
     * @param xdsProtocol
     */
    public void push(AbstractXdsProtocol xdsProtocol) {
        switch (xdsProtocol.getTypeUrl()) {
            case RDS_URL:
                break;
            case CDS_URL:
                break;
            case EDS_URL:
                break;
            case LDS_URL:
                initCountDownLatch.countDown();
                break;
            default:
                RecordLog.error("[XdsDataSource] The protocol is not supported ,typeUrl={}", xdsProtocol.getTypeUrl());
        }
    }

}
